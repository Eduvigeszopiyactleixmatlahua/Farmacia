package mx.tecnm.orizaba.tp.farmacia.modelo;

/**
 * Nivel del cliente; cada constante encapsula su etiqueta y su descuento.
 * <p>Tipificación fuerte: un {@code enum} es un tipo cerrado con estado y comportamiento.</p>
 */
public enum Nivel {
    GENERAL("General", 0.0),
    AFILIADO("Afiliado", 0.1),
    ADULTO_MAYOR("Adulto mayor", 0.2);

    private final String etiqueta;
    private final double descuento;

    Nivel(String etiqueta, double descuento) {
        this.etiqueta = etiqueta;
        this.descuento = descuento;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public double descuento() {
        return descuento;
    }

    /** Aplica el descuento del nivel a un monto, redondeando a 2 decimales. */
    public double aplicar(double monto) {
        // TODO E06: Restar el porcentaje de descuento al monto y redondear a 2 decimales.
        //Calcular el monto con descuento
        monto = monto - (monto * descuento);
        // Redondear ese resultado a 2 decimales
        monto = Math.round(monto * 100) / 100.0;
        //Devolver el resultado con return
        return monto;
        //throw new UnsupportedOperationException("TODO E06");
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
