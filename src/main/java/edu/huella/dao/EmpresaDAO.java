package edu.huella.dao;

import edu.huella.model.Empresa;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla {@code empresa}. Todas las operaciones de lectura/escritura del CRUD
 * filtran por propietario y por registros activos. Sin DELETE: el borrado es lógico.
 */
public interface EmpresaDAO {

    /** Empresas activas del usuario, ordenadas por nombre. */
    List<Empresa> listarActivasPorUsuario(int idUsuario);

    /** Busca por NIT en cualquier estado y propietario (para explicar una colisión de UNIQUE). */
    Optional<Empresa> buscarPorNit(String nit);

    /** Inserta y devuelve el ID. {@code DuplicateKeyException} si el NIT ya existe (aunque esté eliminada). */
    int insertar(int idUsuario, String nit, String nombre);

    /**
     * Actualiza NIT y nombre de una empresa ACTIVA del usuario. Devuelve filas encontradas
     * (0 = no existe, es de otro usuario o está eliminada). Conserva ID, propietario y creado_en.
     */
    int actualizar(int id, int idUsuario, String nit, String nombre);

    /** Marca {@code eliminado_en}. Devuelve filas afectadas (0 = no existe / ajena / ya eliminada). */
    int marcarEliminada(int id, int idUsuario);
}
