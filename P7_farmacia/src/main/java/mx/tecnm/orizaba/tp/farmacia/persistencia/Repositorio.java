package mx.tecnm.orizaba.tp.farmacia.persistencia;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico de repositorio (patrón Repository).
 * Contiene un <strong>tipo miembro de interfaz</strong> ({@link MapeadorFila}), implícitamente
 * {@code public static} (véase {@code java_3_tipos_internos/t2_jerarquias/J05_Tipos_Miembro_Interfaz}).
 *
 * @param <T> entidad
 * @param <K> clave
 */
public interface Repositorio<T, K> {
    /**
     * Guarda una entidad.
     * @param entidad entidad que se almacenará.
     */
    void guardar(T entidad);
    /**
     * Busca una entidad por su clave.

     * @param clave clave que identifica al registro.
     * @return productos o entidad coincidentes, según el repositorio.
     */
    Optional<T> buscar(K clave);

    List<T> todos();
    /**
     * Elimina una entidad por su clave.
     * @param clave clave que identifica al registro.
     * @return {@code true} si se eliminó la entidad.
     */
    boolean eliminar(K clave);
    /**
     * Comprueba la existencia de una entidad.
     * @param clave clave que identifica al registro.
     * @return {@code true} si existe.
     */
    default boolean existe(K clave) {
        return buscar(clave).isPresent();
    }
    /**
     * Guarda las entidades de un iterable.
     * @param entidades colección de entidades a guardar.
     */
    default void guardarTodos(Iterable<? extends T> entidades) {
        entidades.forEach(this::guardar);
    }
/** Convierte la fila actual de un {@link ResultSet} en un objeto. */
    /**
     * Convierte resultados SQL a objetos.
     * @param <R> tipo del resultado.
     */
    @FunctionalInterface
    interface MapeadorFila<R> {
        /**
         * Transforma una fila SQL en una entidad.
         * @param rs fila actual del resultado JDBC.
         * @return entidad reconstruida desde la fila SQL.
         * @throws SQLException si la operación no puede completarse.
         */
        R mapear(ResultSet rs) throws SQLException;
    }
}
