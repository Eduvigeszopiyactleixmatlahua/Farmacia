package mx.tecnm.orizaba.tp.farmacia.ui;

import mx.tecnm.orizaba.tp.farmacia.modelo.Cliente;
import mx.tecnm.orizaba.tp.farmacia.modelo.Nivel;
import mx.tecnm.orizaba.tp.farmacia.modelo.ReglaNegocioException;
import mx.tecnm.orizaba.tp.farmacia.persistencia.PersistenciaException;
import mx.tecnm.orizaba.tp.farmacia.servicio.Farmacia;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelClientes extends JPanel {

    /**
     * Panel gráfico para la gestión de clientes de la farmacia.
     *
     * <p>Permite consultar los clientes registrados, agregar
     * nuevos clientes y modificar su nivel de descuento
     * mediante una interfaz construida con Java Swing.</p>
     *
     * <p>Presenta la información en una tabla que incluye
     * identificador, nombre, correo electrónico y nivel.</p>
     *
     * <p>Las operaciones de consulta y actualización se
     * realizan mediante la fachada {@link Farmacia},
     * manteniendo separada la interfaz gráfica de la
     * lógica de negocio y la persistencia.</p>
     *
     * @see Farmacia
     * @see Cliente
     * @see Nivel
     */

    private final Farmacia farmacia;
    private JTable tabla;

    // Crear el modelo de la tabla
    private final DefaultTableModel modelo =
            new DefaultTableModel(
                    new Object[]{"ID", "Nombre", "Correo", "Nivel"}, 0) {

                /**
                 * Determina si una celda de la tabla puede editarse.
                 *
                 * <p>Deshabilita la edición directa de todas las celdas
                 * para evitar modificaciones fuera de las operaciones
                 * controladas por la interfaz.</p>
                 *
                 * @param fila índice de la fila que se desea editar.
                 * @param columna índice de la columna que se desea editar.
                 * @return false, ya que ninguna celda permite edición directa.
                 */

                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };

    // Constructor
    public PanelClientes(Farmacia farmacia) {

/**
 * Construye e inicializa el panel de gestión de clientes.
 *
 * <p>Configura el diseño del panel, los botones de registro,
 * actualización y cambio de nivel, así como la tabla
 * utilizada para visualizar la información.</p>
 *
 * <p>Al finalizar la inicialización, consulta los clientes
 * registrados y carga sus datos en la tabla.</p>
 *
 * @param farmacia instancia de la fachada utilizada para
 *                 consultar, registrar y modificar clientes.
 */

        super(new BorderLayout(12, 12));
        this.farmacia = farmacia;

        setBorder(
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        );

        JLabel titulo = new JLabel("Gestión de clientes");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        // PASO 3: Botones
        JButton registrar = new JButton("Registrar cliente");
        registrar.addActionListener(e -> registrarCliente());

        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> {
            // Volver a consultar los clientes
            cargarClientes();
            // Mostrar mensaje de confirmación
            JOptionPane.showMessageDialog(
                    this,
                    "La lista de clientes se actualizó correctamente.",
                    "Actualización",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        JButton cambiarNivel = new JButton("Cambiar nivel");
        cambiarNivel.addActionListener(e -> cambiarNivelCliente());

        JPanel botones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        botones.add(registrar);
        botones.add(cambiarNivel);
        botones.add(actualizar);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(botones, BorderLayout.EAST);

        add(encabezado, BorderLayout.NORTH);

        //Tabla
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla = new JTable(modelo);
        tabla.setRowHeight(28);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        cargarClientes();
    }

    /**
     * Consulta y muestra los clientes registrados en la farmacia.
     *
     * <p>Limpia las filas del modelo de la tabla y obtiene
     * la lista de clientes mediante el método
     * {@link Farmacia#clientes()}.</p>
     *
     * <p>Por cada cliente, agrega una fila con su identificador,
     * nombre, correo electrónico y etiqueta del nivel.</p>
     *
     * <p>Este método actualiza únicamente la información
     * visualizada y no modifica los registros almacenados.</p>
     */

    private void cargarClientes() {

        modelo.setRowCount(0);

        for (Cliente c : farmacia.clientes()) {
            modelo.addRow(new Object[]{
                    c.id(),
                    c.nombre(),
                    c.correo(),
                    c.nivel().etiqueta()
            });
        }
    }

    /**
     * Permite registrar un nuevo cliente en la farmacia.
     *
     * <p>Muestra un formulario Swing para capturar el nombre,
     * correo electrónico y nivel del cliente.</p>
     *
     * <p>Si el usuario confirma el formulario, obtiene los
     * datos ingresados y solicita el registro mediante
     * {@link Farmacia#registrarCliente(String, String, Nivel)}.</p>
     *
     * <p>Después de un registro exitoso, actualiza la tabla
     * de clientes y muestra el identificador generado.</p>
     *
     * <p>Los errores de validación y persistencia se capturan
     * y presentan al usuario mediante cuadros de diálogo.</p>
     */

    private void registrarCliente() {

        JTextField campoNombre = new JTextField(20);
        JTextField campoCorreo = new JTextField(20);

        JComboBox<Nivel> campoNivel =
                new JComboBox<>(Nivel.values());

        JPanel formulario = new JPanel(
                new GridLayout(3, 2, 8, 8)
        );

        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        formulario.add(new JLabel("Correo:"));
        formulario.add(campoCorreo);

        formulario.add(new JLabel("Nivel:"));
        formulario.add(campoNivel);

        int opcion = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Registrar cliente",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            // Obtener los datos
            String nombre = campoNombre.getText().strip();
            String correo = campoCorreo.getText().strip();

            Nivel nivel = (Nivel) campoNivel.getSelectedItem();

            // Registrar mediante la fachada
            Cliente cliente = farmacia.registrarCliente(
                    nombre,
                    correo,
                    nivel
            );

            // Actualizar la tabla
            cargarClientes();

            JOptionPane.showMessageDialog(
                    this,
                    "Cliente registrado con ID: " + cliente.id(),
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (ReglaNegocioException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (PersistenciaException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo guardar el cliente: " + ex.getMessage(),
                    "Error de persistencia",
                    JOptionPane.ERROR_MESSAGE
            );
        }

    }

    /**
     * Permite modificar el nivel de descuento de un cliente.
     *
     * <p>Obtiene el cliente seleccionado en la tabla y consulta
     * su información mediante la fachada {@link Farmacia}.</p>
     *
     * <p>Muestra un formulario con los niveles disponibles:
     * general, afiliado y adulto mayor, seleccionando
     * inicialmente el nivel actual del cliente.</p>
     *
     * <p>Si el usuario selecciona un nivel diferente y confirma
     * el cambio, actualiza el registro mediante la fachada
     * y vuelve a cargar los clientes en la tabla.</p>
     *
     * <p>Si no existe una selección, se cancela el formulario
     * o el nivel no cambia, no se realiza ninguna actualización.</p>
     *
     * <p>Los errores de negocio y persistencia se muestran
     * mediante mensajes comprensibles para el usuario.</p>
     */

    private void cambiarNivelCliente() {

        //Obtener la fila seleccionada
        int fila = tabla.getSelectedRow();

        //Verificar que exista una selección
        if (fila == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona un cliente de la tabla.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        //Obtener el ID de la tabla
        int filaModelo = tabla.convertRowIndexToModel(fila);
        int idCliente = (Integer) modelo.getValueAt(filaModelo, 0);

        try {
            //Consultar el cliente
            Cliente cliente = farmacia.cliente(idCliente);

            //Crear lista de niveles
            JComboBox<Nivel> campoNivel =
                    new JComboBox<>(Nivel.values());

            // Mostrar el nivel actual
            campoNivel.setSelectedItem(cliente.nivel());

            // Mostrar el formulario
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    campoNivel,
                    "Cambiar nivel de: " + cliente.nombre(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            //Comprobar si aceptó
            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }

            //Obtener el nivel seleccionado
            Nivel nuevoNivel =
                    (Nivel) campoNivel.getSelectedItem();

            if (nuevoNivel == null) {
                return;
            }

            //Comprobar si realmente cambió
            if (nuevoNivel == cliente.nivel()) {
                JOptionPane.showMessageDialog(
                        this,
                        "El cliente ya tiene ese nivel."
                );
                return;
            }

            //Actualizar mediante Farmacia
            farmacia.cambiarNivel(idCliente, nuevoNivel);

            //Actualizar la tabla
            cargarClientes();

            // Mostrar confirmación
            JOptionPane.showMessageDialog(
                    this,
                    "Nivel actualizado correctamente.\n"
                            + "Cliente: " + cliente.nombre() + "\n"
                            + "Nuevo nivel: " + nuevoNivel.etiqueta(),
                    "Actualización exitosa",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (ReglaNegocioException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (PersistenciaException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo guardar el nivel: " + ex.getMessage(),
                    "Error de persistencia",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


}
