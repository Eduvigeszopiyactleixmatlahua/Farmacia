package mx.tecnm.orizaba.tp.farmacia.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Administra la conexión JDBC con SQLite y el esquema de la base de datos.
 * <p>Proceso: el bloque estático carga el controlador la primera vez que la clase se inicializa
 * (véase {@code java_2_proceso/Proceso07}: uso activo de una clase).</p>
 */
public final class ConexionSQLite implements AutoCloseable {

    private static final String[] ESQUEMA = {
        """
        CREATE TABLE IF NOT EXISTS producto (
            clave        TEXT PRIMARY KEY,
            tipo         TEXT NOT NULL,
            nombre       TEXT NOT NULL,
            precio_base  REAL NOT NULL CHECK (precio_base >= 0),
            atributo     TEXT NOT NULL
        )""",
        """
        CREATE TABLE IF NOT EXISTS cliente (
            id      INTEGER PRIMARY KEY,
            nombre  TEXT NOT NULL,
            correo  TEXT NOT NULL UNIQUE,
            nivel   TEXT NOT NULL
        )""",
        """
        CREATE TABLE IF NOT EXISTS venta (
            id        INTEGER PRIMARY KEY,
            cliente_id  INTEGER NOT NULL REFERENCES cliente(id),
            fecha     TEXT NOT NULL,
            estado    TEXT NOT NULL
        )""",
        """
        CREATE TABLE IF NOT EXISTS venta_linea (
            venta_id   INTEGER NOT NULL REFERENCES venta(id) ON DELETE CASCADE,
            clave_producto TEXT NOT NULL REFERENCES producto(clave),
            unidades        INTEGER NOT NULL CHECK (unidades > 0),
            PRIMARY KEY (venta_id, clave_producto)
        )"""
    };

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("No se encontró el controlador sqlite-jdbc en el classpath");
        }
    }

    private final Connection conexion;
/**
 * Crea la conexión SQLite y el esquema de tablas.
 * @param rutaArchivo ruta de la base de datos SQLite.
 */
    public ConexionSQLite(String rutaArchivo) {
        // TODO E13: Abrir jdbc:sqlite:<ruta>, activar PRAGMA foreign_keys = ON y crear el esquema.
       try {
           //Conexión con SQLite
           conexion = DriverManager.getConnection("jdbc:sqlite:" + rutaArchivo);
           //Crear el Statement
           try(Statement sentencia = conexion.createStatement()) {
               sentencia.execute("PRAGMA foreign_keys = ON");
               for (String sql : ESQUEMA){
                sentencia.execute(sql);
            }
       }

    } catch (SQLException e) {
           throw new PersistenciaException("Error al inicializar SQLite", e);
       }

        //throw new UnsupportedOperationException("TODO E13");
    }
    /**
     * Obtiene la conexión JDBC activa.
     * @return conexión JDBC.
     */
    public Connection conexion() {
        return conexion;
    }

    /**
     * Ejecuta un trabajo dentro de una transacción: confirma si termina bien y revierte ante error.
     * @param <R> tipo genérico utilizado.
     * @param trabajo operación SQL que se ejecutará.
     * @return resultado producido por el trabajo SQL.
     */
    public <R> R enTransaccion(TrabajoSQL<R> trabajo) {
        // TODO E13: setAutoCommit(false); ejecutar; commit; ante SQLException rollback y PersistenciaException; finalmente setAutoCommit(true).
        try {
            //Desactivar la confirmación automática
            conexion.setAutoCommit(false);
            //Ejecutar el trabajo
            R resultado = trabajo.ejecutar(conexion);
            //Confirmar con commit()
            conexion.commit();
            //Devolver el resultado
            return resultado;

        } catch (SQLException e){
            try {
                conexion.rollback();
            } catch (SQLException errorRollback) {
                e.addSuppressed(errorRollback);
            }
            throw new PersistenciaException("Error al ejecutar la transacción", e);

        } catch(RuntimeException e) {
            //Revertir si ocurre un error de ejecución
            try {
                conexion.rollback();
            } catch (SQLException errorRollback) {
                e.addSuppressed(errorRollback);
            }
            throw e;
        } finally {
            //Restaurar la confirmación automática
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                throw new PersistenciaException("Error al restaurar autocommit", e);
            }
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    /** Interfaz funcional miembro: trabajo que puede lanzar {@link SQLException}. */
    /**
     * Operación SQL que puede lanzar una excepción.
     * @param <R> tipo del resultado.
     */
    @FunctionalInterface
    public interface TrabajoSQL<R> {
        R ejecutar(Connection c) throws SQLException;
    }

    @Override
    public void close() {
        try {
            conexion.close();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al cerrar la conexión", e);
        }
    }
}
