package co.edu.uniquindio.parquedelcafe.modelo;

/** Tipos de pasaporte con precios de ejemplo para la demo. */
public enum TipoDePasaporte {
    JUNIOR("Junior", 60000),
    SENIOR("Senior", 60000),
    MULTIPLE("Múltiple", 90000);

    private final String tipo;
    private final double valor;

    TipoDePasaporte(String tipo, double valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    public String getTipo() { return tipo; }
    public double getValor() { return valor; }

    @Override
    public String toString() { return tipo; }
}
