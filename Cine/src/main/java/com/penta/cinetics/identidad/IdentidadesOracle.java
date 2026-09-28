package com.penta.cinetics.identidad;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Registration is atomic with its synthetic wallet. No legacy plaintext credentials are imported. */
public final class IdentidadesOracle {
    @FunctionalInterface public interface Conexion { Connection abrir() throws SQLException; }
    public enum Rol { CLIENTE, EMPLEADO, ADMIN }
    public record Usuario(String id, String hash, Rol rol, boolean activo) {
        @Override public String toString() { return "Usuario["+id+","+rol+"]"; }
    }
    public static final class Duplicado extends RuntimeException {}
    private final Conexion conexiones;
    private final PasswordEncoder passwords;
    public IdentidadesOracle(Conexion conexiones,PasswordEncoder passwords) { this.conexiones=conexiones;this.passwords=passwords; }
    public String registrar(RegistroCliente registro) throws SQLException { return provisionar(registro,Rol.CLIENTE); }
    /** Trusted operator entry point only; public registration never accepts a role. */
    public String provisionar(RegistroCliente registro,Rol rol) throws SQLException {
        if(registro.password()==null || registro.password().length()<8 || registro.password().getBytes(StandardCharsets.UTF_8).length>72)
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres y como máximo 72 bytes UTF-8.");
        String id="U_"+UUID.randomUUID().toString().replace("-","");
        String hash=passwords.encode(registro.password());
        try(Connection c=conexiones.abrir()) {
            c.setAutoCommit(false);
            try {
                try(var s=c.prepareStatement("insert into CINE_OWNER.demo_wallets(customer_id,balance,active) values (?,1000,1)")) {
                    s.setString(1,id);s.executeUpdate();
                }
                try(var s=c.prepareStatement("insert into CINE_OWNER.web_users(customer_id,password_hash,role_name,first_name,last_name,address,phone,email) values (?,?,?,?,?,?,?,?)")) {
                    s.setString(1,id);s.setString(2,hash);s.setString(3,rol.name());s.setString(4,registro.nombre().strip());
                    s.setString(5,registro.apellidos().strip());s.setString(6,registro.direccion().strip());
                    s.setString(7,registro.telefono().strip());s.setString(8,registro.correo().strip().toLowerCase(Locale.ROOT));s.executeUpdate();
                }
                c.commit(); return id;
            } catch(SQLException|RuntimeException error) {
                try { c.rollback(); } catch(SQLException rollback) { error.addSuppressed(rollback); }
                if(error instanceof SQLException sql && sql.getErrorCode()==1) throw new Duplicado();
                throw error;
            }
        }
    }
    public Usuario buscar(String id) throws SQLException {
        try(Connection c=conexiones.abrir();var s=c.prepareStatement("select u.password_hash,u.role_name,w.active from CINE_OWNER.web_users u join CINE_OWNER.demo_wallets w on w.customer_id=u.customer_id where u.customer_id=?")) {
            s.setString(1,id);
            try(var r=s.executeQuery()) {
                return r.next()?new Usuario(id,r.getString(1),Rol.valueOf(r.getString(2)),r.getInt(3)==1):null;
            }
        }
    }
}
