package mx.tecnm.orizaba.tp.farmacia.funcional;

import mx.tecnm.orizaba.tp.farmacia.modelo.Producto;

/**
 * Fábrica genérica acotada, diseñada para recibir <strong>referencias a constructor</strong>:
 * {@code FabricaElemento<Producto.Patente, String> f = Producto.Patente::new;}
 * (véase {@code java_5_lambdas/c_referencias_metodos/Referencias4}).
 *
 * @param <E> subtipo concreto de Producto
 * @param <A> tipo del atributo específico del subtipo
 */
@FunctionalInterface
public interface FabricaElemento<E extends Producto, A> {
    /**
     * Construye un medicamento con sus atributos.
     * @param nombre nombre del producto o cliente.
     * @param precioBase precio unitario del medicamento.
     * @param atributo dato específico del subtipo.
     * @return instancia construida.
     */
    E crear(String nombre, double precioBase, A atributo);
}
