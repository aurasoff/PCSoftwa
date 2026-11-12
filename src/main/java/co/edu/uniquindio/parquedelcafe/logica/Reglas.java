package co.edu.uniquindio.parquedelcafe.logica;

import co.edu.uniquindio.parquedelcafe.modelo.Cortesia;
import co.edu.uniquindio.parquedelcafe.modelo.EstadoPasaporte;
import co.edu.uniquindio.parquedelcafe.modelo.Pasaporte;
import co.edu.uniquindio.parquedelcafe.modelo.Visitante;

import java.time.LocalDate;
import java.util.List;

/**
 * Reglas de negocio. Se validan en el servicio (fuente de verdad);
 * las pantallas solo dan un aviso previo (RNF-01).
 */
public final class Reglas {

    private Reglas() { }

    /** Otorgar cortesía de cumpleaños: R4, R3, R1 y R2 (en ese orden, igual que el MVP web). */
    public static Resultado validarReglas(String documento, Visitante visitante, LocalDate fechaVisita, List<Cortesia> cortesias) {
        // R4: documento de identidad válido (el empleado revisa que esté legible; el sistema exige el número).
        String doc = documento == null ? "" : documento.trim();
        if (!doc.matches("\\d{5,15}")) {
            return Resultado.bloqueado("R4", 422, "R4: ingresa un documento de identidad válido (solo números).");
        }
        // R3: visitante registrado y activo.
        if (visitante == null) {
            return Resultado.bloqueado("R3", 422, "R3: no encontramos un visitante con ese documento. Regístralo para continuar.");
        }
        if (!visitante.estaActivo()) {
            return Resultado.bloqueado("R3", 422, "R3: el visitante está inactivo y no puede recibir la cortesía.");
        }
        // R1: día y mes de nacimiento = día y mes de la visita.
        if (!visitante.cumpleAniosEn(fechaVisita)) {
            return Resultado.bloqueado("R1", 422, "R1: la fecha de cumpleaños no coincide con la fecha de la visita.");
        }
        // R2: máximo una cortesía por año calendario.
        boolean yaTiene = cortesias.stream()
                .anyMatch(c -> c.getVisitanteId().equals(visitante.getIdentificacion()) && c.esDelAnio(fechaVisita.getYear()));
        if (yaTiene) {
            return Resultado.bloqueado("R2", 422, "R2: este visitante ya recibió su cortesía este año.");
        }
        return Resultado.aprobado();
    }

    /** Devolución del pasaporte: R7 (pagado, auténtico, no usado) y R9 (cortesía ya confirmada hoy). */
    public static Resultado validarDevolucion(Pasaporte pasaporte, List<Cortesia> cortesias, LocalDate hoy) {
        if (pasaporte == null) {
            return Resultado.bloqueado("R7", 404, "R7: no encontramos un pasaporte con ese código.");
        }
        if (pasaporte.getEstado() == EstadoPasaporte.CANCELADO) {
            return Resultado.bloqueado("R7", 422, "R7: este pasaporte ya fue cancelado y devuelto.");
        }
        if (pasaporte.getEstado() == EstadoPasaporte.SIN_PAGO) {
            return Resultado.bloqueado("R7", 422, "R7: el pasaporte no aparece como pagado.");
        }
        if (pasaporte.getEstado() == EstadoPasaporte.USADO) {
            return Resultado.bloqueado("R7", 422, "R7: el pasaporte ya fue usado (registra ingresos).");
        }
        if (!pasaporte.isAutentico()) {
            return Resultado.bloqueado("R7", 422, "R7: el pasaporte no es auténtico.");
        }
        if (!cortesiaConfirmadaHoy(pasaporte.getVisitanteId(), cortesias, hoy)) {
            return Resultado.bloqueado("R9", 422,
                    "R9: primero confirma la entrega de la cortesía de este visitante en la pantalla Atención al visitante.");
        }
        return Resultado.aprobado();
    }

    public static boolean cortesiaConfirmadaHoy(String visitanteId, List<Cortesia> cortesias, LocalDate hoy) {
        return cortesias.stream().anyMatch(c -> c.getVisitanteId().equals(visitanteId) && c.getFechaVisita().equals(hoy));
    }
}
