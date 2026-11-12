package co.edu.uniquindio.parquedelcafe.vista;

import javafx.scene.Node;

/** Lo que las pantallas le piden a la ventana principal. */
public interface Navegacion {
    void irAAtencionVisitante();
    void desplazarA(Node nodo);
}
