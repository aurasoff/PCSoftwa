package co.edu.uniquindio.parquedelcafe.modelo;

/** Estado del pasaporte comprado (R7). */
public enum EstadoPasaporte {
    PAGADO("pagado"),
    SIN_PAGO("sin pago"),
    USADO("usado"),
    CANCELADO("cancelado");

    private final String texto;

    EstadoPasaporte(String texto) { this.texto = texto; }

    @Override
    public String toString() { return texto; }
}
