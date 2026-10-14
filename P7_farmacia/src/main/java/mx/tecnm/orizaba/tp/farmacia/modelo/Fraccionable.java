package mx.tecnm.orizaba.tp.farmacia.modelo;

/**
 * Interfaz de capacidad (clasificación múltiple, véase {@code java_4_modelo/t4_jerarquia/t1_clasificacion}).
 * Declara un método por defecto {@code descripcionCorta()} que colisiona deliberadamente con el de la otra
 * capacidad; quien implemente ambas debe resolver la colisión con {@code Interfaz.super.descripcionCorta()}
 * (véase {@code java_5_lambdas/a_interfaces_default/Default3}).
 */
public interface Fraccionable {

    int dosisPorEnvase();

    default String descripcionCorta() {
        // TODO E05: Devolver "Dosis por envase: " concatenado con el valor de dosisPorEnvase().
        return "Dosis por envase: " + dosisPorEnvase();
        //throw new UnsupportedOperationException("TODO E05");
    }
}
