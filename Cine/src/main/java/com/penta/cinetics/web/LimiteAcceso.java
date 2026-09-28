package com.penta.cinetics.web;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

/** Small, bounded per-process limiter for local login/registration endpoints. */
public final class LimiteAcceso {
    private record Ventana(long inicio,int intentos) {}
    private final Map<String,Ventana> ventanas=new HashMap<>();
    private final Clock clock;
    private final int maximo;
    public LimiteAcceso(Clock clock,int maximo) {
        if(maximo<1) throw new IllegalArgumentException("Límite inválido.");
        this.clock=clock;this.maximo=maximo;
    }
    public synchronized boolean permitir(String origen) {
        long now=clock.millis();
        ventanas.entrySet().removeIf(e->now-e.getValue().inicio()>=60000);
        var current=ventanas.get(origen);
        if(current==null) {
            if(ventanas.size()>=1024) return false;
            ventanas.put(origen,new Ventana(now,1));return true;
        }
        if(current.intentos()>=maximo) return false;
        ventanas.put(origen,new Ventana(current.inicio(),current.intentos()+1));return true;
    }
}
