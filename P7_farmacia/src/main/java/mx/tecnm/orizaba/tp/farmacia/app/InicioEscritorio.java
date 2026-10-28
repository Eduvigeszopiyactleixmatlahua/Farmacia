package mx.tecnm.orizaba.tp.farmacia.app;

import mx.tecnm.orizaba.tp.farmacia.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.farmacia.servicio.Farmacia;
import mx.tecnm.orizaba.tp.farmacia.ui.VentanaPrincipal;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Font;


/**
 * Punto de entrada de la aplicación de escritorio de farmacia.
 *
 * <p>Esta clase inicializa la apariencia gráfica mediante FlatLaf,
 * configura los componentes Swing y establece la conexión
 * con la base de datos SQLite.</p>
 *
 * <p>También crea la fachada {@link Farmacia}, muestra la ventana
 * principal y gestiona el cierre de los recursos utilizados
 * por la aplicación.</p>
 *
 * @see Farmacia
 * @see VentanaPrincipal
 * @see ConexionSQLite
 */

public final class InicioEscritorio {
    /**
     * Inicia la aplicación gráfica de farmacia.
     *
     * <p>Configura la apariencia visual de Swing mediante FlatLaf
     * y utiliza {@link SwingUtilities#invokeLater(Runnable)}
     * para crear y mostrar la ventana principal en el hilo
     * de despacho de eventos de Swing.</p>
     *
     * <p>Inicializa la base de datos SQLite y la fachada del
     * sistema. Al cerrar la ventana, libera los recursos
     * asociados al servicio y a la conexión.</p>
     *
     * @param args argumentos recibidos desde la línea de comandos;
     *             actualmente no son utilizados.
     */

    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);

        UIManager.put(
                "defaultFont",
                new Font("Segoe UI", Font.PLAIN, 14)
        );
        SwingUtilities.invokeLater(() -> {
            ConexionSQLite conexion = null;
            Farmacia farmacia = null;

            try {
                Path carpeta = Path.of("datos");
                Files.createDirectories(carpeta);

                conexion = new ConexionSQLite(
                        carpeta.resolve("farmacia.db").toString()
                );
                farmacia = new Farmacia(conexion);

                ConexionSQLite bdActiva = conexion;
                Farmacia servicioActivo = farmacia;

                VentanaPrincipal ventana = new VentanaPrincipal(farmacia);

                ventana.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        try {
                            servicioActivo.close();
                        } finally {
                            bdActiva.close();
                        }
                    }
                });

                ventana.setVisible(true);

            } catch (Exception ex) {
                if (farmacia != null) farmacia.close();
                if (conexion != null) conexion.close();

                JOptionPane.showMessageDialog(
                        null,
                        ex.getMessage(),
                        "Error al iniciar",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
