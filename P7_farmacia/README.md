# Miniproyecto 7: Punto de Venta y Surtido de una Farmacia

Proyecto de **Tecnologías de Programación** desarrollado en **Java 21**. Permite administrar medicamentos, clientes y ventas desde una interfaz de escritorio, consultar reportes, monitorear ventas y exportar respaldos.

> **Alcance:** este README describe el código que contiene la versión del proyecto revisada el 3 de noviembre de 2026. Las capturas de la interfaz y la ejecución de la compilación en el equipo de entrega deben verificarse antes de presentar el trabajo.

## 1. Tecnologías utilizadas

| Tecnología | Uso en el proyecto |
|---|---|
| Java 21 | Lenguaje de programación y modelo orientado a objetos. |
| Swing (JDK) | Ventanas, formularios, tablas y navegación de escritorio. |
| FlatLaf 3.7.2 | Apariencia visual moderna de los componentes Swing. |
| SQLite | Almacenamiento local de medicamentos, clientes y ventas. |
| SQLite JDBC 3.46.1.3 | Acceso a SQLite mediante JDBC y consultas SQL. |
| Maven | Administración de dependencias y compilación del proyecto. |
| Git | Control de versiones e historial de cambios. |

### ¿Por qué Swing y FlatLaf?

Se eligió **Swing** porque permite desarrollar una aplicación gráfica de escritorio con Java 21, sin requerir un servidor web ni un navegador. La interfaz aprovecha componentes como `JFrame`, `JPanel`, `JTable`, formularios y cuadros de diálogo. **FlatLaf** se utiliza para mejorar su apariencia visual sin reemplazar Swing ni modificar la lógica de negocio.

La ventana se inicia mediante `SwingUtilities.invokeLater(...)` para crear la interfaz en el hilo de eventos de Swing. Los componentes de la interfaz invocan operaciones de la fachada `Farmacia` en lugar de ejecutar consultas SQL directamente.

## 2. Requisitos previos

1. **JDK 21** instalado y configurado.
2. **Apache Maven** disponible en la terminal, o Maven integrado en IntelliJ IDEA.
3. IntelliJ IDEA (opcional, recomendado para ejecutar y depurar).
4. Conexión a Internet **solo durante la primera descarga de dependencias**, si estas aún no están en la caché local de Maven.

En PowerShell, comprobar:

```powershell
java -version
mvn -version
```

Java debe indicar la versión 21. No es necesario instalar SQLite por separado: el controlador JDBC es una dependencia declarada en `pom.xml`.

## 3. Instalación y compilación

Abrir una terminal **dentro de la carpeta que contiene `pom.xml`**. Si el repositorio tiene una carpeta interna `P7_farmacia`, entrar en ella primero:

```powershell
cd .\P7_farmacia
```

Compilar todo el código fuente con **un solo comando**:

```powershell
mvn clean compile
```

El proyecto utiliza el complemento `maven-compiler-plugin` y configura `maven.compiler.release=21`.

## 4. Cómo ejecutar la aplicación de escritorio

Desde la misma carpeta que contiene `pom.xml`, ejecutar:

```powershell
mvn compile exec:java "-Dexec.mainClass=mx.tecnm.orizaba.tp.farmacia.app.InicioEscritorio"
```

También se puede abrir `src/main/java/mx/tecnm/orizaba/tp/farmacia/app/InicioEscritorio.java` en IntelliJ IDEA y ejecutar su método `main`.

**Directorio de trabajo:** debe ser la carpeta de `pom.xml`, porque `InicioEscritorio` utiliza la ruta relativa `datos/farmacia.db`. Al iniciar, crea la carpeta `datos` si no existe y abre el archivo SQLite. Si la base está vacía, se crea el esquema; si existe con registros, se cargan a través de los repositorios.

> Si se va a entregar una base con datos de ejemplo, copiar `farmacia.db` a `datos/farmacia.db` **antes de abrir la aplicación**. Respaldar cualquier base con información importante antes de reemplazarla.

### Ejecutar pruebas y demostración del modelo

El archivo `pom.xml` tiene configurada la clase `PruebasModelo` como punto de entrada predeterminado:

```powershell
mvn compile exec:java
```

Para ejecutar la demostración alternativa:

```powershell
mvn compile exec:java -Pdemo
```

**Nota:** estas instrucciones corresponden a la configuración del proyecto; el resultado de las pruebas y la compilación debe comprobarse en el equipo donde se entregue.

## 5. Uso del sistema

La ventana principal ofrece un menú lateral con seis secciones:

| Sección | Funcionalidad |
|---|---|
| **Inicio** | Indicadores, resumen de ventas y reportes gráficos. |
| **Inventario** | Registrar medicamentos, consultar, buscar, filtrar, editar y retirar productos. |
| **Clientes** | Registrar y consultar clientes; administrar sus niveles. |
| **Ventas** | Crear ventas, agregar o quitar medicamentos, consultar líneas y cambiar su estado. |
| **Monitor** | Iniciar o detener la revisión periódica de ventas mediante una tarea concurrente. |
| **Respaldos** | Exportar y verificar respaldos binarios con extensión `.farmacia`. |

### Ejemplo de flujo de trabajo

1. Abrir **Inventario** y registrar un medicamento de **Patente**, **Genérico** o **Biológico**.
2. Entrar en **Clientes** y registrar un cliente con su nombre, correo y nivel.
3. Abrir **Ventas**, seleccionar **Nueva venta** y asociarla con un cliente.
4. Utilizar **Agregar medicamento** para crear líneas de venta, indicando producto y cantidad.
5. Aplicar transiciones permitidas: por ejemplo, pasar de **ABIERTA** a **EN_CURSO** y posteriormente a **CERRADA**, según las reglas del modelo.
6. Consultar **Inicio** para visualizar los indicadores y reportes (actualizar cuando corresponda).
7. En **Respaldos**, exportar un archivo `.farmacia` y verificarlo desde el panel.

**Importante:** una línea de venta registra un producto y su cantidad dentro de una venta. Las reglas de transición impiden cambios de estado no permitidos; no se debe interpretar que todas las operaciones están disponibles en cualquier estado.

## 6. Estructura del proyecto

```text
P7_farmacia/
├── pom.xml
├── README.md
├── TODO.md
├── BITACORA_IA.md
├── datos/
│   ├── farmacia.db                # Base SQLite de ejemplo (al integrarla)
│   ├── carga_ejemplo.sql          # Script opcional del entregable 6
│   └── verificar_ejemplo.sql      # Consultas opcionales de verificación
├── diagramas/
│   ├── modelo.puml
│   ├── modelo.png
│   ├── modelo.dot
│   ├── arquitectura.dot
│   └── arquitectura.png
├── docs/
│   └── capturas/                  # Pendiente: agregar capturas reales
└── src/main/java/mx/tecnm/orizaba/tp/farmacia/
    ├── app/                       # InicioEscritorio, pruebas y demo
    ├── modelo/                    # Producto, Cliente, Venta, Estado, etc.
    ├── servicio/                  # Fachada Farmacia
    ├── persistencia/              # JDBC, repositorios y respaldo
    ├── funcional/                 # Reglas y fábrica de productos
    ├── eventos/                   # Eventos y oyentes
    ├── concurrencia/              # TareaPeriodica
    └── ui/                        # VentanaPrincipal y paneles de Swing
```

Las carpetas `datos/` y `docs/capturas/` se muestran como estructura de entrega prevista. Se deben incorporar sus archivos al repositorio; no todas estaban presentes en el ZIP original.

## 7. Decisiones de diseño

### Separación de responsabilidades

- **`modelo`**: contiene las entidades y reglas de dominio, como `Producto`, `Cliente`, `Venta`, `Sku`, `Estado` y `Anaquel`.
- **`servicio/Farmacia`**: actúa como fachada de la aplicación; coordina operaciones, colecciones, persistencia, eventos y reportes.
- **`persistencia`**: encapsula el acceso a SQLite con `ConexionSQLite` y repositorios de productos, clientes y ventas. La conexión habilita las claves foráneas (`PRAGMA foreign_keys = ON`).
- **`ui`**: muestra los datos y recibe acciones del usuario por medio de Swing; usa la fachada en vez de acceder directamente a JDBC.

### Modelo orientado a objetos

`Producto` es una clase abstracta sellada con tres variantes: `Patente`, `Generico` y `Biologico`. El identificador del producto se modela con `Sku`. Las interfaces `Vendible`, `Fraccionable` y `RequiereReceta` permiten expresar comportamientos especializados.

### Ventas y estados

Una venta se vincula con un cliente y contiene una colección de líneas. El estado se representa con el enumerado `Estado`, que controla las transiciones válidas entre `ABIERTA`, `EN_CURSO`, `CERRADA` y `CANCELADA`.

### Persistencia y seguridad de datos

SQLite mantiene las tablas `producto`, `cliente`, `venta` y `venta_linea`. El repositorio de ventas utiliza transacciones para persistir encabezado y líneas juntos. El respaldo exportado como `.farmacia` usa serialización de objetos Java y **no reemplaza** al archivo de base de datos `.db`.

### Reportes y concurrencia

La fachada incluye reportes con la API de Streams, como ingresos por tipo, producto más demandado y estadísticas de ventas cerradas. El sistema también incorpora un monitor periódico para revisar situaciones de ventas de acuerdo con las reglas implementadas.

## 8. Base de datos de ejemplo — Entregable 6

El conjunto de datos demostrativo preparado para la entrega contiene:

| Tabla | Registros de ejemplo |
|---|---:|
| `producto` | 6 |
| `cliente` | 3 |
| `venta` | 4 |
| `venta_linea` | 8 |

Las cuatro ventas están distribuidas entre los estados `ABIERTA`, `EN_CURSO`, `CERRADA` y `CANCELADA`.

Para revisar la base con un cliente SQLite, se pueden ejecutar las consultas del archivo `datos/verificar_ejemplo.sql`. El requisito académico menciona `datos.db`, mientras que **esta implementación concreta abre `datos/farmacia.db`**. Antes de entregar, verificar si el docente exige literalmente el nombre `datos.db`; cambiar solo el nombre del archivo sin ajustar la ruta Java impediría que la aplicación lo encuentre.

## 9. Diagramas UML

El repositorio incluye archivos para revisar y editar el modelo y la arquitectura:

- [Diagrama de clases del modelo](diagramas/modelo.png)
- [Código fuente PlantUML del modelo](diagramas/modelo.puml)
- [Diagrama de arquitectura](diagramas/arquitectura.png)

**Pendiente de verificación para la entrega:** actualizar el diagrama de clases conforme a las implementaciones más recientes, especialmente la fachada, repositorios y clases de interfaz gráfica.

## 10. Capturas de pantalla — pendientes de incorporar

Agregar **capturas reales de la aplicación en ejecución** en la carpeta `docs/capturas/`, preferentemente en formato PNG. Se recomiendan al menos las siguientes:

- [ ] `01-inicio.png`: indicadores y gráficas de la pantalla Inicio.
- [ ] `02-inventario.png`: tabla con productos de los tres tipos.
- [ ] `03-clientes.png`: clientes registrados.
- [ ] `04-ventas.png`: ventas y sus estados.
- [ ] `05-respaldos.png`: exportación o verificación de un respaldo.

Una vez agregados los archivos, quitar los comentarios de las siguientes líneas para visualizar las imágenes directamente en GitHub:

<!-- ![Pantalla Inicio](docs/capturas/01-inicio.png) -->
<!-- ![Inventario](docs/capturas/02-inventario.png) -->
<!-- ![Clientes](docs/capturas/03-clientes.png) -->
<!-- ![Ventas](docs/capturas/04-ventas.png) -->
<!-- ![Respaldos](docs/capturas/05-respaldos.png) -->

## 11. Control de versiones y uso de IA

El desarrollo se mantiene en Git, con cambios incrementales en el modelo, la persistencia, la interfaz y la documentación. El archivo [`BITACORA_IA.md`](BITACORA_IA.md) documenta el apoyo recibido de herramientas de inteligencia artificial y su revisión durante el desarrollo.

## 12. Lista de verificación antes de entregar

- [ ] Ejecutar `mvn clean compile` y guardar evidencia de compilación exitosa.
- [ ] Ejecutar `PruebasModelo` y documentar los resultados obtenidos.
- [ ] Comprobar la carga de los datos de ejemplo en la interfaz.
- [ ] Agregar capturas reales a `docs/capturas/` y activar sus enlaces.
- [ ] Actualizar y revisar el diagrama UML de clases.
- [ ] Confirmar en GitHub el README, la bitácora, el código, los diagramas y la base requerida.
- [ ] Verificar con el docente si el archivo de base debe llamarse exactamente `datos.db`.

---

**Asignatura:** Tecnologías de Programación · **Proyecto:** P7_farmacia
