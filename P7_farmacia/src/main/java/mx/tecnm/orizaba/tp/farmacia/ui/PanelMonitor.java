package mx.tecnm.orizaba.tp.farmacia.ui;

import mx.tecnm.orizaba.tp.farmacia.eventos.Evento;
import mx.tecnm.orizaba.tp.farmacia.eventos.OyenteEvento;
import mx.tecnm.orizaba.tp.farmacia.servicio.Farmacia;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

/**
 * Panel gráfico para supervisar los eventos y el monitor
 * de surtido de la farmacia.
 *
 * <p>Permite iniciar y detener el monitor periódico,
 * consultar su estado y ejecutar revisiones manuales
 * de las ventas registradas.</p>
 *
 * <p>Presenta una bitácora de los eventos generados
 * por la fachada {@link Farmacia}, mostrando la hora,
 * el tipo de evento y su descripción.</p>
 *
 * <p>Utiliza {@link SwingUtilities#invokeLater(Runnable)}
 * y {@link SwingWorker} para respetar el hilo de eventos
 * de Swing y evitar bloqueos durante las operaciones
 * de supervisión.</p>
 *
 * @see Farmacia
 * @see Evento
 * @see OyenteEvento
 */

public class PanelMonitor extends JPanel {

    private static final long PERIODO_MS = 5000L;

    private final Farmacia farmacia;
    private final JLabel estado = new JLabel();
    private final JTextArea historial = new JTextArea();
    private final JButton iniciar = new JButton("Iniciar monitor");
    private final JButton detener = new JButton("Detener monitor");
    private final JButton revisar = new JButton("Revisar ahora");
    private final OyenteEvento oyente;

    /**
     * Construye e inicializa el panel de supervisión.
     *
     * <p>Configura los botones para iniciar, detener
     * y revisar el monitor, así como el área de texto
     * destinada al historial de eventos.</p>
     *
     * <p>Registra un oyente mediante la fachada
     * {@link Farmacia} para recibir los eventos
     * y mostrarlos de forma segura en Swing.</p>
     *
     * <p>Al finalizar la inicialización, consulta
     * el estado actual del monitor.</p>
     *
     * @param farmacia instancia de la fachada utilizada
     *                 para gestionar el monitor y sus eventos.
     */

    public PanelMonitor(Farmacia farmacia) {
        super(new BorderLayout(12, 12));
        this.farmacia = farmacia;
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titulo = new JLabel("Monitor de surtido y eventos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(titulo, BorderLayout.NORTH);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controles.add(iniciar);
        controles.add(detener);
        controles.add(revisar);

        JButton actualizarEstado = new JButton("Consultar estado");
        actualizarEstado.addActionListener(e -> actualizarEstado());
        controles.add(actualizarEstado);
        controles.add(estado);

        iniciar.addActionListener(e -> iniciarMonitor());
        detener.addActionListener(e -> detenerMonitor());
        revisar.addActionListener(e -> revisarAhora());

        historial.setEditable(false);
        historial.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        historial.setLineWrap(true);
        historial.setWrapStyleWord(true);

        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.add(controles, BorderLayout.NORTH);
        centro.add(new JScrollPane(historial), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JLabel advertencia = new JLabel(
                "La revisión puede cancelar ventas abiertas y vacías de al menos un día de antigüedad."
        );
        add(advertencia, BorderLayout.SOUTH);

        // Los eventos pueden llegar desde el hilo del monitor: actualizar Swing en el EDT.
        oyente = evento -> SwingUtilities.invokeLater(() -> mostrarEvento(evento));
        farmacia.suscribir(oyente);

        actualizarEstado();
    }

    /**
     * Muestra un evento recibido desde la fachada.
     *
     * <p>Obtiene la hora del evento con el formato HH:mm:ss
     * y agrega al historial su tipo y mensaje.</p>
     *
     * <p>Desplaza el cursor hacia el final del área de texto
     * para mantener visible el evento más reciente
     * y actualiza el estado del monitor.</p>
     *
     * @param evento evento que contiene la fecha y hora,
     *               el tipo y el mensaje que se mostrará.
     */

    private void mostrarEvento(Evento evento) {
        String hora = evento.instante().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        historial.append(hora + " [" + evento.tipo() + "] " + evento.mensaje() + "\n");
        historial.setCaretPosition(historial.getDocument().getLength());
        actualizarEstado();
    }

    /**
     * Actualiza el estado visual del monitor de surtido.
     *
     * <p>Consulta mediante la fachada {@link Farmacia}
     * si el monitor se encuentra activo o detenido.</p>
     *
     * <p>Modifica la etiqueta de estado y habilita
     * o deshabilita los botones de inicio y detención
     * según el resultado obtenido.</p>
     */

    private void actualizarEstado() {
        boolean activo = farmacia.monitorActivo();
        estado.setText(activo ? "Estado: ACTIVO" : "Estado: DETENIDO");
        iniciar.setEnabled(!activo);
        detener.setEnabled(activo);
    }

    /**
     * Solicita el inicio del monitor periódico de surtido.
     *
     * <p>Invoca {@link Farmacia#iniciarMonitor(long)}
     * utilizando el período de revisión configurado
     * en la constante PERIODO_MS.</p>
     *
     * <p>Si el monitor inicia correctamente, registra
     * un mensaje en el historial y actualiza
     * los controles de la interfaz.</p>
     *
     * <p>Captura los errores de argumentos y de estado
     * para mostrarlos mediante cuadros de diálogo.</p>
     */

    private void iniciarMonitor() {
        try {
            farmacia.iniciarMonitor(PERIODO_MS);
            historial.append("Monitor iniciado.\n");
            actualizarEstado();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo iniciar", JOptionPane.ERROR_MESSAGE);
            actualizarEstado();
        }
    }

    /**
     * Solicita la detención del monitor de surtido.
     *
     * <p>Deshabilita temporalmente el botón de detención
     * y utiliza un {@link SwingWorker} para ejecutar
     * la operación en un hilo secundario.</p>
     *
     * <p>La detención se realiza mediante la fachada
     * {@link Farmacia#detenerMonitor()}, evitando bloquear
     * el hilo de eventos de Swing mientras termina
     * la ejecución del monitor.</p>
     *
     * <p>Al finalizar, muestra una confirmación
     * o el error correspondiente y actualiza
     * los controles del panel.</p>
     */

    private void detenerMonitor() {
        detener.setEnabled(false);

        // detenerMonitor espera la finalización del hilo: no bloquear la interfaz Swing.
        new SwingWorker<Void, Void>() {

            /**
             * Ejecuta la detención del monitor en segundo plano.
             *
             * @return {@code null}, porque la operación
             *         no produce un resultado.
             * @throws InterruptedException si el hilo es
             *         interrumpido durante la espera.
             */

            @Override
            protected Void doInBackground() throws InterruptedException {
                farmacia.detenerMonitor();
                return null;
            }

            /**
             * Procesa la finalización de la tarea de detención.
             *
             * <p>Comprueba el resultado, informa al usuario
             * y actualiza el estado visual del monitor.</p>
             */

            @Override
            protected void done() {
                try {
                    get();
                    historial.append("Monitor detenido.\n");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    mostrarError(ex);
                } catch (ExecutionException ex) {
                    mostrarError(ex.getCause());
                } finally {
                    actualizarEstado();
                }
            }
        }.execute();
    }

    /**
     * Ejecuta una revisión manual de las ventas registradas.
     *
     * <p>Deshabilita temporalmente el botón de revisión
     * y utiliza un {@link SwingWorker} para solicitar
     * la operación mediante {@link Farmacia#revisar()}.</p>
     *
     * <p>La revisión comprueba las reglas del monitor,
     * incluyendo la cancelación de ventas abiertas vacías
     * y la emisión de alertas por surtidos demorados.</p>
     *
     * <p>Al finalizar, registra la cantidad de acciones
     * realizadas, muestra los errores correspondientes
     * y vuelve a habilitar el botón de revisión.</p>
     */

    private void revisarAhora() {
        revisar.setEnabled(false);

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return farmacia.revisar();
            }

            @Override
            protected void done() {
                try {
                    int acciones = get();
                    historial.append("Revisión manual: " + acciones + " acción(es).\n");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    mostrarError(ex);
                } catch (ExecutionException ex) {
                    mostrarError(ex.getCause());
                } finally {
                    revisar.setEnabled(true);
                    actualizarEstado();
                }
            }
        }.execute();
    }

    /**
     * Muestra un mensaje de error relacionado con el monitor.
     *
     * <p>Obtiene la descripción del problema y la presenta
     * mediante un cuadro de diálogo de Swing.</p>
     *
     * @param error excepción o error que contiene
     *              la descripción del problema ocurrido.
     */

    private void mostrarError(Throwable error) {
        JOptionPane.showMessageDialog(this, error.getMessage(),
                "Error del monitor", JOptionPane.ERROR_MESSAGE);
    }
}

