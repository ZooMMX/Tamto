package hello.calidad;

import javax.persistence.metamodel.SingularAttribute;
import java.sql.Blob;
import java.util.Date;

/**
 * Proyecto tamto
 * User: octavioruizcastillo
 * Date: 07/05/15
 * Time: 16:47
 */
@javax.persistence.metamodel.StaticMetamodel(DocumentoInterno.class)
public class DocumentoInterno_ {
    public static volatile SingularAttribute<DocumentoInterno, Long> id;
    public static volatile SingularAttribute<DocumentoInterno, Integer> nivel;
    public static volatile SingularAttribute<DocumentoInterno, TipoDocumento> tipo;
    public static volatile SingularAttribute<DocumentoInterno, String> codigo;
    public static volatile SingularAttribute<DocumentoInterno, ROLCS> rolcs;
    public static volatile SingularAttribute<DocumentoInterno, String> titulo;
    public static volatile SingularAttribute<DocumentoInterno, Date> fechaElaboracion;
    public static volatile SingularAttribute<DocumentoInterno, Date> ultimaAprobacion;
    public static volatile SingularAttribute<DocumentoInterno, Date> proximaRevision;
    public static volatile SingularAttribute<DocumentoInterno, Long> version;
    public static volatile SingularAttribute<DocumentoInterno, Departamento> departamento;
    public static volatile SingularAttribute<DocumentoInterno, Blob> documento;
    public static volatile SingularAttribute<DocumentoInterno, String> fileName;
    public static volatile SingularAttribute<DocumentoInterno, Long> fileSize;
    public static volatile SingularAttribute<DocumentoInterno, Date> created;
    public static volatile SingularAttribute<DocumentoInterno, Date> updated;
    public static volatile SingularAttribute<DocumentoInterno, String> fileType;
    public static volatile SingularAttribute<DocumentoInterno, Boolean> enabled;

}
