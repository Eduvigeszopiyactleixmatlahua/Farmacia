package mx.tecnm.orizaba.tp.farmacia.modelo;

/**
 * Ciclo de vida de un(a) venta: máquina de estados finita.
 * <pre>ABIERTA → EN_CURSO → CERRADA;  ABIERTA|EN_CURSO → CANCELADA</pre>
 */
public enum Estado {
    ABIERTA("Abierta"), EN_CURSO("En surtido"), CERRADA("Pagada"), CANCELADA("Cancelada");

    private final String etiqueta;

    Estado(String etiqueta) {
        this.etiqueta = etiqueta;
    }
    /**
     * Obtiene el nombre visible del estado.
     * @return etiqueta legible.
     */
    public String etiqueta() {
        return etiqueta;
    }

    /** Indica si la transición hacia {@code destino} es válida (switch como expresión). */
    public boolean permiteTransicionA(Estado destino) {
        // TODO E07: Implementar con switch-expresión las transiciones del diagrama; los estados terminales no permiten ninguna.
        return switch (this) {
            case ABIERTA -> destino == EN_CURSO || destino == CANCELADA;
            case EN_CURSO -> destino == CERRADA || destino == CANCELADA;
            case CERRADA, CANCELADA -> false;
        };
        //throw new UnsupportedOperationException("TODO E07");
    }

    public boolean esTerminal() {
        return this == CERRADA || this == CANCELADA;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
