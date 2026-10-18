# TrazaPM — Gestión y trazabilidad de productos médicos implantables

Prototipo de escritorio desarrollado para el **Seminario de Práctica Informática (INF275 – Universidad Siglo 21)**.
Autor: **Luciano Felipe Comercci**. Entrega incremental: TP3 (desarrollo en Java).

Repositorio del proyecto: <https://github.com/nanocomercci/SeminarioPracticaInformatica>

TrazaPM registra la recepción, los movimientos internos, la cuarentena, la devolución y la implantación de
unidades identificadas por **GTIN + número de serie**, conserva el historial auditable de cada unidad y mantiene
una cola local de eventos para informar al SNT-PM (ANMAT). **La integración con el SNT-PM es simulada** y todos los
datos son sintéticos.

## Tecnologías

| Componente | Versión |
|---|---|
| Java (JDK) | 17 o superior (probado con 21) |
| Interfaz | JavaFX 21 (aplicación de escritorio, no web) |
| Base de datos | MySQL 8.0 (driver MySQL Connector/J 9.1) |
| Construcción | Maven 3.8+ (o un IDE con soporte Maven: IntelliJ IDEA, NetBeans, Eclipse) |

## Puesta en marcha

1. **Base de datos** (MySQL Workbench o consola, como root), en este orden:
   ```
   sql/01_esquema.sql            -- crea la base trazapm y sus tablas
   sql/02_datos_prueba.sql       -- datos sintéticos (usuarios, productos, ubicaciones)
   sql/06_usuario_aplicacion.sql -- usuario trazapm/trazapm que usa la aplicación
   ```
2. **Configuración** (opcional): los datos de conexión están en `src/main/resources/trazapm.properties`.
   Para usar otro servidor, copiar ese archivo junto al programa y editar `db.url`, `db.usuario` y `db.clave`.
3. **Ejecución**:
   ```
   mvn clean javafx:run
   ```
   Desde el IDE: abrir la carpeta como proyecto Maven y ejecutar `com.trazapm.Lanzador`.

Si MySQL no está disponible, la aplicación avisa y **continúa en modo demostración** con los mismos datos
sintéticos en memoria. Para forzar ese modo: `mvn javafx:run -Djavafx.args="--memoria"`.

Si aparece `Access denied for user 'trazapm'@'localhost'`, volver a ejecutar
`sql/06_usuario_aplicacion.sql` desde una conexión administrativa de MySQL Workbench. El script crea el usuario
si falta y también restablece la contraseña de desarrollo cuando ya existía con otra configuración.

## Usuarios de prueba (selector "Usuario" de la barra de herramientas)

| Usuario | Rol | Puede |
|---|---|---|
| operador.demo | Operador de depósito | recepción, traslado, puesta en cuarentena |
| supervisor.demo | Responsable de trazabilidad | todo, incluido levantar cuarentena y devolver |
| profesional.demo | Profesional implantador | implantación |

GTIN válidos del catálogo: `07791234000012` (marcapasos), `07791234000029` (stent), `07791234000036` (prótesis de cadera).

## Estructura del código

```
src/main/java/com/trazapm
├── App.java / Lanzador.java     punto de entrada JavaFX
├── config/Contexto              arma los objetos; elige MySQL o memoria
├── modelo/                      entidades (solo estado): EntidadBase, Persona, Producto y subclases,
│                                UnidadProducto, Evento, enums
├── control/                     reglas de negocio: ControlRecepcion (CU-01), ServicioTrazabilidad (CU-02..06),
│   │                            ServicioIntegracion (CU-07, cola FIFO), ConectorSnt (+ simulado)
│   └── operaciones/             OperacionTrazabilidad (abstracta) y sus 5 subclases polimórficas
├── dao/                         interfaces DAO + FuenteDatos (transacciones)
│   ├── jdbc/                    implementación MySQL con PreparedStatement
│   └── memoria/                 implementación en memoria (ArrayList + HashMap)
├── excepciones/                 TrazabilidadException (checked) y derivadas; PersistenciaException
├── util/                        Algoritmos (inserción, búsqueda binaria), ValidadorGtin (GS1 módulo 10)
└── vista/                       VentanaPrincipal (menú) y pantallas que heredan de PantallaBase
sql/                             scripts MySQL (esquema, datos, consultas de referencia)
docs/diagramas/                  fuentes PlantUML y PNG (clases, secuencia, DER, despliegue, red física)
docs/capturas/                   evidencias de ejecución del prototipo
```

## Requisitos de la consigna y dónde se cumplen

| Requisito | Implementación |
|---|---|
| Encapsulamiento | atributos `private`, acceso por getters; `Evento` inmutable salvo el estado de envío |
| Herencia | `EntidadBase → Persona → Usuario/Profesional`; `Producto → Marcapasos/StentCoronario/ProtesisCadera`; `OperacionTrazabilidad → 5 operaciones`; `PantallaBase → 6 pantallas` |
| Polimorfismo | `getDiasAlertaVencimiento()`, `getDetalleTecnico()`, `validarEspecifico()`, `estadoResultante()`, `refrescar()` |
| Abstracción | clases abstractas e interfaces `FuenteDatos`, DAO y `ConectorSnt` |
| Excepciones | jerarquía propia checked/unchecked; captura de `NumberFormatException`, `DateTimeParseException` y `SQLException` |
| Menú de selección | barra de menú, barra de herramientas, pestañas y accesos de la pantalla de inicio |
| Estructuras de control | `if/else`, `switch`, `for`, `for-each`, `while` en validaciones, algoritmos y mapeos |
| Estructuras de datos | `ArrayList`, `HashMap`, `EnumMap`, `EnumSet`, `Queue/ArrayDeque` |
| Ordenación y búsqueda | `Algoritmos.ordenarPorInsercion` y `Algoritmos.busquedaBinaria` |
| Persistencia MySQL | `dao/jdbc/FuenteDatosMySQL` con transacciones (commit/rollback) |
