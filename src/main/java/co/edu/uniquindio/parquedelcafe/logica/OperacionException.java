package co.edu.uniquindio.parquedelcafe.logica;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Error de una operación (equivale a la respuesta de error de la API del MVP web):
 * status 400 errores por campo, 401 sin sesión, 403 R10, 404 no encontrado, 409 conflicto, 422 regla de negocio.
 */
public class OperacionException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int status;
    private final String regla;
    private final transient Map<String, String> errores;

    public OperacionException(int status, String regla, String mensaje) {
        super(mensaje);
        this.status = status;
        this.regla = regla;
        this.errores = Collections.emptyMap();
    }

    public OperacionException(Map<String, String> errores) {
        super("Revisa los datos del formulario.");
        this.status = 400;
        this.regla = null;
        this.errores = Collections.unmodifiableMap(new LinkedHashMap<>(errores));
    }

    public static OperacionException desde(Resultado r) {
        return new OperacionException(r.status(), r.regla(), r.mensaje());
    }

    public int getStatus() { return status; }
    public String getRegla() { return regla; }
    public Map<String, String> getErrores() { return errores; }
}
