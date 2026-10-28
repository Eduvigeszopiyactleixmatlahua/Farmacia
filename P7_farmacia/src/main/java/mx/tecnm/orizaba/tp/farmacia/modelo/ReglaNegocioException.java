package mx.tecnm.orizaba.tp.farmacia.modelo;

import java.io.Serial;

/**
 * Excepción no comprobada que señala la violación de una invariante del dominio.
 * <p>Encapsulamiento (Booch): los objetos protegen su estado rechazando valores inválidos.</p>
 */
public class ReglaNegocioException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * Crea una excepción por incumplimiento de una regla.
     * @param mensaje texto informativo del evento.
     */
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
