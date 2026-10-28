package mx.tecnm.orizaba.tp.farmacia.persistencia;

import mx.tecnm.orizaba.tp.farmacia.modelo.Cliente;
import mx.tecnm.orizaba.tp.farmacia.modelo.Anaquel;
import mx.tecnm.orizaba.tp.farmacia.modelo.Venta;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Respaldo binario mediante serialización de objetos (complementa a SQLite).
 * Escribe y lee en el mismo orden (véase {@code java_4_modelo/t7_persistencia/a_serializacion/Gusano_07}).
 */
public final class Respaldo {
    /**
     * Identificador de versión del respaldo.
     */
    public static final String ENCABEZADO = "Respaldo Farmacia v1";

    private Respaldo() {
    }
/**
 * Contenido recuperado de un respaldo.
 * @param encabezado identificador del formato de respaldo.
 * @param fecha fecha del registro.
 * @param catalogo catálogo de medicamentos.
 * @param clientes clientes que se almacenarán.
 * @param registros ventas que se almacenarán.
 */
public record Contenido(String encabezado, LocalDateTime fecha, Anaquel catalogo,
                            List<Cliente> clientes, List<Venta> registros) {
    }
    /**
     * Escribe un respaldo binario del catálogo, clientes y ventas.
     * @param archivo ruta del archivo de respaldo.
     * @param catalogo catálogo de medicamentos.
     * @param clientes clientes que se almacenarán.
     * @param registros ventas que se almacenarán.
     * @throws IOException si la operación no puede completarse.
     */
    public static void exportar(Path archivo, Anaquel catalogo, Collection<Cliente> clientes,
                                Collection<Venta> registros) throws IOException {
        // TODO E14: ObjectOutputStream(BufferedOutputStream(Files.newOutputStream)): encabezado, fecha, catálogo, listas.
        //Flujo de salida de los objetos
        try (ObjectOutputStream salida = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(archivo)))){
           //Guardar
           salida.writeUTF(ENCABEZADO);
           //Guardar la fecha del respaldo
            salida.writeObject(LocalDateTime.now());
            salida.writeObject(catalogo);
            //Guardar la lista de clientes
            salida.writeObject(new ArrayList<>(clientes));
            //Guardar la lista de ventas
            salida.writeObject(new ArrayList<>(registros));
        }
        //throw new UnsupportedOperationException("TODO E14");
    }
    /**
     * Lee y valida un respaldo binario.
     * @param archivo ruta del archivo de respaldo.
     * @return contenido reconstruido del respaldo.
     * @throws IOException si la operación no puede completarse.
     * @throws ClassNotFoundException si la operación no puede completarse.
     */
    @SuppressWarnings("unchecked")
    public static Contenido importar(Path archivo) throws IOException, ClassNotFoundException {
        // TODO E14: Leer en el mismo orden; validar el encabezado (IOException si no coincide).
        //Flujo de entrada
        try (ObjectInputStream entrada = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(archivo)))){
            //Leer el encabezado
            String encabezado = entrada.readUTF();
            //Validar el encabezado
            if(!ENCABEZADO.equals(encabezado)){
                throw new IOException("Encabezado de respaldo inválido");
            }
            //Recuperar la fecha
            LocalDateTime fecha = (LocalDateTime)  entrada.readObject();
            //Recuperar el catálogo
            Anaquel catalogo = (Anaquel) entrada.readObject();
            //Recuperar la lista de clientes
            List<Cliente>  clientes = (List<Cliente>) entrada.readObject();
            //Recuperar la lista de ventas
            List<Venta> registros = (List<Venta>) entrada.readObject();
            //Construir el resultado
            return new Contenido(encabezado, fecha,catalogo,clientes, registros);
        }
        //throw new UnsupportedOperationException("TODO E14");
    }
}
