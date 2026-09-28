package penta.code.cine.gestion.Financiera;
import java.io.*;
import penta.code.cine.gestion.Clientes.Cliente;

/**
 *
 * @author olveratrech
 */
public class RegistroCuentaBancaria {
    private static final String ARCHIVO_METODOS_PAGO = "MetodosDePago/";
    
    public static void registrarEnArchivo(Cliente cliente, CuentaBancaria cuentaBancaria) {
        String archivoMetodoPago = ARCHIVO_METODOS_PAGO + "/" + cuentaBancaria.getTitular() + "_" + cliente.getIdCliente() + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoMetodoPago, false))) {
            writer.write(cuentaBancaria.toString());
            writer.newLine();
            
        } catch (IOException e) {
            System.out.println("Error al registrar en el archivo: " + e.getMessage());
        }
    }
    
    public static CuentaBancaria cargarMetodoDePago(Cliente cliente) throws IOException {
        String archivoMetodoPago = ARCHIVO_METODOS_PAGO + "/" + cliente.getNombre() + " " + cliente.getApellidoPaterno() + " " + 
                cliente.getApellidoMaterno() + "_" + cliente.getIdCliente() + ".txt";
        
        File archivo = new File(archivoMetodoPago);
        
        if (!archivo.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivoMetodoPago))) {
            String linea;
            if ((linea = reader.readLine()) != null) {
                return csvToCuentaBancaria(linea);
            }
        }
        return null;
    }
    
    private static CuentaBancaria csvToCuentaBancaria(String linea) {
        try {
            String[] campo = linea.split(",");
            String titular = campo[0];
            String numeroCuenta = campo[1];
            String fechaVencimiento = campo[2];
            int cvv = Integer.parseInt(campo[3]);
            String tipoCuenta = campo[4];
            boolean estadoCuenta = Boolean.parseBoolean(campo[5]);
            double saldo = Double.parseDouble(campo[6]);
            String ultimaTransaccion = campo[7];

            CuentaBancaria metodoPago = new CuentaBancaria(titular, numeroCuenta, fechaVencimiento, cvv, tipoCuenta, estadoCuenta, saldo, ultimaTransaccion);
            
            return metodoPago;
        } catch (Exception e) {
            System.err.println("Error al leer cuenta bancaria: " + e.getMessage());
            return null;
        }
    }
}
