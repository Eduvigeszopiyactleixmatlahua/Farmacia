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
    /**
     * Crea el repositorio de ventas y sus dependencias.
     * @param bd conexión de la base de datos.
     * @param clientes clientes que se almacenarán.
     * @param elementos repositorio de medicamentos.
     */
    public RepositorioVentaSQLite(ConexionSQLite bd, Repositorio<Cliente, Integer> clientes,
                         Repositorio<Producto, Sku> elementos) {
        this.bd = bd;
        this.clientes = clientes;
        this.elementos = elementos;
    }

    @Override
    public void guardar(Venta t) {
        // TODO E13: En bd.enTransaccion: upsert del encabezado, borrar sus líneas e insertarlas en lote (addBatch/executeBatch).
        //Insertar o actualizar venta
        String sqlVenta = """
            INSERT INTO venta (id, cliente_id, fecha, estado)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                cliente_id = excluded.cliente_id,
                fecha = excluded.fecha,
                estado = excluded.estado
            """;
        //Borrar venta_linea anterior
        String sqlBorrar = """
            DELETE FROM venta_linea
            WHERE venta_id = ?
            """;
        //Insertar las líneas
        String sqlLinea = """
            INSERT INTO venta_linea
                (venta_id, clave_producto, unidades)
            VALUES (?, ?, ?)
            """;
        //Ejecutar todo en una transacción
        bd.enTransaccion(c -> {
            try (PreparedStatement ps = c.prepareStatement(sqlVenta)){
                ps.setInt(1, t.id());
                ps.setInt(2, t.cliente().id());
                ps.setString(3, t.fecha().toString());
                ps.setString(4, t.estado().name());

                ps.executeUpdate();
            }

            try (PreparedStatement ps = c.prepareStatement(sqlBorrar)){
                ps.setInt(1, t.id());
                ps.executeUpdate();
            }
            //Inserta Linea actual
            try (PreparedStatement ps = c.prepareStatement(sqlLinea)){
                for (Venta.Linea linea : t.lineas()){
                   ps.setInt(1, t.id());
                   ps.setString(2, linea.elemento().clave().toString());
                   ps.setInt(3, linea.unidades());
                   //Agrega la linea
                   ps.addBatch();
                }
                //Ejecuta todas las inserciones
                ps.executeBatch();
            }
            return null;
        });
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Venta> buscar(Integer id) {
        // TODO E13: Leer el encabezado, resolver el actor y los elementos con sus repositorios, agregar líneas y restaurar el estado.
        String sql = """
        SELECT id, cliente_id, fecha, estado
        FROM venta
        WHERE id = ?
        """;
        //Obtener la conexión
        Connection c = bd.conexion();
        //Preparar la consulta
        try (PreparedStatement ps = c.prepareStatement(sql)){
            //Asignar el identificador
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()){
                //Comprobar si existe
                if (rs.next()){
                    //Reconstruir el objeto
                    Venta venta = reconstruir(c, rs);
                    return Optional.of(venta);
                }
                return Optional.empty();
            }
        } catch (SQLException error) {
            throw new PersistenciaException("Error al buscar la venta", error);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    private Venta reconstruir(Connection c, ResultSet rs) throws SQLException {
        // TODO E13: Construir el objeto en estado ABIERTA, agregar las líneas y al final restaurarEstado(estado leído).

        int id = rs.getInt("id");
        int clienteId = rs.getInt("cliente_id");
        Cliente cliente = clientes.buscar(clienteId).orElseThrow(() -> new SQLException("Cliente no encontrado: " + clienteId));

        //Recuperar la fecha
        LocalDate fecha = LocalDate.parse(rs.getString("fecha"));

        //Crear la venta en estado Abierta
        Venta venta = new Venta(id, cliente, fecha);
        //Consulta de Lineas
        String sql = """
            SELECT clave_producto, unidades
            FROM venta_linea
            WHERE venta_id = ?
            ORDER BY clave_producto
            """;
        try (PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1, id);
            try (ResultSet lineasRs = ps.executeQuery()){
                //Recuperar cada Linea
                while (lineasRs.next()){
                    //Obtener la clave del producto
                    Sku clave = Sku.parse(lineasRs.getString("clave_producto"));
                    //Recuperar el objeto Producto
                    Producto producto = elementos.buscar(clave).orElseThrow(() -> new SQLException("Producto no encontrado: " + clave));
                    //Obtener la cantidad
                    int unidades = lineasRs.getInt("unidades");
                    //Agregar la linea a la venta
                    venta.agregar(producto, unidades);
                }
            }
        }
        //Restaurar el estado
        Estado estado = Estado.valueOf(rs.getString("estado"));
        venta.restaurarEstado(estado);
        //Devolver la venta reconstruida
        return venta;
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Venta> todos() {
        // TODO E13: Recorrer los ids ordenados y reconstruir cada registro.
        List<Venta> ventas = new ArrayList<>();
        //Consultar las ventas ordenadas por id
        String sql = """
            SELECT id, cliente_id, fecha, estado
            FROM venta
            ORDER BY id
            """;
        //Obtener la conexion
        Connection c = bd.conexion();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            //Recorrer todas las filas
            while (rs.next()){
                //Reconstruir cada venta
                Venta venta = reconstruir(c, rs);
                //Agregr a la lista
                ventas.add(venta);
            }
            //Devolver la lista
            return ventas;
        } catch (SQLException error){
            throw new PersistenciaException("Error al consultar las ventas", error);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE del encabezado (las líneas se eliminan en cascada).
        String sql = "DELETE FROM venta WHERE id = ?";
        //Preparar la instrucción
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)){
            //Asignar el identificador
            ps.setInt(1, id);
            //Ejecutar la eliminacion
            int filasEliminadas = ps.executeUpdate();
            //Devuelve true si se elimino
            return filasEliminadas  > 0;
        } catch (SQLException error){
            throw new PersistenciaException("Error al eliminar la venta", error);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }
}
