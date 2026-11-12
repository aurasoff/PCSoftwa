package co.edu.uniquindio.parquedelcafe.logica;

/** Resultado de validar una regla de negocio. */
public record Resultado(boolean ok, String regla, int status, String mensaje) {

    public static Resultado aprobado() { return new Resultado(true, null, 200, null); }

    public static Resultado bloqueado(String regla, int status, String mensaje) {
        return new Resultado(false, regla, status, mensaje);
    }
}
