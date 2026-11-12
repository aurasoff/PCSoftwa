package co.edu.uniquindio.parquedelcafe;

import co.edu.uniquindio.parquedelcafe.logica.Datos;
import co.edu.uniquindio.parquedelcafe.logica.ServicioAtencion;
import co.edu.uniquindio.parquedelcafe.modelo.Empleado;
import co.edu.uniquindio.parquedelcafe.vista.AtencionVisitanteVista;
import co.edu.uniquindio.parquedelcafe.vista.DevolucionVista;
import co.edu.uniquindio.parquedelcafe.vista.Navegacion;
import co.edu.uniquindio.parquedelcafe.vista.Pantalla;
import co.edu.uniquindio.parquedelcafe.vista.Ui;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Ventana principal del MVP Parque del Café (versión JavaFX).
 * Cabecera con las dos pantallas y el "Empleado en sesión"; abajo, la pantalla activa.
 */
public class App extends Application implements Navegacion {

    private final ServicioAtencion servicio = new ServicioAtencion(new Datos());

    private Stage ventana;
    private final ScrollPane scroll = new ScrollPane();
    private final VBox contenedor = new VBox();
    private Pantalla atencion;
    private Pantalla devolucion;
    private Pantalla actual;
    private Button navAtencion;
    private Button navDevolucion;

    @Override
    public void start(Stage stage) {
        this.ventana = stage;
        atencion = new AtencionVisitanteVista(servicio, this);
        devolucion = new DevolucionVista(servicio, this);

        // ---------- cabecera ----------
        navAtencion = navegacion("Atención al visitante", null);
        navAtencion.setOnAction(e -> mostrar(atencion));
        navDevolucion = navegacion("Devolución del pasaporte", "(Punto de atención 2)");
        navDevolucion.setOnAction(e -> mostrar(devolucion));
        HBox nav = new HBox(14, navAtencion, navDevolucion);
        nav.setAlignment(Pos.CENTER_LEFT);
        VBox izquierda = new VBox(4, Ui.h1("Parque del Café · Atención al visitante"), nav);

        // Usuarios demo (RNF-06): se eligen en el selector de la cabecera.
        ComboBox<Empleado> empleado = new ComboBox<>();
        empleado.getItems().setAll(servicio.getEmpleados());
        empleado.setValue(servicio.getEmpleados().get(0));
        servicio.iniciarSesion(empleado.getValue().getIdentificacion());
        empleado.valueProperty().addListener((obs, antes, ahora) -> {
            if (ahora == null) return;
            servicio.iniciarSesion(ahora.getIdentificacion());
            actual.alCambiarEmpleado();
        });
        Label etiquetaSesion = new Label("Empleado en sesión");
        etiquetaSesion.getStyleClass().add("etiqueta-sesion");
        HBox sesion = new HBox(6, etiquetaSesion, empleado);
        sesion.setAlignment(Pos.CENTER_LEFT);

        HBox cabecera = new HBox(16, izquierda, Ui.espaciador(), sesion);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.getStyleClass().add("encabezado");

        // ---------- contenido (main: máximo 1040 px, centrado) ----------
        contenedor.setAlignment(Pos.TOP_CENTER);
        contenedor.setPadding(new Insets(20, 16, 20, 16));
        scroll.setContent(contenedor);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-principal");

        BorderPane raiz = new BorderPane();
        raiz.setTop(cabecera);
        raiz.setCenter(scroll);

        Scene escena = new Scene(raiz, 1180, 820);
        escena.getStylesheets().add(App.class.getResource("estilos.css").toExternalForm());
        stage.setScene(escena);
        stage.setMinWidth(760);
        stage.setMinHeight(520);
        mostrar(atencion);
        stage.show();
    }

    private Button navegacion(String texto, String nota) {
        Label principal = new Label(texto);
        principal.getStyleClass().add("nav-texto");
        HBox contenido = new HBox(4, principal);
        contenido.setAlignment(Pos.BASELINE_LEFT);
        if (nota != null) {
            Label n = new Label(nota);
            n.getStyleClass().add("nav-nota");
            contenido.getChildren().add(n);
        }
        Button b = new Button();
        b.setGraphic(contenido);
        b.getStyleClass().setAll("button", "nav-link");
        return b;
    }

    private void mostrar(Pantalla p) {
        actual = p;
        navAtencion.getStyleClass().remove("activa");
        navDevolucion.getStyleClass().remove("activa");
        (p == atencion ? navAtencion : navDevolucion).getStyleClass().add("activa");
        Node vista = p.getVista();
        if (vista instanceof javafx.scene.layout.Region r) r.setMaxWidth(1040);
        contenedor.getChildren().setAll(vista);
        ventana.setTitle(p.getTitulo());
        p.alMostrar();
        scroll.setVvalue(0);
    }

    // ---------- Navegacion ----------

    @Override
    public void irAAtencionVisitante() { mostrar(atencion); }

    /** Equivale a scrollIntoView() del MVP web. */
    @Override
    public void desplazarA(Node nodo) {
        Platform.runLater(() -> {
            contenedor.applyCss();
            contenedor.layout();
            Bounds b = contenedor.sceneToLocal(nodo.localToScene(nodo.getBoundsInLocal()));
            double sobrante = contenedor.getBoundsInLocal().getHeight() - scroll.getViewportBounds().getHeight();
            if (sobrante > 0) scroll.setVvalue(Math.max(0, Math.min(1, (b.getMinY() - 20) / sobrante)));
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
