package penta.code.cine.gestion.Financiera;

/**
 *
 * @author olveratrech
 */
public class CuentaBancaria {
    // Atributos
    private String titular;
    private String numeroCuenta;
    private String fechaVencimiento;
    private int cvv;
    private String tipoCuenta;
    private boolean estadoCuenta;
    private double saldo;
    private String ultimaTransaccion;

    // Conatructor vacío
    public CuentaBancaria() {
    }

    // Constructor CuentaBancaria
    
    public CuentaBancaria(String titular, String numeroCuenta, String fechaVencimiento, int cvv, String tipoCuenta, boolean estadoCuenta, double saldo, String ultimaTransaccion) {
        this.titular = titular;
        this.numeroCuenta = numeroCuenta;
        this.fechaVencimiento = fechaVencimiento;
        this.cvv = cvv;
        this.tipoCuenta = tipoCuenta;
        this.estadoCuenta = estadoCuenta;
        this.saldo = saldo;
        this.ultimaTransaccion = ultimaTransaccion;
    }

    // Getters
    public String getTitular() {
        return this.titular;
    }

    public String getNumeroDeCuenta() {
        return this.numeroCuenta;
    }

    public String getFechaDeVencimiento() {
        return this.fechaVencimiento;
    }

    public int getCVV() {
        return this.cvv;
    }

    public String getTipoDeCuenta() {
        return this.tipoCuenta;
    }

    public boolean getEstadoDeLaCuenta() {
        return this.estadoCuenta;
    }

    public double getSaldo() {
        return this.saldo;
    }

    public String getUltimaTransaccion() {
        return this.ultimaTransaccion;
    }

    // Setters
    public void setTitular(String titular) {
        this.titular = titular;
    }

    public void setNumeroDeCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public void setFechaDeVencimiento(int mes, int anio) {
        this.fechaVencimiento = mes + "/" + anio;
    }

    public void setCVV(int cvv) {
        this.cvv = cvv;
    }

    public void setTipoDeCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public void setEstadoDeLaCuenta(boolean estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public void setUltimaTransaccion(String ultimaTransaccion) {
        this.ultimaTransaccion = ultimaTransaccion;
    }
    
    // Métodos
    
    @Override
    public String toString() {
        return titular + "," + numeroCuenta + "," + fechaVencimiento + "," + cvv + "," + tipoCuenta + "," + estadoCuenta + "," + saldo + "," + ultimaTransaccion;
    }
}

