package penta.code.cine.gestion.Personal;
import java.io.IOException;
import java.util.List;
import penta.code.cine.gestion.Datos.RegistroEmpleados;

public class AutenticacionEmpleado {

    public static Empleado autenticar(String idEmpleado, String contrasena) throws IOException {
        if (idEmpleado.startsWith("AG")) {
            return autenticarAyudante(idEmpleado, contrasena);
        } else if (idEmpleado.startsWith("CJ")) {
            return autenticarCajero(idEmpleado, contrasena);
        } else if (idEmpleado.startsWith("GE")) {
            return autenticarGerente(idEmpleado, contrasena);
        } else {
            System.out.println("ID de empleado inválido. No corresponde a un Ayudante, Cajero o Gerente.");
            return null;
        }
    }

    private static AyudanteGeneral autenticarAyudante(String idEmpleado, String contrasena) throws IOException {
        List<AyudanteGeneral> ayudantes = RegistroEmpleados.cargarAyudantesGenerales();
        for (AyudanteGeneral ayudante : ayudantes) {
            if (ayudante.getIdEmpleado().equals(idEmpleado) && ayudante.getContrasena().equals(contrasena)) {
                return ayudante;
            }
        }
        System.out.println("Ayudante General no encontrado o contraseña incorrecta.");
        return null;
    }

    private static Cajero autenticarCajero(String idEmpleado, String contrasena) throws IOException {
        List<Cajero> cajeros = RegistroEmpleados.cargarCajeros();
        for (Cajero cajero : cajeros) {
            if (cajero.getIdEmpleado().equals(idEmpleado) && cajero.getContrasena().equals(contrasena)) {
                return cajero;
            }
        }
        System.out.println("Cajero no encontrado o contraseña incorrecta.");
        return null;
    }

    private static Gerente autenticarGerente(String idEmpleado, String contrasena) throws IOException {
        List<Gerente> gerentes = RegistroEmpleados.cargarGerentes();
        for (Gerente gerente : gerentes) {
            if (gerente.getIdEmpleado().equals(idEmpleado) && gerente.getContrasena().equals(contrasena)) {
                return gerente;
            }
        }
        System.out.println("Gerente no encontrado o contraseña incorrecta.");
        return null;
    }
}
