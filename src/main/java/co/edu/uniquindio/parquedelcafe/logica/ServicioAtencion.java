package co.edu.uniquindio.parquedelcafe.logica;

import co.edu.uniquindio.parquedelcafe.modelo.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador del sistema (en el MVP web era el ServidorAPI): las pantallas solo hablan con esta clase.
 * Aquí se validan las reglas de verdad; si algo no procede lanza {@link OperacionException}.
 */
public class ServicioAtencion {

    private final Datos datos;
    private Empleado empleadoActual;

    public ServicioAtencion(Datos datos) { this.datos = datos; }

    // ------------------------------------------------------------ sesión (RNF-06)

    public List<Empleado> getEmpleados() { return Collections.unmodifiableList(datos.empleados); }

    public LocalDate hoy() { return datos.hoy(); }

    public void iniciarSesion(String empleadoId) {
        empleadoActual = datos.empleados.stream()
                .filter(e -> e.getIdentificacion().equals(empleadoId))
                .findFirst().orElse(null);
    }

    public void cerrarSesion() { empleadoActual = null; }

    public Empleado getEmpleadoActual() { return empleadoActual; }

    private Empleado exigirSesion() {
        if (empleadoActual == null) {
            throw new OperacionException(401, null, "Elige un empleado en sesión para continuar.");
        }
        return empleadoActual;
    }

    // ------------------------------------------------------------ pantalla 1: visitantes (CRUD)

    /** Visitante con el número de cortesías que lleva este año (columna "Cortesías este año"). */
    public record VisitanteConConteo(Visitante visitante, int cortesiasAnio) { }

    /** Datos tal como vienen del formulario. */
    public record FormularioVisitante(String identificacion, String nombreVisitante, LocalDate fechaNacimiento,
                                      String telefono, String lugarResidencia, EstadoVisitante estado) { }

    private int cortesiasDelAnio(String visitanteId, int anio) {
        return (int) datos.cortesias.stream()
                .filter(c -> c.getVisitanteId().equals(visitanteId) && c.esDelAnio(anio)).count();
    }

    private VisitanteConConteo conConteo(Visitante v) {
        return new VisitanteConConteo(v, cortesiasDelAnio(v.getIdentificacion(), hoy().getYear()));
    }

    private Optional<Visitante> visitante(String id) {
        return datos.visitantes.stream().filter(v -> v.getIdentificacion().equals(id)).findFirst();
    }

    /** Buscar por documento o nombre (REQ003). */
    public List<VisitanteConConteo> buscarVisitantes(String query) {
        exigirSesion();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<VisitanteConConteo> lista = new ArrayList<>();
        for (Visitante v : datos.visitantes) {
            if ((v.getIdentificacion() + " " + v.getNombreVisitante()).toLowerCase(Locale.ROOT).contains(q)) {
                lista.add(conConteo(v));
            }
        }
        return lista;
    }

    /** Validaciones básicas del CRUD de Visitante (mismos mensajes del MVP web). */
    public Map<String, String> validarVisitante(FormularioVisitante f, boolean creando) {
        Map<String, String> e = new LinkedHashMap<>();
        String id = f.identificacion() == null ? "" : f.identificacion().trim();
        if (id.isEmpty()) e.put("identificacion", "Ingresa el documento del visitante.");
        else if (!id.matches("\\d{5,15}")) e.put("identificacion", "El documento debe tener solo números (5 a 15 dígitos).");
        else if (creando && visitante(id).isPresent()) e.put("identificacion", "Ya existe un visitante con ese documento.");
        if (f.nombreVisitante() == null || f.nombreVisitante().isBlank()) e.put("nombreVisitante", "Ingresa el nombre completo.");
        if (f.fechaNacimiento() == null) e.put("fechaNacimiento", "Ingresa la fecha de nacimiento.");
        else if (f.fechaNacimiento().isAfter(hoy())) e.put("fechaNacimiento", "La fecha de nacimiento no puede ser futura.");
        String t = f.telefono() == null ? "" : f.telefono().trim();
        if (!t.isEmpty() && !t.matches("\\d{10}")) e.put("telefono", "Ingresa 10 dígitos.");
        return e;
    }

    public VisitanteConConteo crearVisitante(FormularioVisitante f) {
        exigirSesion();
        Map<String, String> errores = validarVisitante(f, true);
        if (!errores.isEmpty()) throw new OperacionException(errores);
        Visitante v = new Visitante(f.identificacion().trim(), f.nombreVisitante().trim(), f.fechaNacimiento(),
                limpiar(f.telefono()), limpiar(f.lugarResidencia()),
                f.estado() == null ? EstadoVisitante.ACTIVO : f.estado());
        datos.visitantes.add(v);
        return conConteo(v);
    }

    public VisitanteConConteo actualizarVisitante(String id, FormularioVisitante f) {
        exigirSesion();
        Visitante v = visitante(id).orElseThrow(() -> new OperacionException(404, null, "No encontramos ese visitante."));
        FormularioVisitante conId = new FormularioVisitante(id, f.nombreVisitante(), f.fechaNacimiento(),
                f.telefono(), f.lugarResidencia(), f.estado());
        Map<String, String> errores = validarVisitante(conId, false);
        if (!errores.isEmpty()) throw new OperacionException(errores);
        v.setNombreVisitante(f.nombreVisitante().trim());
        v.setFechaNacimiento(f.fechaNacimiento());
        v.setTelefono(limpiar(f.telefono()));
        v.setLugarResidencia(limpiar(f.lugarResidencia()));
        v.setEstado(f.estado() == null ? EstadoVisitante.ACTIVO : f.estado());
        return conConteo(v);
    }

    public void eliminarVisitante(String id) {
        exigirSesion();
        Visitante v = visitante(id).orElseThrow(() -> new OperacionException(404, null, "No encontramos ese visitante."));
        boolean tieneHistorial = datos.cortesias.stream().anyMatch(c -> c.getVisitanteId().equals(id))
                || datos.pasaportes.stream().anyMatch(p -> p.getVisitanteId().equals(id));
        if (tieneHistorial) {
            throw new OperacionException(409, null,
                    "No se puede eliminar un visitante con cortesías o pasaportes registrados. Cámbialo a inactivo.");
        }
        datos.visitantes.remove(v);
    }

    private static String limpiar(String s) { return s == null ? "" : s.trim(); }

    // ------------------------------------------------------------ pantalla 1: otorgar cortesía

    /** Fila de la tabla "Últimas entregas registradas". */
    public record FilaEntrega(String codigoCortesia, String visitante, String empleado, LocalDate fecha, LocalTime hora) { }

    /** La transacción estrella. R6: la fecha de visita la asigna el sistema (hoy). */
    public Datos.CortesiaYRegistro otorgarCortesia(String visitanteId) {
        Empleado empleado = exigirSesion();
        String documento = visitanteId == null ? "" : visitanteId.trim();
        Visitante v = visitante(documento).orElse(null);
        LocalDate fechaVisita = hoy();
        Resultado r = Reglas.validarReglas(documento, v, fechaVisita, datos.cortesias);
        if (!r.ok()) throw OperacionException.desde(r);
        // R5 / RNF-05: se guardan la cortesía y su registro juntos, o ninguno.
        Datos.CortesiaYRegistro par = datos.nuevaCortesia(v.getIdentificacion(), fechaVisita, empleado);
        datos.guardarCortesia(par);
        return par;
    }

    public List<FilaEntrega> ultimasEntregas() {
        exigirSesion();
        List<FilaEntrega> salida = new ArrayList<>();
        List<RegistroEntrega> regs = datos.registros;
        for (int i = regs.size() - 1; i >= Math.max(0, regs.size() - 8); i--) {
            RegistroEntrega r = regs.get(i);
            Cortesia c = datos.cortesias.stream()
                    .filter(x -> x.getCodigoCortesia().equals(r.getCodigoCortesia())).findFirst().orElseThrow();
            String nombreV = visitante(c.getVisitanteId()).map(Visitante::getNombreVisitante).orElse(c.getVisitanteId());
            salida.add(new FilaEntrega(c.getCodigoCortesia(), nombreV, nombreEmpleado(r.getEmpleadoId()), r.getFecha(), r.getHora()));
        }
        return salida;
    }

    private String nombreEmpleado(String id) {
        return datos.empleados.stream().filter(e -> e.getIdentificacion().equals(id))
                .map(Empleado::getNombreEmpleado).findFirst().orElse(id);
    }

    // ------------------------------------------------------------ pantalla 2: devolución (solo Punto 2, R10)

    /** Fila de la tabla de pasaportes. */
    public record FilaPasaporte(Pasaporte pasaporte, String nombreVisitante, boolean cortesiaConfirmada) { }

    /** Fila de la tabla "Devoluciones registradas". */
    public record FilaDevolucion(Devolucion devolucion, String visitante, String empleado) { }

    public static final String MOTIVO_POR_DEFECTO = "Compró el pasaporte sin conocer la cortesía de cumpleaños";

    private Empleado exigirPunto2() {
        Empleado e = exigirSesion();
        if (!e.puedeRegistrarDevolucion()) {
            throw new OperacionException(403, "R10", "R10: la devolución del pasaporte solo se atiende en el Punto de atención 2.");
        }
        return e;
    }

    public List<FilaPasaporte> buscarPasaportes(String query) {
        exigirPunto2();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<FilaPasaporte> lista = new ArrayList<>();
        for (Pasaporte p : datos.pasaportes) {
            String nombre = visitante(p.getVisitanteId()).map(Visitante::getNombreVisitante).orElse(p.getVisitanteId());
            String texto = (p.getCodigoPasaporte() + " " + p.getVisitanteId() + " " + nombre).toLowerCase(Locale.ROOT);
            if (texto.contains(q)) {
                lista.add(new FilaPasaporte(p, nombre, Reglas.cortesiaConfirmadaHoy(p.getVisitanteId(), datos.cortesias, hoy())));
            }
        }
        return lista;
    }

    public List<FilaDevolucion> ultimasDevoluciones() {
        exigirPunto2();
        List<FilaDevolucion> salida = new ArrayList<>();
        List<Devolucion> devs = datos.devoluciones;
        for (int i = devs.size() - 1; i >= Math.max(0, devs.size() - 8); i--) {
            Devolucion d = devs.get(i);
            String nombreV = visitante(d.getVisitanteId()).map(Visitante::getNombreVisitante).orElse(d.getVisitanteId());
            salida.add(new FilaDevolucion(d, nombreV, nombreEmpleado(d.getEmpleadoId())));
        }
        return salida;
    }

    public Devolucion registrarDevolucion(String codigoPasaporte, String motivo) {
        Empleado empleado = exigirPunto2();
        String codigo = codigoPasaporte == null ? "" : codigoPasaporte.trim();
        Pasaporte p = datos.pasaportes.stream().filter(x -> x.getCodigoPasaporte().equals(codigo)).findFirst().orElse(null);
        Resultado r = Reglas.validarDevolucion(p, datos.cortesias, hoy());
        if (!r.ok()) throw OperacionException.desde(r);
        String m = motivo == null ? "" : motivo.trim();
        if (m.length() > 200) m = m.substring(0, 200);
        if (m.isEmpty()) m = MOTIVO_POR_DEFECTO;
        // R8 / RNF-05: se cancela el pasaporte y se registra la devolución juntos, o ninguno.
        Devolucion d = datos.nuevaDevolucion(p, m, empleado);
        p.cancelar();
        datos.devoluciones.add(d);
        return d;
    }
}
