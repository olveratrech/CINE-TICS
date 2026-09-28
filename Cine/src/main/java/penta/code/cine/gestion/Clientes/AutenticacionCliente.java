package penta.code.cine.gestion.Clientes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AutenticacionCliente {

    public static Cliente autenticarCliente(String user, String password, String archivoUsuarios) {
        try (BufferedReader reader = new BufferedReader(new FileReader(archivoUsuarios))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] campo = linea.split(",");
                //if (campo.length == 14) {
                    String idCliente =          campo[0];
                    String usuario =            campo[1];
                    String correo =             campo[2];
                    String contrasena =         campo[3];
                    String nombre =             campo[4];
                    String apPaterno =          campo[5];
                    String apMaterno =          campo[6];
                    String fechaNacimiento =    campo[7];
                    int edad = Integer.parseInt(campo[8]);
                    String genero =             campo[9];
                    String direccion =          campo[10];
                    String numTelefono =        campo[11];
                    String rfc =                campo[12];
                    boolean estadoCliente = Boolean.parseBoolean(campo[13]);
                    boolean inscritoProgramaLealtad = Boolean.parseBoolean(campo[14]);
                    if (usuario.equals(user) && contrasena.equals(password)) {
                        Cliente cliente = new Cliente(idCliente, usuario, correo, contrasena, nombre, apPaterno, apMaterno, 
                               fechaNacimiento, edad, genero, direccion, numTelefono, rfc, estadoCliente);
            
                        if (inscritoProgramaLealtad) {
                            cliente.unirseProgramaLealtad();
                        }
                        return cliente;
                    }
                //}
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo de usuarios: " + e.getMessage());
        }
        return null;
    }
}
