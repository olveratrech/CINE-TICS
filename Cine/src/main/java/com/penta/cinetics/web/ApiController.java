package com.penta.cinetics.web;

import com.penta.cinetics.identidad.*;
import com.penta.cinetics.reservas.aplicacion.Reservas;
import com.penta.cinetics.reservas.infraestructura.ReservasOracle;
import com.penta.cinetics.boletos.aplicacion.VentaBoletos;
import com.penta.cinetics.boletos.aplicacion.VentaBoletos.Seleccion;
import com.penta.cinetics.ventas.aplicacion.SolicitudCompra.Item;
import com.penta.cinetics.boletos.infraestructura.BoletosOracle;
import com.penta.cinetics.ventas.aplicacion.SolicitudCompra;
import com.penta.cinetics.ventas.infraestructura.ComprasOracle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
public class ApiController {
    private final ConexionesPorCadena pools;
    private final PasswordEncoder passwords;
    public ApiController(ConexionesPorCadena pools,PasswordEncoder passwords) { this.pools=pools;this.passwords=passwords; }
    @GetMapping("/") public Map<String,Object> index() {
        return Map.of("application","CINE-TICS API","mode","demostración","csrf","/api/auth/csrf","cartelera","/api/public/CINE_TICS/shows");
    }
    @GetMapping("/api/auth/csrf") public Map<String,String> csrf(CsrfToken token) {
        return Map.of("headerName",token.getHeaderName(),"token",token.getToken());
    }
    @PostMapping("/api/public/{cadena}/register") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,String> register(@PathVariable("cadena") String cadena,@Valid @RequestBody RegistroCliente registro) throws SQLException {
        pools.validar(cadena);
        String id=new IdentidadesOracle(()->pools.abrir(cadena),passwords).registrar(registro);
        return Map.of("memberId",id,"username",cadena+":"+id,"role","CLIENTE","mode","saldo ficticio inicial de 1000 MXN");
    }
    @GetMapping("/api/me") public Map<String,Object> me(Authentication auth) throws SQLException {
        var id=IdentidadSesion.de(auth);
        try(var c=pools.abrir(id.cadena());var s=c.prepareStatement("select first_name,last_name,email,balance from CINE_OWNER.web_users u join CINE_OWNER.demo_wallets w on w.customer_id=u.customer_id where u.customer_id=?")) {
            s.setString(1,id.cliente());
            try(var r=s.executeQuery()) {
                if(!r.next()) throw new IllegalArgumentException("Cuenta inexistente.");
                return Map.of("memberId",id.cliente(),"tenant",id.cadena(),"roles",auth.getAuthorities().stream().map(Object::toString).toList(),
                    "nombre",r.getString(1),"apellidos",r.getString(2),"correo",r.getString(3),"saldoDemo",r.getBigDecimal(4));
            }
        }
    }
    @GetMapping("/api/public/{cadena}/shows") public List<Map<String,Object>> shows(@PathVariable("cadena") String cadena) throws SQLException {
        try(var c=pools.abrir(cadena);var s=c.prepareStatement("select f.id,f.title,f.starts_at,f.ends_at,a.name,b.name from CINE_OWNER.screenings f "
                + "join CINE_OWNER.auditoriums a on a.id=f.auditorium_id join CINE_OWNER.branches b on b.id=a.branch_id "
                + "where f.starts_at>systimestamp order by f.starts_at,f.id fetch first 100 rows only");var r=s.executeQuery()) {
            var result=new ArrayList<Map<String,Object>>();
            while(r.next()) result.add(Map.of("id",r.getLong(1),"titulo",r.getString(2),"inicio",r.getObject(3,OffsetDateTime.class),
                "fin",r.getObject(4,OffsetDateTime.class),"sala",r.getString(5),"sucursal",r.getString(6)));
            return result;
        }
    }
    @GetMapping("/api/public/{cadena}/shows/{funcion}/seats") public List<Reservas.Asiento> seats(@PathVariable("cadena") String cadena,@PathVariable("funcion") long funcion) throws SQLException {
        return new ReservasOracle(()->pools.abrir(cadena)).disponibilidad(funcion);
    }
    public record HoldRequest(@Positive long funcion,@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{1,64}") String clave,
                              @NotEmpty @Size(max=10) List<@NotNull @Positive Integer> asientos) {}
    @PostMapping("/api/client/holds") public Reservas.Resultado hold(Authentication auth,@Valid @RequestBody HoldRequest request) throws SQLException {
        var id=IdentidadSesion.de(auth);
        return new ReservasOracle(()->pools.abrir(id.cadena())).reservar(new Reservas.Solicitud(id.cliente(),request.funcion(),request.clave(),request.asientos()));
    }
    @PostMapping("/api/client/holds/{clave}/cancel") public Reservas.Resultado cancel(Authentication auth,@PathVariable("clave") String clave) throws SQLException {
        var id=IdentidadSesion.de(auth);
        return new ReservasOracle(()->pools.abrir(id.cadena())).cancelar(id.cliente(),clave);
    }
    public record PaymentRequest(@NotEmpty @Size(max=10) List<@NotNull Seleccion> seleccion) {}
    @PostMapping("/api/client/holds/{clave}/pay") public VentaBoletos.Resultado pay(Authentication auth,@PathVariable("clave") String clave,@Valid @RequestBody PaymentRequest request) throws SQLException {
        var id=IdentidadSesion.de(auth);
        return new BoletosOracle(()->pools.abrir(id.cadena())).comprar(new VentaBoletos.Solicitud(id.cliente(),clave,request.seleccion()));
    }
    @GetMapping("/api/client/tickets") public List<VentaBoletos.Boleto> tickets(Authentication auth) throws SQLException {
        var id=IdentidadSesion.de(auth);
        return new BoletosOracle(()->pools.abrir(id.cadena())).historial(id.cliente());
    }
    public record ProductPurchase(@Positive long sucursal,@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{1,64}") String clave,
                                  @NotEmpty @Size(max=100) List<@NotNull Item> items) {}
    @PostMapping("/api/client/purchases") public com.penta.cinetics.ventas.aplicacion.Compras.Resultado buy(Authentication auth,@Valid @RequestBody ProductPurchase request) throws SQLException {
        var id=IdentidadSesion.de(auth);
        return new ComprasOracle(()->pools.abrir(id.cadena())).comprar(new SolicitudCompra(id.cliente(),request.sucursal(),request.clave(),request.items()));
    }
    @GetMapping("/api/staff/summary") public Map<String,Object> summary(Authentication auth) throws SQLException {
        var id=IdentidadSesion.de(auth);
        try(var c=pools.abrir(id.cadena());var s=c.prepareStatement("select count(*),nvl(sum(total),0) from CINE_OWNER.ticket_sales");var r=s.executeQuery()) {
            r.next();return Map.of("tenant",id.cadena(),"ventasBoletos",r.getLong(1),"totalDemo",r.getBigDecimal(2));
        }
    }
    public record ScreeningRequest(@Positive long id,@Positive long sala,@NotBlank @Size(max=160) String titulo,
                                   @NotNull OffsetDateTime inicio,@NotNull OffsetDateTime fin) {}
    @PostMapping("/api/admin/shows") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Long> schedule(Authentication auth,@Valid @RequestBody ScreeningRequest request) throws SQLException {
        var id=IdentidadSesion.de(auth);
        new ReservasOracle(()->pools.abrir(id.cadena())).crearFuncion(request.id(),request.sala(),request.titulo(),request.inicio(),request.fin());
        return Map.of("id",request.id());
    }
}
