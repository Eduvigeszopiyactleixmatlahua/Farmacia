package mx.tecnm.orizaba.tp.farmacia.modelo;

import java.util.Comparator;

/**
 * Abstracción raíz del dominio: todo lo que puede venderse expone una clave,
 * un nombre, un precio base y una tarifa por unidad (piezas).
 * <p>Abstracción (Booch): define el <em>qué</em>, no el <em>cómo</em>
 * (véase {@code java_4_modelo/t1_abstraccion/Abstraccion03_interfaces}).</p>
 */
public interface Vendible {

    /** Constante de interfaz: implícitamente {@code public static final}. */
    double TASA_IMPUESTO = 0.16;
    /**
     * Obtiene la clave del producto.
     * @return clave SKU.
     */
    Sku clave();
    /**
     * Obtiene el nombre del producto.
     * @return nombre del producto.
     */
    String nombre();
    /**
     * Obtiene el precio base del producto.
     * @return precio unitario.
     */
    double precioBase();

    /** Tarifa (sin impuestos) para la cantidad de piezas indicada. */
    double tarifa(int unidades);

    /** Método por defecto (extensión de la interfaz sin romper implementaciones). */
    default double tarifaConImpuesto(int unidades) {
        // TODO E02: Aplicar TASA_IMPUESTO a tarifa(unidades) y redondear a 2 decimales.
        double calcularprecio = tarifa(unidades)*(1 + TASA_IMPUESTO);
        double dosdecimales = Math.round(calcularprecio * 100)/100.0;
        return dosdecimales;
        //throw new UnsupportedOperationException("TODO E02");
    }

    /** Método estático de interfaz: comparador por nombre (referencia a método de objeto arbitrario). */
    static Comparator<Vendible> porNombre() {
        // TODO E02: Usar Comparator.comparing(Vendible::nombre, String.CASE_INSENSITIVE_ORDER).
        return Comparator.comparing(Vendible::nombre, String.CASE_INSENSITIVE_ORDER);
        //throw new UnsupportedOperationException("TODO E02");

    }
}
