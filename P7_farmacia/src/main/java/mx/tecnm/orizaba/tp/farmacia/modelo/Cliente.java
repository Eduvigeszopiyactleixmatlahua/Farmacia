package mx.tecnm.orizaba.tp.farmacia.modelo;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Cliente del sistema. Implementa {@link Externalizable}: el propio objeto decide qué y cómo se
 * escribe (véase {@code java_4_modelo/t7_persistencia/b_externalizacion/Cosas2}); por ello requiere un
 * constructor público sin argumentos.
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

    /** Encadenamiento de constructores: nivel por omisión. */
    public Cliente(int id, String nombre, String correo) {
        this(id, nombre, correo, Nivel.values()[0]);
    }

    public int id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public String correo() {
        return correo;
    }

    public Nivel nivel() {
        return nivel;
    }

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

    public final void setCorreo(String correo) {
        // TODO E06: Validar el correo con el patrón CORREO y guardarlo en minúsculas.
        if(correo == null || !CORREO.matcher(correo).matches()){
            throw new ReglaNegocioException("Correo invalido");
        }
        //CConvertir correo a minusculas y guardarlo en el atributo del objeto
        this.correo = correo.toLowerCase();
        //throw new UnsupportedOperationException("TODO E06");
    }

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

    @Override
    public void readExternal(ObjectInput entrada) throws IOException {
        // TODO E14: Leer en el mismo orden en que se escribió y reconstruir el nivel con Nivel.valueOf.
       /*** //Leer el identificar
        int idleido = entrada.readInt();
        //Validar que el identificador sea positivo
        if (idleido  <=0){
            throw new ReglaNegocioException("Invalido");
        }
        //Asignar el identificador al objeto
        this.id = idleido;
        //Leer y asignar el nombre
        String nombreleer = entrada.readUTF();
        //Leer y asignar el correo
        setNombre(nombreleer); ///CHECAR***/
        throw new UnsupportedOperationException("TODO E14");
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
