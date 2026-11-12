# Parque del Café · MVP en Java + JavaFX

Es el mismo MVP de la versión web (Node), pero en **Java 17 + JavaFX 21**. Tiene las mismas dos pantallas,
las mismas reglas y los mismos mensajes, con el mismo diseño (colores, tarjetas, tablas y avisos).

| Pantalla | Quién la usa | Qué hace |
|---|---|---|
| **1. Atención al visitante** | Punto de atención 1 y 2 | Buscar, registrar, editar y eliminar visitantes. Confirmar la entrega de la cortesía de cumpleaños (R1–R6). |
| **2. Devolución del pasaporte** | **Solo Punto de atención 2** (R10) | Solo si la cortesía del visitante ya está confirmada (R9). Registra número, fecha, hora, código del pasaporte, tipo, monto, motivo y empleado, y cancela el pasaporte (R7, R8). |

## Cómo correrlo

Requisitos: **JDK 17 o superior** y **Maven** (Maven descarga JavaFX solo, no hay que instalarlo aparte).

```bash
mvn javafx:run      # abre la aplicación
mvn test            # 13 pruebas de aceptación Given-When-Then (JUnit 5)
```

**Desde IntelliJ / NetBeans / Eclipse:** abrir la carpeta como proyecto Maven y ejecutar la clase
`co.edu.uniquindio.parquedelcafe.Launcher`. Si ejecutan `App` directamente sin module-info, el IDE muestra
"JavaFX runtime components are missing", y para eso está `Launcher`.

Los datos están en memoria: al cerrar la aplicación se reinician. Para probar con otra fecha de "hoy" se puede
definir la variable de entorno `DEMO_FECHA=AAAA-MM-DD` antes de correrla.

## Usuarios y casos de demo

En la cabecera se elige el **Empleado en sesión**:

- **Ana Torres · Punto de atención 1**: puede usar la pantalla 1; en la pantalla 2 le sale el bloqueo R10.
- **Luis Pérez · Punto de atención 2**: puede usar las dos.

| Caso | Qué pasa |
|---|---|
| Laura Gómez (1094880123) | Cumple años hoy → se le puede confirmar la cortesía. Después, su pasaporte PAS-1002 sí se puede devolver. |
| Andrés Castaño (1094771002) | Ya recibió cortesía este año → R2. Su pasaporte PAS-1001 se puede devolver. |
| Marta Salazar (41912345) | Inactiva → R3. |
| Carlos Ríos (1094660111) | No cumple años hoy → R1. |
| PAS-1003 / PAS-1004 / PAS-1005 / PAS-1007 | Usado / sin pago / ya devuelto / no auténtico → R7. |
| PAS-1006 (Valentina) | Sin cortesía confirmada → R9. |

Los precios de los pasaportes son de ejemplo (Junior $35.000, Senior $30.000, Múltiple $60.000) y se cambian en
`modelo/TipoDePasaporte.java`.

## Estructura (igual al diagrama de clases)

```
src/main/java/co/edu/uniquindio/parquedelcafe/
├── App.java                     ventana: cabecera, pestañas y "Empleado en sesión"
├── Launcher.java                main para el IDE
├── modelo/                      «entity»: Visitante, Cortesia, RegistroEntrega, Pasaporte,
│                                TipoDePasaporte, Devolucion, Empleado, PuntoDeAtencion, estados
├── logica/
│   ├── Reglas.java              «control»: validarReglas (R1–R4) y validarDevolucion (R7, R9)
│   ├── ServicioAtencion.java    «control»: lo que en la web era el ServidorAPI (incluye R10 y RNF-06)
│   └── Datos.java               «repository»: datos de prueba en memoria
└── vista/                       «boundary»
    ├── AtencionVisitanteVista.java   pantalla 1
    ├── DevolucionVista.java          pantalla 2
    ├── Aviso.java, Ui.java           piezas de interfaz (avisos, tablas, tarjetas)
src/main/resources/.../estilos.css    los mismos estilos del MVP web
src/test/java/.../AceptacionTest.java pruebas GWT
```
