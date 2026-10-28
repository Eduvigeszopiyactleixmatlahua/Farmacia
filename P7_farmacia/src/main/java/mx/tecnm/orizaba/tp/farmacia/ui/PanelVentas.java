package mx.tecnm.orizaba.tp.farmacia.ui;

import mx.tecnm.orizaba.tp.farmacia.modelo.*;
import mx.tecnm.orizaba.tp.farmacia.persistencia.PersistenciaException;
import mx.tecnm.orizaba.tp.farmacia.servicio.Farmacia;

import java.util.Comparator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel gráfico para la gestión de ventas de la farmacia.
 *
 * <p>Permite registrar ventas, agregar y retirar medicamentos,
 * consultar las líneas de cada venta y visualizar
 * sus respectivos subtotales y totales.</p>
 *
 * <p>Administra las transiciones entre los estados
 * abierta, en surtido, pagada y cancelada,
 * respetando las reglas de negocio establecidas.</p>
 *
 * <p>Las operaciones se realizan mediante la fachada
 * {@link Farmacia}, manteniendo separada la interfaz
 * gráfica de la lógica de negocio y la persistencia.</p>
 *
 * @see Farmacia
 * @see Venta
 * @see Estado
 */

public class PanelVentas extends JPanel {

    private final Farmacia farmacia;
    private JTable tabla;

    //Modelo de la tabla de ventas
    private final DefaultTableModel modelo =
            new DefaultTableModel(
                    new Object[]{"ID", "Fecha", "Cliente", "Estado", "Total"}, 0) {

                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };

    /**
     * Construye e inicializa el panel de gestión de ventas.
     *
     * <p>Configura la tabla de ventas, los botones para
     * crear ventas, agregar medicamentos, consultar detalles
     * y actualizar la información.</p>
     *
     * <p>Incorpora los controles para iniciar el surtido,
     * cerrar o cancelar una venta y carga los registros
     * existentes al finalizar la inicialización.</p>
     *
     * @param farmacia instancia de la fachada utilizada
     *                 para gestionar las operaciones de venta.
     */

    public PanelVentas(Farmacia farmacia) {

        super(new BorderLayout(12, 12));
        this.farmacia = farmacia;

        setBorder(
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        );

        JLabel titulo = new JLabel("Gestión de ventas");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        // Botón para abrir una venta
        JButton nueva = new JButton("Nueva venta");
        nueva.addActionListener(e -> abrirVenta());

        // Botón para recargar la tabla
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> cargarVentas());

        JButton agregar = new JButton("Agregar medicamento");
        agregar.addActionListener(e -> agregarMedicamento());

        JButton detalle = new JButton("Ver detalle");
        detalle.addActionListener(e -> verDetalleVenta());

        JPanel botones =
                new JPanel(new FlowLayout(FlowLayout.RIGHT));

        botones.add(nueva);
        botones.add(agregar);
        botones.add(detalle);
        botones.add(actualizar);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(botones, BorderLayout.EAST);

        add(encabezado, BorderLayout.NORTH);

        // Crear tabla
        tabla = new JTable(modelo);

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        tabla.setRowHeight(28);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Botones del ciclo de vida de una venta (siempre visibles).
        JButton iniciar = new JButton("Iniciar surtido");
        JButton cerrarVenta = new JButton("Cerrar venta");
        JButton cancelar = new JButton("Cancelar venta");

        iniciar.addActionListener(e -> cambiarEstadoVenta(Estado.EN_CURSO));
        cerrarVenta.addActionListener(e -> cambiarEstadoVenta(Estado.CERRADA));
        cancelar.addActionListener(e -> cambiarEstadoVenta(Estado.CANCELADA));

        JPanel controlesEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlesEstado.add(iniciar);
        controlesEstado.add(cerrarVenta);
        controlesEstado.add(cancelar);
        add(controlesEstado, BorderLayout.SOUTH);

        // Consultar las ventas existentes
        cargarVentas();
    }

    /**
     * Consulta y muestra las ventas registradas en la farmacia.
     *
     * <p>Limpia las filas de la tabla y recupera las ventas
     * mediante el método {@link Farmacia#registros()}.</p>
     *
     * <p>Por cada venta, muestra su identificador, fecha,
     * nombre del cliente, estado actual y total calculado.</p>
     *
     * <p>La operación actualiza únicamente la información
     * visualizada, sin modificar los registros almacenados.</p>
     */

    private void cargarVentas() {

        modelo.setRowCount(0);

        for (Venta venta : farmacia.registros()) {

            modelo.addRow(new Object[]{
                    venta.id(),
                    venta.fecha(),
                    venta.cliente().nombre(),
                    venta.estado().etiqueta(),
                    String.format("$%.2f", venta.total())
            });
        }
    }

    /**
     * Permite abrir una nueva venta para un cliente registrado.
     *
     * <p>Consulta los clientes disponibles y muestra un
     * cuadro de diálogo para seleccionar al comprador.</p>
     *
     * <p>Si no existen clientes registrados, informa al
     * usuario que debe registrar uno antes de crear la venta.</p>
     *
     * <p>Una vez confirmada la selección, utiliza
     * {@link Farmacia#abrir(int)} para crear la venta,
     * actualiza la tabla y muestra su identificador.</p>
     *
     * <p>Los errores de negocio y persistencia se presentan
     * mediante mensajes comprensibles para el usuario.</p>
     */

    private void abrirVenta() {

        List<Cliente> clientes = farmacia.clientes();

        // Comprobar si existen clientes
        if (clientes.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Primero registra un cliente en la pestaña Clientes.",
                    "No hay clientes",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Crear lista desplegable de clientes
        JComboBox<Cliente> seleccion =
                new JComboBox<>(
                        clientes.toArray(new Cliente[0])
                );

        // Mostrar formulario
        int opcion = JOptionPane.showConfirmDialog(
                this,
                seleccion,
                "Seleccionar cliente",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        // Recuperar el cliente seleccionado
        Cliente cliente =
                (Cliente) seleccion.getSelectedItem();

        if (cliente == null) {
            return;
        }

        try {
            // Crear la venta mediante Farmacia
            Venta nueva = farmacia.abrir(cliente.id());

            // Actualizar la tabla
            cargarVentas();

            // Mostrar mensaje
            JOptionPane.showMessageDialog(
                    this,
                    "Venta #" + nueva.id()
                            + " abierta para " + cliente.nombre(),
                    "Venta creada",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (ReglaNegocioException | PersistenciaException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "No se pudo crear la venta",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Permite agregar un medicamento a una venta abierta.
     *
     * <p>Obtiene la venta seleccionada y verifica que
     * se encuentre en estado {@link Estado#ABIERTA}.</p>
     *
     * <p>Consulta los medicamentos del catálogo y muestra
     * un formulario para seleccionar un producto y
     * especificar la cantidad de unidades.</p>
     *
     * <p>Utiliza la fachada {@link Farmacia} para agregar
     * la línea de venta, actualizar la tabla y mostrar
     * el subtotal, importe con descuento y total.</p>
     *
     * <p>Los errores de negocio y persistencia se comunican
     * mediante cuadros de diálogo.</p>
     */

    private void agregarMedicamento() {

        // Obtener la fila seleccionada
        int fila = tabla.getSelectedRow();

        // Comprobar que se seleccionó una venta
        if (fila == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona una venta de la tabla.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Obtener el ID de la venta
        int filaModelo = tabla.convertRowIndexToModel(fila);

        int idVenta = ((Number)
                modelo.getValueAt(filaModelo, 0)).intValue();

        try {
            // Recuperar la venta existente
            Venta venta = farmacia.registro(idVenta);

            // Comprobar que esté abierta
            if (venta.estado() != Estado.ABIERTA) {
                JOptionPane.showMessageDialog(
                        this,
                        "Solo se pueden agregar medicamentos a ventas abiertas.",
                        "Estado no permitido",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Obtener los medicamentos del catálogo
            List<Producto> productos =
                    farmacia.catalogo().ordenados(
                            Comparator.comparing(
                                    Producto::nombre,
                                    String.CASE_INSENSITIVE_ORDER
                            )
                    );

            // Comprobar que existan productos
            if (productos.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Registra primero un medicamento en Inventario.",
                        "Sin productos",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Crear lista desplegable
            JComboBox<Producto> campoProducto =
                    new JComboBox<>(
                            productos.toArray(new Producto[0])
                    );

            // Crear selector de unidades
            JSpinner campoUnidades = new JSpinner(
                    new SpinnerNumberModel(
                            1, 1, Integer.MAX_VALUE, 1
                    )
            );

            // Crear formulario
            JPanel formulario = new JPanel(
                    new GridLayout(2, 2, 8, 8)
            );

            formulario.add(new JLabel("Medicamento:"));
            formulario.add(campoProducto);

            formulario.add(new JLabel("Unidades:"));
            formulario.add(campoUnidades);

            // Mostrar el formulario
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    formulario,
                    "Agregar medicamento a venta #" + idVenta,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            // Comprobar que aceptó
            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }

            // Obtener el producto seleccionado
            Producto producto =
                    (Producto) campoProducto.getSelectedItem();

            if (producto == null) {
                return;
            }

            // Obtener las unidades
            int unidades = ((Number)
                    campoUnidades.getValue()).intValue();

            // Agregar línea mediante la fachada
            Venta.Linea linea = farmacia.agregarLinea(
                    idVenta,
                    producto.clave().toString(),
                    unidades
            );

            // Actualizar la tabla
            cargarVentas();

            // Mostrar resultado
            JOptionPane.showMessageDialog(
                    this,
                    String.format(
                            "Medicamento agregado a venta #%d\n"
                                    + "%s x %d\n"
                                    + "Subtotal: $%.2f\n"
                                    + "Importe con descuento: $%.2f\n"
                                    + "Total venta: $%.2f",
                            idVenta,
                            producto.nombre(),
                            unidades,
                            linea.subtotal(),
                            linea.importe(),
                            venta.total()
                    ),
                    "Medicamento agregado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (ReglaNegocioException | PersistenciaException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "No se pudo agregar",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Actualiza el detalle de medicamentos de una venta.
     *
     * <p>Limpia la tabla de detalle y recorre las líneas
     * de la venta para mostrar la clave SKU, nombre
     * del medicamento, unidades, subtotal e importe.</p>
     *
     * <p>También actualiza la etiqueta que presenta
     * el subtotal y total de la venta.</p>
     *
     * @param venta venta cuyos medicamentos serán mostrados.
     * @param modeloDetalle modelo de tabla donde se agregan
     *                      las líneas de la venta.
     * @param resumen etiqueta Swing utilizada para mostrar
     *                el subtotal y total calculados.
     */

    private void cargarDetalleVenta(
            Venta venta,
            DefaultTableModel modeloDetalle,
            JLabel resumen) {

        //Limpiar la tabla
        modeloDetalle.setRowCount(0);

        // Recorrer los medicamentos de la venta
        for (Venta.Linea linea : venta.lineas()) {

            //Agregar una fila por medicamento
            modeloDetalle.addRow(new Object[]{

                    linea.elemento().clave(),

                    linea.elemento().nombre(),

                    linea.unidades(),

                    String.format("$%.2f", linea.subtotal()),

                    String.format("$%.2f", linea.importe())
            });
        }

        //Mostrar subtotal y total de la venta
        resumen.setText(
                String.format(
                        "Subtotal: $%.2f     Total: $%.2f",
                        venta.subtotal(),
                        venta.total()
                )
        );
    }

    /**
     * Muestra los medicamentos asociados a una venta seleccionada.
     *
     * <p>Obtiene la venta mediante la fachada {@link Farmacia}
     * y construye una ventana secundaria con el detalle
     * de sus líneas, subtotales e importe total.</p>
     *
     * <p>Si la venta se encuentra abierta, permite
     * seleccionar y quitar medicamentos previa confirmación
     * del usuario.</p>
     *
     * <p>Las modificaciones se realizan mediante la fachada
     * y posteriormente se actualizan las tablas de ventas
     * y de detalle.</p>
     *
     * <p>Los errores de negocio y persistencia se muestran
     * mediante cuadros de diálogo.</p>
     */

    private void verDetalleVenta() {

        // Obtener la venta seleccionada
        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona una venta primero.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Obtener el ID de la venta
        int filaModelo = tabla.convertRowIndexToModel(fila);

        int idVenta = ((Number)
                modelo.getValueAt(filaModelo, 0)).intValue();

        try {
            // Recuperar la venta desde Farmacia
            Venta venta = farmacia.registro(idVenta);

            // Crear tabla de medicamentos
            DefaultTableModel modeloDetalle =
                    new DefaultTableModel(
                            new Object[]{
                                    "SKU", "Medicamento", "Unidades",
                                    "Subtotal", "Importe"
                            }, 0) {

                        @Override
                        public boolean isCellEditable(int fila, int columna) {
                            return false;
                        }
                    };

            JTable tablaDetalle = new JTable(modeloDetalle);

            tablaDetalle.setSelectionMode(
                    ListSelectionModel.SINGLE_SELECTION
            );

            tablaDetalle.setRowHeight(28);

            JLabel resumen = new JLabel();

            // Cargar las líneas existentes
            cargarDetalleVenta(venta, modeloDetalle, resumen);

            //Crear ventana secundaria
            JDialog dialogo = new JDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Detalle de venta #" + idVenta,
                    Dialog.ModalityType.APPLICATION_MODAL
            );

            dialogo.setLayout(new BorderLayout(10, 10));

            JLabel encabezado = new JLabel(
                    "Cliente: " + venta.cliente().nombre()
                            + "     Estado: " + venta.estado().etiqueta()
            );

            dialogo.add(encabezado, BorderLayout.NORTH);

            dialogo.add(
                    new JScrollPane(tablaDetalle),
                    BorderLayout.CENTER
            );

            //Botón para quitar medicamento
            JButton quitar = new JButton("Quitar medicamento");

            quitar.setEnabled(venta.estado() == Estado.ABIERTA);

            quitar.addActionListener(e -> {

                int seleccion = tablaDetalle.getSelectedRow();

                if (seleccion == -1) {
                    JOptionPane.showMessageDialog(
                            dialogo,
                            "Selecciona un medicamento."
                    );
                    return;
                }

                int indice =
                        tablaDetalle.convertRowIndexToModel(seleccion);

                String sku =
                        modeloDetalle.getValueAt(indice, 0).toString();

                String nombre =
                        modeloDetalle.getValueAt(indice, 1).toString();

                // Confirmar el retiro de la línea
                int confirmar = JOptionPane.showConfirmDialog(
                        dialogo,
                        "¿Quitar " + nombre + " (" + sku + ") de la venta?",
                        "Confirmar",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirmar != JOptionPane.YES_OPTION) {
                    return;
                }

                try {
                    // Eliminar la línea mediante Farmacia
                    boolean quitada =
                            farmacia.quitarLinea(idVenta, sku);

                    if (quitada) {

                        // Actualizar detalle y lista de ventas
                        cargarDetalleVenta(
                                venta, modeloDetalle, resumen
                        );

                        cargarVentas();

                        JOptionPane.showMessageDialog(
                                dialogo,
                                "Medicamento quitado de la venta."
                        );

                    } else {
                        JOptionPane.showMessageDialog(
                                dialogo,
                                "No se encontró el medicamento en esta venta."
                        );
                    }

                } catch (ReglaNegocioException |
                         PersistenciaException ex) {

                    JOptionPane.showMessageDialog(
                            dialogo,
                            ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            });

            // Botón para cerrar el detalle
            JButton cerrarDetalle = new JButton("Cerrar detalle");

            cerrarDetalle.addActionListener(e -> dialogo.dispose());

            JPanel acciones =
                    new JPanel(new FlowLayout(FlowLayout.RIGHT));

            acciones.add(quitar);
            acciones.add(cerrarDetalle);

            JPanel pie = new JPanel(new BorderLayout());

            pie.add(resumen, BorderLayout.CENTER);
            pie.add(acciones, BorderLayout.EAST);

            dialogo.add(pie, BorderLayout.SOUTH);

            // Configurar y mostrar ventana
            dialogo.setSize(900, 420);
            dialogo.setLocationRelativeTo(this);
            dialogo.setVisible(true);

            cargarVentas();

        } catch (ReglaNegocioException |
                 PersistenciaException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Solicita y ejecuta el cambio de estado de una venta.
     *
     * <p>Obtiene la venta seleccionada y comprueba que la
     * transición desde su estado actual hacia el estado
     * solicitado esté permitida.</p>
     *
     * <p>Verifica que una venta contenga al menos una línea
     * antes de comenzar el surtido y solicita confirmación
     * al usuario antes de realizar el cambio.</p>
     *
     * <p>Mediante la fachada {@link Farmacia}, permite
     * iniciar el surtido, cerrar o cancelar la venta,
     * según el estado de destino.</p>
     *
     * <p>Después de la operación, actualiza la tabla
     * y muestra el resultado al usuario.</p>
     *
     * @param destino estado al que se desea cambiar la venta.
     *
     * @see Estado
     * @see Venta
     */

    private void cambiarEstadoVenta(Estado destino) {

        // PASO 1: Comprobar que se seleccionó una venta
        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona una venta de la tabla.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // PASO 2: Obtener el identificador
        int filaModelo = tabla.convertRowIndexToModel(fila);

        int idVenta = ((Number)
                modelo.getValueAt(filaModelo, 0)).intValue();

        try {
            // PASO 3: Recuperar la venta
            Venta venta = farmacia.registro(idVenta);

            Estado origen = venta.estado();

            // PASO 4: Validar la transición
            if (!origen.permiteTransicionA(destino)) {

                JOptionPane.showMessageDialog(
                        this,
                        "No se puede pasar de "
                                + origen.etiqueta()
                                + " a " + destino.etiqueta(),
                        "Transición no permitida",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // PASO 5: Una venta vacía no puede iniciar
            if (destino == Estado.EN_CURSO
                    && venta.lineas().isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Agrega al menos un medicamento antes "
                                + "de iniciar el surtido.",
                        "Venta vacía",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // PASO 6: Solicitar confirmación
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Cambiar la venta #" + idVenta
                            + " de " + origen.etiqueta()
                            + " a " + destino.etiqueta() + "?",
                    "Confirmar cambio de estado",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (opcion != JOptionPane.YES_OPTION) {
                return;
            }

            // PASO 7: Ejecutar operación desde Farmacia
            switch (destino) {

                case EN_CURSO -> farmacia.iniciar(idVenta);

                case CERRADA -> farmacia.cerrar(idVenta);

                case CANCELADA -> farmacia.cancelar(idVenta);

                case ABIERTA -> throw new IllegalArgumentException(
                        "No se permite reabrir una venta."
                );
            }

            // PASO 8: Actualizar la tabla
            cargarVentas();

            // PASO 9: Mostrar confirmación
            JOptionPane.showMessageDialog(
                    this,
                    "Venta #" + idVenta
                            + " actualizada a " + destino.etiqueta(),
                    "Cambio exitoso",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (ReglaNegocioException |
                 PersistenciaException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error al cambiar el estado",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

}
