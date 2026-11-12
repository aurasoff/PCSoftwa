package co.edu.uniquindio.parquedelcafe.logica;

import co.edu.uniquindio.parquedelcafe.modelo.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos de prueba en memoria (se reinician al cerrar la aplicación).
 * "Hoy" del sistema (R6): para la demo se puede forzar con -Ddemo.fecha=AAAA-MM-DD o la variable DEMO_FECHA.
 */
public class Datos {

    private final LocalDate hoyFijo;

    public final List<PuntoDeAtencion> puntos = new ArrayList<>();
    public final List<Empleado> empleados = new ArrayList<>();
    public final List<Visitante> visitantes = new ArrayList<>();
    public final List<Cortesia> cortesias = new ArrayList<>();
    public final List<RegistroEntrega> registros = new ArrayList<>();
    public final List<Pasaporte> pasaportes = new ArrayList<>();
    public final List<Devolucion> devoluciones = new ArrayList<>();
    private int secuencia = 0;
    private int secuenciaDev = 0;

    public Datos() { this(fechaConfigurada()); }

    public Datos(LocalDate hoyFijo) {
        this.hoyFijo = hoyFijo;
        cargarDatosDePrueba();
    }

    private static LocalDate fechaConfigurada() {
        String f = System.getProperty("demo.fecha", System.getenv("DEMO_FECHA"));
        return (f == null || f.isBlank()) ? null : LocalDate.parse(f.trim());
    }

    public LocalDate hoy() { return hoyFijo != null ? hoyFijo : LocalDate.now(); }

    private static LocalTime ahora() { return LocalTime.now().withNano(0); }

    /** Cumpleaños con el mismo día y mes de (hoy + offset), pero en el año de nacimiento. */
    private LocalDate cumple(int anioNac, int offset) {
        return MonthDay.from(hoy().plusDays(offset)).atYear(anioNac);
    }

    private void cargarDatosDePrueba() {
        PuntoDeAtencion p1 = new PuntoDeAtencion("Punto de atención 1", "Entrada principal");
        PuntoDeAtencion p2 = new PuntoDeAtencion("Punto de atención 2", "Plazoleta de servicios");
        puntos.add(p1);
        puntos.add(p2);

        // Usuarios demo (RNF-06). R10: solo el Punto de atención 2 atiende devoluciones.
        empleados.add(new Empleado("E001", "Ana Torres", "Atención al visitante", p1, false));
        empleados.add(new Empleado("E002", "Luis Pérez", "Atención al visitante", p2, true));

        // 14 visitantes: los 4 primeros cumplen años HOY (casos de demo para R2 y R3).
        EstadoVisitante A = EstadoVisitante.ACTIVO, I = EstadoVisitante.INACTIVO;
        visitantes.add(new Visitante("1094880123", "Laura Gómez", cumple(1998, 0), "3101234567", "Montenegro", A));
        visitantes.add(new Visitante("1094880456", "Sofía Ramírez", cumple(2001, 0), "3112345678", "Armenia", A));
        visitantes.add(new Visitante("41912345", "Marta Salazar", cumple(1961, 0), "", "Calarcá", I));
        visitantes.add(new Visitante("1094771002", "Andrés Castaño", cumple(1995, 0), "3209876543", "Armenia", A));
        visitantes.add(new Visitante("1094660111", "Carlos Ríos", cumple(1995, 12), "3001112233", "Pereira", A));
        visitantes.add(new Visitante("1094550222", "Valentina Osorio", cumple(2003, 27), "3123334455", "Montenegro", A));
        visitantes.add(new Visitante("1094440333", "Juan Pablo Mejía", cumple(1989, 41), "3145556677", "Quimbaya", A));
        visitantes.add(new Visitante("1094330444", "Daniela Marín", cumple(1999, 63), "3157778899", "Armenia", A));
        visitantes.add(new Visitante("1094220555", "Felipe Duque", cumple(2005, 88), "", "Circasia", A));
        visitantes.add(new Visitante("1094110666", "Camila Vélez", cumple(1992, 120), "3169990011", "Salento", A));
        visitantes.add(new Visitante("1094000777", "Mateo Arias", cumple(2010, 150), "", "Montenegro", A));
        visitantes.add(new Visitante("41800888", "Luz Marina Cano", cumple(1958, 200), "3171112222", "Calarcá", A));
        visitantes.add(new Visitante("1093990999", "Sebastián León", cumple(1997, 250), "3182223333", "Pereira", A));
        visitantes.add(new Visitante("1093881000", "Isabela Rojas", cumple(2000, 300), "3193334444", "Armenia", I));

        // Cortesía ya entregada hoy a Andrés Castaño (caso demo de R2).
        guardarCortesia(nuevaCortesia("1094771002", hoy(), empleados.get(0)));

        // Pasaportes comprados. "valor" es lo que pagó el visitante según el tipo.
        pasaportes.add(new Pasaporte("PAS-1001", TipoDePasaporte.SENIOR, EstadoPasaporte.PAGADO, "1094771002", true));    // Andrés: procede
        pasaportes.add(new Pasaporte("PAS-1002", TipoDePasaporte.JUNIOR, EstadoPasaporte.PAGADO, "1094880123", true));    // Laura: procede tras confirmar la cortesía (R9)
        pasaportes.add(new Pasaporte("PAS-1003", TipoDePasaporte.MULTIPLE, EstadoPasaporte.USADO, "1094880456", true));   // Sofía: ya usado
        pasaportes.add(new Pasaporte("PAS-1004", TipoDePasaporte.JUNIOR, EstadoPasaporte.SIN_PAGO, "1094660111", true));  // Carlos: sin pago
        pasaportes.add(new Pasaporte("PAS-1005", TipoDePasaporte.JUNIOR, EstadoPasaporte.CANCELADO, "1094771002", true)); // Andrés: ya devuelto
        pasaportes.add(new Pasaporte("PAS-1006", TipoDePasaporte.SENIOR, EstadoPasaporte.PAGADO, "1094550222", true));    // Valentina: sin cortesía (R9)
        pasaportes.add(new Pasaporte("PAS-1007", TipoDePasaporte.MULTIPLE, EstadoPasaporte.PAGADO, "1094880123", false)); // Laura: no auténtico
    }

    /** Par cortesía + registro de entrega (R5): se crean y se guardan juntos. */
    public record CortesiaYRegistro(Cortesia cortesia, RegistroEntrega registro) { }

    public CortesiaYRegistro nuevaCortesia(String visitanteId, LocalDate fechaVisita, Empleado empleado) {
        secuencia++;
        String codigo = String.format("COR-%d-%04d", fechaVisita.getYear(), secuencia);
        Cortesia c = new Cortesia(codigo, visitanteId, fechaVisita, hoy(), "emitida", empleado.getPunto().getNombre());
        RegistroEntrega r = new RegistroEntrega(codigo, hoy(), ahora(), "Otorgamiento de cortesía", empleado.getIdentificacion());
        return new CortesiaYRegistro(c, r);
    }

    public void guardarCortesia(CortesiaYRegistro par) {
        cortesias.add(par.cortesia());
        registros.add(par.registro());
    }

    /** R8: el monto devuelto es el valor pagado según el tipo de pasaporte. */
    public Devolucion nuevaDevolucion(Pasaporte pasaporte, String motivo, Empleado empleado) {
        secuenciaDev++;
        return new Devolucion(
                String.format("DEV-%d-%04d", hoy().getYear(), secuenciaDev),
                hoy(), ahora(),
                pasaporte.getCodigoPasaporte(), pasaporte.getTipo().getTipo(),
                pasaporte.getValor(), pasaporte.getValor(),
                motivo, pasaporte.getVisitanteId(), empleado.getIdentificacion());
    }
}
