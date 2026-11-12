package co.edu.uniquindio.parquedelcafe.modelo;

public class Pasaporte {
    private final String codigoPasaporte;
    private final TipoDePasaporte tipo;
    private final double valor;          // lo que pagó el visitante
    private EstadoPasaporte estado;
    private final boolean autentico;
    private final String visitanteId;

    public Pasaporte(String codigoPasaporte, TipoDePasaporte tipo, EstadoPasaporte estado, String visitanteId, boolean autentico) {
        this.codigoPasaporte = codigoPasaporte;
        this.tipo = tipo;
        this.valor = tipo.getValor();
        this.estado = estado;
        this.visitanteId = visitanteId;
        this.autentico = autentico;
    }

    /** R7: pagado, auténtico y sin usar. */
    public boolean procedeDevolucion() { return estado == EstadoPasaporte.PAGADO && autentico; }

    public void cancelar() { this.estado = EstadoPasaporte.CANCELADO; }

    public String getCodigoPasaporte() { return codigoPasaporte; }
    public TipoDePasaporte getTipo() { return tipo; }
    public double getValor() { return valor; }
    public EstadoPasaporte getEstado() { return estado; }
    public boolean isAutentico() { return autentico; }
    public String getVisitanteId() { return visitanteId; }
}
