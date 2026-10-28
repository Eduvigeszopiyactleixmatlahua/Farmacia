package mx.tecnm.orizaba.tp.farmacia.funcional;

import mx.tecnm.orizaba.tp.farmacia.modelo.Producto;

import java.util.Locale;

/**
 * Biblioteca de reglas reutilizables (clase utilitaria no instanciable).
 */
public final class Reglas {

    private Reglas() {
        throw new AssertionError("No instanciable");
    }
    /**
     * Crea una regla de precio máximo.
     * @param maximo precio máximo admitido.
     * @return condición reutilizable para filtrar productos.
     */
    public static Regla<Producto> precioHasta(double maximo) {
        // TODO E11: Lambda que acepta los elementos cuyo precioBase() sea menor o igual que maximo.
        return producto -> producto.precioBase()  <= maximo;
        //throw new UnsupportedOperationException("TODO E11");
    }
    /**
     * Crea una regla de búsqueda por nombre.
     * @param texto texto para búsqueda o conversión.
     * @return condición reutilizable para filtrar productos.
     */
    public static Regla<Producto> nombreContiene(String texto) {
        // TODO E11: Lambda insensible a mayúsculas/minúsculas sobre nombre().
        return producto -> producto.nombre().toLowerCase().contains(texto.toLowerCase());
        //throw new UnsupportedOperationException("TODO E11");
    }
/**
 * Elementos que implementan una capacidad (p. ej. {@code RequiereReceta.class}).
 * @param capacidad interfaz que debe implementar el producto.
 * @return condición reutilizable para filtrar productos.
 */
    public static Regla<Producto> conCapacidad(Class<?> capacidad) {
        // TODO E11: Referencia a método de instancia particular: capacidad::isInstance.
        return capacidad::isInstance;
        //throw new UnsupportedOperationException("TODO E11");
    }
}
