package co.edu.uniquindio.parquedelcafe;

import co.edu.uniquindio.parquedelcafe.logica.Datos;
import co.edu.uniquindio.parquedelcafe.logica.OperacionException;
import co.edu.uniquindio.parquedelcafe.logica.ServicioAtencion;
import co.edu.uniquindio.parquedelcafe.logica.ServicioAtencion.FormularioVisitante;
import co.edu.uniquindio.parquedelcafe.modelo.Devolucion;
import co.edu.uniquindio.parquedelcafe.modelo.EstadoVisitante;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de aceptación (Given-When-Then) del MVP Parque del Café. Correr con: mvn test */
class AceptacionTest {

    private ServicioAtencion servicio;

    @BeforeEach
    void datosNuevos() {
        servicio = new ServicioAtencion(new Datos(LocalDate.now()));
        servicio.iniciarSesion("E001"); // Ana Torres · Punto de atención 1
    }

    private OperacionException falla(Runnable accion) {
        return assertThrows(OperacionException.class, accion::run);
    }

    // ---------- Pantalla 1: Atención al visitante ----------

    @Test
    @DisplayName("GWT-V1: dado un empleado sin sesión, cuando consulta visitantes, entonces responde 401 (RNF-06)")
    void sinSesion() {
        servicio.cerrarSesion();
        assertEquals(401, falla(() -> servicio.buscarVisitantes("")).getStatus());
    }

    @Test
    @DisplayName("GWT-V2: dado un documento, cuando se busca, entonces muestra el visitante (REQ003)")
    void buscarPorDocumento() {
        var lista = servicio.buscarVisitantes("1094880123");
        assertEquals("Laura Gómez", lista.get(0).visitante().getNombreVisitante());
    }

    @Test
    @DisplayName("GWT-V3: dado un visitante nuevo con datos válidos, cuando se registra, entonces queda guardado (REQ004)")
    void registrarVisitante() {
        servicio.crearVisitante(new FormularioVisitante("1094999888", "Pedro Gil", LocalDate.of(2000, 1, 15),
                "3001234567", "Armenia", EstadoVisitante.ACTIVO));
        assertEquals(1, servicio.buscarVisitantes("1094999888").size());
    }

    @Test
    @DisplayName("GWT-V4: dado un documento con letras, cuando se registra, entonces muestra el error en el campo (R4)")
    void documentoConLetras() {
        OperacionException e = falla(() -> servicio.crearVisitante(new FormularioVisitante("10948A", "X",
                LocalDate.of(2000, 1, 1), "", "", EstadoVisitante.ACTIVO)));
        assertEquals(400, e.getStatus());
        assertTrue(e.getErrores().containsKey("identificacion"));
    }

    @Test
    @DisplayName("GWT-C1: dado un visitante activo que cumple años hoy, cuando se confirma la entrega, entonces se crea la cortesía y su registro (R5)")
    void otorgarCortesia() {
        var par = servicio.otorgarCortesia("1094880123");
        assertTrue(par.cortesia().getCodigoCortesia().matches("COR-\\d{4}-\\d{4}"));
        assertEquals("Otorgamiento de cortesía", par.registro().getTipoOperacion());
    }

    @Test
    @DisplayName("GWT-C2: dado un visitante que no cumple años hoy, cuando se confirma, entonces se bloquea (R1)")
    void noCumpleHoy() {
        assertEquals("R1", falla(() -> servicio.otorgarCortesia("1094660111")).getRegla());
    }

    @Test
    @DisplayName("GWT-C3: dado un visitante que ya recibió su cortesía este año, cuando se confirma, entonces se bloquea (R2)")
    void yaRecibio() {
        assertEquals("R2", falla(() -> servicio.otorgarCortesia("1094771002")).getRegla());
    }

    @Test
    @DisplayName("GWT-C4: dado un visitante inactivo, cuando se confirma, entonces se bloquea (R3)")
    void inactivo() {
        assertEquals("R3", falla(() -> servicio.otorgarCortesia("41912345")).getRegla());
    }

    // ---------- Pantalla 2: Devolución del pasaporte (solo Punto 2) ----------

    @Test
    @DisplayName("GWT-D1: dado un empleado del Punto 1, cuando intenta devolver, entonces responde 403 (R10)")
    void punto1NoDevuelve() {
        OperacionException e = falla(() -> servicio.registrarDevolucion("PAS-1001", ""));
        assertEquals(403, e.getStatus());
        assertEquals("R10", e.getRegla());
    }

    @Test
    @DisplayName("GWT-D2: dado un visitante sin cortesía confirmada, cuando se devuelve su pasaporte, entonces se bloquea (R9)")
    void sinCortesia() {
        servicio.iniciarSesion("E002");
        assertEquals("R9", falla(() -> servicio.registrarDevolucion("PAS-1006", "")).getRegla());
    }

    @Test
    @DisplayName("GWT-D3: dado un pasaporte usado, sin pago o no auténtico, cuando se devuelve, entonces se bloquea (R7)")
    void pasaporteNoProcede() {
        servicio.iniciarSesion("E002");
        for (String cod : new String[]{"PAS-1003", "PAS-1004", "PAS-1007"}) {
            assertEquals("R7", falla(() -> servicio.registrarDevolucion(cod, "")).getRegla(), cod);
        }
    }

    @Test
    @DisplayName("GWT-D4: dada una cortesía confirmada, cuando el Punto 2 devuelve el pasaporte, entonces registra código, tipo, monto y fecha (R8)")
    void devolucionProcede() {
        servicio.otorgarCortesia("1094880123");   // el Punto 1 confirma la cortesía de Laura
        servicio.iniciarSesion("E002");
        Devolucion d = servicio.registrarDevolucion("PAS-1002", "");
        assertEquals("PAS-1002", d.getCodigoPasaporte());
        assertEquals("Junior", d.getTipoPasaporte());
        assertEquals(35000, d.getMonto());
        assertEquals(LocalDate.now(), d.getFecha());
        assertTrue(d.getNumeroDevolucion().matches("DEV-\\d{4}-\\d{4}"));
    }

    @Test
    @DisplayName("GWT-D5: dado un pasaporte ya devuelto, cuando se intenta devolver otra vez, entonces se bloquea (R7)")
    void yaDevuelto() {
        servicio.otorgarCortesia("1094880123");
        servicio.iniciarSesion("E002");
        servicio.registrarDevolucion("PAS-1002", "");
        assertEquals("R7", falla(() -> servicio.registrarDevolucion("PAS-1002", "")).getRegla());
    }
}
