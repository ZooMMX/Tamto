package hello.calidad;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

/**
 * Proyecto tamto
 * User: octavioruizcastillo
 * Date: 04/05/15
 * Time: 8:16
 */
public interface ListaMaestraRepository extends CrudRepository<DocumentoInterno, Long> {
    @Query("SELECT count(di) FROM DocumentoInterno di WHERE di.enabled = 1 AND di.aprobado = 1")
    public Long countEnabled();

    //Regresa todos los documentos pendientes de aprobación
    @Query("SELECT di FROM DocumentoInterno di WHERE di.aprobado = 0")
    public List<DocumentoInterno> findDocsSinAprobar();

    @Query(value = "SELECT id, titulo, proxima_revision FROM documento_interno WHERE " +
            "proxima_revision >= CURDATE() " +
            "AND aprobado = 1 " +
            "AND enabled = 1 " +
            "AND proxima_revision < (DATE_ADD(CURDATE(), INTERVAL 1 MONTH));", nativeQuery = true)
    public List<Object[]> getDocumentosProximaRevision();

    @Query(value = "SELECT id, titulo, proxima_revision FROM documento_interno WHERE " +
            "aprobado = 1 AND " +
            "enabled = 1 AND " +
            "proxima_revision <= CURDATE() ", nativeQuery = true)
    public List<Object[]> getDocumentosRevisionVencida();
//
//    @Query(value = "SELECT id, aprobado, codigo, created, departamento, editable_file_name, editable_file_type, enabled, file_name, file_type, nivel, proxima_revision, rolcs, tipo, titulo, updated, version, aprobo_username FROM documento_interno WHERE " +
//            "proxima_revision >= CURDATE() " +
//            "AND aprobado = 1 " +
//            "AND enabled = 1 " +
//            "AND proxima_revision < (DATE_ADD(CURDATE(), INTERVAL 1 MONTH));", nativeQuery = true)
//    public List<DocumentoInternoSinBinarios> getDocumentosProximaRevision();
//
//    @Query(value = "SELECT id, aprobado, codigo, created, departamento, editable_file_name, editable_file_type, enabled, file_name, file_type, nivel, proxima_revision, rolcs, tipo, titulo, updated, version, aprobo_username FROM documento_interno WHERE " +
//            "aprobado = 1 AND " +
//            "enabled = 1 AND " +
//            "proxima_revision <= CURDATE();", nativeQuery = true)
//    public List<DocumentoInternoSinBinarios> getDocumentosRevisionVencida();

    @Query(value = "SELECT doc1.rev, r.timestamp, doc1.version FROM documento_interno_aud doc1 LEFT JOIN documento_interno_aud doc2 ON (doc1.version = doc2.version AND doc1.rev < doc2.rev) JOIN revision r ON r.id = doc1.rev WHERE doc2.version IS NULL AND doc1.id = ? ORDER BY rev DESC", nativeQuery = true)
    public List<Object> getVersiones(Long docId);

    public List<DocumentoInterno> findByNivelAndEnabled(Integer nivel, Boolean enabled);
}
