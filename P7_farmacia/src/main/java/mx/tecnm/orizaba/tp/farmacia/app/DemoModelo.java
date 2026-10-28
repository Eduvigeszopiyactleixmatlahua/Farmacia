package mx.tecnm.orizaba.tp.farmacia.app;

import mx.tecnm.orizaba.tp.farmacia.eventos.OyenteEvento;
import mx.tecnm.orizaba.tp.farmacia.eventos.Evento;
import mx.tecnm.orizaba.tp.farmacia.funcional.Reglas;
import mx.tecnm.orizaba.tp.farmacia.modelo.*;
import mx.tecnm.orizaba.tp.farmacia.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.farmacia.servicio.Farmacia;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Recorrido de consola por el modelo: sirve como referencia de uso para construir la UI.
 * La UI del estudiante debe ofrecer, como mínimo, lo que aquí se muestra.
 */
public final class DemoModelo {
    /**
     * Ejecuta la demostración del modelo.
     * @param args argumentos de línea de comandos.
     * @throws Exception si la operación no puede completarse.
     */
    public static void main(String[] args) throws Exception {
        Path bd = args.length > 0 ? Path.of(args[0]) : Files.createTempFile("farmacia", ".db");
        AtomicReference<LocalDate> hoy = new AtomicReference<>(LocalDate.now());

        try (ConexionSQLite cx = new ConexionSQLite(bd.toString());
             Farmacia g = new Farmacia(cx, hoy::get)) {

            // Suscripción con clase anónima (la UI usaría Platform.runLater / invokeLater aquí).
            g.suscribir(new OyenteEvento() {
                @Override
                public void notificar(Evento e) {
                    System.out.println("   evento " + e + " [" + Thread.currentThread().getName() + "]");
                }
            });

            System.out.println("== Alta de productos (referencias a constructor)");
            g.registrar(Producto.Patente::new, "Amoxicilina 500 mg", 185.0, "Laboratorio A");
            g.registrar(Producto.Patente::new, "Losartán 50 mg", 320.0, "Laboratorio B");
            g.registrar(Producto.Generico::new, "Paracetamol", 45.0, 500);
            g.registrar(Producto.Generico::new, "Metformina", 90.0, 850);
            g.registrar(Producto.Biologico::new, "Insulina glargina", 420.0, 8);
            g.registrar(Producto.Biologico::new, "Vacuna contra influenza", 380.0, 6);

            System.out.println("== clientes");
            g.registrarCliente("Óscar Paredes", "oparedes@gmail.com", Nivel.GENERAL);
            g.registrarCliente("Paula Rivas", "paula.rivas@hotmail.com", Nivel.AFILIADO);
            g.registrarCliente("Ramón Torres", "rtorres@yahoo.com", Nivel.ADULTO_MAYOR);

            System.out.println("== Flujo de venta");
            Venta t = g.abrir(1);
            g.agregarLinea(t.id(), "PAT-0001", 2);
            g.agregarLinea(t.id(), "BIO-0001", 3);
            g.iniciar(t.id());
            t.forEach(System.out::println);
            g.cerrar(t.id());
            Venta t2 = g.abrir(2);
            g.agregarLinea(t2.id(), "GEN-0001", 1);
            g.iniciar(t2.id());
            g.abrir(3);

            System.out.println("== Reportes");
            System.out.println(g.catalogo().resumen());
            System.out.println("Ingresos por tipo: " + g.ingresosPorTipo());
            System.out.println("Estadísticas: " + g.estadisticas());
            System.out.println("Conteo por estado: " + g.conteoPorEstado());
            g.mejores(3).forEach(System.out::println);
            g.buscar(Reglas.conCapacidad(RequiereReceta.class)).stream().map(g::describir).forEach(System.out::println);

            System.out.println("== Monitor en segundo plano (reloj simulado +" + (Farmacia.PLAZO_DIAS + 1) + " días)");
            g.iniciarMonitor(100);
            hoy.set(hoy.get().plusDays(Farmacia.PLAZO_DIAS + 1));
            Thread.sleep(400);
            g.detenerMonitor();
            System.out.println("Base de datos: " + bd.toAbsolutePath());
        }
    }
}
