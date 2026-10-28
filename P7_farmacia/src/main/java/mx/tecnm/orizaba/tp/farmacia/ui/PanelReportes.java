package mx.tecnm.orizaba.tp.farmacia.ui;

import mx.tecnm.orizaba.tp.farmacia.modelo.Cliente;
import mx.tecnm.orizaba.tp.farmacia.modelo.Estado;
import mx.tecnm.orizaba.tp.farmacia.modelo.Producto;
import mx.tecnm.orizaba.tp.farmacia.servicio.Farmacia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.DoubleSummaryStatistics;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Panel gráfico que presenta los reportes estadísticos de la farmacia.
 *
 * <p>Muestra indicadores del catálogo, clientes registrados,
 * ventas totales, ventas pagadas e ingresos acumulados.</p>
 *
 * <p>Incluye gráficas de distribución de ventas por estado,
 * ingresos por tipo de medicamento, producto más demandado
 * y clientes con mayores importes acumulados.</p>
 *
 * <p>Los datos se consultan mediante la fachada
 * {@link Farmacia}, evitando duplicar las reglas
 * de negocio dentro de la interfaz Swing.</p>
 *
 * @see Farmacia
 * @see Estado
 * @see Producto
 * @see Cliente
 */

public class PanelReportes extends JPanel {

    private static final Color FONDO = new Color(244, 247, 250);
    private static final Color BLANCO = Color.WHITE;
    private static final Color TEXTO = new Color(30, 53, 66);
    private static final Color SECUNDARIO = new Color(105, 123, 135);
    private static final Color BORDE = new Color(225, 232, 236);
    private static final Color VERDE = new Color(29, 139, 122);
    private static final Color AZUL = new Color(53, 112, 169);
    private static final Color VIOLETA = new Color(119, 93, 177);
    private static final Color DORADO = new Color(190, 132, 55);

    private final Farmacia farmacia;

    // Conservamos las etiquetas: el botón Actualizar cambia sus valores,
    // sin reconstruir todo el panel ni colocar números fijos.
    private final JLabel valorProductos = etiquetaValor();
    private final JLabel valorClientes = etiquetaValor();
    private final JLabel valorVentas = etiquetaValor();
    private final JLabel valorPagadas = etiquetaValor();
    private final JLabel valorIngresos = etiquetaValor();
    private final JLabel productoDemandado = new JLabel("Sin ventas pagadas");
    private final JTextArea mejoresClientes = new JTextArea();
    private final JLabel ultimaActualizacion = new JLabel("");

    private final Map<Estado, FilaBarra> barrasEstados = new EnumMap<>(Estado.class);
    private final Map<String, FilaBarra> barrasIngresos = new LinkedHashMap<>();

    /**
     * Construye e inicializa el panel de reportes de la farmacia.
     *
     * <p>Configura el encabezado, las tarjetas de indicadores,
     * las gráficas de ventas e ingresos y las secciones
     * de productos y clientes destacados.</p>
     *
     * <p>Incorpora desplazamiento vertical para adaptar
     * el contenido a diferentes tamaños de ventana.</p>
     *
     * <p>Al finalizar la construcción, carga los reportes
     * utilizando los datos proporcionados por la fachada.</p>
     *
     * @param farmacia instancia de la fachada utilizada
     *                 para consultar los reportes del sistema.
     */

    public PanelReportes(Farmacia farmacia) {
        super(new BorderLayout(0, 16));
        this.farmacia = farmacia;
        setOpaque(true);
        setBackground(FONDO);
        setBorder(new EmptyBorder(22, 24, 18, 24));

        add(crearEncabezado(), BorderLayout.NORTH);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JPanel indicadores = crearTarjetasResumen();
        indicadores.setAlignmentX(Component.LEFT_ALIGNMENT);
        indicadores.setPreferredSize(new Dimension(900, 116));
        indicadores.setMaximumSize(new Dimension(Integer.MAX_VALUE, 116));
        contenido.add(indicadores);
        contenido.add(Box.createVerticalStrut(16));

        JPanel visualizaciones = new JPanel(new GridLayout(1, 2, 16, 0));
        visualizaciones.setOpaque(false);
        visualizaciones.add(crearPanelEstados());
        visualizaciones.add(crearPanelIngresos());
        visualizaciones.setAlignmentX(Component.LEFT_ALIGNMENT);
        visualizaciones.setPreferredSize(new Dimension(900, 300));
        visualizaciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        contenido.add(visualizaciones);
        contenido.add(Box.createVerticalStrut(16));

        JPanel resumen = crearPanelResumenInferior();
        resumen.setAlignmentX(Component.LEFT_ALIGNMENT);
        resumen.setPreferredSize(new Dimension(900, 150));
        resumen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        contenido.add(resumen);

        // Scroll vertical para pantallas pequeñas, sin scroll horizontal.
        JScrollPane desplazamiento = new JScrollPane(
                new ContenidoAdaptable(contenido));
        desplazamiento.setBorder(null);
        desplazamiento.setOpaque(false);
        desplazamiento.getViewport().setOpaque(false);
        desplazamiento.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        desplazamiento.getVerticalScrollBar().setUnitIncrement(18);
        add(desplazamiento, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        JLabel nota = new JLabel("Ingresos y productos destacados: solo ventas pagadas.");
        nota.setForeground(SECUNDARIO);
        nota.setFont(fuente(Font.PLAIN, 12));
        pie.add(nota, BorderLayout.WEST);
        ultimaActualizacion.setForeground(SECUNDARIO);
        ultimaActualizacion.setFont(fuente(Font.PLAIN, 12));
        pie.add(ultimaActualizacion, BorderLayout.EAST);
        add(pie, BorderLayout.SOUTH);

        cargarReportes();
    }

    /**
     * Construye el encabezado del panel de reportes.
     *
     * <p>Incluye el título principal, una descripción
     * de los indicadores y un botón que permite
     * actualizar manualmente la información.</p>
     *
     * @return panel Swing que contiene el encabezado
     *         y los controles de actualización.
     */

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout(12, 0));
        encabezado.setOpaque(false);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Resumen general de la farmacia");
        titulo.setForeground(TEXTO);
        titulo.setFont(fuente(Font.BOLD, 24));

        JLabel subtitulo = new JLabel("Indicadores, ventas e información importante del negocio");
        subtitulo.setForeground(SECUNDARIO);
        subtitulo.setFont(fuente(Font.PLAIN, 13));

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(subtitulo);
        encabezado.add(textos, BorderLayout.WEST);

        JButton actualizar = new JButton("Actualizar reportes");
        actualizar.setFont(fuente(Font.BOLD, 13));
        actualizar.setBackground(VERDE);
        actualizar.setForeground(Color.WHITE);
        actualizar.setFocusPainted(false);
        actualizar.setPreferredSize(new Dimension(180, 39));
        actualizar.addActionListener(e -> cargarReportes());
        encabezado.add(actualizar, BorderLayout.EAST);
        return encabezado;
    }

    /**
     * Crea las tarjetas con los indicadores generales de la farmacia.
     *
     * <p>Organiza cinco indicadores: productos registrados,
     * clientes, ventas totales, ventas pagadas e ingresos
     * correspondientes a ventas cerradas.</p>
     *
     * <p>Utiliza un diseño de cuadrícula para distribuir
     * las tarjetas horizontalmente.</p>
     *
     * @return panel con las cinco tarjetas estadísticas.
     */

    private JPanel crearTarjetasResumen() {
        JPanel tarjetas = new JPanel(new GridLayout(1, 5, 12, 0));
        tarjetas.setOpaque(false);
        tarjetas.add(tarjetaIndicador("Productos", valorProductos, 0, AZUL));
        tarjetas.add(tarjetaIndicador("Clientes", valorClientes, 1, VIOLETA));
        tarjetas.add(tarjetaIndicador("Ventas totales", valorVentas, 2, DORADO));
        tarjetas.add(tarjetaIndicador("Ventas pagadas", valorPagadas, 3, VERDE));
        tarjetas.add(tarjetaIndicador("Ingresos pagados", valorIngresos, 4, VERDE));
        return tarjetas;
    }

    /**
     * Construye una tarjeta visual para mostrar un indicador.
     *
     * <p>La tarjeta contiene un nombre descriptivo, una etiqueta
     * con el valor estadístico y un icono vectorial cuyo color
     * se determina mediante el parámetro de acento.</p>
     *
     * @param nombre título descriptivo del indicador.
     * @param valor etiqueta que muestra el valor del indicador.
     * @param tipoIcono identificador del icono que se dibujará.
     * @param acento color utilizado para destacar el icono.
     * @return panel Swing con la tarjeta configurada.
     */

    private JPanel tarjetaIndicador(String nombre, JLabel valor, int tipoIcono, Color acento) {
        JPanel tarjeta = tarjetaBlanca(new BorderLayout(4, 6));
        tarjeta.setBorder(new EmptyBorder(14, 15, 14, 15));

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        JLabel etiqueta = new JLabel(nombre);
        etiqueta.setForeground(SECUNDARIO);
        etiqueta.setFont(fuente(Font.PLAIN, 12));
        cabecera.add(etiqueta, BorderLayout.WEST);
        cabecera.add(new JLabel(new IconoMetrica(tipoIcono, acento)), BorderLayout.EAST);

        valor.setFont(fuente(Font.BOLD, 25));
        valor.setForeground(TEXTO);
        valor.setHorizontalAlignment(SwingConstants.LEFT);

        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(valor, BorderLayout.CENTER);
        return tarjeta;
    }

    /**
     * Construye la gráfica de distribución de ventas por estado.
     *
     * <p>Genera una fila con barra de progreso para cada
     * estado definido en la enumeración {@link Estado}:
     * abierta, en surtido, pagada y cancelada.</p>
     *
     * <p>Asigna un color diferente a cada estado
     * para facilitar su identificación visual.</p>
     *
     * @return panel que contiene las barras de ventas por estado.
     */

    private JPanel crearPanelEstados() {
        JPanel tarjeta = tarjetaBlanca(new BorderLayout(0, 12));
        tarjeta.setBorder(new EmptyBorder(18, 20, 18, 20));
        tarjeta.add(cabeceraTarjeta("Ventas por estado", "Distribución del total de ventas"),
                BorderLayout.NORTH);

        JPanel filas = new JPanel();
        filas.setOpaque(false);
        filas.setLayout(new BoxLayout(filas, BoxLayout.Y_AXIS));

        for (Estado estado : Estado.values()) {
            Color color = switch (estado) {
                case ABIERTA -> AZUL;
                case EN_CURSO -> DORADO;
                case CERRADA -> VERDE;
                case CANCELADA -> new Color(191, 104, 108);
            };
            FilaBarra fila = new FilaBarra(estado.etiqueta(), color);
            barrasEstados.put(estado, fila);
            filas.add(fila);
            filas.add(Box.createVerticalStrut(11));
        }
        tarjeta.add(filas, BorderLayout.CENTER);
        return tarjeta;
    }

    /**
     * Construye la gráfica de ingresos por tipo de medicamento.
     *
     * <p>Presenta barras horizontales para medicamentos
     * de patente, genéricos y biológicos.</p>
     *
     * <p>Los importes mostrados corresponden a ventas
     * pagadas y consideran los descuentos aplicados
     * por las reglas de negocio.</p>
     *
     * @return panel Swing que contiene la gráfica de ingresos.
     */

    private JPanel crearPanelIngresos() {
        JPanel tarjeta = tarjetaBlanca(new BorderLayout(0, 12));
        tarjeta.setBorder(new EmptyBorder(18, 20, 18, 20));
        tarjeta.add(cabeceraTarjeta("Ingresos por tipo", "Ventas pagadas, con descuentos aplicados"),
                BorderLayout.NORTH);

        JPanel filas = new JPanel();
        filas.setOpaque(false);
        filas.setLayout(new BoxLayout(filas, BoxLayout.Y_AXIS));

        agregarFilaIngreso(filas, "Patente", VERDE);
        agregarFilaIngreso(filas, "Generico", AZUL);
        agregarFilaIngreso(filas, "Biologico", VIOLETA);
        tarjeta.add(filas, BorderLayout.CENTER);
        return tarjeta;
    }

    /**
     * Agrega una fila visual a la gráfica de ingresos.
     *
     * <p>Crea una barra para el tipo de medicamento
     * indicado y establece su color representativo.</p>
     *
     * <p>Convierte los nombres internos de los tipos
     * a etiquetas con acentuación para su presentación.</p>
     *
     * @param filas panel contenedor de las filas de ingresos.
     * @param tipo nombre del tipo de medicamento.
     * @param color color asignado a la barra gráfica.
     */

    private void agregarFilaIngreso(JPanel filas, String tipo, Color color) {
        String visible = switch (tipo) {
            case "Generico" -> "Genérico";
            case "Biologico" -> "Biológico";
            default -> tipo;
        };
        FilaBarra fila = new FilaBarra(visible, color);
        barrasIngresos.put(tipo, fila);
        filas.add(fila);
        filas.add(Box.createVerticalStrut(18));
    }

    /**
     * Construye el resumen inferior del panel de reportes.
     *
     * <p>Incluye dos secciones informativas: el medicamento
     * más demandado por unidades vendidas y los clientes
     * con mayor importe acumulado en ventas pagadas.</p>
     *
     * <p>Los datos se presentan utilizando etiquetas
     * y áreas de texto de solo lectura.</p>
     *
     * @return panel con las secciones de productos
     *         y clientes destacados.
     */

    private JPanel crearPanelResumenInferior() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setOpaque(false);

        JPanel demandado = tarjetaBlanca(new BorderLayout(0, 12));
        demandado.setBorder(new EmptyBorder(18, 20, 18, 20));
        demandado.add(cabeceraTarjeta("Producto más demandado", "Por unidades en ventas pagadas"),
                BorderLayout.NORTH);
        productoDemandado.setFont(fuente(Font.BOLD, 15));
        productoDemandado.setForeground(TEXTO);
        demandado.add(productoDemandado, BorderLayout.CENTER);

        JPanel clientes = tarjetaBlanca(new BorderLayout(0, 12));
        clientes.setBorder(new EmptyBorder(18, 20, 18, 20));
        clientes.add(cabeceraTarjeta("Mejores clientes", "Por importe acumulado en ventas pagadas"),
                BorderLayout.NORTH);
        mejoresClientes.setEditable(false);
        mejoresClientes.setFocusable(false);
        mejoresClientes.setOpaque(false);
        mejoresClientes.setFont(fuente(Font.PLAIN, 14));
        mejoresClientes.setForeground(TEXTO);
        mejoresClientes.setLineWrap(true);
        mejoresClientes.setWrapStyleWord(true);
        clientes.add(mejoresClientes, BorderLayout.CENTER);

        panel.add(demandado);
        panel.add(clientes);
        return panel;
    }

    /**
     * Crea un encabezado reutilizable para las tarjetas.
     *
     * <p>Organiza verticalmente un título principal
     * y un subtítulo descriptivo, utilizando diferentes
     * tamaños y colores de texto.</p>
     *
     * @param titulo texto principal del encabezado.
     * @param subtitulo descripción complementaria del título.
     * @return panel Swing con ambos elementos de texto.
     */

    private JPanel cabeceraTarjeta(String titulo, String subtitulo) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel principal = new JLabel(titulo);
        principal.setFont(fuente(Font.BOLD, 17));
        principal.setForeground(TEXTO);
        JLabel secundario = new JLabel(subtitulo);
        secundario.setFont(fuente(Font.PLAIN, 12));
        secundario.setForeground(SECUNDARIO);
        panel.add(principal);
        panel.add(Box.createVerticalStrut(3));
        panel.add(secundario);
        return panel;
    }

    private static JLabel etiquetaValor() {
        return new JLabel("—");
    }

    private static Font fuente(int estilo, int tamanio) {
        return new Font("Segoe UI", estilo, tamanio);
    }

    private static String dinero(double cantidad) {
        return String.format(Locale.US, "$%,.2f", cantidad);
    }

    private JPanel tarjetaBlanca(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLANCO);
                g2.fill(new RoundRectangle2D.Double(0.5, 0.5,
                        getWidth() - 1.0, getHeight() - 1.0, 18, 18));
                g2.setColor(BORDE);
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5,
                        getWidth() - 1.0, getHeight() - 1.0, 18, 18));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Todos los datos se consultan nuevamente desde Farmacia.
     */
    private void cargarReportes() {
        try {
            DoubleSummaryStatistics estadisticas = farmacia.estadisticas();
            Map<Estado, Long> estados = farmacia.conteoPorEstado();
            Map<String, Double> ingresos = farmacia.ingresosPorTipo();

            int totalVentas = farmacia.registros().size();
            valorProductos.setText(String.valueOf(farmacia.catalogo().tamanio()));
            valorClientes.setText(String.valueOf(farmacia.clientes().size()));
            valorVentas.setText(String.valueOf(totalVentas));
            valorPagadas.setText(String.valueOf(estadisticas.getCount()));
            valorIngresos.setText(dinero(estadisticas.getSum()));

            for (Estado estado : Estado.values()) {
                long cantidad = estados.getOrDefault(estado, 0L);
                double fraccion = totalVentas == 0 ? 0.0 : (double) cantidad / totalVentas;
                barrasEstados.get(estado).actualizar(
                        String.valueOf(cantidad), fraccion);
            }

            double ingresosMaximos = ingresos.values().stream()
                    .mapToDouble(Double::doubleValue).max().orElse(0.0);
            for (Map.Entry<String, FilaBarra> entrada : barrasIngresos.entrySet()) {
                double importe = ingresos.getOrDefault(entrada.getKey(), 0.0);
                double fraccion = ingresosMaximos == 0 ? 0.0 : importe / ingresosMaximos;
                entrada.getValue().actualizar(dinero(importe), fraccion);
            }

            Optional<Producto> masDemandado = farmacia.masDemandado();
            String nombreProducto = masDemandado
                    .map(p -> p.clave() + "  ·  " + p.nombre())
                    .orElse("Aún no hay ventas pagadas");
            productoDemandado.setText(nombreProducto);
            productoDemandado.setToolTipText(nombreProducto);

            List<Cliente> mejores = farmacia.mejores(3);
            if (mejores.isEmpty()) {
                mejoresClientes.setText("Aún no hay ventas pagadas");
            } else {
                StringBuilder nombres = new StringBuilder();
                for (int i = 0; i < mejores.size(); i++) {
                    if (i > 0) nombres.append('\n');
                    nombres.append(i + 1).append(".  ").append(mejores.get(i).nombre());
                }
                mejoresClientes.setText(nombres.toString());
            }
            ultimaActualizacion.setText("Información actualizada");

        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudieron actualizar los reportes:\n" + ex.getMessage(),
                    "Error al consultar datos",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Fila compacta con cifra y barra visual de proporcionalidad.
     */
    private static final class FilaBarra extends JPanel {
        private final JLabel valor = new JLabel("0");
        private final Barra barra;

        FilaBarra(String nombre, Color color) {
            super(new BorderLayout(0, 6));
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

            JPanel cabecera = new JPanel(new BorderLayout());
            cabecera.setOpaque(false);
            JLabel etiqueta = new JLabel(nombre);
            etiqueta.setFont(fuente(Font.PLAIN, 13));
            etiqueta.setForeground(TEXTO);
            valor.setFont(fuente(Font.BOLD, 13));
            valor.setForeground(TEXTO);
            cabecera.add(etiqueta, BorderLayout.WEST);
            cabecera.add(valor, BorderLayout.EAST);
            add(cabecera, BorderLayout.NORTH);

            barra = new Barra(color);
            add(barra, BorderLayout.CENTER);
        }

        void actualizar(String texto, double proporcion) {
            valor.setText(texto);
            barra.setProporcion(proporcion);
        }
    }

    /**
     * Barra redondeada: proporción entre 0 y 1, sin bibliotecas adicionales.
     */
    private static final class Barra extends JComponent {
        private final Color color;
        private double proporcion;

        Barra(Color color) {
            this.color = color;
            setPreferredSize(new Dimension(200, 9));
            setMinimumSize(new Dimension(20, 9));
        }

        void setProporcion(double proporcion) {
            this.proporcion = Math.max(0, Math.min(1, proporcion));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int alto = Math.min(9, getHeight());
            int arriba = (getHeight() - alto) / 2;
            g2.setColor(new Color(229, 236, 239));
            g2.fillRoundRect(0, arriba, getWidth(), alto, alto, alto);
            if (proporcion > 0) {
                int ancho = Math.max(alto, (int) Math.round(getWidth() * proporcion));
                g2.setColor(color);
                g2.fillRoundRect(0, arriba, Math.min(ancho, getWidth()), alto, alto, alto);
            }
            g2.dispose();
        }
    }

    /**
     * Íconos vectoriales básicos para mantener la UI sin recursos externos.
     */
    private static final class IconoMetrica implements Icon {
        private final int tipo;
        private final Color color;

        IconoMetrica(int tipo, Color color) {
            this.tipo = tipo;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 25;
        }

        @Override
        public int getIconHeight() {
            return 25;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D d = (Graphics2D) g.create();
            d.translate(x, y);
            d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            d.setColor(color);
            d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (tipo) {
                case 0 -> { // paquete
                    d.drawRoundRect(4, 6, 17, 15, 3, 3);
                    d.drawLine(4, 11, 21, 11);
                    d.drawLine(12, 6, 12, 15);
                }
                case 1 -> { // personas
                    d.drawOval(9, 3, 8, 8);
                    d.drawArc(4, 12, 18, 12, 0, 180);
                }
                case 2 -> { // recibo
                    d.drawRoundRect(6, 3, 13, 19, 2, 2);
                    d.drawLine(9, 9, 16, 9);
                    d.drawLine(9, 13, 16, 13);
                    d.drawLine(9, 17, 14, 17);
                }
                case 3 -> { // comprobación
                    d.drawOval(3, 3, 19, 19);
                    d.drawLine(8, 13, 11, 16);
                    d.drawLine(11, 16, 17, 9);
                }
                default -> { // moneda
                    d.drawOval(3, 3, 19, 19);
                    d.setFont(fuente(Font.BOLD, 15));
                    d.drawString("$", 9, 18);
                }
            }
            d.dispose();
        }
    }

    /**
     * Hace que el contenido se ajuste al ancho visible del JScrollPane.
     */
    private static final class ContenidoAdaptable extends JPanel implements Scrollable {
        ContenidoAdaptable(JPanel contenido) {
            super(new BorderLayout());
            setOpaque(false);
            add(contenido, BorderLayout.NORTH);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientacion, int direccion) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientacion, int direccion) {
            return Math.max(60, visible.height - 40);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
