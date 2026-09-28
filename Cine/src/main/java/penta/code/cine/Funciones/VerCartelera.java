package penta.code.cine.Funciones;

import java.util.List;
import penta.code.cine.gestion.CineTICS.Sucursal;
import penta.code.cine.gestion.Funciones.Funcion;

/**
 *
 * @author olveratrech
 */
public class VerCartelera {
    public static void mostrarCartelera(Sucursal sucursal) {
        //System.out.println("\nPeliculas disponibles hoy en " + sucursal.getNombreSucursal());
        for (int i = 1; i <= 3; i++) {
            System.out.println("\n\tSALA " + i + "\n");
            showFunciones(Funcion.cargarFuncionesDesdeArchivo(sucursal.getNombreSucursal(), i));
        }
    }
    
    public static void mostrarCarteleraPorSala(Sucursal sucursal, int idSala) {
        System.out.println("\nPeliculas disponibles hoy en " + sucursal.getNombreSucursal());
        
        System.out.println("\n\tSALA " + idSala + "\n");
        showFunciones(Funcion.cargarFuncionesDesdeArchivo(sucursal.getNombreSucursal(), idSala));
    }
    
    private static void showFunciones(List<Funcion> funciones) {
        for (Funcion funcion : funciones) {
            System.out.println(funcion);
            System.out.println();
        }
    }
}
