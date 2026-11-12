package co.edu.uniquindio.parquedelcafe.vista;

import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

/**
 * Recuadro de mensaje (verde = ok, rojo = err), igual al ".aviso" del MVP web.
 * Las partes pueden ser texto, {@link Negrita}, {@link Grande} (código en grande) o {@link Enlace}.
 */
public class Aviso extends VBox {

    public record Negrita(String texto) { }
    public record Grande(String texto) { }
    public record Enlace(String texto, Runnable accion) { }

    private final TextFlow flujo = new TextFlow();

    public Aviso() {
        getStyleClass().add("aviso");
        getChildren().add(flujo);
        Ui.mostrar(this, false);
    }

    public void ok(Object... partes) { mostrar("ok", partes); }

    public void err(Object... partes) { mostrar("err", partes); }

    public void mostrar(String tipo, Object... partes) {
        getStyleClass().removeAll("ok", "err");
        if (tipo != null && !tipo.isEmpty()) getStyleClass().add(tipo);
        List<Node> nodos = new ArrayList<>();
        for (Object p : partes) {
            if (p instanceof Negrita n) {
                nodos.add(texto(n.texto(), "negrita"));
            } else if (p instanceof Grande g) {
                nodos.add(texto("\n" + g.texto() + "\n", "dinero"));
            } else if (p instanceof Enlace e) {
                Hyperlink h = new Hyperlink(e.texto());
                h.setOnAction(ev -> e.accion().run());
                nodos.add(h);
            } else {
                nodos.add(texto(String.valueOf(p), null));
            }
        }
        flujo.getChildren().setAll(nodos);
        Ui.mostrar(this, true);
    }

    public void ocultar() {
        flujo.getChildren().clear();
        Ui.mostrar(this, false);
    }

    private static Text texto(String s, String clase) {
        Text t = new Text(s);
        t.getStyleClass().add("aviso-texto");
        if (clase != null) t.getStyleClass().add(clase);
        return t;
    }
}
