package com.penta.cinetics.web;

import java.time.*;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LimiteAccesoTest {
    @Test void limitsAttemptsAndReopensAfterWindow() {
        var time=new AtomicLong();
        var clock=new Clock() {
            public ZoneId getZone(){return ZoneOffset.UTC;}
            public Clock withZone(ZoneId zone){return this;}
            public Instant instant(){return Instant.ofEpochMilli(time.get());}
        };
        var limiter=new LimiteAcceso(clock,2);
        assertTrue(limiter.permitir("a"));assertTrue(limiter.permitir("a"));assertFalse(limiter.permitir("a"));
        assertTrue(limiter.permitir("b"));time.set(60000);assertTrue(limiter.permitir("a"));
    }
    @Test void boundsTrackedOriginsAndRejectsInvalidConfiguration() {
        var limiter=new LimiteAcceso(Clock.fixed(Instant.EPOCH,ZoneOffset.UTC),1);
        for(int i=0;i<1024;i++)assertTrue(limiter.permitir("origin-"+i));
        assertFalse(limiter.permitir("new"));
        assertThrows(IllegalArgumentException.class,()->new LimiteAcceso(Clock.systemUTC(),0));
    }
}
