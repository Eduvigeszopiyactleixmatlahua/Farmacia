package mx.tecnm.orizaba.tp.farmacia.eventos;

/** Clasificación de los eventos publicados por el servicio. */
public enum TipoEvento {
    /** Registro de una entidad. */
    ALTA,
    /** Modificación del estado de una venta. */
    CAMBIO_ESTADO,
    /** Aviso de surtido demorado. */
    ALERTA,
    /** Cancelación automática por venta vacía. */
    CANCELACION_AUTOMATICA
}
