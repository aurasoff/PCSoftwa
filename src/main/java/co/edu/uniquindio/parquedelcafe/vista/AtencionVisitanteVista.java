package co.edu.uniquindio.parquedelcafe.vista;

import co.edu.uniquindio.parquedelcafe.logica.Datos;
import co.edu.uniquindio.parquedelcafe.logica.OperacionException;
import co.edu.uniquindio.parquedelcafe.logica.ServicioAtencion;
import co.edu.uniquindio.parquedelcafe.logica.ServicioAtencion.FilaEntrega;
import co.edu.uniquindio.parquedelcafe.logica.ServicioAtencion.FormularioVisitante;
import co.edu.uniquindio.parquedelcafe.logica.ServicioAtencion.VisitanteConConteo;
import co.edu.uniquindio.parquedelcafe.modelo.EstadoVisitante;
import co.edu.uniquindio.parquedelcafe.modelo.Visitante;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pantalla 1 – Atención al visitante (Punto de atención 1 y 2):
 * buscar, registrar, editar y eliminar visitantes, y confirmar la entrega de la cortesía de cumpleaños.
 */
public class AtencionVisitanteVista implements Pantalla {

    private final ServicioAtencion servicio;
    private final Navegacion nav;
    private final VBox raiz = new VBox(18);

    // Buscar visitante
    private final TextField q = Ui.texto("1094880123", 420);
    private final Aviso avisoLista = new Aviso();
    private final VBox lista = new VBox();

    // Registrar / editar visitante
    private final Label t2 = Ui.h2("Registrar visitante");
    private final VBox panelForm;
    private final TextField fIdentificacion = Ui.texto("1094880123", 0);
    private final TextField fNombre = Ui.texto("Laura Gómez", 0);
    private final DatePicker fFechaNacimiento = new DatePicker();
    private final TextField fTelefono = Ui.texto("3101234567", 0);
    private final TextField fLugar = Ui.texto("Armenia", 0);
    private final ComboBox<EstadoVisitante> fEstado = new ComboBox<>();
    private final Map<String, Label> errores = new LinkedHashMap<>();
    private final Map<String, Node> controles = new LinkedHashMap<>();
    private String editando;

    // Otorgar cortesía
    private final VBox panelCortesia;
    private final VBox resumen = new VBox();
    private final TextField fechaVisita = Ui.soloLectura(160);
    private final Aviso mensaje = new Aviso();

    // Últimas entregas
    private final VBox ultimas = new VBox();

    private List<VisitanteConConteo> datos = new ArrayList<>();
    private VisitanteConConteo elegido;

    public AtencionVisitanteVista(ServicioAtencion servicio, Navegacion nav) {
        this.servicio = servicio;
        this.nav = nav;

        // ---------- tarjeta: Buscar visitante ----------
        Label t1 = Ui.h2("Buscar visitante");
        t1.setStyle("-fx-padding: 0;");
        Button nuevo = Ui.boton("Registrar visitante", true);
        nuevo.setOnAction(e -> abrirForm(null, soloDigitos(q.getText())));
        HBox cabecera = new HBox(8, t1, Ui.espaciador(), nuevo);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(cabecera, new javafx.geometry.Insets(0, 0, 12, 0));
        q.textProperty().addListener((obs, viejo, nuevoTexto) -> { avisoLista.ocultar(); cargar(); });
        VBox buscar = Ui.tarjeta(cabecera, Ui.campo("Documento o nombre del visitante", q, null), avisoLista, lista);

        // ---------- tarjeta: Registrar / editar visitante ----------
        fFechaNacimiento.setPromptText("dd/mm/aaaa");
        fFechaNacimiento.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate f) { return Ui.fecha(f); }
            @Override public LocalDate fromString(String s) { return leerFecha(s); }
        });
        fFechaNacimiento.setMaxWidth(Double.MAX_VALUE);
        fEstado.getItems().setAll(EstadoVisitante.values());
        registrarCampo("identificacion", fIdentificacion);
        registrarCampo("nombreVisitante", fNombre);
        registrarCampo("fechaNacimiento", fFechaNacimiento);
        registrarCampo("telefono", fTelefono);
        registrarCampo("lugarResidencia", fLugar);
        Button guardar = Ui.boton("Guardar", true);
        guardar.setOnAction(e -> guardar());
        Button cancelar = Ui.boton("Cancelar", false);
        cancelar.setOnAction(e -> Ui.mostrar(panelForm(), false));
        panelForm = Ui.tarjeta(t2,
                Ui.campo("Documento", fIdentificacion, errores.get("identificacion")),
                Ui.campo("Nombre completo", fNombre, errores.get("nombreVisitante")),
                Ui.campo("Fecha de nacimiento", fFechaNacimiento, errores.get("fechaNacimiento")),
                Ui.campo("Teléfono (opcional)", fTelefono, errores.get("telefono")),
                Ui.campo("Lugar de residencia (opcional)", fLugar, errores.get("lugarResidencia")),
                Ui.campo("Estado", fEstado, null),
                Ui.fila(guardar, cancelar));
        Ui.mostrar(panelForm, false);

        // ---------- tarjeta: Otorgar cortesía de cumpleaños ----------
        Button confirmar = Ui.boton("Confirmar entrega", true);
        confirmar.setOnAction(e -> confirmar());
        Button descartar = Ui.boton("Cancelar", false);
        descartar.setOnAction(e -> descartar());
        panelCortesia = Ui.tarjeta(Ui.h2("Otorgar cortesía de cumpleaños"), resumen,
                Ui.campo("Fecha de la visita", fechaVisita, null), mensaje, Ui.fila(confirmar, descartar));
        VBox.setMargin(mensaje, new javafx.geometry.Insets(0, 0, 14, 0));
        Ui.mostrar(panelCortesia, false);

        // ---------- tarjeta: Últimas entregas registradas ----------
        VBox tarjetaUltimas = Ui.tarjeta(Ui.h2("Últimas entregas registradas"), ultimas);

        raiz.getChildren().addAll(buscar, panelForm, panelCortesia, tarjetaUltimas);
    }

    private VBox panelForm() { return panelForm; }

    private void registrarCampo(String clave, Node control) {
        Label error = Ui.errorCampo();
        errores.put(clave, error);
        controles.put(clave, control);
        if (control instanceof TextField t) {
            t.textProperty().addListener((o, a, b) -> marcar(clave, ""));
        } else if (control instanceof DatePicker d) {
            d.getEditor().textProperty().addListener((o, a, b) -> marcar(clave, ""));
        }
    }

    @Override public Node getVista() { return raiz; }

    @Override public String getTitulo() { return "Atención al visitante · Parque del Café"; }

    @Override
    public void alMostrar() {
        fechaVisita.setText(Ui.fecha(servicio.hoy()));
        cargar();
        cargarUltimas();
    }

    @Override
    public void alCambiarEmpleado() {
        descartar();
        cargarUltimas();
    }

    // ---------------------------------------------------------------- lista y búsqueda

    private void cargar() {
        try {
            datos = servicio.buscarVisitantes(q.getText());
            pintarLista();
        } catch (OperacionException e) {
            avisoLista.err(e.getMessage());
        }
    }

    private void pintarLista() {
        String texto = q.getText().trim();
        if (datos.isEmpty()) {
            Button reg = Ui.boton("Registrar visitante", true);
            reg.setOnAction(e -> abrirForm(null, soloDigitos(texto)));
            lista.getChildren().setAll(texto.isEmpty()
                    ? Ui.vacio("Registra tu primer visitante", "Aquí aparecen los visitantes que ya pueden recibir una cortesía.", reg)
                    : Ui.vacio("No encontramos un visitante con ese dato", "Si es la primera vez que visita el parque, regístralo para continuar.", reg));
            return;
        }
        List<Node[]> filas = new ArrayList<>();
        for (VisitanteConConteo vc : datos) {
            Visitante v = vc.visitante();
            filas.add(new Node[]{
                    Ui.celda(v.getIdentificacion(), true),
                    Ui.celda(v.getNombreVisitante()),
                    Ui.celda(Ui.fecha(v.getFechaNacimiento()), true),
                    Ui.celda(v.getEstado().toString()),
                    Ui.celda(String.valueOf(vc.cortesiasAnio())),
                    Ui.acciones(
                            Ui.botonTabla("Otorgar cortesía", true, () -> seleccionar(vc)),
                            Ui.botonTabla("Editar", false, () -> abrirForm(v, null)),
                            Ui.botonTabla("Eliminar", false, () -> eliminar(v.getIdentificacion())))
            });
        }
        lista.getChildren().setAll(Ui.tabla(
                new String[]{"Documento", "Nombre", "Nacimiento", "Estado", "Cortesías este año", ""},
                filas, false, true, false, true, true, false));
    }

    // ---------------------------------------------------------------- registrar / editar visitante (CRUD)

    private void abrirForm(Visitante v, String doc) {
        editando = v == null ? null : v.getIdentificacion();
        t2.setText(v == null ? "Registrar visitante" : "Editar visitante");
        fIdentificacion.setText(v == null ? (doc == null ? "" : doc) : v.getIdentificacion());
        fIdentificacion.setEditable(editando == null);
        fIdentificacion.getStyleClass().remove("solo-lectura");
        if (editando != null) fIdentificacion.getStyleClass().add("solo-lectura");
        fNombre.setText(v == null ? "" : v.getNombreVisitante());
        fFechaNacimiento.setValue(v == null ? null : v.getFechaNacimiento());
        fFechaNacimiento.getEditor().setText(v == null ? "" : Ui.fecha(v.getFechaNacimiento()));
        fTelefono.setText(v == null ? "" : v.getTelefono());
        fLugar.setText(v == null ? "" : v.getLugarResidencia());
        fEstado.setValue(v == null ? EstadoVisitante.ACTIVO : v.getEstado());
        errores.keySet().forEach(k -> marcar(k, ""));
        Ui.mostrar(panelForm, true);
        nav.desplazarA(panelForm);
        boolean conDocumento = editando != null || !fIdentificacion.getText().isEmpty();
        (conDocumento ? fNombre : fIdentificacion).requestFocus();
    }

    private void marcar(String clave, String texto) {
        Ui.marcar(controles.get(clave), errores.get(clave), texto);
    }

    /** Aviso rápido (RNF-01); el servicio vuelve a validar. */
    private Map<String, String> validarCliente(FormularioVisitante o) {
        Map<String, String> e = new LinkedHashMap<>();
        if (o.identificacion().isEmpty()) e.put("identificacion", "Ingresa el documento del visitante.");
        else if (!o.identificacion().matches("\\d{5,15}")) e.put("identificacion", "El documento debe tener solo números (5 a 15 dígitos).");
        if (o.nombreVisitante().isEmpty()) e.put("nombreVisitante", "Ingresa el nombre completo.");
        if (o.fechaNacimiento() == null) e.put("fechaNacimiento", "Ingresa la fecha de nacimiento.");
        if (!o.telefono().isEmpty() && !o.telefono().matches("\\d{10}")) e.put("telefono", "Ingresa 10 dígitos.");
        return e;
    }

    private void guardar() {
        FormularioVisitante o = new FormularioVisitante(
                fIdentificacion.getText().trim(), fNombre.getText().trim(),
                leerFecha(fFechaNacimiento.getEditor().getText()),
                fTelefono.getText().trim(), fLugar.getText().trim(), fEstado.getValue());
        errores.keySet().forEach(k -> marcar(k, ""));
        Map<String, String> ec = validarCliente(o);
        if (o.fechaNacimiento() == null && !fFechaNacimiento.getEditor().getText().isBlank()) {
            ec.put("fechaNacimiento", "Usa una fecha válida (dd/mm/aaaa).");
        }
        if (!ec.isEmpty()) {
            ec.forEach(this::marcar);
            controles.get(ec.keySet().iterator().next()).requestFocus();
            return;
        }
        boolean eraEdicion = editando != null;
        try {
            VisitanteConConteo v = eraEdicion ? servicio.actualizarVisitante(editando, o) : servicio.crearVisitante(o);
            Ui.mostrar(panelForm, false);
            q.setText(o.identificacion());
            cargar();
            avisoLista.ok(eraEdicion ? "Visitante actualizado."
                    : "Visitante registrado. Ya puedes otorgarle su cortesía si cumple años hoy.");
            if (!eraEdicion) {
                seleccionar(datos.stream()
                        .filter(x -> x.visitante().getIdentificacion().equals(v.visitante().getIdentificacion()))
                        .findFirst().orElse(v));
            }
        } catch (OperacionException e) {
            if (!e.getErrores().isEmpty()) e.getErrores().forEach(this::marcar);
            else avisoLista.err(e.getMessage());
        }
    }

    private void eliminar(String id) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar al visitante " + id + "? Esta acción no se puede deshacer.", ButtonType.OK, ButtonType.CANCEL);
        alerta.setHeaderText(null);
        alerta.setTitle("Eliminar visitante");
        Optional<ButtonType> r = alerta.showAndWait();
        if (r.isEmpty() || r.get() != ButtonType.OK) return;
        try {
            servicio.eliminarVisitante(id);
            if (elegido != null && elegido.visitante().getIdentificacion().equals(id)) descartar();
            cargar();
            avisoLista.ok("Visitante eliminado.");
        } catch (OperacionException e) {
            avisoLista.err(e.getMessage());
        }
    }

    // ---------------------------------------------------------------- otorgar cortesía (transacción)

    /** Aviso rápido en la pantalla; la validación que vale es la del servicio. */
    private void avisoPrevio(VisitanteConConteo vc) {
        Visitante v = vc.visitante();
        if (!v.estaActivo()) mensaje.err("R3: el visitante está inactivo y no puede recibir la cortesía.");
        else if (!v.cumpleAniosEn(servicio.hoy())) mensaje.err("R1: la fecha de cumpleaños no coincide con la fecha de la visita.");
        else if (vc.cortesiasAnio() > 0) mensaje.err("R2: este visitante ya recibió su cortesía este año.");
        else mensaje.ok("Cumple las condiciones. Ya puedes confirmar la entrega.");
    }

    private void seleccionar(VisitanteConConteo vc) {
        elegido = vc;
        Visitante v = vc.visitante();
        resumen.getChildren().setAll(Ui.resumen(
                new Ui.Dato("Documento", v.getIdentificacion()),
                new Ui.Dato("Nombre", v.getNombreVisitante()),
                new Ui.Dato("Fecha de nacimiento", Ui.fecha(v.getFechaNacimiento())),
                new Ui.Dato("Estado", v.getEstado().toString()),
                new Ui.Dato("Cortesías este año", String.valueOf(vc.cortesiasAnio()))));
        VBox.setMargin(resumen, new javafx.geometry.Insets(0, 0, 14, 0));
        avisoPrevio(vc);
        Ui.mostrar(panelCortesia, true);
        nav.desplazarA(panelCortesia);
    }

    private void descartar() {
        elegido = null;
        Ui.mostrar(panelCortesia, false);
    }

    private void confirmar() {
        if (elegido == null) {
            mensaje.err("Elige un visitante antes de confirmar.");
            return;
        }
        try {
            Datos.CortesiaYRegistro r = servicio.otorgarCortesia(elegido.visitante().getIdentificacion());
            mensaje.ok("Cortesía otorgada", new Aviso.Grande(r.cortesia().getCodigoCortesia()),
                    "Entrega registrada el " + Ui.fecha(r.registro().getFecha()) + " a las " + Ui.hora(r.registro().getHora()) + ".");
            resumen.getChildren().clear();
            elegido = null;
            cargar();
            cargarUltimas();
        } catch (OperacionException e) {
            mensaje.err(e.getMessage());
        }
    }

    private void cargarUltimas() {
        try {
            List<FilaEntrega> l = servicio.ultimasEntregas();
            if (l.isEmpty()) {
                ultimas.getChildren().setAll(Ui.vacio("Todavía no hay entregas", "Las cortesías que otorgues aparecen aquí.", null));
                return;
            }
            List<Node[]> filas = new ArrayList<>();
            for (FilaEntrega x : l) {
                filas.add(new Node[]{Ui.celda(x.codigoCortesia(), true), Ui.celda(x.visitante()), Ui.celda(x.empleado()),
                        Ui.celda(Ui.fecha(x.fecha()), true), Ui.celda(Ui.hora(x.hora()), true)});
            }
            ultimas.getChildren().setAll(Ui.tabla(new String[]{"Código", "Visitante", "Empleado", "Fecha", "Hora"},
                    filas, true, true, true, true, true));
        } catch (OperacionException e) {
            ultimas.getChildren().clear();
        }
    }

    // ---------------------------------------------------------------- utilidades

    private static String soloDigitos(String s) {
        String t = s == null ? "" : s.trim();
        return t.matches("\\d+") ? t : "";
    }

    /** Acepta dd/mm/aaaa (como se muestra) o aaaa-mm-dd. */
    private static LocalDate leerFecha(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        try {
            return t.contains("/") ? LocalDate.parse(t, Ui.FECHA) : LocalDate.parse(t);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
