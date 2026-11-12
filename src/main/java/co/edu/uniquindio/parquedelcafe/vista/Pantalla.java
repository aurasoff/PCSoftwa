package co.edu.uniquindio.parquedelcafe.vista;

import javafx.scene.Node;

/** Cada una de las dos pantallas del MVP. */
public interface Pantalla {
    Node getVista();
    String getTitulo();
    /** Se llama cada vez que el empleado abre la pantalla. */
    void alMostrar();
    /** Se llama cuando cambia el "Empleado en sesión". */
    void alCambiarEmpleado();
}
