package co.edu.uniquindio.parquedelcafe.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

/** R8: la devolución guarda número, código del pasaporte, tipo, monto y fecha. */
public class Devolucion {
    private final String numeroDevolucion;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final String codigoPasaporte;
    private final String tipoPasaporte;
    private final double valorPagado;
    private final double monto;
    private final String motivo;
    private final String visitanteId;
    private final String empleadoId;

    public Devolucion(String numeroDevolucion, LocalDate fecha, LocalTime hora, String codigoPasaporte, String tipoPasaporte,
                      double valorPagado, double monto, String motivo, String visitanteId, String empleadoId) {
        this.numeroDevolucion = numeroDevolucion;
        this.fecha = fecha;
        this.hora = hora;
        this.codigoPasaporte = codigoPasaporte;
        this.tipoPasaporte = tipoPasaporte;
        this.valorPagado = valorPagado;
        this.monto = monto;
        this.motivo = motivo;
        this.visitanteId = visitanteId;
        this.empleadoId = empleadoId;
    }

    public String getNumeroDevolucion() { return numeroDevolucion; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public String getCodigoPasaporte() { return codigoPasaporte; }
    public String getTipoPasaporte() { return tipoPasaporte; }
    public double getValorPagado() { return valorPagado; }
    public double getMonto() { return monto; }
    public String getMotivo() { return motivo; }
    public String getVisitanteId() { return visitanteId; }
    public String getEmpleadoId() { return empleadoId; }
}
