package co.edu.uniquindio.parquedelcafe.modelo;

import java.time.LocalDate;
import java.time.MonthDay;

public class Visitante {
    private String identificacion;
    private String nombreVisitante;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String lugarResidencia;
    private EstadoVisitante estado;

    public Visitante(String identificacion, String nombreVisitante, LocalDate fechaNacimiento,
                     String telefono, String lugarResidencia, EstadoVisitante estado) {
        this.identificacion = identificacion;
        this.nombreVisitante = nombreVisitante;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.lugarResidencia = lugarResidencia;
        this.estado = estado;
    }

    /** R1: el día y el mes de nacimiento coinciden con la fecha dada. */
    public boolean cumpleAniosEn(LocalDate fecha) {
        return MonthDay.from(fechaNacimiento).equals(MonthDay.from(fecha));
    }

    public boolean estaActivo() { return estado == EstadoVisitante.ACTIVO; }

    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public String getNombreVisitante() { return nombreVisitante; }
    public void setNombreVisitante(String nombreVisitante) { this.nombreVisitante = nombreVisitante; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getLugarResidencia() { return lugarResidencia; }
    public void setLugarResidencia(String lugarResidencia) { this.lugarResidencia = lugarResidencia; }
    public EstadoVisitante getEstado() { return estado; }
    public void setEstado(EstadoVisitante estado) { this.estado = estado; }
}
