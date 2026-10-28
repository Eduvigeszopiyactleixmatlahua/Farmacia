package mx.tecnm.orizaba.tp.farmacia.modelo;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Representa una clave SKU única de un medicamento.
 *
 * <p>Es un objeto valor inmutable implementado mediante
 * un record, compuesto por un prefijo de tres letras
 * mayúsculas y un número entero positivo.</p>
 *
 * <p>Su representación textual utiliza el formato
 * {@code ABC-0001}.</p>
 *
 * <p>Implementa Comparable para ordenar claves y
 * Serializable para permitir su serialización.</p>
 *
 * <p>Administra secuencias de identificación mediante
 * ConcurrentHashMap y AtomicInteger, permitiendo
 * operaciones atómicas entre múltiples hilos.</p>
 *
 * @param prefijo tres letras mayúsculas que identifican
 *                el tipo de medicamento.
 * @param numero número positivo que identifica
 *               al producto dentro del prefijo.
 *
 * @see Comparable
 * @see Serializable
 * @see AtomicInteger
 */

public record Sku(String prefijo, int numero) implements Comparable<Sku>, Serializable {

    private static final Map<String, AtomicInteger> SECUENCIAS;

    static {
        // Inicialización estática: se ejecuta una sola vez, al primer uso activo de la clase.
        SECUENCIAS = new ConcurrentHashMap<>();
    }

    /**
     * Valida los componentes de una clave SKU.
     *
     * <p>Comprueba que el número sea mayor que cero
     * y que el prefijo contenga exactamente tres
     * letras mayúsculas de la A a la Z.</p>
     *
     * <p>Si alguno de los valores es inválido,
     * impide la construcción del objeto.</p>
     *
     * @param prefijo prefijo de tres letras mayúsculas.
     * @param numero identificador entero positivo.
     * @throws IllegalArgumentException si el prefijo
     *         es nulo, tiene un formato incorrecto
     *         o el número no es positivo.
     */

    public Sku {
        // TODO E03: Validar que el prefijo tenga exactamente 3 letras mayúsculas y que el número sea positivo (IllegalArgumentException).
        if (numero  <= 0) {
            throw new IllegalArgumentException(
                    "El numero ingresado debe ser positivo"
            );
        }
        if (prefijo == null || !prefijo.matches("[A-Z]{3}")){
            throw new IllegalArgumentException("Prefijo invalido");

        }
        //throw new UnsupportedOperationException("TODO E03");
    }

    /**
     * Genera la siguiente clave del prefijo indicado (operación atómica).
     * @param prefijo prefijo de tres letras del SKU.
     * @return nueva clave SKU.
     */
    public static Sku siguiente(String prefijo) {
        // TODO E03: Obtener (o crear) la secuencia del prefijo con computeIfAbsent e incrementarla atómicamente.
        AtomicInteger contador = SECUENCIAS.computeIfAbsent(prefijo,valorprefijo-> new AtomicInteger(0));
        //Incrementar el contador y obtener el nuevo número
        int numero = contador.incrementAndGet();
        // Construir y devolver el nuevo Sku
        return new  Sku(prefijo, numero);
        //throw new UnsupportedOperationException("TODO E03");
    }

    /** Ajusta la secuencia para que nunca genere una clave ya existente (p. ej., tras leer la BD). */
    public static void registrar(Sku clave) {
        // TODO E03: Actualizar la secuencia del prefijo con accumulateAndGet(numero, Math::max).
       AtomicInteger contador = SECUENCIAS.computeIfAbsent(clave.prefijo(), valorprefijo -> new AtomicInteger(0));
       contador.accumulateAndGet(clave.numero(), Math::max);
        //throw new UnsupportedOperationException("TODO E03");
    }
    /**
     * Convierte un texto {@code ABC-0001} en una clave.
     * @param texto texto para búsqueda o conversión.
     * @return clave SKU reconstruida.
     */
    public static Sku parse(String texto) {
        // TODO E03: Validar el formato con la expresión [A-Z]{3}-\d{4,} y construir la clave.

        if (texto == null || !texto.matches("[A-Z]{3}-\\d{4,}")) {
            throw new IllegalArgumentException("Formato incorrecto");
        }
        //Extraer prefijo
        String prefijo = texto.substring(0,3);
        //Extraer y convertir el número
        String partenumerica = texto.substring(4);
        //Convertir el String a int
        int numero = Integer.parseInt(partenumerica);
        //Contruir y devolver el objeto Sku
        return new Sku(prefijo, numero);
        //throw new UnsupportedOperationException("TODO E03");
    }

    @Override
    public int compareTo(Sku otra) {
        // TODO E03: Ordenar primero por prefijo y después por número.
        //Compara dos cadenas de texto alfabéticamente
        int comparacionprefijo = prefijo.compareTo(otra.prefijo());
        //Si son diferentes devuelve el resultado
        if (comparacionprefijo != 0){
            return comparacionprefijo;
        }
        //Si son iguales, compara los números
        return Integer.compare(numero, otra.numero());
        //throw new UnsupportedOperationException("TODO E03");
    }

    /**
     * Obtiene la representación textual de la clave SKU.
     *
     * <p>Combina el prefijo con el número utilizando
     * un guion como separador. El número se presenta
     * con al menos cuatro dígitos, agregando ceros
     * a la izquierda cuando es necesario.</p>
     *
     * @return clave SKU en formato {@code ABC-0001}.
     */

    @Override
    public String toString() {

        return String.format("%s-%04d", prefijo, numero);
    }
}
