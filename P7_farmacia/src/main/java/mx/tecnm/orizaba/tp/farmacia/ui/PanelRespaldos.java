package mx.tecnm.orizaba.tp.farmacia.ui;

import mx.tecnm.orizaba.tp.farmacia.servicio.Farmacia;
import mx.tecnm.orizaba.tp.farmacia.persistencia.Respaldo;
import mx.tecnm.orizaba.tp.farmacia.modelo.Producto;
import mx.tecnm.orizaba.tp.farmacia.modelo.Cliente;
import mx.tecnm.orizaba.tp.farmacia.modelo.Venta;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Panel gráfico para la exportación y verificación de
 * respaldos binarios de la farmacia.
 *
 * <p>Permite guardar en un archivo los productos,
 * clientes y ventas registrados en el sistema,
 * utilizando la serialización de objetos Java.</p>
 *
 * <p>También permite seleccionar un respaldo existente
 * y comprobar su estructura e integridad básica sin
 * modificar la base de datos SQLite.</p>
 *
 * <p>La interfaz está desarrollada con Java Swing
 * y utiliza la fachada {@link Farmacia} para
 * solicitar la exportación de información.</p>
 *
 * @see Farmacia
 * @see Respaldo
 */

public class PanelRespaldos extends JPanel {

    private final Farmacia farmacia;
    private final JLabel ultimoRespaldo =
            new JLabel("Todavía no se ha exportado un respaldo en esta sesión.");

    /**
     * Construye e inicializa el panel de respaldos.
     *
     * <p>Configura el título, los botones de exportación
     * y verificación, y un área informativa que describe
     * el formato de los archivos generados.</p>
     *
     * <p>También incorpora una etiqueta que muestra
     * la ubicación del último respaldo exportado
     * durante la sesión actual.</p>
     *
     * @param farmacia instancia de la fachada utilizada
     *                 para exportar la información del sistema.
     */

    public PanelRespaldos(Farmacia farmacia) {
        super(new BorderLayout(12, 12));
        this.farmacia = farmacia;

        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titulo = new JLabel("Respaldos de farmacia");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        JButton exportar = new JButton("Exportar respaldo");
        exportar.addActionListener(e -> exportarRespaldo());

        JButton verificar = new JButton("Verificar respaldo");
        verificar.addActionListener(e -> verificarRespaldo());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(exportar);
        botones.add(verificar);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(botones, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        JTextArea informacion = new JTextArea(
                "El respaldo incluye el catálogo de medicamentos, los clientes y las ventas.\n\n"
                        + "Se guarda como un archivo binario de objetos Java (*.farmacia).\n"
                        + "No es una base de datos SQLite ni sustituye datos/farmacia.db.\n\n"
                        + "Guárdalo en una carpeta segura y recuerda su ubicación."
        );
        informacion.setEditable(false);
        informacion.setLineWrap(true);
        informacion.setWrapStyleWord(true);
        informacion.setOpaque(false);
        informacion.setFont(new Font("SansSerif", Font.PLAIN, 14));
        informacion.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        add(informacion, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBorder(BorderFactory.createTitledBorder("Última exportación"));
        pie.add(ultimoRespaldo, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
    }

    /**
     * Exporta un respaldo binario de los datos de la farmacia.
     *
     * <p>Muestra un selector de archivos que permite elegir
     * la ubicación y el nombre del respaldo, utilizando
     * la extensión {@code .farmacia}.</p>
     *
     * <p>Si el archivo ya existe, solicita confirmación
     * antes de reemplazarlo. La exportación se delega
     * al método {@link Farmacia#exportarRespaldo(Path)}.</p>
     *
     * <p>Cuando la operación finaliza correctamente,
     * muestra la ruta y el tamaño del archivo generado
     * y actualiza la etiqueta de la última exportación.</p>
     *
     * <p>Los errores de entrada y salida o de ejecución
     * se capturan y muestran mediante cuadros de diálogo.</p>
     */

    private void exportarRespaldo() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar respaldo de farmacia");
        selector.setFileFilter(new FileNameExtensionFilter(
                "Respaldo de farmacia (*.farmacia)", "farmacia"));
        selector.setSelectedFile(new File(
                "respaldo-farmacia-" + LocalDate.now() + ".farmacia"));

        int opcion = selector.showSaveDialog(this);
        if (opcion != JFileChooser.APPROVE_OPTION) {
            return;  // El usuario canceló la operación.
        }

        File elegido = selector.getSelectedFile();
        if (elegido == null) {
            return;
        }

        // El sufijo evita confundir el respaldo serializado con un archivo .db.
        Path destino = elegido.toPath().toAbsolutePath();
        if (!destino.getFileName().toString().toLowerCase(java.util.Locale.ROOT)
                .endsWith(".farmacia")) {
            destino = destino.resolveSibling(destino.getFileName() + ".farmacia");
        }

        if (Files.exists(destino)) {
            int confirmar = JOptionPane.showConfirmDialog(
                    this,
                    "El archivo ya existe:\n" + destino
                            + "\n¿Deseas reemplazarlo?",
                    "Confirmar reemplazo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            // Delegar la serialización al servicio ya implementado.
            farmacia.exportarRespaldo(destino);

            // Solo informar éxito cuando el método terminó sin lanzar excepción.
            long bytes = Files.size(destino);
            ultimoRespaldo.setText(destino.toString());
            JOptionPane.showMessageDialog(
                    this,
                    "Respaldo exportado correctamente.\nArchivo: " + destino
                            + "\nTamaño: " + bytes + " bytes",
                    "Exportación exitosa",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException | RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo exportar el respaldo:\n" + ex.getMessage(),
                    "Error de respaldo",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Verifica la lectura y coherencia de un respaldo binario.
     *
     * <p>Permite seleccionar un archivo con extensión
     * {@code .farmacia} y comprueba que exista,
     * sea un archivo regular y no esté vacío.</p>
     *
     * <p>Deserializa su contenido mediante
     * {@link Respaldo#importar(Path)} y posteriormente
     * ejecuta las validaciones de integridad.</p>
     *
     * <p>Cuando el respaldo es válido, muestra su fecha,
     * cantidad de productos, clientes, ventas y tamaño
     * en bytes.</p>
     *
     * <p>Esta operación únicamente verifica el archivo;
     * no restaura ni modifica la base de datos SQLite.</p>
     *
     * <p>Los errores de lectura, deserialización y validación
     * se muestran mediante cuadros de diálogo.</p>
     */

   private void verificarRespaldo() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Seleccionar respaldo para verificar");
        selector.setFileFilter(new FileNameExtensionFilter(
                "Respaldo de farmacia (*.farmacia)", "farmacia"));

        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File elegido = selector.getSelectedFile();
        if (elegido == null) {
            return;
        }

        Path archivo = elegido.toPath();
        try {
            if (!Files.isRegularFile(archivo) || Files.size(archivo) == 0) {
                throw new IOException("El respaldo no existe o está vacío.");
            }

            // Importar solo archivos creados por esta aplicación y de confianza.
            // Este método NO restaura ni modifica SQLite.
            Respaldo.Contenido copia = Respaldo.importar(archivo);
            comprobarIntegridad(copia);

            JOptionPane.showMessageDialog(
                    this,
                    "Respaldo legible y coherente.\n"
                            + "Fecha: " + copia.fecha() + "\n"
                            + "Productos: " + copia.catalogo().tamanio() + "\n"
                            + "Clientes: " + copia.clientes().size() + "\n"
                            + "Ventas: " + copia.registros().size() + "\n"
                            + "Tamaño: " + Files.size(archivo) + " bytes\n"
                            + "No se modificó la base de datos.",
                    "Verificación correcta",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException | ClassNotFoundException | RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo verificar el respaldo:\n" + ex.getMessage(),
                    "Error de verificación",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Comprueba la integridad básica de un respaldo deserializado.
     *
     * <p>Verifica que el encabezado coincida con el formato
     * esperado y que la fecha, el catálogo, los clientes
     * y las ventas no sean nulos.</p>
     *
     * <p>Comprueba que los productos tengan claves SKU únicas,
     * que los clientes y ventas no tengan identificadores
     * duplicados y que las referencias entre ventas,
     * clientes y medicamentos sean coherentes.</p>
     *
     * <p>También valida que las líneas de venta pertenezcan
     * a su venta correspondiente, que los productos existan
     * en el catálogo y que las unidades sean positivas.</p>
     *
     * @param copia contenido del respaldo que se desea validar.
     * @throws IOException si se detecta una estructura
     *                     incompleta, identificadores duplicados
     *                     o referencias inconsistentes.
     */

    private static void comprobarIntegridad(Respaldo.Contenido copia) throws IOException {
        if (!Respaldo.ENCABEZADO.equals(copia.encabezado())
                || copia.fecha() == null || copia.catalogo() == null
                || copia.clientes() == null || copia.registros() == null) {
            throw new IOException("Encabezado o estructura incompleta.");
        }

        Map<String, Producto> productos = new HashMap<>();
        for (Producto p : copia.catalogo()) {
            if (p == null || productos.putIfAbsent(p.clave().toString(), p) != null) {
                throw new IOException("Producto nulo o SKU repetido.");
            }
        }
        if (productos.size() != copia.catalogo().tamanio()) {
            throw new IOException("Cantidad inconsistente de productos.");
        }

        Set<Integer> clientes = new HashSet<>();
        for (Cliente c : copia.clientes()) {
            if (c == null || !clientes.add(c.id())) {
                throw new IOException("Cliente nulo o ID repetido.");
            }
        }

        Set<Integer> ventas = new HashSet<>();
        for (Venta v : copia.registros()) {
            if (v == null || !ventas.add(v.id()) || v.cliente() == null
                    || !clientes.contains(v.cliente().id())
                    || v.fecha() == null || v.estado() == null) {
                throw new IOException("Venta duplicada o con referencias incompletas.");
            }
            for (Venta.Linea linea : v.lineas()) {
                if (linea == null || linea.contenedor() != v
                        || linea.elemento() == null || linea.unidades() <= 0
                        || !productos.containsKey(linea.elemento().clave().toString())) {
                    throw new IOException("Línea de venta inconsistente.");
                }
            }
        }
    }

}
