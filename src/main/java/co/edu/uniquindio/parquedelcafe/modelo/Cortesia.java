package co.edu.uniquindio.parquedelcafe.modelo;

import java.time.LocalDate;

public class Cortesia {
    private final String codigoCortesia;
    private final String visitanteId;
    private final LocalDate fechaVisita;
    private final LocalDate fechaEmision;
    private final String estado;
    private final String puntoDeAtencion;

    public Cortesia(String codigoCortesia, String visitanteId, LocalDate fechaVisita, LocalDate fechaEmision,
                    String estado, String puntoDeAtencion) {
        this.codigoCortesia = codigoCortesia;
        this.visitanteId = visitanteId;
        this.fechaVisita = fechaVisita;
        this.fechaEmision = fechaEmision;
        this.estado = estado;
        this.puntoDeAtencion = puntoDeAtencion;
    }

    /** R2: la cortesía es del año indicado. */
    public boolean esDelAnio(int anio) { return fechaVisita.getYear() == anio; }

    public String getCodigoCortesia() { return codigoCortesia; }
    public String getVisitanteId() { return visitanteId; }
    public LocalDate getFechaVisita() { return fechaVisita; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public String getEstado() { return estado; }
    public String getPuntoDeAtencion() { return puntoDeAtencion; }
}
