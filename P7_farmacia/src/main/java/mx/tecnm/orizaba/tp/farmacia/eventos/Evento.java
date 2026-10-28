package mx.tecnm.orizaba.tp.farmacia.eventos;

import java.time.LocalDateTime;
import java.util.Objects;
/**
 * Evento inmutable del dominio.
 * @param tipo tipo de evento o subtipo solicitado.
 * @param mensaje texto informativo del evento.
 * @param instante fecha y hora del evento.
 */
public record Evento(TipoEvento tipo, String mensaje, LocalDateTime instante) {
    /**
     * Atributo interno del componente.
     */
    public Evento {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(mensaje, "mensaje");
        Objects.requireNonNull(instante, "instante");
    }

    /**
     * Fábrica estática (útil como referencia a método estático).
     * @param tipo tipo de evento o subtipo solicitado.
     * @param mensaje texto informativo del evento.
     * @return evento creado o regla adaptada.
     */

    public static Evento de(TipoEvento tipo, String mensaje) {
        return new Evento(tipo, mensaje, LocalDateTime.now());
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", tipo, mensaje);
    }
}
