package penta.code.cine.gestion.Dulceria;
import java.io.Serializable;
import java.io.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
/**
 *
 * @author olveratrech
 */
public class Producto implements Serializable {
    private String codigo;
    private String nombre;
    private double precio;
    private String categoria;
    private int stock;

    public Producto(String codigo, String nombre, double precio, String categoria, int stock) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
        this.stock = stock;
    }

    // Getters y Setters
    public String getCodigo() {
        return codigo;
    }
    
    public String getNombre() {
            return nombre;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public double getPrecio() {
        return precio;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public int getStock() {
        return stock;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void disminuirStock(int cantidad) {
        this.stock = this.stock - cantidad;
    }

    @Override
    public String toString() {
        return codigo + "," + nombre + "," + precio + "," + categoria + "," + stock;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Producto producto = (Producto) obj;
        return codigo.equals(producto.codigo); // Considera iguales los productos con el mismo código.
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

}