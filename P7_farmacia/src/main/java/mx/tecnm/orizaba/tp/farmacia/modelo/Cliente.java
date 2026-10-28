package mx.tecnm.orizaba.tp.farmacia.modelo;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Representa un cliente registrado en la farmacia.
 *
 * <p>Almacena su identificador, nombre, correo
 * electrónico y nivel de descuento.</p>
 *
 * <p>Implementa {@link Externalizable} para controlar
 * manualmente la escritura y lectura de sus atributos
 * durante la externalización.</p>
 *
 * <p>La identidad de los clientes se determina
 * mediante su identificador numérico.</p>
 *
 * @see Nivel
 * @see Externalizable
 */

public class Cliente implements Externalizable {

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private int id;
    private String nombre;
    private String correo;
    private Nivel nivel;

    /** Requerido por la externalización: se invoca antes de readExternal. */
    public Cliente() {
    }

    /**
     * Construye un cliente con sus datos y nivel de descuento.
     *
     * <p>Valida que el identificador sea positivo
     * y utiliza los métodos modificadores para
     * validar el nombre, correo y nivel.</p>
     *
     * @param id identificador positivo del cliente.
     * @param nombre nombre del cliente.
     * @param correo correo electrónico del cliente.
     * @param nivel nivel de descuento asignado.
     * @throws ReglaNegocioException si el identificador,
     *         nombre o correo es inválido.
     * @throws NullPointerException si el nivel es nulo.
     */

    public Cliente(int id, String nombre, String correo, Nivel nivel) {
        // TODO E06: Validar id > 0 y asignar nombre, correo y nivel mediante sus setters.
        //Validar que id sea mayor que cero
        if(id   <= 0){
            throw new ReglaNegocioException("El id debe ser positivo");
        }
        //Asignar el id al atributo del objeto
        this.id = id;
        //Asignar nombre mediante setNombre()
        setNombre(nombre);
        //Asignar correo mediante setCorreo()
        setCorreo(correo);
        //Asignar nivel mediante setNivel()
        setNivel(nivel);
        //throw new UnsupportedOperationException("TODO E06");
    }

/**
 * Construye un cliente con el nivel predeterminado.
 *
 * @param id identificador positivo del cliente.
 * @param nombre nombre del cliente.
 * @param correo correo electrónico del cliente.
 */

    /** Encadenamiento de constructores: nivel por omisión. */
    public Cliente(int id, String nombre, String correo) {
        this(id, nombre, correo, Nivel.values()[0]);
    }

    /**
     * Obtiene el identificador del cliente.
     *
     * @return identificador numérico del cliente.
     */

    public int id() {
        return id;
    }

    /**
     * Obtiene el nombre del cliente.
     *
     * @return nombre del cliente.
     */

    public String nombre() {
        return nombre;
    }

    /**
     * Obtiene el correo electrónico del cliente.
     *
     * @return correo electrónico registrado.
     */

    public String correo() {
        return correo;
    }

    /**
     * Obtiene el nivel de descuento del cliente.
     *
     * @return nivel actual del cliente.
     */

    public Nivel nivel() {
        return nivel;
    }

    /**
     * Establece el nombre del cliente.
     *
     * <p>Comprueba que el nombre recibido no sea nulo
     * ni esté formado únicamente por espacios en blanco.</p>
     *
     * @param nombre nuevo nombre del cliente.
     * @throws ReglaNegocioException si el nombre
     *         es nulo o está en blanco.
     */

    public final void setNombre(String nombre) {
        // TODO E06: Rechazar nombre nulo o en blanco.
        //Comprobar si es nulo o está en blanco
        if (nombre == null || nombre.isBlank()){
            //Excepción
            throw new ReglaNegocioException("Verificar nombre");
        }
        this.nombre = nombre;
        //throw new UnsupportedOperationException("TODO E06");
    }


    /**
     * Establece el correo electrónico del cliente.
     *
     * <p>Valida el correo utilizando el patrón
     * definido en la constante CORREO.</p>
     *
     * <p>Cuando el formato es válido, convierte
     * la dirección a minúsculas antes de almacenarla.</p>
     *
     * @param correo nuevo correo electrónico.
     * @throws ReglaNegocioException si el correo es nulo
     *         o no cumple el formato establecido.
     */

    public final void setCorreo(String correo) {
        // TODO E06: Validar el correo con el patrón CORREO y guardarlo en minúsculas.
        if(correo == null || !CORREO.matcher(correo).matches()){
            throw new ReglaNegocioException("Correo invalido");
        }
        //CConvertir correo a minusculas y guardarlo en el atributo del objeto
        this.correo = correo.toLowerCase();
        //throw new UnsupportedOperationException("TODO E06");
    }

    /**
     * Modifica el nivel de descuento del cliente.
     *
     * <p>Utiliza Objects.requireNonNull para impedir
     * que el nivel asignado sea nulo.</p>
     *
     * @param nivel nuevo nivel de descuento.
     * @throws NullPointerException si el nivel es nulo.
     */

    public final void setNivel(Nivel nivel) {
        this.nivel = Objects.requireNonNull(nivel, "nivel");
    }

    @Override
    public void writeExternal(ObjectOutput salida) throws IOException {
        // TODO E14: Escribir id, nombre, correo y el nombre del nivel (writeInt / writeUTF).
        salida.writeInt(id);
        //Escribir el nombre
        salida.writeUTF(nombre);
        //Escribir el correo
        salida.writeUTF(correo);
        //Escribir el nombre de la constante Nivel
        salida.writeUTF(nivel.name());
        //throw new UnsupportedOperationException("TODO E14");
    }

    /**
     * Reconstruye los atributos de un cliente externalizado.
     *
     * <p>Lee los valores en el mismo orden utilizado
     * por {@link #writeExternal(ObjectOutput)}.</p>
     *
     * <p>Valida que el identificador sea positivo
     * y utiliza los setters para comprobar
     * el nombre, correo y nivel.</p>
     *
     * <p>Reconstruye el nivel mediante Nivel.valueOf.</p>
     *
     * @param entrada flujo del que se recuperan los datos.
     * @throws IOException si ocurre un error durante la lectura.
     * @throws ReglaNegocioException si el identificador,
     *         nombre o correo es inválido.
     * @throws IllegalArgumentException si el nombre
     *         del nivel no corresponde a una constante válida.
     */

    @Override
    public void readExternal(ObjectInput entrada) throws IOException {
        // TODO E14: Leer en el mismo orden en que se escribió y reconstruir el nivel con Nivel.valueOf.
        //Leer el identificar
        int idleido = entrada.readInt();
        //Validar que el identificador sea positivo
        if (idleido  <=0){
            throw new ReglaNegocioException("Invalido");
        }
        //Asignar el identificador al objeto
        this.id = idleido;
        //Leer y asignar el nombre
        String nombreleer = entrada.readUTF();
        setNombre(nombreleer);
        //Leer y asignar el correo
        setCorreo(entrada.readUTF());
        //Leer y reconstruir el nivel
        setNivel(Nivel.valueOf(entrada.readUTF()));
        //throw new UnsupportedOperationException("TODO E14");
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Cliente otro && id == otro.id);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("%s #%d %s <%s> (%s)", "Cliente", id, nombre, correo, nivel);
    }
}
