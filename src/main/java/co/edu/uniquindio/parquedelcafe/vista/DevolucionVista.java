package co.edu.uniquindio.parquedelcafe.vista;

import co.edu.uniquindio.parquedelcafe.logica.OperacionException;
import co.edu.uniquindio.parquedelcafe.logica.ServicioAtencion;
import co.edu.uniquindio.parquedelcafe.logica.ServicioAtencion.FilaDevolucion;
import co.edu.uniquindio.parquedelcafe.logica.ServicioAtencion.FilaPasaporte;
import co.edu.uniquindio.parquedelcafe.modelo.Devolucion;
import co.edu.uniquindio.parquedelcafe.modelo.Empleado;
import co.edu.uniquindio.parquedelcafe.modelo.EstadoPasaporte;
import co.edu.uniquindio.parquedelcafe.modelo.Pasaporte;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla 2 – Devolución del pasaporte.
 * Solo la usa el Punto de atención 2 (R10) y solo procede si la cortesía del visitante ya está confirmada (R9).
 * Registra número, fecha, hora, código del pasaporte, tipo, monto, motivo y empleado (R8).
 */
public class DevolucionVista implements Pantalla {

    private final ServicioAtencion servicio;
    private final Navegacion nav;
    private final VBox raiz = new VBox(18);

    private final Aviso bloqueo = new Aviso();
    private final VBox contenido = new VBox(18);

    // Buscar pasaporte
    private final TextField q = Ui.texto("PAS-1001 o 1094771002", 0);
    private final Label qError = Ui.errorCampo();
    private final VBox lista = new VBox();

    // Registrar devolución
    private final VBox panel;
    private final VBox detalle = new VBox();
    private final TextField motivo = Ui.texto("", 0);
    private final TextField fechaDev = Ui.soloLectura(160);
    private final Aviso mensaje = new Aviso();

    // Devoluciones registradas
    private final VBox registradas = new VBox();

    private List<FilaPasaporte> datos = new ArrayList<>();
    private FilaPasaporte elegido;

    public DevolucionVista(ServicioAtencion servicio, Navegacion nav) {
        this.servicio = servicio;
        this.nav = nav;

        // ---------- tarjeta: Buscar pasaporte ----------
        q.setMinWidth(220);
        q.setPrefWidth(420);
        q.setMaxWidth(420);
        HBox.setHgrow(q, Priority.ALWAYS);
        q.setOnAction(e -> buscar());            // Enter
        Button btnBuscar = Ui.boton("Buscar", false);
        btnBuscar.setOnAction(e -> buscar());
        VBox campoQ = Ui.campo("Código del pasaporte o documento del visitante", Ui.fila(q, btnBuscar), qError);
        VBox buscar = Ui.tarjeta(Ui.h2("Buscar pasaporte"), campoQ, lista);

        // ---------- tarjeta: Registrar devolución ----------
        motivo.setText(ServicioAtencion.MOTIVO_POR_DEFECTO);
        motivo.setMaxWidth(Double.MAX_VALUE);
        motivo.textProperty().addListener((o, a, b) -> {
            if (b.length() > 200) motivo.setText(b.substring(0, 200));   // maxlength="200"
        });
        Button confirmar = Ui.boton("Confirmar devolución", true);
        confirmar.setOnAction(e -> confirmar());
        Button descartar = Ui.boton("Cancelar", false);
        descartar.setOnAction(e -> descartar());
        VBox.setMargin(mensaje, new Insets(0, 0, 14, 0));
        panel = Ui.tarjeta(Ui.h2("Registrar devolución"), detalle,
                Ui.campo("Motivo", motivo, null),
                Ui.campo("Fecha de la devolución", fechaDev, null),
                mensaje, Ui.fila(confirmar, descartar));
        Ui.mostrar(panel, false);

        // ---------- tarjeta: Devoluciones registradas ----------
        VBox tarjetaRegistradas = Ui.tarjeta(Ui.h2("Devoluciones registradas"), registradas);

        contenido.getChildren().addAll(buscar, panel, tarjetaRegistradas);
        raiz.getChildren().addAll(bloqueo, contenido);
    }

    @Override public Node getVista() { return raiz; }

    @Override public String getTitulo() { return "Devolución del pasaporte · Parque del Café"; }

    @Override
    public void alMostrar() {
        fechaDev.setText(Ui.fecha(servicio.hoy()));
        iniciar();
    }

    @Override
    public void alCambiarEmpleado() { iniciar(); }

    private void iniciar() {
        descartar();
        if (!permitido()) return;
        buscar();
        cargarRegistradas();
    }

    /** R10: solo el Punto de atención 2 atiende devoluciones (el servicio también lo valida). */
    private boolean permitido() {
        Empleado e = servicio.getEmpleadoActual();
        boolean ok = e != null && e.puedeRegistrarDevolucion();
        Ui.mostrar(contenido, ok);
        if (ok) {
            bloqueo.ocultar();
        } else {
            String quien = e == null ? "" : e.getNombreEmpleado() + " (" + e.getPunto().getNombre() + ")";
            bloqueo.err("R10: la devolución del pasaporte solo se atiende en el ", new Aviso.Negrita("Punto de atención 2"),
                    ". Estás en sesión como " + quien + ". Cambia de empleado arriba o atiende al visitante en el otro punto; la pantalla ",
                    new Aviso.Enlace("Atención al visitante", nav::irAAtencionVisitante), " sí está disponible aquí.");
        }
        return ok;
    }

    // ---------------------------------------------------------------- buscar pasaporte

    private void buscar() {
        Ui.marcar(q, qError, "");
        try {
            datos = servicio.buscarPasaportes(q.getText());
            pintarLista();
        } catch (OperacionException e) {
            lista.getChildren().clear();
            Ui.marcar(q, qError, e.getMessage());
            q.getStyleClass().remove("invalido");
        }
    }

    private void pintarLista() {
        if (datos.isEmpty()) {
            lista.getChildren().setAll(Ui.vacio("No encontramos pasaportes con ese dato",
                    "Revisa el código o el documento del visitante.", null));
            return;
        }
        List<Node[]> filas = new ArrayList<>();
        for (FilaPasaporte pv : datos) {
            Pasaporte p = pv.pasaporte();
            filas.add(new Node[]{
                    Ui.celda(p.getCodigoPasaporte(), true),
                    Ui.celda(p.getTipo().getTipo()),
                    Ui.celda(Ui.pesos(p.getValor()), true),
                    Ui.celda(pv.nombreVisitante()),
                    Ui.celda(p.getEstado().toString()),
                    Ui.celda(pv.cortesiaConfirmada() ? "Sí" : "No"),
                    Ui.acciones(Ui.botonTabla("Devolver", true, () -> seleccionar(pv)))
            });
        }
        lista.getChildren().setAll(Ui.tabla(
                new String[]{"Código", "Tipo", "Valor pagado", "Visitante", "Estado", "Cortesía confirmada hoy", ""},
                filas, false, true, false, true, true, true, false));
    }

    // ---------------------------------------------------------------- registrar devolución

    /** Aviso rápido en la pantalla (RNF-01); la validación que vale es la del servicio. */
    private void avisoPrevio(FilaPasaporte pv) {
        Pasaporte p = pv.pasaporte();
        if (p.getEstado() == EstadoPasaporte.CANCELADO) mensaje.err("R7: este pasaporte ya fue cancelado y devuelto.");
        else if (p.getEstado() == EstadoPasaporte.SIN_PAGO) mensaje.err("R7: el pasaporte no aparece como pagado.");
        else if (p.getEstado() == EstadoPasaporte.USADO) mensaje.err("R7: el pasaporte ya fue usado (registra ingresos).");
        else if (!p.isAutentico()) mensaje.err("R7: el pasaporte no es auténtico.");
        else if (!pv.cortesiaConfirmada()) mensaje.err("R9: primero confirma la entrega de la cortesía de este visitante en ",
                new Aviso.Enlace("Atención al visitante", nav::irAAtencionVisitante), ".");
        else mensaje.ok("El pasaporte procede para devolución. Puedes confirmarla.");
    }

    private void seleccionar(FilaPasaporte pv) {
        elegido = pv;
        Pasaporte p = pv.pasaporte();
        detalle.getChildren().setAll(Ui.resumen(
                new Ui.Dato("Código del pasaporte", p.getCodigoPasaporte()),
                new Ui.Dato("Tipo de pasaporte", p.getTipo().getTipo()),
                new Ui.Dato("Monto a devolver", Ui.pesos(p.getValor()), true),
                new Ui.Dato("Visitante", pv.nombreVisitante() + " (" + p.getVisitanteId() + ")"),
                new Ui.Dato("Estado del pasaporte", p.getEstado().toString()),
                new Ui.Dato("Cortesía confirmada hoy", pv.cortesiaConfirmada() ? "Sí" : "No")));
        VBox.setMargin(detalle, new Insets(0, 0, 14, 0));
        avisoPrevio(pv);
        Ui.mostrar(panel, true);
        nav.desplazarA(panel);
    }

    private void descartar() {
        elegido = null;
        Ui.mostrar(panel, false);
    }

    private void confirmar() {
        if (elegido == null) {
            mensaje.err("Elige un pasaporte antes de confirmar.");
            return;
        }
        try {
            Devolucion d = servicio.registrarDevolucion(elegido.pasaporte().getCodigoPasaporte(), motivo.getText());
            mensaje.ok("Devolución registrada", new Aviso.Grande(d.getNumeroDevolucion()),
                    "Pasaporte " + d.getCodigoPasaporte() + " (" + d.getTipoPasaporte() + ") cancelado. Monto a reintegrar: ",
                    new Aviso.Negrita(Ui.pesos(d.getMonto())), ".");
            detalle.getChildren().clear();
            elegido = null;
            buscar();
            cargarRegistradas();
        } catch (OperacionException e) {
            mensaje.err(e.getMessage());
        }
    }

    private void cargarRegistradas() {
        try {
            List<FilaDevolucion> l = servicio.ultimasDevoluciones();
            if (l.isEmpty()) {
                registradas.getChildren().setAll(Ui.vacio("Todavía no hay devoluciones",
                        "Las devoluciones que registres aparecen aquí.", null));
                return;
            }
            List<Node[]> filas = new ArrayList<>();
            for (FilaDevolucion x : l) {
                Devolucion d = x.devolucion();
                filas.add(new Node[]{
                        Ui.celda(d.getNumeroDevolucion(), true),
                        Ui.celda(Ui.fecha(d.getFecha()), true),
                        Ui.celda(Ui.hora(d.getHora()), true),
                        Ui.celda(d.getCodigoPasaporte(), true),
                        Ui.celda(d.getTipoPasaporte()),
                        Ui.celda(Ui.pesos(d.getMonto()), true),
                        Ui.celda(x.visitante()),
                        Ui.celda(x.empleado()),
                        Ui.celda(d.getMotivo())
                });
            }
            registradas.getChildren().setAll(Ui.tabla(
                    new String[]{"Número", "Fecha", "Hora", "Pasaporte", "Tipo", "Monto", "Visitante", "Empleado", "Motivo"},
                    filas, false, false, false, false, true, false, true, true, true));
        } catch (OperacionException e) {
            registradas.getChildren().clear();
        }
    }
}
