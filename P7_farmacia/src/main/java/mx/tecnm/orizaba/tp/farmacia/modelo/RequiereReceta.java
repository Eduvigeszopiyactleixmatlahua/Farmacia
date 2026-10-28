package mx.tecnm.orizaba.tp.farmacia.modelo;

/**
 * Interfaz de capacidad (clasificación múltiple, véase {@code java_4_modelo/t4_jerarquia/t1_clasificacion}).
 * Declara un método por defecto {@code descripcionCorta()} que colisiona deliberadamente con el de la otra
 * capacidad; quien implemente ambas debe resolver la colisión con {@code Interfaz.super.descripcionCorta()}
 * (véase {@code java_5_lambdas/a_interfaces_default/Default3}).
 */
public interface RequiereReceta {
    /**
     * Obtiene el tipo de receta requerido.
     * @return tipo de receta requerida.
     */
    String tipoReceta();
    /**
     * Describe la receta necesaria.
     * @return descripción breve de la capacidad.
     */
    default String descripcionCorta() {
        // TODO E05: Devolver "Receta: " concatenado con el valor de tipoReceta().
        return "Receta: " + tipoReceta();
        //throw new UnsupportedOperationException("TODO E05");
    }
}
