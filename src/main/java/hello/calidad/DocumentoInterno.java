package hello.calidad;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.envers.Audited;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
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

    @DateTimeFormat(pattern="dd/MM/yyyy")
    @Column(name = "fecha_elaboracion")
    private Date fechaElaboracion;

    @DateTimeFormat(pattern="dd/MM/yyyy")
    @Column(name = "ultima_aprobacion")
    private Date ultimaAprobacion;

    @Column
    private BigDecimal version;

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

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        updated = new Date();
        if (created==null) {
            created = new Date();
        }
    }

    @Deprecated
    public String getHtmlAction() {
        StringBuilder builder = new StringBuilder();
        builder.append(new String("<a href=\"/calidad/documento/0/"+ getId() +"\" class=\"btn btn-xs blue\"><i class=\"fa fa-search\"></i> Ver</a>"));
        builder.append(new String("<a href=\"/calidad/documento/editar/"+ getId() + "\" class=\"btn btn-xs default\"><i class=\"icon-pencil\"></i> Editar</a>"));
        return builder.toString();
    }

    public String getHtmlCheckbox() {
        String ret = new String("<input type=\"checkbox\" name=\"id[]\" value=\"" + getId() + "\">");
        return ret;
    }

    @JsonIgnore
    private String getDateString(Date date) {
        DateFormat sdf = SimpleDateFormat.getDateInstance();
        String fechaString;
        if(date == null)
            return "01/01/2000";
        else
            return sdf.format(date);

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

    public Date getFechaElaboracion() {
        return fechaElaboracion;
    }

    public String getFechaElaboracionString() { return getDateString(getFechaElaboracion()); }

    public void setFechaElaboracion(Date fechaElaboracion) {
        this.fechaElaboracion = fechaElaboracion;
    }

    public Date getUltimaAprobacion() {
        return ultimaAprobacion;
    }

    public String getUltimaAprobacionString() { return getDateString(getUltimaAprobacion()); }

    public void setUltimaAprobacion(Date ultimaAprobacion) {
        this.ultimaAprobacion = ultimaAprobacion;
    }

    public BigDecimal getVersion() {
        return version;
    }

    public void setVersion(BigDecimal version) {
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

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

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
}
