package com.penta.cinetics.web;

import com.penta.cinetics.identidad.*;
import jakarta.validation.Validation;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Local operator tool. Credentials come from an interactive wrapper, never command arguments. */
public final class ProvisionarUsuario {
    public static void main(String[] args) throws Exception {
        if(args.length!=2) throw new IllegalArgumentException("Uso: cadena CLIENTE|EMPLEADO|ADMIN");
        var role=IdentidadesOracle.Rol.valueOf(args[1]);
        var profile=new RegistroCliente(System.getenv("CINE_USER_FIRST"),System.getenv("CINE_USER_LAST"),System.getenv("CINE_USER_ADDRESS"),
            System.getenv("CINE_USER_PHONE"),System.getenv("CINE_USER_EMAIL"),System.getenv("CINE_USER_PASSWORD"));
        try(var factory=Validation.buildDefaultValidatorFactory()) {
            if(!factory.getValidator().validate(profile).isEmpty()) throw new IllegalArgumentException("Datos de usuario inválidos.");
        }
        try(var pools=new ConexionesPorCadena()) {
            pools.validar(args[0]);
            String id=new IdentidadesOracle(()->pools.abrir(args[0]),new BCryptPasswordEncoder(12)).provisionar(profile,role);
            System.out.println("Usuario creado: "+args[0]+":"+id+" | rol "+role);
            System.out.println("Saldo de demostración: 1000 MXN. La contraseña no se muestra ni se guarda en texto plano.");
        }
    }
}
