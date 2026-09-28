package com.penta.cinetics.ventas.aplicacion;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record SolicitudCompra(String cliente, long sucursal, String clave, List<Item> items) {
    public record Item(long producto, int cantidad) {
        public Item {
            if (producto <= 0 || cantidad <= 0) throw new IllegalArgumentException("Producto y cantidad deben ser positivos.");
        }
    }

    public SolicitudCompra {
        if (cliente == null || !cliente.matches("[A-Za-z0-9_-]{1,40}") || sucursal <= 0
                || clave == null || !clave.matches("[A-Za-z0-9_-]{1,64}")
                || items == null || items.isEmpty() || items.size() > 100) {
            throw new IllegalArgumentException("Solicitud de compra inválida.");
        }
        Map<Long, Integer> cantidades = new TreeMap<>();
        for (Item item : items) {
            if (item == null) throw new IllegalArgumentException("Producto inválido.");
            cantidades.merge(item.producto(), item.cantidad(), Math::addExact);
        }
        items = cantidades.entrySet().stream().map(e -> new Item(e.getKey(), e.getValue())).toList();
    }

    /** Canonical identity excludes current catalog prices: retries replay the original result. */
    public String huella() {
        StringBuilder payload = new StringBuilder(cliente).append('|').append(sucursal);
        for (Item item : items) payload.append('|').append(item.producto()).append(':').append(item.cantidad());
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(payload.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
