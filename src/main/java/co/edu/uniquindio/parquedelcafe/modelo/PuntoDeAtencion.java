package co.edu.uniquindio.parquedelcafe.modelo;

public class PuntoDeAtencion {
    private final String nombre;
    private final String ubicacion;

    public PuntoDeAtencion(String nombre, String ubicacion) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
    }

    public String getNombre() { return nombre; }
    public String getUbicacion() { return ubicacion; }

    @Override
    public String toString() { return nombre; }
}
