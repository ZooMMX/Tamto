package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.sql.Blob;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 21/11/14
 * Time: 15:29
 */
@Audited(withModifiedFlag = true)
@Entity
@JsonIgnoreProperties({"bytes", "pieza", "bytesPdf"})
public class Archivo {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    @Column
    String fileName;

    @Column
    String fileSize;

    @Column
    String fileType;

    @Column
    @Enumerated(EnumType.STRING)
    TipoArchivo tamtoType;

    @Column
    Date created;

    @Column
    Date updated;

    @Lob
    private Blob bytes;

    // ******* Set de atributos de archivo para vista previa PDF *********
    @Column
    String fileNamePdf;

    @Column
    String fileSizePdf;

    @Column
    String fileTypePdf;

    @Lob
    private Blob bytesPdf;

    // ******* ********************************************************** *********

    @ManyToOne
    @JoinColumn(name="pieza_fk", insertable=false, updatable=false)
    Pieza pieza;

    @Column(name="pieza_fk")
    private Long pieza_fk;

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        updated = new Date();
        if (created==null) {
          created = new Date();
        }
    }

    public Pieza getPieza() {
        return pieza;
    }

    public void setPieza(Pieza pieza) {
        this.pieza = pieza;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getPieza_fk() {
        return pieza_fk;
    }

    public void setPieza_fk(Long pieza_fk) {
        this.pieza_fk = pieza_fk;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public TipoArchivo getTamtoType() {
        return tamtoType;
    }

    public void setTamtoType(TipoArchivo tamtoType) {
        this.tamtoType = tamtoType;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getUpdatedFormatted() {
        if(getUpdated()==null) return "";
        DateFormat sdf = new SimpleDateFormat("dd MMM yyyy ' a las ' hh:mm aa");
        return sdf.format(getUpdated());
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Blob getBytes() {
        return bytes;
    }

    public void setBytes(Blob bytes) {
        this.bytes = bytes;
    }

    public String getFileNamePdf() { return fileNamePdf; }

    public void setFileNamePdf(String fileNamePdf) { this.fileNamePdf = fileNamePdf; }

    public String getFileSizePdf() { return fileSizePdf; }

    public void setFileSizePdf(String fileSizePdf) { this.fileSizePdf = fileSizePdf; }

    public String getFileTypePdf() { return fileTypePdf; }

    public void setFileTypePdf(String fileTypePdf) { this.fileTypePdf = fileTypePdf; }

    public Blob getBytesPdf() { return bytesPdf; }

    public void setBytesPdf(Blob bytesPdf) { this.bytesPdf = bytesPdf; }

    public String getFileActionsHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"/piezaDownload/"+getId()+"\" class=\"btn btn-xs green\"><i class=\"icon-cloud-download\"></i> Descargar</a>");
        sb.append("<a href=\"/archivoEdicion/"+getId()+"\" data-target=\"#ajax\" data-toggle=\"modal\" class=\"btn btn-xs red\"><i class=\"icon-cloud-upload\"></i> Editar</a>");
        return sb.toString();
    }

    public String getFileTypeHtml() {
        String type = getFileType();
        StringBuilder sb = new StringBuilder();

        if (type.equals("application/vnd.ms-excel")) {
            sb.append("<span class=\"label label-sm label-warning\">");
            sb.append("EXCEL");

        } else if (type.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            sb.append("<span class=\"label label-sm label-warning\">");
            sb.append("EXCEL");

        } else {
            sb.append("<span class=\"label label-sm label-danger\">");
            sb.append(type);
        }

        sb.append("</span>");

        return sb.toString();
    }

    public String getTamtoTypeHtml() {
            String type = getTamtoType().name();
            if(type == null) return "";

            StringBuilder sb = new StringBuilder();

            if (type.equals("DIBUJO")) {
                if(getFileType().contains("application/pdf")) sb.append("<span class=\"label label-sm label-default\">PDF</span>&nbsp;");
                sb.append("<span class=\"label label-sm label-warning\">");
                sb.append("Dibujo");

            } else if (type.equals("PROGRAMA")) {
                sb.append("<span class=\"label label-sm label-info\">");
                sb.append("Programa");

            } else if (type.equals("ITEM")) {
                            sb.append("<span class=\"label label-sm label-success\">");
                            sb.append("Item");

            } else {
                sb.append("<span class=\"label label-sm label-danger\">");
                sb.append(type);
            }

            sb.append("</span>");

            return sb.toString();
        }

}
