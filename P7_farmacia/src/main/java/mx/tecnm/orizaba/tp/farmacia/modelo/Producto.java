package mx.tecnm.orizaba.tp.farmacia.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase abstracta <strong>sellada</strong> que factoriza el estado y el comportamiento común de todo
 * producto. Sus tres subtipos se declaran como clases anidadas estáticas finales
 * (véase {@code java_4_modelo/t1_abstraccion/Abstraccion04_anidadas}).
 * <ul>
 *   <li>Abstracción + Jerarquía: patrón <em>Template Method</em> ({@link #tarifa(int)} es final y delega en
 *       {@link #calcularTarifa(int)}).</li>
 *   <li>Proceso: bloque estático, bloque de instancia y encadenamiento {@code this(...)}/{@code super(...)}
 *       (véase {@code java_2_proceso/Proceso03}, {@code Proceso09}).</li>
 *   <li>Persistencia: {@code Serializable} con un campo {@code transient}
 *       (véase {@code java_4_modelo/t7_persistencia/a_serializacion/NoSerializar_09}).</li>
 * </ul>
 */
public abstract sealed class Producto implements Vendible, Serializable
        permits Producto.Patente, Producto.Generico, Producto.Biologico {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final AtomicInteger INSTANCIAS_CREADAS;

    static {
        INSTANCIAS_CREADAS = new AtomicInteger();
    }

    private final Sku clave;
    private String nombre;
    private double precioBase;
    private final LocalDateTime creadoEn;
    /** No se serializa: tras recuperar un respaldo vuelve a 0. */
    private transient int cotizaciones;

    {
        // Bloque de inicialización de instancia: se ejecuta antes del cuerpo de cualquier constructor.
        creadoEn = LocalDateTime.now();
        INSTANCIAS_CREADAS.incrementAndGet();
    }

    /** Constructor para objetos nuevos: genera la clave y encadena con {@code this(...)}. */
    protected Producto(String prefijo, String nombre, double precioBase) {
        this(Sku.siguiente(prefijo), nombre, precioBase);
    }

    /** Constructor para objetos reconstruidos (persistencia) con clave conocida. */
    protected Producto(Sku clave, String nombre, double precioBase) {
        // TODO E04: Asignar la clave (no nula), registrarla en Sku.registrar y validar nombre y precio mediante los setters.
        throw new UnsupportedOperationException("TODO E04");
    }

    public static int instanciasCreadas() {
        return INSTANCIAS_CREADAS.get();
    }

    @Override
    public final Sku clave() {
        return clave;
    }

    @Override
    public final String nombre() {
        return nombre;
    }

    @Override
    public final double precioBase() {
        return precioBase;
    }

    public final LocalDateTime creadoEn() {
        return creadoEn;
    }

    public final int cotizaciones() {
        return cotizaciones;
    }

    public final void setNombre(String nombre) {
        // TODO E04: Rechazar nombres nulos o en blanco (ReglaNegocioException); guardar el nombre sin espacios extremos.
        throw new UnsupportedOperationException("TODO E04");
    }

    public final void setPrecioBase(double precioBase) {
        // TODO E04: Rechazar precios negativos, NaN o infinitos (ReglaNegocioException).
        throw new UnsupportedOperationException("TODO E04");
    }

    /** Método plantilla: valida, contabiliza y redondea; la variación la aporta cada subtipo. */
    @Override
    public final double tarifa(int unidades) {
        // TODO E04: Validar unidades > 0 (ReglaNegocioException), incrementar cotizaciones y devolver calcularTarifa redondeada a 2 decimales.
        throw new UnsupportedOperationException("TODO E04");
    }

    /** Punto de variación del Template Method. */
    protected abstract double calcularTarifa(int unidades);

    /** Copia con clave nueva; los subtipos la redefinen con <strong>retorno covariante</strong>. */
    public abstract Producto copiar();

    /** Nombre corto del subtipo (útil para la UI y la persistencia). */
    public final String tipo() {
        return getClass().getSimpleName();
    }

    @Override
    public final boolean equals(Object o) {
        return this == o || (o instanceof Producto otro && clave.equals(otro.clave));
    }

    @Override
    public final int hashCode() {
        return clave.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s[%s, %s, $%.2f]", tipo(), clave, nombre, precioBase);
    }

    /**
     * Patente: precio por pieza.
     * <p>Subtipo de Producto declarado como clase anidada estática final; implementa RequiereReceta.</p>
     */
    public static final class Patente extends Producto implements RequiereReceta {
        @Serial
        private static final long serialVersionUID = 1L;

        public static final String PREFIJO = "PAT";

        private final String laboratorio;

        public Patente(String nombre, double precioBase, String laboratorio) {
            super(PREFIJO, nombre, precioBase);
            this.laboratorio = validar(laboratorio);
        }

        public Patente(Sku clave, String nombre, double precioBase, String laboratorio) {
            super(clave, nombre, precioBase);
            this.laboratorio = validar(laboratorio);
        }

        private static String validar(String laboratorio) {
            // TODO E05: Validar: laboratorio != null && !laboratorio.isBlank() (si no se cumple: ReglaNegocioException "El laboratorio es obligatorio").
            throw new UnsupportedOperationException("TODO E05");
        }

        public String laboratorio() {
            return laboratorio;
        }

        @Override
        protected double calcularTarifa(int unidades) {
            // TODO E05: Patente: precio por pieza. Fórmula: precioBase() * unidades
            throw new UnsupportedOperationException("TODO E05");
        }

        /** Retorno covariante: devuelve Patente, no Producto. */
        @Override
        public Patente copiar() {
            // TODO E05: Crear un nuevo Patente con el mismo nombre, precio y atributo (clave nueva).
            throw new UnsupportedOperationException("TODO E05");
        }

        @Override
        public String tipoReceta() {
            return "Receta simple";
        }

        @Override
        public String toString() {
            return super.toString() + " {laboratorio=" + laboratorio + "}";
        }
    }

    /**
     * Genérico: 10 % adicional si supera 500 mg.
     * <p>Subtipo de Producto declarado como clase anidada estática final; implementa Fraccionable.</p>
     */
    public static final class Generico extends Producto implements Fraccionable {
        @Serial
        private static final long serialVersionUID = 1L;

        public static final String PREFIJO = "GEN";

        private final int concentracionMg;

        public Generico(String nombre, double precioBase, int concentracionMg) {
            super(PREFIJO, nombre, precioBase);
            this.concentracionMg = validar(concentracionMg);
        }

        public Generico(Sku clave, String nombre, double precioBase, int concentracionMg) {
            super(clave, nombre, precioBase);
            this.concentracionMg = validar(concentracionMg);
        }

        private static int validar(int concentracionMg) {
            // TODO E05: Validar: concentracionMg > 0 (si no se cumple: ReglaNegocioException "La concentración debe ser positiva").
            throw new UnsupportedOperationException("TODO E05");
        }

        public int concentracionMg() {
            return concentracionMg;
        }

        @Override
        protected double calcularTarifa(int unidades) {
            // TODO E05: Genérico: 10 % adicional si supera 500 mg. Fórmula: precioBase() * unidades * (concentracionMg > 500 ? 1.1 : 1.0)
            throw new UnsupportedOperationException("TODO E05");
        }

        /** Retorno covariante: devuelve Generico, no Producto. */
        @Override
        public Generico copiar() {
            // TODO E05: Crear un nuevo Generico con el mismo nombre, precio y atributo (clave nueva).
            throw new UnsupportedOperationException("TODO E05");
        }

        @Override
        public int dosisPorEnvase() {
            return 30;
        }

        @Override
        public String toString() {
            return super.toString() + " {concentración (mg)=" + concentracionMg + "}";
        }
    }

    /**
     * Biológico: 8 % de descuento a partir de 10 piezas.
     * <p>Subtipo de Producto declarado como clase anidada estática final; implementa RequiereReceta, Fraccionable.</p>
     */
    public static final class Biologico extends Producto implements RequiereReceta, Fraccionable {
        @Serial
        private static final long serialVersionUID = 1L;

        public static final String PREFIJO = "BIO";

        private final int temperaturaMax;

        public Biologico(String nombre, double precioBase, int temperaturaMax) {
            super(PREFIJO, nombre, precioBase);
            this.temperaturaMax = validar(temperaturaMax);
        }

        public Biologico(Sku clave, String nombre, double precioBase, int temperaturaMax) {
            super(clave, nombre, precioBase);
            this.temperaturaMax = validar(temperaturaMax);
        }

        private static int validar(int temperaturaMax) {
            // TODO E05: Validar: temperaturaMax >= 2 && temperaturaMax <= 8 (si no se cumple: ReglaNegocioException "La temperatura de conservación debe estar entre 2 y 8 °C").
            throw new UnsupportedOperationException("TODO E05");
        }

        public int temperaturaMax() {
            return temperaturaMax;
        }

        @Override
        protected double calcularTarifa(int unidades) {
            // TODO E05: Biológico: 8 % de descuento a partir de 10 piezas. Fórmula: unidades >= 10 ? precioBase() * unidades * 0.92 : precioBase() * unidades
            throw new UnsupportedOperationException("TODO E05");
        }

        /** Retorno covariante: devuelve Biologico, no Producto. */
        @Override
        public Biologico copiar() {
            // TODO E05: Crear un nuevo Biologico con el mismo nombre, precio y atributo (clave nueva).
            throw new UnsupportedOperationException("TODO E05");
        }

        @Override
        public String tipoReceta() {
            return "Receta retenida";
        }

        @Override
        public int dosisPorEnvase() {
            return 10;
        }

        /** Resolución explícita de la colisión entre métodos por defecto. */
        @Override
        public String descripcionCorta() {
            // TODO E05: Combinar RequiereReceta.super.descripcionCorta() y Fraccionable.super.descripcionCorta() separados por " | ".
            throw new UnsupportedOperationException("TODO E05");
        }

        @Override
        public String toString() {
            return super.toString() + " {temperatura máxima (°C)=" + temperaturaMax + "}";
        }
    }
}
