package com.penta.cinetics.web;

import org.springframework.security.core.Authentication;

/** Parsed only from the authenticated principal; never from an action's JSON body. */
public record IdentidadSesion(String cadena,String cliente) {
    public static IdentidadSesion de(Authentication auth) { return parsear(auth.getName()); }
    public static IdentidadSesion parsear(String name) {
        if(name==null || !name.matches("(CINE_TICS|CADENA_DEMO):[A-Za-z0-9_-]{1,40}"))
            throw new IllegalArgumentException("Identidad inválida.");
        String[] parts=name.split(":",2);
        return new IdentidadSesion(parts[0],parts[1]);
    }
}
