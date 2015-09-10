package hello.calidad;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hello.User;
import org.hibernate.envers.Audited;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.sql.Blob;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Proyecto tamto
 * User: octavioruizcastillo
 * Date: 03/05/15
 * Time: 20:22
 */
@JsonIgnoreProperties({"documento", "documentoEditable"})
@Audited(withModifiedFlag = true)
@Entity
public class DocumentoInterno {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column
    @Min(1) @Max(8)
    private Integer nivel;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private TipoDocumento tipo;

    @Column
    @Size(min=0, max=20)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ROLCS rolcs;

    @Column
    @Size(min=0, max=200)
    private String titulo;

    @Column
    private String version;

    @DateTimeFormat(pattern="dd/MM/yyyy")
    @Column(name = "proxima_revision")
    private Date proximaRevision;

    @Column(length = 32)
    @Enumerated(EnumType.STRING)
    private Departamento departamento;

    // Almacena la versión PDF
    @Lob
    private Blob documento;

    // PDF / no editable
    @Column
    private String fileName;

    // PDF / no editable
    @Column
    private Long fileSize;

    // PDF / no editable
    @Column
    private String fileType;

    @Lob
    private Blob documentoEditable;

    @Column
    private String editableFileName;

    @Column
    private Long editableFileSize;

    @Column
    private String editableFileType;

    @Lob
    private String notas;

    @Column
    private Date created;

    @Column
    private Date updated;

    @Column(nullable = false)
    private boolean enabled = true;

    //Los documentos deben pasar por un proceso de aprobación, comienzan no aprobados
    @Column(nullable = false)
    private boolean aprobado = false;

    @ManyToOne
    private User aprobo;

    //Clase encargada de renderizar trozos de html relacionados con esta entidad
    @Transient
    DocumentoInternoHtmlHelper htmlHelper;

    @JsonIgnore
    private String getDateString(Date date) {
        DateFormat sdf = SimpleDateFormat.getDateInstance();
        String fechaString;
        if(date == null)
            return "01/01/2000";
        else
            return sdf.format(date);

    }

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        updated = new Date();
        if (created==null) {
            created = new Date();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }

    public TipoDocumento getTipo() {
        return tipo;
    }

    public void setTipo(TipoDocumento tipo) {
        this.tipo = tipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public ROLCS getRolcs() {
        return rolcs;
    }

    public void setRolcs(ROLCS rolcs) {
        this.rolcs = rolcs;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getProximaRevision() {
        return proximaRevision;
    }

    public String getProximaRevisionString() { return getDateString(getProximaRevision()); }

    public void setProximaRevision(Date proximaRevision) {
        this.proximaRevision = proximaRevision;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public Blob getDocumento() {
        return documento;
    }

    public void setDocumento(Blob documento) {
        this.documento = documento;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Date getCreated() {
        return created;
    }

    public String getCreatedString() { return getDateString(getCreated()); }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public String getUpdatedString() { return getDateString(getUpdated()); }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Blob getDocumentoEditable() {
        return documentoEditable;
    }

    public void setDocumentoEditable(Blob documentoEditable) {
        this.documentoEditable = documentoEditable;
    }

    public String getEditableFileName() {
        return editableFileName;
    }

    public void setEditableFileName(String editableFileName) {
        this.editableFileName = editableFileName;
    }

    public Long getEditableFileSize() {
        return editableFileSize;
    }

    public void setEditableFileSize(Long editableFileSize) {
        this.editableFileSize = editableFileSize;
    }

    public String getEditableFileType() {
        return editableFileType;
    }

    public void setEditableFileType(String editableFileType) {
        this.editableFileType = editableFileType;
    }

    public boolean isAprobado() {
        return aprobado;
    }

    public void setAprobado(boolean aprobado) {
        this.aprobado = aprobado;
    }

    public User getAprobo() {
        return aprobo;
    }

    public void setAprobo(User aprobo) {
        this.aprobo = aprobo;
    }

    public DocumentoInternoHtmlHelper getHtmlHelper() {

        return htmlHelper;
    }

    public void setHtmlHelper(DocumentoInternoHtmlHelper htmlHelper) {
        htmlHelper.setParent(this);
        this.htmlHelper = htmlHelper;
    }
}
