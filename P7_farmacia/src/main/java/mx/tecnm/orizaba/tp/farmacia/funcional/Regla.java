package mx.tecnm.orizaba.tp.farmacia.funcional;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Interfaz funcional propia (un solo método abstracto) con métodos por defecto de composición
 * (véase {@code java_5_lambdas/b_interfaces_funcionales} y {@code d_orden_superior/Superior2}).
 *
 * @param <T> tipo evaluado
 */
@FunctionalInterface
public interface Regla<T> {
    /**
     * Evalúa si el elemento cumple la condición.
     * @param t elemento que será evaluado.
     * @return {@code true} cuando cumple la condición.
     */
    boolean evaluar(T t);
    /**
     * Combina esta regla con otra mediante AND.

     * @param otra regla que se combinará.
     * @return regla que exige ambas condiciones.
     */
    default Regla<T> y(Regla<? super T> otra) {
        // TODO E11: Devolver la lambda t -> evaluar(t) && otra.evaluar(t) (validar otra no nula).
        //Validar que otra no sea null
        Objects.requireNonNull(otra, "La regla no puede ser nula");
        //Devulve lambda con el parametro t
        return t -> evaluar(t) && otra.evaluar(t);
        //throw new UnsupportedOperationException("TODO E11");
    }
    /**
     * Combina esta regla con otra mediante OR.

     * @param otra regla que se combinará.
     * @return regla que acepta cualquiera de las condiciones.
     */
    default Regla<T> o(Regla<? super T> otra) {
        // TODO E11: Devolver la lambda t -> evaluar(t) || otra.evaluar(t).
        //Validar que otra no sea null
        Objects.requireNonNull(otra, "La regla no puede ser nula");
        //Devuelve lambda con el parametro t usando or
        return t -> evaluar(t) || otra.evaluar(t);
        //throw new UnsupportedOperationException("TODO E11");
    }
    /**
     * Invierte el resultado de esta regla.

     * @return regla con resultado invertido.
     */
    default Regla<T> negar() {
        // TODO E11: Devolver la lambda t -> !evaluar(t).
        return t -> !evaluar(t);
        //throw new UnsupportedOperationException("TODO E11");
    }

    /** Adaptador hacia {@link Predicate} mediante referencia a método de instancia particular. */
    default Predicate<T> comoPredicado() {
        return this::evaluar;
    }

    static <T> Regla<T> todas() {
        return t -> true;
    }
    /**
     * Adaptador desde {@link Predicate}.
     * @param <T> tipo de elemento evaluado.
     * @param p predicado utilizado para crear la regla.
     * @return evento creado o regla adaptada.
     */
    static <T> Regla<T> de(Predicate<? super T> p) {
        return p::test;
    }
}
