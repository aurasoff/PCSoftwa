package co.edu.uniquindio.parquedelcafe.vista;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

/** Piezas de interfaz reutilizadas por las dos pantallas (equivalen a las clases CSS del MVP web). */
public final class Ui {

    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final NumberFormat MILES = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"));

    private Ui() { }

    // ---------- formatos ----------
    public static String fecha(LocalDate f) { return f == null ? "" : FECHA.format(f); }
    public static String hora(LocalTime h) { return h == null ? "" : HORA.format(h); }
    public static String pesos(double n) { return "$ " + MILES.format(Math.round(n)); }

    // ---------- visibilidad (equivale al atributo hidden) ----------
    public static void mostrar(Node n, boolean visible) {
        n.setVisible(visible);
        n.setManaged(visible);
    }

    // ---------- textos ----------
    public static Label h1(String t) { return conClase(new Label(t), "h1"); }
    public static Label h2(String t) { return conClase(new Label(t), "h2"); }
    public static Label etiqueta(String t) { return conClase(new Label(t), "etiqueta"); }

    public static Label errorCampo() {
        Label l = conClase(new Label(), "error-campo");
        l.setWrapText(true);
        mostrar(l, false);
        return l;
    }

    /** Marca (o limpia, con texto vacío) el error de un campo. */
    public static void marcar(Node control, Label error, String texto) {
        boolean hay = texto != null && !texto.isEmpty();
        error.setText(hay ? texto : "");
        mostrar(error, hay);
        control.getStyleClass().remove("invalido");
        if (hay) control.getStyleClass().add("invalido");
    }

    private static <T extends Node> T conClase(T n, String clase) {
        n.getStyleClass().add(clase);
        return n;
    }

    // ---------- contenedores ----------
    public static VBox tarjeta(Node... hijos) {
        VBox v = new VBox(hijos);
        v.getStyleClass().add("tarjeta");
        return v;
    }

    public static VBox campo(String etiqueta, Node control, Label error) {
        VBox v = new VBox(4, etiqueta(etiqueta), control);
        if (error != null) v.getChildren().add(error);
        v.getStyleClass().add("campo");
        return v;
    }

    public static HBox fila(Node... hijos) {
        HBox h = new HBox(8, hijos);
        h.setAlignment(Pos.BOTTOM_LEFT);
        return h;
    }

    public static Region espaciador() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    // ---------- campos ----------
    public static TextField texto(String placeholder, double ancho) {
        TextField t = new TextField();
        t.setPromptText(placeholder);
        if (ancho > 0) {
            t.setPrefWidth(ancho);
            t.setMaxWidth(ancho);
        }
        return t;
    }

    public static TextField soloLectura(double ancho) {
        TextField t = texto("", ancho);
        t.setEditable(false);
        t.setFocusTraversable(false);
        t.getStyleClass().add("solo-lectura");
        return t;
    }

    // ---------- botones ----------
    public static Button boton(String texto, boolean principal) {
        Button b = new Button(texto);
        if (principal) b.getStyleClass().add("principal");
        return b;
    }

    public static Button botonTabla(String texto, boolean principal, Runnable accion) {
        Button b = boton(texto, principal);
        b.getStyleClass().add("btn-tabla");
        b.setOnAction(e -> accion.run());
        return b;
    }

    // ---------- estado vacío (.vacio) ----------
    public static VBox vacio(String titulo, String detalle, Node extra) {
        Label t = conClase(new Label(titulo), "vacio-titulo");
        Label d = conClase(new Label(detalle), "vacio-detalle");
        d.setWrapText(true);
        VBox v = new VBox(4, t, d);
        if (extra != null) {
            VBox.setMargin(extra, new javafx.geometry.Insets(10, 0, 0, 0));
            v.getChildren().add(extra);
        }
        v.setAlignment(Pos.CENTER);
        v.getStyleClass().add("vacio");
        return v;
    }

    // ---------- resumen (dl.resumen) ----------
    public record Dato(String etiqueta, String valor, boolean dinero) {
        public Dato(String etiqueta, String valor) { this(etiqueta, valor, false); }
    }

    public static TilePane resumen(Dato... datos) {
        TilePane t = new TilePane(12, 12);
        t.setPrefTileWidth(180);
        t.setTileAlignment(Pos.TOP_LEFT);
        t.getStyleClass().add("resumen");
        for (Dato d : datos) {
            Label dt = conClase(new Label(d.etiqueta()), "dt");
            Label dd = conClase(new Label(d.valor()), "dd");
            if (d.dinero()) dd.getStyleClass().add("dinero");
            dd.setWrapText(true);
            t.getChildren().add(new VBox(2, dt, dd));
        }
        return t;
    }

    // ---------- tablas (table / th / td) ----------
    /** Celda de texto. Si {@code sinSalto} es true no parte la línea (clase .nw del MVP web). */
    public static Node celda(String texto, boolean sinSalto) {
        Label l = new Label(texto);
        l.setWrapText(!sinSalto);
        if (sinSalto) l.setMinWidth(Region.USE_PREF_SIZE);
        return l;
    }

    public static Node celda(String texto) { return celda(texto, false); }

    public static Node acciones(Node... botones) {
        HBox h = new HBox(4, botones);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setMinWidth(Region.USE_PREF_SIZE);
        return h;
    }

    /**
     * Tabla simple con encabezados y filas.
     * @param crece columnas que se reparten el espacio libre (las demás toman su tamaño natural)
     */
    public static GridPane tabla(String[] encabezados, List<Node[]> filas, boolean... crece) {
        GridPane g = new GridPane();
        g.getStyleClass().add("tabla");
        g.setMaxWidth(Double.MAX_VALUE);
        for (int c = 0; c < encabezados.length; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            boolean grow = c < crece.length && crece[c];
            cc.setHgrow(grow ? Priority.ALWAYS : Priority.NEVER);
            cc.setFillWidth(true);
            g.getColumnConstraints().add(cc);
            g.add(envolver(new Label(encabezados[c]), "celda-th"), c, 0);
        }
        for (int r = 0; r < filas.size(); r++) {
            Node[] fila = filas.get(r);
            for (int c = 0; c < fila.length; c++) {
                g.add(envolver(fila[c], "celda"), c, r + 1);
            }
        }
        return g;
    }

    private static Node envolver(Node contenido, String clase) {
        HBox caja = new HBox(contenido);
        caja.setAlignment(Pos.CENTER_LEFT);
        caja.setMaxWidth(Double.MAX_VALUE);
        caja.setMaxHeight(Double.MAX_VALUE);
        caja.getStyleClass().add(clase);
        if (contenido instanceof Label l && l.isWrapText()) HBox.setHgrow(l, Priority.ALWAYS);
        return caja;
    }
}
