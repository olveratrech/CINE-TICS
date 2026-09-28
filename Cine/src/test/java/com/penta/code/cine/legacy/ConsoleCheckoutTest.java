package com.penta.code.cine.legacy;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConsoleCheckoutTest {
    @TempDir Path directory;

    @ParameterizedTest
    @CsvSource({"products,false", "products,true", "tickets,false", "tickets,true"})
    void publicConsoleFlowRecordsSalesOnlyForApprovedPayment(String kind, boolean approved) throws Exception {
        String classes = Path.of("target/test-classes").toAbsolutePath() + File.pathSeparator
                + Path.of("target/classes").toAbsolutePath();
        Path output = directory.resolve("console.log");
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", classes, ConsoleCheckoutProbe.class.getName(), kind, Boolean.toString(approved))
                .directory(directory.toFile()).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        boolean finished = process.waitFor(20, TimeUnit.SECONDS);
        if (!finished) process.destroyForcibly();
        assertTrue(finished, "Console process timed out");
        assertEquals(0, process.exitValue(), Files.readString(output));
        Path sales = directory.resolve(kind.equals("products") ? "Ventas" : "VentasBoletos");
        assertEquals(approved, Files.exists(sales), Files.readString(output));
        if (approved) {
            try (var paths = Files.walk(sales)) {
                var tickets = paths.filter(p -> p.getParent().getFileName().toString().equals("Tickets"))
                        .filter(Files::isRegularFile).toList();
                assertEquals(1, tickets.size());
                assertTrue(Files.readString(tickets.getFirst()).contains("130"));
            }
        }
    }
}
