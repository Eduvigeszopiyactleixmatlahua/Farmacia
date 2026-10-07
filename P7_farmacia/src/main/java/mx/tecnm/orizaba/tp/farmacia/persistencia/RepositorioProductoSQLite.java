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

    public RepositorioProductoSQLite(ConexionSQLite bd) {
        this.bd = bd;
    }

    /** Atributo específico del subtipo, obtenido con pattern matching. */
    static String atributoDe(Producto e) {
        // TODO E13: switch (e) { case Producto.Patente x -> ...; ... } sin default (la jerarquía es sellada).
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public void guardar(Producto e) {
        // TODO E13: INSERT ... ON CONFLICT(clave) DO UPDATE ... con PreparedStatement.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Producto> buscar(Sku clave) {
        // TODO E13: SELECT por clave; mapear con mapeador si hay fila.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Producto> todos() {
        // TODO E13: SELECT ordenado por clave; mapear cada fila.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Sku clave) {
        // TODO E13: DELETE por clave; devolver si se afectó alguna fila.
        throw new UnsupportedOperationException("TODO E13");
    }
}
