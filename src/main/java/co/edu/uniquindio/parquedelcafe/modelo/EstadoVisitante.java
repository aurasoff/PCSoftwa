package co.edu.uniquindio.parquedelcafe.modelo;

/** Estado del visitante (R3: solo un visitante activo recibe la cortesía). */
public enum EstadoVisitante {
    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String texto;

    EstadoVisitante(String texto) { this.texto = texto; }

    @Override
    public String toString() { return texto; }
}
