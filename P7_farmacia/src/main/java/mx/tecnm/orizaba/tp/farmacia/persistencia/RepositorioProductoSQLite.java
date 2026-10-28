package mx.tecnm.orizaba.tp.farmacia.persistencia;

import mx.tecnm.orizaba.tp.farmacia.modelo.Producto;
import mx.tecnm.orizaba.tp.farmacia.modelo.Sku;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio JDBC de productos (tabla de herencia única: columna {@code tipo}).
 * <ul>
 *   <li>Escritura: {@code switch} con <em>pattern matching</em> exhaustivo sobre la jerarquía sellada.</li>
 *   <li>Lectura: mapa de reconstructores (lambdas) indexado por el nombre del subtipo.</li>
 * </ul>
 */
public class RepositorioProductoSQLite implements Repositorio<Producto, Sku> {

    /** Interfaz funcional privada anidada. */
    @FunctionalInterface
    private interface Reconstructor {
        Producto crear(Sku clave, String nombre, double precio, String atributo);
    }

    private static final Map<String, Reconstructor> RECONSTRUCTORES = Map.of(
            "Patente", (c, n, p, a) -> new Producto.Patente(c, n, p, a),
            "Generico", (c, n, p, a) -> new Producto.Generico(c, n, p, Integer.parseInt(a)),
            "Biologico", (c, n, p, a) -> new Producto.Biologico(c, n, p, Integer.parseInt(a))
    );

    private final ConexionSQLite bd;
    private final MapeadorFila<Producto> mapeador = rs -> {
        Reconstructor r = RECONSTRUCTORES.get(rs.getString("tipo"));
        if (r == null) {
            throw new SQLException("Tipo desconocido: " + rs.getString("tipo"));
        }
        return r.crear(Sku.parse(rs.getString("clave")), rs.getString("nombre"),
                rs.getDouble("precio_base"), rs.getString("atributo"));
    };
    /**
     * Crea el repositorio de medicamentos en SQLite.
     * @param bd conexión de la base de datos.
     */
    public RepositorioProductoSQLite(ConexionSQLite bd) {
        this.bd = bd;
    }

    /** Atributo específico del subtipo, obtenido con pattern matching. */
    static String atributoDe(Producto e) {
        // TODO E13: switch (e) { case Producto.Patente x -> ...; ... } sin default (la jerarquía es sellada).
        return switch (e){
            case Producto.Patente p ->  p.laboratorio();
            case Producto.Generico g -> String.valueOf(g.concentracionMg());
            case Producto.Biologico b-> String.valueOf(b.temperaturaMax());
        };
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public void guardar(Producto e) {
        // TODO E13: INSERT ... ON CONFLICT(clave) DO UPDATE ... con PreparedStatement.
        //Instrucción SQL para insertar a actualizar
        String sql = """
                INSERT INTO producto (clave, tipo, nombre, precio_base, atributo)
                VALUES (?,?,?,?,?)
                ON CONFLICT(clave) DO UPDATE SET
                tipo = excluded.tipo,
                nombre = excluded.nombre,
                precio_base = excluded.precio_base,
                atributo = excluded.atributo
                """;
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)) {
            //Asignar los cinco valores
            ps.setString(1, e.clave().toString());
            ps.setString(2, e.tipo());
            ps.setString(3, e.nombre());
            ps.setDouble(4, e.precioBase());
            ps.setString(5, atributoDe(e));

            //Ejecutar INSERT o UPDATE
            ps.executeUpdate();
        } catch (SQLException error) {
            throw new PersistenciaException("Error al guardar el producto", error);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Producto> buscar(Sku clave) {
        // TODO E13: SELECT por clave; mapear con mapeador si hay fila.
        String sql = """
                SELECT clave, tipo, nombre, precio_base, atributo
                FROM producto
                WHERE clave = ?
                """;
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)){
            ps.setString(1, clave.toString());
            //Ejecutar la consulta
            try (ResultSet rs = ps.executeQuery()){
                //Comprobar si existe una fila
                if (rs.next()){
                    //Convertir la fila en un producto
                    Producto producto = mapeador.mapear(rs);
                    return Optional.of(producto);
                }
                //No existe un producto con esa clave
                return Optional.empty();
            }
        } catch (SQLException error){
            throw new PersistenciaException("Error al buscar el producto", error);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Producto> todos() {
        // TODO E13: SELECT ordenado por clave; mapear cada fila.
        List <Producto> productos = new ArrayList<>();
        String sql = """
        SELECT clave, tipo, nombre, precio_base, atributo
        FROM producto
        ORDER BY clave
        """;
        //Prepara y ejecuta la consulta
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            //Recorrer todas las filas
                while (rs.next()){
                    //Reconstruir el objeto y agregarlo
                    productos.add(mapeador.mapear(rs));
                }
                return productos;
        } catch (SQLException error){
                throw new PersistenciaException("Error al consultar los productos", error);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Sku clave) {
        // TODO E13: DELETE por clave; devolver si se afectó alguna fila.
        String sql = "DELETE FROM producto WHERE clave = ?";
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)){
            //Asignar la clave del producto
            ps.setString(1, clave.toString());
            //Eliminar
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException error){
            throw new PersistenciaException("Error al eliminar el producto", error);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }
}
