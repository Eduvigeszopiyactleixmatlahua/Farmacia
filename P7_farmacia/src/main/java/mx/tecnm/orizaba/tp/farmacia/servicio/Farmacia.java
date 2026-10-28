package mx.tecnm.orizaba.tp.farmacia.servicio;

import mx.tecnm.orizaba.tp.farmacia.concurrencia.TareaPeriodica;
import mx.tecnm.orizaba.tp.farmacia.eventos.Evento;
import mx.tecnm.orizaba.tp.farmacia.eventos.OyenteEvento;
import mx.tecnm.orizaba.tp.farmacia.eventos.TipoEvento;
import mx.tecnm.orizaba.tp.farmacia.funcional.FabricaElemento;
import mx.tecnm.orizaba.tp.farmacia.funcional.Regla;
import mx.tecnm.orizaba.tp.farmacia.modelo.Cliente;
import mx.tecnm.orizaba.tp.farmacia.modelo.Producto;
import mx.tecnm.orizaba.tp.farmacia.modelo.Anaquel;
import mx.tecnm.orizaba.tp.farmacia.modelo.Sku;
import mx.tecnm.orizaba.tp.farmacia.modelo.Estado;
import mx.tecnm.orizaba.tp.farmacia.modelo.Nivel;
import mx.tecnm.orizaba.tp.farmacia.modelo.ReglaNegocioException;
import mx.tecnm.orizaba.tp.farmacia.modelo.Venta;
import mx.tecnm.orizaba.tp.farmacia.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.farmacia.persistencia.Repositorio;
import mx.tecnm.orizaba.tp.farmacia.persistencia.RepositorioClienteSQLite;
import mx.tecnm.orizaba.tp.farmacia.persistencia.RepositorioProductoSQLite;
import mx.tecnm.orizaba.tp.farmacia.persistencia.RepositorioVentaSQLite;
import mx.tecnm.orizaba.tp.farmacia.persistencia.Respaldo;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Fachada principal de servicios del sistema de farmacia.
 *
 * <p>Centraliza las operaciones de registro, consulta,
 * actualización y eliminación de productos, así como
 * la administración de clientes y ventas.</p>
 *
 * <p>Coordina la persistencia mediante repositorios SQLite,
 * genera reportes utilizando Streams y administra
 * los eventos y el monitor periódico de surtido.</p>
 *
 * <p>La interfaz gráfica utiliza esta fachada para acceder
 * a las operaciones del sistema, manteniendo separadas
 * las responsabilidades de presentación, negocio
 * y persistencia.</p>
 *
 * <p>Implementa {@link AutoCloseable} para permitir
 * la detención ordenada del monitor al finalizar
 * la aplicación.</p>
 *
 * @see Anaquel
 * @see Cliente
 * @see Venta
 * @see ConexionSQLite
 */

public class Farmacia implements AutoCloseable {

    /** Días tras los cuales un(a) venta en curso genera una alerta. */
    public static final int PLAZO_DIAS = 2;

    private final Anaquel catalogo = new Anaquel();
    private final Map<Integer, Cliente> clientes = new ConcurrentHashMap<>();
    private final Map<Integer, Venta> registros = new ConcurrentHashMap<>();
    private final Set<Integer> alertados = ConcurrentHashMap.newKeySet();
    private final List<OyenteEvento> oyentes = new CopyOnWriteArrayList<>();

    private final Repositorio<Producto, Sku> repoElementos;
    private final Repositorio<Cliente, Integer> repoClientes;
    private final Repositorio<Venta, Integer> repoRegistros;
    private final Supplier<LocalDate> reloj;
    private MonitorSurtido monitor;

    /**
     * Construye el servicio de farmacia utilizando
     * la fecha actual del sistema.
     *
     * <p>Delega la inicialización al constructor que recibe
     * un proveedor de fechas, utilizando la referencia
     * a método {@code LocalDate::now}.</p>
     *
     * @param bd conexión SQLite utilizada para crear
     *           los repositorios de persistencia.
     */

    public Farmacia(ConexionSQLite bd) {
        this(bd, LocalDate::now);
    }

/**
 * Construye e inicializa el servicio de farmacia.
 *
 * <p>Crea los repositorios SQLite de productos,
 * clientes y ventas, configura el proveedor de fechas
 * y recupera los registros existentes en la base
 * de datos para cargarlos en memoria.</p>
 *
 * <p>El proveedor de fechas permite utilizar fechas
 * simuladas durante las pruebas del monitor de surtido.</p>
 *
 * @param bd conexión SQLite utilizada por los repositorios.
 * @param reloj proveedor de la fecha actual,
 *              inyectable para pruebas y simulaciones.
 */

    /** @param reloj proveedor de la fecha actual (inyectable para pruebas y simulación) */
    public Farmacia(ConexionSQLite bd, Supplier<LocalDate> reloj) {
        // TODO E15: Crear los tres repositorios SQLite, asignar el reloj y cargar los datos existentes en memoria.
        //Repositorio de productos
        this.repoElementos = new RepositorioProductoSQLite(bd);
        //Repositorio de clientes
        this.repoClientes = new RepositorioClienteSQLite(bd);
        //Crear  el repositorio de ventas
        this.repoRegistros = new RepositorioVentaSQLite(bd, repoClientes, repoElementos);
        //Asignar el reloj
        this.reloj = reloj;
        cargar();
        //throw new UnsupportedOperationException("TODO E15");
    }

    private void cargar() {
        // TODO E15: Usar referencias a método: repoElementos.todos().forEach(catalogo::agregar), etc.
        //Recuperar los probuctos
        repoElementos.todos().forEach(catalogo::agregar);
        //Recuperar los clientes
        repoClientes.todos().forEach(cliente -> clientes.put(cliente.id(), cliente));
        //Recuperar las ventas
        repoRegistros.todos().forEach(venta -> registros.put(venta.id(), venta));
        //throw new UnsupportedOperationException("TODO E15");
    }

    // ------------------------------------------------------------------ eventos

    /**
     * Registra un oyente para recibir eventos de la farmacia.
     *
     * <p>Agrega el oyente a la colección de suscriptores
     * que reciben notificaciones cuando ocurren
     * operaciones relevantes en el sistema.</p>
     *
     * @param oyente objeto que recibirá las notificaciones.
     */

    public void suscribir(OyenteEvento oyente) {
        oyentes.add(oyente);
    }

    public void desuscribir(OyenteEvento oyente) {
        oyentes.remove(oyente);
    }

    /**
     * Publica un evento entre los oyentes registrados.
     *
     * <p>Crea un objeto Evento con el tipo y mensaje
     * proporcionados y notifica a cada suscriptor
     * mediante su método notificar.</p>
     *
     * @param tipo categoría del evento generado.
     * @param mensaje descripción del acontecimiento.
     */

    private void publicar(TipoEvento tipo, String mensaje) {
        Evento e = Evento.de(tipo, mensaje);
        oyentes.forEach(o -> o.notificar(e));
    }

    // ------------------------------------------------------------------ catálogo

    /**
     * Alta genérica de cualquier subtipo mediante una fábrica (función de orden superior).
     * Ejemplo: {@code registrar(Producto.Patente::new, "...", 100.0, "Laboratorio A")}.
     */
    public <E extends Producto, A> E registrar(FabricaElemento<E, A> fabrica, String nombre, double precio, A atributo) {
        // TODO E15: Crear con la fábrica, persistir, agregar al catálogo, publicar ALTA y devolver el elemento.
        //Crear producto mediante fabrica
        E producto = fabrica.crear(nombre, precio, atributo);
        //Guardar en SQLite
        repoElementos.guardar(producto);
        //  Agregar al catalogo en memoria
        catalogo.agregar(producto);
        publicar(TipoEvento.ALTA, "Producto registrado: " + producto.clave());
        return producto;
        //throw new UnsupportedOperationException("TODO E15");
    }

    /**
     * Actualiza el nombre y precio base de un medicamento.
     *
     * <p>Modifica los atributos del producto recibido
     * mediante sus métodos modificadores y guarda
     * sus nuevos valores en SQLite.</p>
     *
     * @param elemento producto que será actualizado.
     * @param nombre nuevo nombre del medicamento.
     * @param precio nuevo precio base del medicamento.
     */

    public void actualizar(Producto elemento, String nombre, double precio) {
        elemento.setNombre(nombre);
        elemento.setPrecioBase(precio);
        repoElementos.guardar(elemento);
    }

    /**
     * Retira un medicamento del catálogo de farmacia.
     *
     * <p>Comprueba que el producto no esté referenciado
     * por ninguna línea de venta registrada.</p>
     *
     * <p>Si no existen referencias, solicita su eliminación
     * en SQLite y posteriormente lo retira del catálogo
     * en memoria cuando la eliminación es exitosa.</p>
     *
     * @param clave identificador SKU del medicamento.
     * @return {@code true} si el producto fue eliminado;
     *         {@code false} si no se eliminó ningún registro.
     * @throws ReglaNegocioException si el producto
     *         está asociado a alguna venta.
     */

    public boolean retirar(Sku clave) {
        // TODO E15: Impedir retirar elementos referenciados por algún registro; eliminar de la BD y del catálogo.
        boolean referenciado = registros.values().stream().anyMatch(venta -> venta.lineas().stream().anyMatch(lineas -> lineas.elemento().clave().equals(clave)));

        if (referenciado){
            throw new ReglaNegocioException("No se puede retirar un producto asociado a una venta");
        }
        boolean eliminado = repoElementos.eliminar(clave);

        if (eliminado){
            catalogo.retirar(clave);
        }
        return eliminado;
        //throw new UnsupportedOperationException("TODO E15");
    }

    /**
     * Obtiene el catálogo de productos de la farmacia.
     *
     * @return instancia de Anaquel que contiene
     *         los medicamentos registrados.
     */

    public Anaquel catalogo() {
        return catalogo;
    }

    /**
     * Busca un medicamento mediante su clave SKU.
     *
     * <p>Convierte la cadena proporcionada en un objeto
     * Sku y consulta el producto correspondiente
     * dentro del catálogo.</p>
     *
     * @param clave identificador SKU del medicamento.
     * @return producto asociado con la clave indicada.
     * @throws IllegalArgumentException si la clave
     *         no tiene un formato SKU válido.
     * @throws ReglaNegocioException si el producto
     *         solicitado no existe en el catálogo.
     */

    public Producto elemento(String clave) {
        return catalogo.buscar(Sku.parse(clave))
                .orElseThrow(() -> new ReglaNegocioException("No existe " + clave));
    }

    // ------------------------------------------------------------------ clientes

    /**
     * Registra un nuevo cliente en la farmacia.
     *
     * <p>Verifica que no exista otro cliente con el
     * mismo correo electrónico, ignorando diferencias
     * entre mayúsculas y minúsculas.</p>
     *
     * <p>Genera el siguiente identificador disponible
     * a partir del identificador máximo, crea el cliente,
     * lo guarda en SQLite y publica un evento de alta.</p>
     *
     * @param nombre nombre del cliente.
     * @param correo correo electrónico del cliente.
     * @param nivel nivel de descuento asignado.
     * @return cliente creado y registrado.
     * @throws ReglaNegocioException si el correo
     *         ya se encuentra registrado.
     */

    public Cliente registrarCliente(String nombre, String correo, Nivel nivel) {
        // TODO E15: Rechazar correos repetidos; id = máximo actual + 1 (stream mapToInt/max); persistir y publicar ALTA.
        boolean repetido = clientes.values().stream().anyMatch(cliente -> cliente.correo().equalsIgnoreCase(correo));
        if (repetido){
            throw new ReglaNegocioException("El correo ya está registrado");
        }
        //Calcular el ID
        int id = clientes.values().stream().mapToInt(Cliente::id).max().orElse(0) + 1;
        //Crear el cliente
        Cliente nuevo = new Cliente(id, nombre, correo, nivel);
        repoClientes.guardar(nuevo);
        //Agregar al mapa
        clientes.put(nuevo.id(), nuevo);
        //publicar evento ALTA
        publicar(TipoEvento.ALTA, "Cliente registrado: " + nuevo.id());
        return nuevo;
        //throw new UnsupportedOperationException("TODO E15");
    }

    /**
     * Modifica el nivel de descuento de un cliente.
     *
     * <p>Localiza el cliente mediante su identificador,
     * actualiza el nivel asignado y guarda el cambio
     * en la base de datos SQLite.</p>
     *
     * @param idCliente identificador del cliente.
     * @param nivel nuevo nivel de descuento.
     * @throws ReglaNegocioException si el cliente no existe.
     */

    public void cambiarNivel(int idCliente, Nivel nivel) {
        Cliente a = cliente(idCliente);
        a.setNivel(nivel);
        repoClientes.guardar(a);
    }

    public Cliente cliente(int id) {
        return Optional.ofNullable(clientes.get(id))
                .orElseThrow(() -> new ReglaNegocioException("No existe el registro " + id));
    }

    public List<Cliente> clientes() {
        return clientes.values().stream().sorted(Comparator.comparingInt(Cliente::id)).toList();
    }

    // ------------------------------------------------------------------ ventas

    /**
     * Abre una nueva venta para un cliente registrado.
     *
     * <p>Localiza al cliente, genera un identificador
     * consecutivo y crea una venta utilizando la fecha
     * proporcionada por el reloj del sistema.</p>
     *
     * <p>Guarda la venta en SQLite, la incorpora a
     * los registros en memoria y publica un evento
     * de cambio de estado.</p>
     *
     * @param idCliente identificador del cliente comprador.
     * @return nueva venta registrada en estado abierta.
     * @throws ReglaNegocioException si el cliente no existe.
     */

    public Venta abrir(int idCliente) {
        // TODO E15: Crear el registro con la fecha del reloj, persistir, guardar en el mapa y publicar CAMBIO_ESTADO.
        //Buscar al cliente
        Cliente cliente = cliente(idCliente);
        //Calcular el siguiente id
        int id = registros.values().stream().mapToInt(Venta::id).max().orElse(0) + 1;
        //Crear la venta con la fecha del reloj
        Venta nueva = new Venta(id, cliente, reloj.get());
        //Guardar la venta en SQLite
        repoRegistros.guardar(nueva);
        //Agregar la venta al mapa
        registros.put(nueva.id(), nueva);
        //Publicar el evento CAMBIO_ESTADO
        publicar(TipoEvento.CAMBIO_ESTADO, "Venta #" + nueva.id() + " abierta");
        return nueva;
        //throw new UnsupportedOperationException("TODO E15");
    }

    /**
     * Agrega un medicamento a una venta existente.
     *
     * <p>Localiza la venta y el producto mediante sus
     * identificadores y agrega una línea con la
     * cantidad de unidades solicitada.</p>
     *
     * <p>Guarda la venta actualizada en SQLite.</p>
     *
     * @param idVenta identificador de la venta.
     * @param clave clave SKU del medicamento.
     * @param unidades cantidad de piezas solicitadas.
     * @return línea creada dentro de la venta.
     * @throws ReglaNegocioException si la venta o el producto
     *         no existe, o si se incumple una regla de la venta.
     */

    public Venta.Linea agregarLinea(int idVenta, String clave, int unidades) {
        // TODO E15: Localizar registro y elemento, agregar la línea y persistir.
        //Localizar la venta
        Venta venta = registro(idVenta);
        //Localizar el producto
        Producto producto = elemento(clave);
        //Agregar el producto a la venta
        Venta.Linea linea = venta.agregar(producto, unidades);
        //Guardar la venta actualizada en SQLite
        repoRegistros.guardar(venta);
        return  linea;
        //throw new UnsupportedOperationException("TODO E15");
    }

    public boolean quitarLinea(int idVenta, String clave) {
        Venta t = registro(idVenta);
        boolean ok = t.quitar(Sku.parse(clave));
        repoRegistros.guardar(t);
        return ok;
    }

    public void iniciar(int idVenta) {
        transitar(idVenta, Venta::iniciar);
    }

    public void cerrar(int idVenta) {
        transitar(idVenta, Venta::cerrar);
    }

    public void cancelar(int idVenta) {
        transitar(idVenta, Venta::cancelar);
    }

    /** Orden superior: la transición concreta llega como {@link Consumer} (referencia a método). */
    private void transitar(int id, Consumer<Venta> transicion) {
        // TODO E15: Aplicar la transición, persistir y publicar CAMBIO_ESTADO.
        //Localizar la venta
        Venta venta = registro(id);
        //Aplicar la transicion de estado
        transicion.accept(venta);
        //Guardar los cambios en SQLite
        repoRegistros.guardar(venta);
        //Publicar el evento
        publicar(TipoEvento.CAMBIO_ESTADO,"Venta #" + venta.id() + " cambio a" + venta.estado());
        //throw new UnsupportedOperationException("TODO E15");
    }

    public Venta registro(int id) {
        return Optional.ofNullable(registros.get(id))
                .orElseThrow(() -> new ReglaNegocioException("No existe el registro " + id));
    }

    public List<Venta> registros() {
        return registros.values().stream().sorted(Comparator.comparingInt(Venta::id)).toList();
    }

    public List<Venta> registrosEn(Estado estado) {
        return registros().stream().filter(t -> t.estado() == estado).toList();
    }

    // ------------------------------------------------------------------ reportes (streams)

    /**
     * Cierra una venta que se encuentra en surtido.
     *
     * <p>Solicita la transición a CERRADA mediante
     * la referencia a método {@code Venta::cerrar}.</p>
     *
     * @throws ReglaNegocioException si la transición
     *         no está permitida o la venta no existe.
     */

    private Stream<Venta> cerrados() {
        return registros.values().stream().filter(t -> t.estado() == Estado.CERRADA);
    }

    /** Ingresos por subtipo (sólo registros cerrados). */
    public Map<String, Double> ingresosPorTipo() {
        // TODO E16: flatMap de líneas; groupingBy(tipo, TreeMap::new, summingDouble(importe)).
        return cerrados().flatMap(Venta -> Venta.lineas().stream()).collect(Collectors.groupingBy(linea -> linea.elemento().tipo(),TreeMap::new, Collectors.summingDouble(Venta.Linea::importe)));
        //throw new UnsupportedOperationException("TODO E16");
    }

    /** El producto con más unidades en registros cerrados. */
    public Optional<Producto> masDemandado() {
        // TODO E16: groupingBy(elemento, summingInt(unidades)) y máximo por valor con Map.Entry.comparingByValue().
        Map<Producto, Integer> unidadesPorProducto = cerrados().flatMap(Venta -> Venta.lineas().stream()).collect(Collectors.groupingBy(
                Venta.Linea::elemento, Collectors.summingInt(Venta.Linea::unidades)));
        //Encontrar el producto con mas unidades
        return unidadesPorProducto.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
        //throw new UnsupportedOperationException("TODO E16");
    }

    /** Los {@code n} clientes con mayor importe acumulado. */
    public List<Cliente> mejores(int n) {
        // TODO E16: groupingBy(actor, summingDouble(total)); ordenar descendentemente por valor; limit(n).
        Map<Cliente, Double> totales = cerrados().collect(Collectors.groupingBy(Venta::cliente, Collectors.summingDouble(Venta::total)));
        return totales.entrySet().stream().sorted(Map.Entry.<Cliente, Double>comparingByValue().reversed()).limit(n).map(Map.Entry::getKey).toList();
        //throw new UnsupportedOperationException("TODO E16");
    }

    public DoubleSummaryStatistics estadisticas() {
        // TODO E16: mapToDouble(Venta::total).summaryStatistics() sobre los cerrados.
        //obtener ventas cerradavertir cada venta en su importes com
        return cerrados().mapToDouble(Venta::total).summaryStatistics();
        //throw new UnsupportedOperationException("TODO E16");
    }

    public Map<Boolean, List<Producto>> particionarPorPrecio(double umbral) {
        // TODO E16: partitioningBy(e -> e.precioBase() >= umbral).
        return catalogo.ordenados(Comparator.comparing(Producto::clave)).stream().collect(Collectors.partitioningBy(e -> e.precioBase() >= umbral));
        //throw new UnsupportedOperationException("TODO E16");
    }

    public Map<Estado, Long> conteoPorEstado() {
        return registros.values().stream()
                .collect(Collectors.groupingBy(Venta::estado, () -> new TreeMap<>(), Collectors.counting()));
    }

    public List<Producto> buscar(Regla<? super Producto> regla) {
        return catalogo.filtrar(regla);
    }

    /** Proyección genérica (orden superior). */
    public <R> List<R> proyectar(Function<? super Producto, ? extends R> funcion) {
        // TODO E16: Recorrer el catálogo ordenado por clave y aplicar map(funcion).
        return catalogo.ordenados(Comparator.comparing(Producto::clave)).stream().<R>map(funcion).toList();
        //throw new UnsupportedOperationException("TODO E16");
    }

    /** Descripción con <em>pattern matching</em> exhaustivo sobre la jerarquía sellada (sin default). */
    public String describir(Producto e) {
        // TODO E16: switch (e) con un caso por subtipo y al menos un caso con guarda "when".
        return switch (e) {
            case Producto.Patente p -> "Patente: " + "Laboratorio: " + p.laboratorio();
            case Producto.Generico g when g.concentracionMg() > 500 -> "Generico de alta concentración: " + g.nombre();
            case Producto.Generico g -> "Generico: " + g.nombre() + "Concentración: " + g.concentracionMg() + "mg";
            case Producto.Biologico b -> "Biologico: " + b.nombre() + "Temperatura máxima: " + b.temperaturaMax() + "ºC";
        };
        //throw new UnsupportedOperationException("TODO E16");
    }

    // ------------------------------------------------------------------ monitor

    /**
     * Un ciclo de revisión (síncrono y probado de forma determinista):
     * cancela registros ABIERTOS sin líneas con al menos un día de antigüedad y emite una
     * ALERTA (una sola vez) por cada registro EN_CURSO con más de {@link #PLAZO_DIAS} días.
     *
     * @return número de acciones realizadas
     */
    public synchronized int revisar() {
        // TODO E12: Recorrer los registros y aplicar las dos reglas con un switch sobre el estado.
        int acciones = 0;
        LocalDate hoy = reloj.get();
        for (Venta venta : registros.values()){
            long dias = venta.diasDesde(hoy);
            switch (venta.estado()){
                case ABIERTA -> {
                    if (venta.lineas().isEmpty() && dias >= 1){
                        venta.cancelar();
                        repoRegistros.guardar(venta);
                        publicar(
                                TipoEvento.CANCELACION_AUTOMATICA,
                                "Venta #" + venta.id() + " cancelada"
                        );
                        acciones++;
                    }
                }
                case EN_CURSO -> {
                    if (dias > PLAZO_DIAS && alertados.add(venta.id())){
                        publicar(TipoEvento.ALERTA,"Venta #" + venta.id() + "vencida");
                        acciones++;
                    }
                }
                case CERRADA, CANCELADA -> {

                }
            }
        }
        return acciones;
        //throw new UnsupportedOperationException("TODO E12");
    }

    public synchronized void iniciarMonitor(long periodoMs) {
        // TODO E12: Crear el monitor interno (new MonitorSurtido(periodoMs)) e iniciarlo con un nombre de hilo descriptivo.
        if (monitor != null && monitor.activa()) {
            throw new IllegalStateException("El monitor ya está activo");
        }
        monitor = new MonitorSurtido(periodoMs);
        monitor.iniciar("Monitor surtido farmacia");
        //throw new UnsupportedOperationException("TODO E12");
    }

    public void detenerMonitor() throws InterruptedException {
        MonitorSurtido m;
        synchronized (this) {
            m = monitor;
        }
        if (m != null) {
            m.detener();
        }
    }

    /**
     * Consulta si el monitor de surtido está activo.
     *
     * @return {@code true} si existe un monitor
     *         en ejecución; {@code false} en otro caso.
     */

    public synchronized boolean monitorActivo() {
        return monitor != null && monitor.activa();
    }

    /**
     * Clase interna: ES UNA {@link TareaPeriodica} y TIENE ACCESO a la envolvente.
     */
    public final class MonitorSurtido extends TareaPeriodica {

        private MonitorSurtido(long periodoMs) {
            super(periodoMs);
        }

        @Override
        protected void ejecutarCiclo() {
            // TODO E12: Delegar en la envolvente: Farmacia.this.revisar().
            Farmacia.this.revisar();
            //throw new UnsupportedOperationException("TODO E12");
        }
    }

    // ------------------------------------------------------------------ respaldo

    /**
     * Exporta un respaldo binario de la información.
     *
     * <p>Utiliza la clase Respaldo para serializar
     * el catálogo de medicamentos, la lista de clientes
     * y las ventas registradas.</p>
     *
     * @param archivo ruta del archivo de destino.
     * @throws IOException si ocurre un error
     *         durante la escritura del respaldo.
     */

    public void exportarRespaldo(Path archivo) throws IOException {
        Respaldo.exportar(archivo, catalogo, clientes(), registros());
    }

/**
 * Libera los recursos asociados al monitor de surtido.
 *
 * <p>Solicita la detención del monitor cuando existe.
 * Si la espera es interrumpida, restablece el estado
 * de interrupción del hilo actual.</p>
 *
 * <p>Este método forma parte del contrato
 * {@link AutoCloseable}.</p>
 */

    /** Detiene el monitor (si está activo). Invocar al cerrar la aplicación. */
    @Override
    public void close() {
        try {
            detenerMonitor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
