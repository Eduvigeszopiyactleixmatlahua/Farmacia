package mx.tecnm.orizaba.tp.farmacia.ui;

import mx.tecnm.orizaba.tp.farmacia.modelo.Producto;
import mx.tecnm.orizaba.tp.farmacia.servicio.Farmacia;
import mx.tecnm.orizaba.tp.farmacia.modelo.ReglaNegocioException;
import mx.tecnm.orizaba.tp.farmacia.persistencia.PersistenciaException;
import mx.tecnm.orizaba.tp.farmacia.modelo.Sku;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.RowFilter;
import java.awt.*;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.text.Normalizer;
/**
 * Ventana principal de la aplicación de farmacia.
 *
 * <p>Proporciona una interfaz gráfica construida con Java Swing,
 * con navegación lateral para consultar el inventario,
 * administrar clientes, gestionar ventas, visualizar reportes,
 * supervisar eventos y realizar respaldos.</p>
 *
 * <p>Utiliza la fachada {@link Farmacia} para acceder a las
 * operaciones del sistema sin ejecutar directamente
 * instrucciones SQL.</p>
 *
 * @see Farmacia
 */
public class VentanaPrincipal extends JFrame {

    private final Farmacia farmacia;
    private JTable tabla;
    private JTextField campoBuscar;
    private JComboBox<String> campoTipo;
    private JLabel totalCatalogo;
    private JLabel totalPatentes;
    private JLabel totalGenericos;
    private JLabel totalBiologicos;
    private JLabel resultadosFiltro;

    private static final Color FONDO = new Color(244, 247, 250);
    private static final Color TEXTO = new Color(19, 48, 62);
    private static final Color SUBTEXTO = new Color(97, 119, 133);
    private static final Color VERDE = new Color(26, 142, 128);
    private static final Color BORDE = new Color(224, 232, 237);

    private final DefaultTableModel modelo =
            new DefaultTableModel(
                    new Object[]{"SKU", "Producto", "Tipo", "Precio base"}, 0) {

                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };

    private final TableRowSorter<DefaultTableModel> ordenador =
            new TableRowSorter<>(modelo);
    /**
     * Construye la ventana principal del sistema.
     *
     * <p>Inicializa los componentes visuales, las tablas,
     * los controles del inventario y la navegación
     * entre las diferentes secciones de la aplicación.</p>
     *
     * @param farmacia instancia de la fachada que permite
     *                 consultar y administrar la información
     *                 del sistema.
     */

    public VentanaPrincipal(Farmacia farmacia) {
        super("P7 Farmacia - Punto de Venta");
        this.farmacia = farmacia;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // PRÁCTICA 20: Inventario moderno, conservando el CRUD.
        JPanel contenido = new JPanel(new BorderLayout(0, 18));
        contenido.setBackground(FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel superior = new JPanel();
        superior.setLayout(new BoxLayout(superior, BoxLayout.Y_AXIS));
        superior.setOpaque(false);

        // Encabezado y acciones.
        JPanel encabezado = new JPanel(new BorderLayout(16, 0));
        encabezado.setOpaque(false);
        encabezado.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Inventario de medicamentos");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(TEXTO);
        JLabel subtituloInventario = new JLabel("Consulta y administra los productos registrados");
        subtituloInventario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtituloInventario.setForeground(SUBTEXTO);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(5));
        textos.add(subtituloInventario);

        JButton registrar = new JButton("+ Registrar producto");
        registrar.setBackground(VERDE);
        registrar.setForeground(Color.WHITE);
        registrar.addActionListener(e -> registrarProducto());

        JButton editar = new JButton("Editar");
        editar.addActionListener(e -> editarProducto());
        JButton retirar = new JButton("Retirar");
        retirar.setForeground(new Color(174, 65, 75));
        retirar.addActionListener(e -> retirarProducto());
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> cargarProductos());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(registrar);
        botones.add(editar);
        botones.add(retirar);
        botones.add(actualizar);

        encabezado.add(textos, BorderLayout.WEST);
        encabezado.add(botones, BorderLayout.EAST);
        superior.add(encabezado);
        superior.add(Box.createVerticalStrut(20));

        // Tarjetas de conteo, actualizadas desde el catálogo real.
        totalCatalogo = new JLabel("0");
        totalPatentes = new JLabel("0");
        totalGenericos = new JLabel("0");
        totalBiologicos = new JLabel("0");
        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 12, 0));
        tarjetas.setOpaque(false);
        tarjetas.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjetas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 94));
        tarjetas.add(crearTarjeta("Total de productos", totalCatalogo, new Color(51, 113, 181)));
        tarjetas.add(crearTarjeta("Patente", totalPatentes, new Color(168, 116, 48)));
        tarjetas.add(crearTarjeta("Genéricos", totalGenericos, VERDE));
        tarjetas.add(crearTarjeta("Biológicos", totalBiologicos, new Color(117, 94, 183)));
        superior.add(tarjetas);
        superior.add(Box.createVerticalStrut(18));

        // Buscador en tiempo real y filtro por tipo.
        JPanel filtros = crearSuperficie();
        filtros.setLayout(new BorderLayout(16, 8));
        filtros.setAlignmentX(Component.LEFT_ALIGNMENT);
        filtros.setMaximumSize(new Dimension(Integer.MAX_VALUE, 73));

        JPanel zonaBusqueda = new JPanel(new BorderLayout(8, 0));
        zonaBusqueda.setOpaque(false);
        JLabel buscarLabel = new JLabel("Buscar");
        buscarLabel.setForeground(TEXTO);
        campoBuscar = new JTextField(25);
        campoBuscar.putClientProperty("JTextField.placeholderText", "Nombre o SKU del medicamento");
        campoBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });
        zonaBusqueda.add(buscarLabel, BorderLayout.WEST);
        zonaBusqueda.add(campoBuscar, BorderLayout.CENTER);

        JPanel zonaTipo = new JPanel(new BorderLayout(8, 0));
        zonaTipo.setOpaque(false);
        JLabel tipoLabel = new JLabel("Tipo");
        tipoLabel.setForeground(TEXTO);
        campoTipo = new JComboBox<>(new String[]{
                "Todos los tipos", "Patente", "Genérico", "Biológico"
        });
        campoTipo.setPreferredSize(new Dimension(164, 34));
        campoTipo.addActionListener(e -> aplicarFiltros());
        zonaTipo.add(tipoLabel, BorderLayout.WEST);
        zonaTipo.add(campoTipo, BorderLayout.CENTER);

        filtros.add(zonaBusqueda, BorderLayout.CENTER);
        filtros.add(zonaTipo, BorderLayout.EAST);
        superior.add(filtros);
        contenido.add(superior, BorderLayout.NORTH);

        // Tabla, ordenación y contador de resultados.
        JPanel superficieTabla = crearSuperficie();
        superficieTabla.setLayout(new BorderLayout(0, 12));
        JLabel tituloTabla = new JLabel("Catálogo de medicamentos");
        tituloTabla.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tituloTabla.setForeground(TEXTO);
        resultadosFiltro = new JLabel("0 resultados");
        resultadosFiltro.setForeground(SUBTEXTO);
        JPanel cabeceraTabla = new JPanel(new BorderLayout());
        cabeceraTabla.setOpaque(false);
        cabeceraTabla.add(tituloTabla, BorderLayout.WEST);
        cabeceraTabla.add(resultadosFiltro, BorderLayout.EAST);
        superficieTabla.add(cabeceraTabla, BorderLayout.NORTH);

        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(43);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(new Color(239, 243, 246));
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(Color.WHITE);
        tabla.setRowSorter(ordenador);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 42));
        ordenador.addRowSorterListener(e -> actualizarContador());
        tabla.getColumnModel().getColumn(0).setPreferredWidth(115);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(315);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(115);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(128);

        // Mostrar precios como moneda, manteniendo Double en el modelo.
        DefaultTableCellRenderer moneda = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number numero) {
                    setText(String.format(Locale.forLanguageTag("es-MX"),
                            "$%,.2f", numero.doubleValue()));
                } else {
                    super.setValue(value);
                }
            }
        };
        moneda.setHorizontalAlignment(SwingConstants.RIGHT);
        tabla.getColumnModel().getColumn(3).setCellRenderer(moneda);

        // Acentos solo en la presentación (sin modificar el tipo del modelo).
        DefaultTableCellRenderer tipos = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                String tipo = String.valueOf(value);
                switch (normalizar(tipo)) {
                    case "generico" -> setText("Genérico");
                    case "biologico" -> setText("Biológico");
                    default -> setText(tipo);
                }
            }
        };
        tabla.getColumnModel().getColumn(2).setCellRenderer(tipos);

        JScrollPane desplazar = new JScrollPane(tabla);
        desplazar.setBorder(BorderFactory.createEmptyBorder());
        desplazar.getViewport().setBackground(Color.WHITE);
        superficieTabla.add(desplazar, BorderLayout.CENTER);
        contenido.add(superficieTabla, BorderLayout.CENTER);

        JLabel ayuda = new JLabel("Selecciona un medicamento para editarlo o retirarlo del catálogo.");
        ayuda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ayuda.setForeground(SUBTEXTO);
        contenido.add(ayuda, BorderLayout.SOUTH);

        // Evitar acciones sin selección.
        editar.setEnabled(false);
        retirar.setEnabled(false);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean seleccionado = tabla.getSelectedRow() >= 0;
                editar.setEnabled(seleccionado);
                retirar.setEnabled(seleccionado);
            }
        });

        // Navegación lateral: cada sección conserva su panel y sus funciones.
        CardLayout gestorPantallas = new CardLayout();
        JPanel pantallas = new JPanel(gestorPantallas);
        pantallas.add(new PanelReportes(farmacia), "Inicio");
        pantallas.add(contenido, "Inventario");
        pantallas.add(new PanelClientes(farmacia), "Clientes");
        pantallas.add(new PanelVentas(farmacia), "Ventas");
        pantallas.add(new PanelMonitor(farmacia), "Monitor");
        pantallas.add(new PanelRespaldos(farmacia), "Respaldos");

        // Colores del menú lateral.
        Color fondoMenu = new Color(19, 48, 62);
        Color colorActivo = new Color(25, 115, 112);
        Color textoSuave = new Color(176, 206, 214);

        JPanel menuLateral = new JPanel();
        menuLateral.setLayout(new BoxLayout(menuLateral, BoxLayout.Y_AXIS));
        menuLateral.setBackground(fondoMenu);
        menuLateral.setBorder(BorderFactory.createEmptyBorder(26, 12, 18, 12));
        menuLateral.setPreferredSize(new Dimension(212, 0));

        JLabel marca = new JLabel("P7 FARMACIA");
        marca.setFont(new Font("Segoe UI", Font.BOLD, 20));
        marca.setForeground(Color.WHITE);
        marca.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuLateral.add(marca);

        JLabel subtitulo = new JLabel("Gestión de farmacia");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitulo.setForeground(textoSuave);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuLateral.add(Box.createVerticalStrut(5));
        menuLateral.add(subtitulo);
        menuLateral.add(Box.createVerticalStrut(32));

        String[] secciones = {
                "Inicio", "Inventario", "Clientes",
                "Ventas", "Monitor", "Respaldos"
        };
        LinkedHashMap<String, JButton> botonesMenu = new LinkedHashMap<>();

        for (String seccion : secciones) {
            JButton boton = new JButton(seccion);
            boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            boton.setForeground(Color.WHITE);
            boton.setBackground(seccion.equals("Inicio") ? colorActivo : fondoMenu);
            boton.setHorizontalAlignment(SwingConstants.LEFT);
            boton.setAlignmentX(Component.LEFT_ALIGNMENT);
            boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            boton.setPreferredSize(new Dimension(185, 44));
            boton.setFocusPainted(false);
            boton.setBorderPainted(false);
            boton.setOpaque(true);
            boton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 8));
            botonesMenu.put(seccion, boton);

            boton.addActionListener(e -> {
                if ("Inventario".equals(seccion)) {
                    cargarProductos();
                }
                gestorPantallas.show(pantallas, seccion);
                botonesMenu.forEach((nombre, botonMenu) ->
                        botonMenu.setBackground(
                                nombre.equals(seccion) ? colorActivo : fondoMenu
                        )
                );
            });

            menuLateral.add(boton);
            menuLateral.add(Box.createVerticalStrut(7));
        }

        menuLateral.add(Box.createVerticalGlue());
        JLabel pieMenu = new JLabel("Sistema de escritorio · Java 21");
        pieMenu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pieMenu.setForeground(textoSuave);
        pieMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuLateral.add(pieMenu);

        JPanel principal = new JPanel(new BorderLayout());
        principal.add(menuLateral, BorderLayout.WEST);
        principal.add(pantallas, BorderLayout.CENTER);
        setContentPane(principal);

        // Dashboard como pantalla inicial; inventario preparado para usarse.
        gestorPantallas.show(pantallas, "Inicio");
        setSize(1300, 760);
        setLocationRelativeTo(null);
        cargarProductos();
    }

    /**
     * Carga los productos registrados en el catálogo de farmacia.
     *
     * <p>Obtiene los medicamentos mediante la fachada
     * {@link Farmacia}, los ordena por su clave SKU y
     * actualiza las filas de la tabla del inventario.</p>
     *
     * <p>Después de cargar los registros, actualiza las
     * tarjetas estadísticas y aplica los filtros activos.</p>
     */

    // Reutiliza la capa de negocio; filtrar no modifica SQLite.
    private void cargarProductos() {
        modelo.setRowCount(0);
        for (Producto p : farmacia.catalogo()
                .ordenados(Comparator.comparing(Producto::clave))) {
            modelo.addRow(new Object[]{
                    p.clave(), p.nombre(), p.tipo(), p.precioBase()
            });
        }
        actualizarTarjetas();
        aplicarFiltros();
    }

    /**
     * Crea un panel visual con fondo blanco y bordes personalizados.
     *
     * @return un JPanel configurado para contener
     *         componentes de la interfaz gráfica.
     */

    private static JPanel crearSuperficie() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        return panel;
    }


    /**
     * Crea una tarjeta informativa para el panel de inventario.
     *
     * <p>La tarjeta muestra un título y un indicador numérico,
     * utilizando un color de acento para resaltar su valor.</p>
     *
     * @param titulo texto descriptivo de la tarjeta.
     * @param valor etiqueta que muestra el indicador numérico.
     * @param acento color utilizado para destacar el valor.
     * @return un JPanel con la tarjeta configurada.
     */

    private static JPanel crearTarjeta(String titulo, JLabel valor, Color acento) {
        JPanel tarjeta = crearSuperficie();
        tarjeta.setLayout(new BorderLayout(0, 4));
        JLabel texto = new JLabel(titulo);
        texto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        texto.setForeground(SUBTEXTO);
        valor.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valor.setForeground(acento);
        tarjeta.add(texto, BorderLayout.NORTH);
        tarjeta.add(valor, BorderLayout.CENTER);
        return tarjeta;
    }


    /**
     * Actualiza los indicadores estadísticos del inventario.
     *
     * <p>Recorre los productos cargados en el modelo de la tabla
     * y calcula la cantidad total de medicamentos de patente,
     * genéricos y biológicos.</p>
     *
     * <p>Los resultados se muestran en las tarjetas
     * informativas de la interfaz.</p>
     */

    private void actualizarTarjetas() {
        int patentes = 0;
        int genericos = 0;
        int biologicos = 0;
        for (int fila = 0; fila < modelo.getRowCount(); fila++) {
            String tipo = normalizar(String.valueOf(modelo.getValueAt(fila, 2)));
            switch (tipo) {
                case "patente" -> patentes++;
                case "generico" -> genericos++;
                case "biologico" -> biologicos++;
            }
        }
        totalCatalogo.setText(String.valueOf(modelo.getRowCount()));
        totalPatentes.setText(String.valueOf(patentes));
        totalGenericos.setText(String.valueOf(genericos));
        totalBiologicos.setText(String.valueOf(biologicos));
    }

    /**
     * Aplica los filtros de búsqueda sobre la tabla del inventario.
     *
     * <p>Permite localizar medicamentos mediante su nombre
     * o clave SKU y filtrar los resultados por tipo de producto.</p>
     *
     * <p>La búsqueda ignora diferencias entre mayúsculas,
     * minúsculas y acentos, sin modificar los registros
     * almacenados en el catálogo.</p>
     */

    private void aplicarFiltros() {
        if (campoBuscar == null || campoTipo == null) return;
        String texto = normalizar(campoBuscar.getText().strip());
        String tipoSeleccionado = normalizar(String.valueOf(campoTipo.getSelectedItem()));

        ordenador.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entrada) {
                String sku = normalizar(String.valueOf(entrada.getValue(0)));
                String nombre = normalizar(String.valueOf(entrada.getValue(1)));
                String tipo = normalizar(String.valueOf(entrada.getValue(2)));
                boolean coincideTexto = sku.contains(texto) || nombre.contains(texto);
                boolean coincideTipo = "todos los tipos".equals(tipoSeleccionado)
                        || tipo.equals(tipoSeleccionado);
                return coincideTexto && coincideTipo;
            }
        });
        actualizarContador();
    }


    /**
     * Actualiza el contador de productos visibles en la tabla.
     *
     * <p>Muestra la cantidad de resultados obtenidos después
     * de aplicar los filtros de búsqueda y clasificación.</p>
     */

    private void actualizarContador() {
        if (resultadosFiltro != null && tabla != null) {
            int cantidad = tabla.getRowCount();
            resultadosFiltro.setText(cantidad == 1 ? "1 resultado" : cantidad + " resultados");
        }
    }


    /**
     * Normaliza una cadena de texto para facilitar las búsquedas.
     *
     * <p>Elimina las marcas de acentuación y convierte
     * los caracteres a minúsculas utilizando Locale.ROOT.</p>
     *
     * @param s cadena de texto que se desea normalizar.
     * @return cadena normalizada sin acentos y en minúsculas.
     */

    private static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }


    /**
     * Registra un medicamento de patente en el catálogo.
     *
     * <p>Solicita al usuario el nombre, precio base y laboratorio
     * del medicamento mediante un formulario Swing.</p>
     *
     * <p>Convierte el precio a un valor numérico y utiliza
     * la fachada {@link Farmacia} junto con la referencia
     * al constructor {@link Producto.Patente#Patente(
     * String, double, String)} para registrar el producto.</p>
     *
     * <p>Después de un registro exitoso, actualiza la tabla
     * del inventario y muestra la clave SKU generada.</p>
     *
     * <p>Los errores de formato y de reglas de negocio
     * se presentan mediante cuadros de diálogo.</p>
     */

    private void registrarPatente() {

        JTextField campoNombre = new JTextField(20);
        JTextField campoPrecio = new JTextField(10);
        JTextField campoLaboratorio = new JTextField(20);

        JPanel formulario = new JPanel(new GridLayout(3, 2, 8, 8));

        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        formulario.add(new JLabel("Precio base:"));
        formulario.add(campoPrecio);

        formulario.add(new JLabel("Laboratorio:"));
        formulario.add(campoLaboratorio);

        int opcion = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Registrar medicamento de patente",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            //Obtener nombre del medicamento
            String nombre = campoNombre.getText().strip();

            //Obtener el laboratorio
            String laboratorio = campoLaboratorio.getText().strip();

            //Convertir el precio de String a double
            double precio = Double.parseDouble(
                    campoPrecio.getText().strip()
            );

            //Registrar el producto mediante Farmacia
            Producto.Patente producto = farmacia.registrar(
                    Producto.Patente::new,
                    nombre,
                    precio,
                    laboratorio
            );

            // Actualizar la tabla de inventario
            cargarProductos();

            //Mostrar mensaje de registro exitoso
            JOptionPane.showMessageDialog(
                    this,
                    "Producto registrado correctamente.\nSKU: " + producto.clave(),
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException ex) {

            // Error si el precio no es un número válido
            JOptionPane.showMessageDialog(
                    this,
                    "El precio debe ser un número válido.",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (ReglaNegocioException ex) {

            // Error si no se cumplen las reglas del producto
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE
            );
        }

    }


    /**
     * Permite modificar los datos de un producto existente.
     *
     * <p>Obtiene el producto seleccionado en la tabla del
     * inventario y consulta su información mediante
     * la fachada {@link Farmacia}.</p>
     *
     * <p>Muestra un formulario con el nombre y precio base
     * actuales para que el usuario pueda modificarlos.</p>
     *
     * <p>Valida que el nombre no esté vacío y que el precio
     * sea finito y no negativo. Los cambios se guardan
     * utilizando el método actualizar de la fachada.</p>
     *
     * <p>Después de una actualización exitosa, refresca
     * la tabla y muestra un mensaje de confirmación.</p>
     *
     * <p>Los errores de formato, validación y persistencia
     * se notifican mediante cuadros de diálogo.</p>
     */

    private void editarProducto() {

        // Obtener la fila seleccionada
        int fila = tabla.getSelectedRow();

        // Comprobar que se seleccionó un producto
        if (fila == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona un producto de la tabla.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        //Obtener la clave del producto
        int filaModelo = tabla.convertRowIndexToModel(fila);

        String sku = modelo.getValueAt(filaModelo, 0).toString();

        try {
            //Buscar el producto mediante Farmacia
            Producto producto = farmacia.elemento(sku);

            //Mostrar los valores actuales
            JTextField campoNombre =
                    new JTextField(producto.nombre(), 20);

            JTextField campoPrecio =
                    new JTextField(
                            String.valueOf(producto.precioBase()), 10
                    );

            // Construir el formulario
            JPanel formulario =
                    new JPanel(new GridLayout(2, 2, 8, 8));

            formulario.add(new JLabel("Nombre:"));
            formulario.add(campoNombre);

            formulario.add(new JLabel("Precio base:"));
            formulario.add(campoPrecio);

            // PASO 7: Mostrar formulario
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    formulario,
                    "Editar producto: " + sku,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }

            // PASO 8: Recuperar nuevos valores
            String nombre = campoNombre.getText().strip();

            double precio = Double.parseDouble(
                    campoPrecio.getText().strip()
            );

            // PASO 9: Validar antes de modificar el objeto
            if (nombre.isBlank()) {
                throw new ReglaNegocioException(
                        "El nombre no puede estar vacío"
                );
            }

            if (!Double.isFinite(precio) || precio < 0) {
                throw new ReglaNegocioException(
                        "El precio debe ser finito y no negativo"
                );
            }

            // PASO 10: Actualizar mediante la fachada
            farmacia.actualizar(producto, nombre, precio);

            // PASO 11: Actualizar la tabla
            cargarProductos();

            // PASO 12: Confirmar operación
            JOptionPane.showMessageDialog(
                    this,
                    "Producto actualizado: " + sku,
                    "Actualización exitosa",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "El precio debe ser un número válido.",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE
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
                    "No se pudieron guardar los cambios: "
                            + ex.getMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Permite retirar un medicamento del catálogo.
     *
     * <p>Obtiene la clave SKU y el nombre del producto
     * seleccionado en la tabla del inventario.</p>
     *
     * <p>Solicita confirmación al usuario antes de ejecutar
     * la operación de retiro mediante la fachada
     * {@link Farmacia}.</p>
     *
     * <p>Si el producto se retira correctamente, actualiza
     * la tabla y muestra un mensaje de confirmación.</p>
     *
     * <p>Si el producto no existe o incumple alguna regla
     * de negocio que impide su eliminación, presenta
     * el mensaje correspondiente sin cerrar la aplicación.</p>
     */

    private void retirarProducto() {

        // Obtener la fila seleccionada
        int fila = tabla.getSelectedRow();

        // Comprobar si se seleccionó un producto
        if (fila == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona un producto para retirar.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        //Obtener la clave y nombre del producto
        int filaModelo = tabla.convertRowIndexToModel(fila);

        String sku = modelo.getValueAt(filaModelo, 0).toString();
        String nombre = modelo.getValueAt(filaModelo, 1).toString();

        // Solicitar confirmación
        int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas retirar el producto?\n"
                        + "SKU: " + sku + "\n"
                        + "Nombre: " + nombre,
                "Confirmar retiro",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        // Cancelar si el usuario selecciona NO
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            //Convertir String a Sku y retirar
            boolean eliminado = farmacia.retirar(Sku.parse(sku));

            //Comprobar el resultado
            if (eliminado) {

                // Actualizar tabla
                cargarProductos();

                JOptionPane.showMessageDialog(
                        this,
                        "Producto retirado: " + sku,
                        "Retiro exitoso",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "No se encontró el producto.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (ReglaNegocioException ex) {

            // El producto podría estar asociado a una venta
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "No se puede retirar",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (PersistenciaException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error al retirar el producto: " + ex.getMessage(),
                    "Error de persistencia",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void registrarProducto() {

        String[] tipos = {
                "Patente",
                "Genérico",
                "Biológico"
        };

        String seleccion = (String) JOptionPane.showInputDialog(
                this,
                "Selecciona el tipo de medicamento:",
                "Registrar producto",
                JOptionPane.QUESTION_MESSAGE,
                null,
                tipos,
                tipos[0]
        );

        if (seleccion == null) {
            return;
        }

        switch (seleccion) {
            case "Patente" -> registrarPatente();
            case "Genérico" -> registrarGenerico();
            case "Biológico" -> registrarBiologico();
        }
    }


    /**
     * Registra un medicamento genérico en el catálogo.
     *
     * <p>Solicita el nombre, precio base y concentración
     * del medicamento expresada en miligramos.</p>
     *
     * <p>Valida que el nombre no esté vacío, que el precio
     * sea finito y no negativo, y que la concentración
     * sea mayor que cero.</p>
     *
     * <p>Utiliza la fachada {@link Farmacia} y la referencia
     * al constructor {@link Producto.Generico} para realizar
     * el registro y generar automáticamente su clave SKU.</p>
     *
     * <p>Actualiza el inventario y muestra un mensaje de
     * confirmación. Los errores se presentan al usuario
     * mediante cuadros de diálogo.</p>
     */

    private void registrarGenerico() {

        // PASO 1: Crear los campos
        JTextField campoNombre = new JTextField(20);
        JTextField campoPrecio = new JTextField(10);
        JTextField campoConcentracion = new JTextField(10);

        // PASO 2: Organizar los campos
        JPanel formulario = new JPanel(
                new GridLayout(3, 2, 8, 8)
        );

        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        formulario.add(new JLabel("Precio base:"));
        formulario.add(campoPrecio);

        formulario.add(new JLabel("Concentración (mg):"));
        formulario.add(campoConcentracion);

        // PASO 3: Mostrar formulario
        int opcion = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Registrar medicamento genérico",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            // PASO 4: Recuperar los datos
            String nombre = campoNombre.getText().strip();

            double precio = Double.parseDouble(
                    campoPrecio.getText().strip()
            );

            int concentracion = Integer.parseInt(
                    campoConcentracion.getText().strip()
            );

            // PASO 5: Validar antes de construir
            if (nombre.isBlank()) {
                throw new ReglaNegocioException(
                        "El nombre es obligatorio"
                );
            }

            if (!Double.isFinite(precio) || precio < 0) {
                throw new ReglaNegocioException(
                        "El precio debe ser finito y no negativo"
                );
            }

            if (concentracion <= 0) {
                throw new ReglaNegocioException(
                        "La concentración debe ser positiva"
                );
            }

            // PASO 6: Registrar mediante Farmacia
            Producto.Generico producto = farmacia.registrar(
                    Producto.Generico::new,
                    nombre,
                    precio,
                    concentracion
            );

            // PASO 7: Actualizar el inventario
            cargarProductos();

            // PASO 8: Confirmar registro
            JOptionPane.showMessageDialog(
                    this,
                    "Genérico registrado correctamente.\n"
                            + "SKU: " + producto.clave(),
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "El precio debe ser numérico y "
                            + "la concentración un número entero.",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (ReglaNegocioException |
                 PersistenciaException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "No se pudo registrar el genérico",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    /**
     * Registra un medicamento biológico en el catálogo.
     *
     * <p>Solicita el nombre, precio base y temperatura
     * máxima de conservación del medicamento.</p>
     *
     * <p>Valida que el nombre no esté vacío, que el precio
     * sea finito y no negativo, y que la temperatura
     * esté comprendida entre 2 y 8 grados Celsius.</p>
     *
     * <p>El registro se realiza mediante la fachada
     * {@link Farmacia}, utilizando la referencia
     * al constructor {@link Producto.Biologico}.</p>
     *
     * <p>Al finalizar correctamente, actualiza el inventario
     * y muestra la clave SKU del medicamento registrado.</p>
     *
     * <p>Los errores de validación, formato y persistencia
     * se muestran mediante mensajes comprensibles.</p>
     */

    private void registrarBiologico() {

        // PASO 1: Crear los campos
        JTextField campoNombre = new JTextField(20);
        JTextField campoPrecio = new JTextField(10);
        JTextField campoTemperatura = new JTextField(10);

        // PASO 2: Crear el formulario
        JPanel formulario = new JPanel(
                new GridLayout(3, 2, 8, 8)
        );

        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        formulario.add(new JLabel("Precio base:"));
        formulario.add(campoPrecio);

        formulario.add(new JLabel("Temperatura máxima (°C):"));
        formulario.add(campoTemperatura);

        // PASO 3: Mostrar el formulario
        int opcion = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Registrar medicamento biológico",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            // PASO 4: Obtener los datos
            String nombre = campoNombre.getText().strip();

            double precio = Double.parseDouble(
                    campoPrecio.getText().strip()
            );

            int temperatura = Integer.parseInt(
                    campoTemperatura.getText().strip()
            );

            // PASO 5: Validaciones
            if (nombre.isBlank()) {
                throw new ReglaNegocioException(
                        "El nombre es obligatorio"
                );
            }

            if (!Double.isFinite(precio) || precio < 0) {
                throw new ReglaNegocioException(
                        "El precio debe ser finito y no negativo"
                );
            }

            if (temperatura < 2 || temperatura > 8) {
                throw new ReglaNegocioException(
                        "La temperatura de conservación debe estar entre 2 y 8 °C"
                );
            }

            // PASO 6: Registrar mediante la fachada
            Producto.Biologico producto = farmacia.registrar(
                    Producto.Biologico::new,
                    nombre,
                    precio,
                    temperatura
            );

            // PASO 7: Actualizar inventario
            cargarProductos();

            // PASO 8: Mostrar confirmación
            JOptionPane.showMessageDialog(
                    this,
                    "Biológico registrado correctamente.\n"
                            + "SKU: " + producto.clave(),
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "El precio debe ser numérico y "
                            + "la temperatura un número entero.",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (ReglaNegocioException |
                 PersistenciaException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "No se pudo registrar el biológico",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


}
