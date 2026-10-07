package mx.tecnm.orizaba.tp.farmacia.persistencia;

import mx.tecnm.orizaba.tp.farmacia.modelo.Cliente;
import mx.tecnm.orizaba.tp.farmacia.modelo.Producto;
import mx.tecnm.orizaba.tp.farmacia.modelo.Sku;
import mx.tecnm.orizaba.tp.farmacia.modelo.Estado;
import mx.tecnm.orizaba.tp.farmacia.modelo.Venta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JDBC de ventas y sus líneas. Guardar es <strong>atómico</strong>:
 * encabezado y líneas se escriben en una sola transacción (commit/rollback).
 */
public class RepositorioVentaSQLite implements Repositorio<Venta, Integer> {

    private final ConexionSQLite bd;
    private final Repositorio<Cliente, Integer> clientes;
    private final Repositorio<Producto, Sku> elementos;

    public RepositorioVentaSQLite(ConexionSQLite bd, Repositorio<Cliente, Integer> clientes,
                         Repositorio<Producto, Sku> elementos) {
        this.bd = bd;
        this.clientes = clientes;
        this.elementos = elementos;
    }

    @Override
    public void guardar(Venta t) {
        // TODO E13: En bd.enTransaccion: upsert del encabezado, borrar sus líneas e insertarlas en lote (addBatch/executeBatch).
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Venta> buscar(Integer id) {
        // TODO E13: Leer el encabezado, resolver el actor y los elementos con sus repositorios, agregar líneas y restaurar el estado.
        throw new UnsupportedOperationException("TODO E13");
    }

    private Venta reconstruir(Connection c, ResultSet rs) throws SQLException {
        // TODO E13: Construir el objeto en estado ABIERTA, agregar las líneas y al final restaurarEstado(estado leído).
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Venta> todos() {
        // TODO E13: Recorrer los ids ordenados y reconstruir cada registro.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE del encabezado (las líneas se eliminan en cascada).
        throw new UnsupportedOperationException("TODO E13");
    }
}
