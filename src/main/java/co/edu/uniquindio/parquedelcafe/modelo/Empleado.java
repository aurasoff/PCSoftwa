package co.edu.uniquindio.parquedelcafe.modelo;

/** Empleado de atención al visitante. R10: solo el del Punto de atención 2 puede registrar devoluciones. */
public class Empleado {
    private final String identificacion;
    private final String nombreEmpleado;
    private final String cargo;
    private final PuntoDeAtencion punto;
    private final boolean puedeDevolver;

    public Empleado(String identificacion, String nombreEmpleado, String cargo, PuntoDeAtencion punto, boolean puedeDevolver) {
        this.identificacion = identificacion;
        this.nombreEmpleado = nombreEmpleado;
        this.cargo = cargo;
        this.punto = punto;
        this.puedeDevolver = puedeDevolver;
    }

    public String getIdentificacion() { return identificacion; }
    public String getNombreEmpleado() { return nombreEmpleado; }
    public String getCargo() { return cargo; }
    public PuntoDeAtencion getPunto() { return punto; }

    public boolean puedeRegistrarDevolucion() { return puedeDevolver; }

    /** Así aparece en el selector "Empleado en sesión". */
    @Override
    public String toString() { return nombreEmpleado + " · " + punto.getNombre(); }
}
