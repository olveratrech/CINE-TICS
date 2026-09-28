package com.penta.cinetics.ventas;

import static org.junit.jupiter.api.Assertions.*;
import com.penta.cinetics.ventas.aplicacion.CompraSimulada;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CompraSimuladaTest {
    @ParameterizedTest
    @CsvSource({"130,100,true,SALDO_INSUFICIENTE", "130,200,false,CUENTA_INACTIVA",
        "0,200,true,IMPORTE_INVALIDO", "-1,200,true,IMPORTE_INVALIDO",
        "10,-1,true,IMPORTE_INVALIDO", "1.001,200,true,IMPORTE_INVALIDO"})
    void deniedPaymentNeverRecordsSalePointsOrClearsCart(String amount, String balance,
            boolean active, CompraSimulada.Estado expected) throws Exception {
        List<String> effects = new ArrayList<>();
        var result = new CompraSimulada().ejecutar(new BigDecimal(amount), new BigDecimal(balance),
                active, remaining -> effects.add("sale-ticket-points-clear"));
        assertEquals(expected, result.estado());
        assertFalse(result.aprobada());
        assertTrue(effects.isEmpty());
    }

    @Test
    void approvedPaymentPassesExactBalanceAndConfirmsOnce() throws Exception {
        List<BigDecimal> effects = new ArrayList<>();
        var result = new CompraSimulada().ejecutar(new BigDecimal("0.10"), new BigDecimal("0.30"),
                true, effects::add);
        assertTrue(result.aprobada());
        assertEquals(new BigDecimal("0.20"), result.saldoRestante());
        assertEquals(List.of(new BigDecimal("0.20")), effects);
    }

    @Test
    void exactBalanceCanBeSpent() throws Exception {
        var result = new CompraSimulada().ejecutar(new BigDecimal("130"), new BigDecimal("130"),
                true, remaining -> assertEquals(new BigDecimal("0.00"), remaining));
        assertTrue(result.aprobada());
    }

    @Test
    void recordingFailureIsNotReportedAsApproved() {
        assertThrows(IOException.class, () -> new CompraSimulada().ejecutar(
                new BigDecimal("65"), new BigDecimal("100"), true,
                remaining -> { throw new IOException("simulated failure"); }));
    }
}
