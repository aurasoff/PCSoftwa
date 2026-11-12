package co.edu.uniquindio.parquedelcafe.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

/** R5: cada cortesía queda con su registro de entrega. */
public class RegistroEntrega {
    private final String codigoCortesia;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final String tipoOperacion;
    private final String empleadoId;

    public RegistroEntrega(String codigoCortesia, LocalDate fecha, LocalTime hora, String tipoOperacion, String empleadoId) {
        this.codigoCortesia = codigoCortesia;
        this.fecha = fecha;
        this.hora = hora;
        this.tipoOperacion = tipoOperacion;
        this.empleadoId = empleadoId;
    }

    public String getCodigoCortesia() { return codigoCortesia; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public String getTipoOperacion() { return tipoOperacion; }
    public String getEmpleadoId() { return empleadoId; }
}
