package mx.tecnm.orizaba.tp.farmacia.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Venta: agregado con ciclo de vida ({@link Estado}) compuesto por líneas.
 * <ul>
 *   <li>Composición: {@link Linea} es una clase <strong>interna</strong> (no estática) con constructor
 *       privado; sólo existe dentro de un(a) venta y accede a su envolvente con
 *       {@code Venta.this} (véase {@code java_3_tipos_internos/t1_fundamentos/Parcel01..04}).</li>
 *   <li>Concurrencia: los métodos que cambian estado están sincronizados porque el monitor
 *       los consulta desde otro hilo.</li>
 *   <li>Persistencia: serializar un objeto de clase interna exige que la envolvente sea serializable
 *       (véase {@code SerializaClasesInternas_08}).</li>
 * </ul>
 */
public class Venta implements Iterable<Venta.Linea>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final AtomicInteger SECUENCIA = new AtomicInteger();

    private final int id;
    private final Cliente cliente;
    private final LocalDate fecha;
    private Estado estado;
    private final List<Linea> lineas = new ArrayList<>();

    {
        estado = Estado.ABIERTA;
    }

    /** Nuevo(a) venta con identificador consecutivo y fecha de hoy. */
    public Venta(Cliente cliente) {
        this(SECUENCIA.incrementAndGet(), cliente, LocalDate.now());
    }

    /** Constructor completo (también usado al reconstruir desde la base de datos). */
    public Venta(int id, Cliente cliente, LocalDate fecha) {
        // TODO E07: Validar id > 0, actor y fecha no nulos; asignar y sincronizar SECUENCIA con accumulateAndGet(id, Math::max).
        if ( id  <= 0){
            throw  new ReglaNegocioException("El identificador es invalido");
        }
        //Validar referencias
        Objects.requireNonNull(cliente);
        Objects.requireNonNull(fecha);
        //Asignar atributos
        this.id = id;
        this.cliente = cliente;
        this.fecha = fecha;
        //Actualiza el valor de AtomicInteger con accumulateAndGet
        SECUENCIA.accumulateAndGet(id, Math::max);
        //throw new UnsupportedOperationException("TODO E07");
    }

    public int id() {
        return id;
    }

    public Cliente cliente() {
        return cliente;
    }

    public LocalDate fecha() {
        return fecha;
    }

    public synchronized Estado estado() {
        return estado;
    }

    /** Agrega una línea; sólo es posible mientras está ABIERTA y sin repetir producto. */
    public synchronized Linea agregar(Producto elemento, int unidades) {
        // TODO E07: Exigir estado ABIERTA, rechazar elementos repetidos (stream anyMatch), crear la Linea interna y añadirla.
        //Validar el estado
        if (estado != Estado.ABIERTA){
            throw new ReglaNegocioException("La venta no está abierta");
        }
        //Comprobar productos repetidos
        boolean repetido = lineas.stream().anyMatch(linea -> linea.elemento().clave().equals(elemento.clave()));
        //Rechazar si esta repetido
        if (repetido){
            throw new ReglaNegocioException("Producto repetido");
        }
        //Crear y añadir linea pendiente
        Linea nuevalinea = new Linea(elemento, unidades);
        //Agregar la lista
        lineas.add(nuevalinea);
        //Devolver linea
        return nuevalinea;
        //throw new UnsupportedOperationException("TODO E07");
    }

    /** Quita la línea del producto indicado (sólo en estado ABIERTA). */
    public synchronized boolean quitar(Sku clave) {
        // TODO E07: Exigir estado ABIERTA y eliminar con removeIf la línea cuyo elemento tenga esa clave.
        //Validar que la venta esté ABIERTA
        if (estado != Estado.ABIERTA){
            throw new ReglaNegocioException("La venta no está abierta");
        }
        //Eliminar la línea cuya clave coincida
        return lineas.removeIf(linea -> linea.elemento().clave().equals(clave));

        //throw new UnsupportedOperationException("TODO E07");
    }

    public synchronized void cambiarEstado(Estado destino) {
        // TODO E07: Validar la transición con permiteTransicionA y exigir al menos una línea para pasar a EN_CURSO.
        //Validar la transición
        if (!estado.permiteTransicionA(destino)){
            throw new ReglaNegocioException("Transición no permitida");
        }

        if(destino == Estado.EN_CURSO && lineas.isEmpty()){
           throw new ReglaNegocioException("La venta no tiene productos");
        }
        //Actualizar el estado
        this.estado = destino;
        //throw new UnsupportedOperationException("TODO E07");
    }

    public void iniciar() {
        cambiarEstado(Estado.EN_CURSO);
    }

    public void cerrar() {
        cambiarEstado(Estado.CERRADA);
    }

    public void cancelar() {
        cambiarEstado(Estado.CANCELADA);
    }

    /** Uso exclusivo de la capa de persistencia al reconstruir el objeto. */
    public synchronized void restaurarEstado(Estado estado) {
        this.estado = Objects.requireNonNull(estado);
    }

    public synchronized List<Linea> lineas() {
        return Collections.unmodifiableList(new ArrayList<>(lineas));
    }

    /** Suma de subtotales sin descuento. */
    public synchronized double subtotal() {
        // TODO E08: Sumar los subtotales de las líneas con mapToDouble y redondear a 2 decimales.
        //Suma los subtotales de cada línea
        double suma = lineas.stream().mapToDouble(linea -> linea.subtotal()).sum();
        //Redondear a dos decimales
        return redondear(suma);
        //throw new UnsupportedOperationException("TODO E08");
    }

    /** Total con el descuento del nivel del cliente. */
    public synchronized double total() {
        // TODO E08: Sumar los importes de las líneas (ya con descuento) y redondear a 2 decimales.

        double suma = lineas.stream().mapToDouble(linea -> linea.importe()).sum();
        return redondear(suma);
        //throw new UnsupportedOperationException("TODO E08");
    }

    public long diasDesde(LocalDate hoy) {
        return ChronoUnit.DAYS.between(fecha, hoy);
    }

    @Override
    public Iterator<Linea> iterator() {
        return lineas().iterator();
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return String.format("Venta #%d [%s] %s, %d línea(s), total $%.2f",
                id, estado(), cliente.nombre(), lineas().size(), total());
    }

    /**
     * Línea de venta: clase interna (composición). Su constructor es privado, de modo que
     * sólo la envolvente puede crearla; accede al estado de la envolvente con {@code Venta.this}.
     */
    public final class Linea implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final Producto elemento;
        private final int unidades;

        private Linea(Producto elemento, int unidades) {
            // TODO E07: Validar unidades > 0 y asignar los campos.
            if (unidades  <= 0){
                throw new ReglaNegocioException("Unidades inválidas");
            }
            this.elemento = Objects.requireNonNull(elemento);
            this.unidades = unidades;
            //throw new UnsupportedOperationException("TODO E07");
        }

        public Producto elemento() {
            return elemento;
        }

        public int unidades() {
            return unidades;
        }

        public double subtotal() {
            return elemento.tarifa(unidades);
        }

        /** Importe con el descuento del nivel del cliente de la envolvente. */
        public double importe() {
            // TODO E08: Usar Venta.this.cliente.nivel().aplicar(subtotal()).
            return Venta.this.cliente.nivel().aplicar(subtotal());
            //throw new UnsupportedOperationException("TODO E08");
        }

        public Venta contenedor() {
            return Venta.this;
        }

        @Override
        public String toString() {
            return String.format("%s x %d piezas = $%.2f", elemento.nombre(), unidades, importe());
        }
    }
}
