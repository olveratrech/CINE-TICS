package com.penta.cinetics.ventas;

import com.penta.cinetics.web.CineWebApplication;
import com.penta.cinetics.identidad.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/** Real embedded HTTP server and both Oracle PDBs; no mocked security or persistence. */
class WebOracleIT {
    static ConfigurableApplicationContext app;
    static String base;
    static final ObjectMapper JSON=new ObjectMapper();
    static final String PASSWORD="Ficticia-2026-prueba";
    record Account(String tenant,String id) {}
    static final List<Account> accounts=new ArrayList<>();
    @BeforeAll static void start() {
        app=SpringApplication.run(CineWebApplication.class,"--server.port=0","--spring.main.banner-mode=off","--cine.auth.max-attempts=200");
        base="http://127.0.0.1:"+app.getEnvironment().getProperty("local.server.port");
    }
    @AfterAll static void stop() throws Exception {
        try {
            for(var account:accounts) try(var c=ComprasOracleIT.open(account.tenant())) {
                for(String table:List.of("web_users","demo_wallets")) try(var s=c.prepareStatement("delete from CINE_OWNER."+table+" where customer_id=?")) {
                    s.setString(1,account.id());s.executeUpdate();
                }
            }
        } finally {if(app!=null)app.close();}
    }
    static class Browser {
        final CookieManager cookies=new CookieManager(null,CookiePolicy.ACCEPT_ALL);
        final HttpClient client=HttpClient.newBuilder().cookieHandler(cookies).followRedirects(HttpClient.Redirect.NEVER).build();
        String csrf;
        HttpResponse<String> send(String method,String path,String body,String type,boolean token) throws Exception {
            var req=HttpRequest.newBuilder(URI.create(base+path)).timeout(java.time.Duration.ofSeconds(20));
            if(type!=null)req.header("Content-Type",type);
            if(token)req.header("X-CSRF-TOKEN",csrf);
            req.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body));
            return client.send(req.build(),HttpResponse.BodyHandlers.ofString());
        }
        HttpResponse<String> get(String path) throws Exception {return send("GET",path,null,null,false);}
        HttpResponse<String> post(String path,Object body) throws Exception {return send("POST",path,JSON.writeValueAsString(body),"application/json",true);}
        void csrf() throws Exception {var response=get("/api/auth/csrf");assertEquals(200,response.statusCode());csrf=JSON.readTree(response.body()).get("token").asString();}
        HttpResponse<String> login(Account account,String password) throws Exception {
            csrf();
            var response=send("POST","/api/auth/login","username="+URLEncoder.encode(account.tenant()+":"+account.id(),StandardCharsets.UTF_8)
                +"&password="+URLEncoder.encode(password,StandardCharsets.UTF_8),"application/x-www-form-urlencoded",true);
            if(response.statusCode()==200)csrf();
            return response;
        }
    }
    static Map<String,Object> registration(String email) {
        return new HashMap<>(Map.of("nombre","Prueba","apellidos","Ficticia","direccion","Dirección de prueba","telefono","5555555555","correo",email,"password",PASSWORD));
    }
    static Account register(Browser browser,String tenant) throws Exception {
        browser.csrf();
        var result=browser.post("/api/public/"+tenant+"/register",registration(UUID.randomUUID()+"@example.test"));
        assertEquals(201,result.statusCode(),result.body());
        var account=new Account(tenant,JSON.readTree(result.body()).get("memberId").asString());accounts.add(account);return account;
    }
    static Account operator(String tenant,IdentidadesOracle.Rol role) throws Exception {
        var profile=new RegistroCliente("Prueba","Operador","Dirección ficticia","5555555555",UUID.randomUUID()+"@example.test",PASSWORD);
        var id=new IdentidadesOracle(()->ComprasOracleIT.open(tenant),new BCryptPasswordEncoder(12)).provisionar(profile,role);
        var account=new Account(tenant,id);accounts.add(account);return account;
    }
    @Test void registrationHashLoginSessionRotationAndLogout() throws Exception {
        var browser=new Browser();browser.csrf();
        var before=browser.cookies.getCookieStore().getCookies().stream().filter(c->c.getName().equals("CINE_SESSION")).findFirst().orElseThrow().getValue();
        var account=register(browser,"CINE_TICS");
        try(var c=ComprasOracleIT.open(account.tenant());var s=c.prepareStatement("select password_hash,role_name from CINE_OWNER.web_users where customer_id=?")) {
            s.setString(1,account.id());try(var r=s.executeQuery()) {
                assertTrue(r.next());assertNotEquals(PASSWORD,r.getString(1));assertTrue(new BCryptPasswordEncoder().matches(PASSWORD,r.getString(1)));assertEquals("CLIENTE",r.getString(2));
            }
        }
        assertEquals(401,browser.login(account,"wrong-password").statusCode());
        assertEquals(200,browser.login(account,PASSWORD).statusCode());
        var cookie=browser.cookies.getCookieStore().getCookies().stream().filter(c->c.getName().equals("CINE_SESSION")).findFirst().orElseThrow();
        assertNotEquals(before,cookie.getValue());assertTrue(cookie.isHttpOnly());
        var me=browser.get("/api/me");assertEquals(200,me.statusCode());assertFalse(me.body().contains("password"));assertFalse(me.body().contains("hash"));
        assertEquals(account.id(),JSON.readTree(me.body()).get("memberId").asString());
        assertEquals(200,browser.post("/api/auth/logout",Map.of()).statusCode());
        assertEquals(401,browser.get("/api/me").statusCode());
    }
    @Test void csrfAndUnknownFieldsBlockPrivilegeAndOwnershipInjection() throws Exception {
        var browser=new Browser();var account=register(browser,"CINE_TICS");
        assertEquals(403,browser.send("POST","/api/auth/login","username=x&password=y","application/x-www-form-urlencoded",false).statusCode());
        assertEquals(200,browser.login(account,PASSWORD).statusCode());
        assertEquals(403,browser.send("POST","/api/client/holds","{}","application/json",false).statusCode());
        assertEquals(400,browser.post("/api/client/holds",Map.of("funcion",1,"clave","one","asientos",List.of(1),"cliente","DEMO","cadena","CADENA_DEMO")).statusCode());
        assertEquals(403,browser.get("/api/staff/summary").statusCode());
        assertEquals(403,browser.post("/api/admin/shows",Map.of()).statusCode());
        var injected=registration(UUID.randomUUID()+"@example.test");injected.put("role","ADMIN");
        assertEquals(400,browser.post("/api/public/CINE_TICS/register",injected).statusCode());
        var anonymous=new Browser();assertEquals(401,anonymous.get("/api/client/tickets").statusCode());
        anonymous.csrf();assertEquals(401,anonymous.post("/api/client/holds",Map.of()).statusCode());
    }
    @Test void fullHttpPurchaseIsIdempotentAndPrivate() throws Exception {
        try(var e=new ReservasOracleIT.Escenario()) {
            var a=new Browser();var account=register(a,"CINE_TICS");assertEquals(200,a.login(account,PASSWORD).statusCode());
            assertEquals(200,a.get("/api/public/CINE_TICS/shows/"+e.f.branch+"/seats").statusCode());
            assertEquals(200,a.post("/api/client/holds",Map.of("funcion",e.f.branch,"clave","web","asientos",List.of(1,2))).statusCode());
            var b=new Browser();var other=register(b,"CINE_TICS");assertEquals(200,b.login(other,PASSWORD).statusCode());
            assertEquals(404,b.post("/api/client/holds/web/cancel",Map.of()).statusCode());
            var pay=Map.of("seleccion",List.of(Map.of("asiento",1,"tipo","ADULTO"),Map.of("asiento",2,"tipo","NINO")));
            assertEquals(404,b.post("/api/client/holds/web/pay",pay).statusCode());
            var first=a.post("/api/client/holds/web/pay",pay);assertEquals(200,first.statusCode(),first.body());
            var retry=a.post("/api/client/holds/web/pay",pay);assertEquals(200,retry.statusCode());
            assertEquals(JSON.readTree(first.body()).get("venta"),JSON.readTree(retry.body()).get("venta"));
            assertTrue(JSON.readTree(retry.body()).get("repetida").asBoolean());
            assertEquals(2,JSON.readTree(a.get("/api/client/tickets").body()).size());
            assertEquals(0,JSON.readTree(b.get("/api/client/tickets").body()).size());
            assertEquals(870,JSON.readTree(a.get("/api/me").body()).get("saldoDemo").asInt());
        }
    }
    @Test void tenantCannotBeChangedByHeaderAndOtherPdbCannotAuthenticateAccount() throws Exception {
        try(var a=new ReservasOracleIT.Escenario();var b=new ReservasOracleIT.Escenario(new ComprasOracleIT.Fixture("CADENA_DEMO",a.f.customer,a.f.branch))) {
            var browser=new Browser();var account=register(browser,"CINE_TICS");
            assertEquals(401,browser.login(new Account("CADENA_DEMO",account.id()),PASSWORD).statusCode());
            assertEquals(200,browser.login(account,PASSWORD).statusCode());
            var req=HttpRequest.newBuilder(URI.create(base+"/api/client/holds")).header("X-Tenant","CADENA_DEMO")
                .header("X-CSRF-TOKEN",browser.csrf).header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(Map.of("funcion",a.f.branch,"clave","tenant","asientos",List.of(1))))).build();
            assertEquals(200,browser.client.send(req,HttpResponse.BodyHandlers.ofString()).statusCode());
            assertFalse(a.servicio.disponibilidad(a.f.branch).getFirst().disponible());
            assertTrue(b.servicio.disponibilidad(b.f.branch).getFirst().disponible());
            assertEquals(400,browser.get("/api/public/UNKNOWN/shows").statusCode());
        }
    }
    @Test void staffAndAdminPermissionsAreEnforced() throws Exception {
        try(var e=new ReservasOracleIT.Escenario()) {
            var staff=new Browser();assertEquals(200,staff.login(operator("CINE_TICS",IdentidadesOracle.Rol.EMPLEADO),PASSWORD).statusCode());
            assertEquals(200,staff.get("/api/staff/summary").statusCode());
            assertEquals(403,staff.post("/api/admin/shows",Map.of()).statusCode());
            assertEquals(403,staff.get("/api/client/tickets").statusCode());
            var admin=new Browser();assertEquals(200,admin.login(operator("CINE_TICS",IdentidadesOracle.Rol.ADMIN),PASSWORD).statusCode());
            var show=Map.of("id",e.f.branch+1,"sala",e.f.branch,"titulo","HTTP admin","inicio",e.inicio.plusDays(1).toString(),"fin",e.inicio.plusDays(1).plusHours(2).toString());
            assertEquals(201,admin.post("/api/admin/shows",show).statusCode());
            assertEquals(200,admin.get("/api/staff/summary").statusCode());
        }
    }
    @Test void inactiveAccountLosesExistingSession() throws Exception {
        var b=new Browser();var account=register(b,"CINE_TICS");assertEquals(200,b.login(account,PASSWORD).statusCode());
        try(var c=ComprasOracleIT.open(account.tenant());var s=c.prepareStatement("update CINE_OWNER.demo_wallets set active=0 where customer_id=?")) {
            s.setString(1,account.id());s.executeUpdate();
        }
        assertEquals(401,b.get("/api/me").statusCode());assertEquals(401,b.get("/api/client/tickets").statusCode());
    }
    @Test void malformedRegistrationDoesNotCreateAccountAndDuplicateRollsBackWallet() throws Exception {
        var browser=new Browser();browser.csrf();
        var invalid=registration(UUID.randomUUID()+"@example.test");invalid.put("password","short");
        assertEquals(400,browser.post("/api/public/CINE_TICS/register",invalid).statusCode());
        var input=registration(UUID.randomUUID()+"@example.test");var first=browser.post("/api/public/CINE_TICS/register",input);
        assertEquals(201,first.statusCode());accounts.add(new Account("CINE_TICS",JSON.readTree(first.body()).get("memberId").asString()));
        long before=wallets();assertEquals(409,browser.post("/api/public/CINE_TICS/register",input).statusCode());assertEquals(before,wallets());
    }
    static long wallets() throws SQLException {
        try(var c=ComprasOracleIT.open("CINE_TICS");var s=c.createStatement();var r=s.executeQuery("select count(*) from CINE_OWNER.demo_wallets")) {r.next();return r.getLong(1);}
    }
}
