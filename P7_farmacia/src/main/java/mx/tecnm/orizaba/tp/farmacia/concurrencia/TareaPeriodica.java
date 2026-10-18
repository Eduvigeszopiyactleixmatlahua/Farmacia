package mx.tecnm.orizaba.tp.farmacia.concurrencia;

/**
 * Tarea que se ejecuta periódicamente en su propio hilo.
 * <ul>
 *   <li>Implementa {@link Runnable} en lugar de extender {@link Thread}
 *       (véase {@code java_4_modelo/t6_concurrencia/EjemploHilos}).</li>
 *   <li>Detención cooperativa: bandera {@code volatile} + {@code interrupt()} + {@code join()}.</li>
 *   <li>Template Method: {@link #run()} es final; la variación está en {@link #ejecutarCiclo()}.</li>
 * </ul>
 */
public abstract class TareaPeriodica implements Runnable {

    private final long periodoMs;
    private volatile boolean activa;
    private Thread hilo;
    private volatile long ciclos;

    protected TareaPeriodica(long periodoMs) {
        if (periodoMs <= 0) {
            throw new IllegalArgumentException("Periodo inválido: " + periodoMs);
        }
        this.periodoMs = periodoMs;
    }

    /** Arranca el hilo (demonio) con el nombre indicado. */
    public final synchronized void iniciar(String nombreHilo) {
        // TODO E12: Si ya está activa lanzar IllegalStateException; crear new Thread(this, nombreHilo), marcarlo demonio, activar y start().
        //Evitar comenzar una tarea ya activa
        if(activa){
            throw new IllegalStateException("La terea ya está activa");
        }
        //Crear el hilo que ejecutará la tarea
        hilo = new Thread(this, nombreHilo);
        hilo.setDaemon(true);
        //Marcar la tarea en activa
        activa = true;
        //Comenzar el nuevo hilo
        hilo.start();

        //throw new UnsupportedOperationException("TODO E12");
    }

    /** Solicita la detención y espera a que el hilo termine. */
    public final void detener() throws InterruptedException {
        // TODO E12: Bajar la bandera, interrumpir el hilo y esperar con join() (si existe).
        //Hilo no debe repetir el trabajo
        activa = false;
        //Verificar que el hilo exista
        if(hilo != null){
            //Interrumpir una posible espera
            hilo.interrupt();
            //Esperar que el hilo termine
            hilo.join();
        }
        //throw new UnsupportedOperationException("TODO E12");
    }

    public final boolean activa() {
        return activa;
    }

    public final long ciclos() {
        return ciclos;
    }

    @Override
    public final void run() {
        // TODO E12: Mientras activa: ejecutarCiclo(), incrementar ciclos y dormir periodoMs; ante InterruptedException restaurar la interrupción y salir.
        while (activa){
            try {
                //Ejecutar el trabajo
                ejecutarCiclo();
                //Contar un ciclo
                ciclos++;
                //Esperar antes del siguiente ciclo
                Thread.sleep(periodoMs);
            } catch (InterruptedException e){
                //Conservar el estado de interrupcion
                Thread.currentThread().interrupt();
                break;
            }
        }
        activa = false;
        //throw new UnsupportedOperationException("TODO E12");
    }

    /** Trabajo de un ciclo. */
    protected abstract void ejecutarCiclo();
}
