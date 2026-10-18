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
 * Fachada del dominio (capa de servicio). <strong>La UI sólo debe depender de esta clase</strong>
 * y de los tipos del paquete {@code modelo}/{@code eventos}; nunca de JDBC.
 * <ul>
 *   <li>Concurrencia: {@link MonitorSurtido} es una clase interna que <em>es una</em>
 *       {@link TareaPeriodica} y <em>tiene acceso</em> a {@code Farmacia.this}: herencia múltiple de
 *       implementación mediante clases internas (véase {@code J04_Multiple_De_Implementacion}).</li>
 *   <li>Programación funcional: reportes con streams y funciones de orden superior.</li>
 * </ul>
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

    public Farmacia(ConexionSQLite bd) {
        this(bd, LocalDate::now);
    }

    /** @param reloj proveedor de la fecha actual (inyectable para pruebas y simulación) */
    public Farmacia(ConexionSQLite bd, Supplier<LocalDate> reloj) {
        // TODO E15: Crear los tres repositorios SQLite, asignar el reloj y cargar los datos existentes en memoria.
        throw new UnsupportedOperationException("TODO E15");
    }

    private void cargar() {
        // TODO E15: Usar referencias a método: repoElementos.todos().forEach(catalogo::agregar), etc.
        throw new UnsupportedOperationException("TODO E15");
    }

    // ------------------------------------------------------------------ eventos

    public void suscribir(OyenteEvento oyente) {
        oyentes.add(oyente);
    }

    public void desuscribir(OyenteEvento oyente) {
        oyentes.remove(oyente);
    }

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
        throw new UnsupportedOperationException("TODO E15");
    }

    public void actualizar(Producto elemento, String nombre, double precio) {
        elemento.setNombre(nombre);
        elemento.setPrecioBase(precio);
        repoElementos.guardar(elemento);
    }

    public boolean retirar(Sku clave) {
        // TODO E15: Impedir retirar elementos referenciados por algún registro; eliminar de la BD y del catálogo.
        throw new UnsupportedOperationException("TODO E15");
    }

    public Anaquel catalogo() {
        return catalogo;
    }

    public Producto elemento(String clave) {
        return catalogo.buscar(Sku.parse(clave))
                .orElseThrow(() -> new ReglaNegocioException("No existe " + clave));
    }

    // ------------------------------------------------------------------ clientes

    public Cliente registrarCliente(String nombre, String correo, Nivel nivel) {
        // TODO E15: Rechazar correos repetidos; id = máximo actual + 1 (stream mapToInt/max); persistir y publicar ALTA.
        throw new UnsupportedOperationException("TODO E15");
    }

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

    public Venta abrir(int idCliente) {
        // TODO E15: Crear el registro con la fecha del reloj, persistir, guardar en el mapa y publicar CAMBIO_ESTADO.
        throw new UnsupportedOperationException("TODO E15");
    }

    public Venta.Linea agregarLinea(int idVenta, String clave, int unidades) {
        // TODO E15: Localizar registro y elemento, agregar la línea y persistir.
        throw new UnsupportedOperationException("TODO E15");
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
        throw new UnsupportedOperationException("TODO E15");
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

    private Stream<Venta> cerrados() {
        return registros.values().stream().filter(t -> t.estado() == Estado.CERRADA);
    }

    /** Ingresos por subtipo (sólo registros cerrados). */
    public Map<String, Double> ingresosPorTipo() {
        // TODO E16: flatMap de líneas; groupingBy(tipo, TreeMap::new, summingDouble(importe)).
        throw new UnsupportedOperationException("TODO E16");
    }

    /** El producto con más unidades en registros cerrados. */
    public Optional<Producto> masDemandado() {
        // TODO E16: groupingBy(elemento, summingInt(unidades)) y máximo por valor con Map.Entry.comparingByValue().
        throw new UnsupportedOperationException("TODO E16");
    }

    /** Los {@code n} clientes con mayor importe acumulado. */
    public List<Cliente> mejores(int n) {
        // TODO E16: groupingBy(actor, summingDouble(total)); ordenar descendentemente por valor; limit(n).
        throw new UnsupportedOperationException("TODO E16");
    }

    public DoubleSummaryStatistics estadisticas() {
        // TODO E16: mapToDouble(Venta::total).summaryStatistics() sobre los cerrados.
        throw new UnsupportedOperationException("TODO E16");
    }

    public Map<Boolean, List<Producto>> particionarPorPrecio(double umbral) {
        // TODO E16: partitioningBy(e -> e.precioBase() >= umbral).
        throw new UnsupportedOperationException("TODO E16");
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
        throw new UnsupportedOperationException("TODO E16");
    }

    /** Descripción con <em>pattern matching</em> exhaustivo sobre la jerarquía sellada (sin default). */
    public String describir(Producto e) {
        // TODO E16: switch (e) con un caso por subtipo y al menos un caso con guarda "when".
        throw new UnsupportedOperationException("TODO E16");
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

    public void exportarRespaldo(Path archivo) throws IOException {
        Respaldo.exportar(archivo, catalogo, clientes(), registros());
    }

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
