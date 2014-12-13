package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.sql.Blob;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 21/11/14
 * Time: 15:29
 */
@Audited(withModifiedFlag = true)
@Entity
@JsonIgnoreProperties({"bytes", "pieza"})
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

    @Lob
    private Blob bytes;

    @ManyToOne
    @JoinColumn(name="pieza_fk", insertable=false, updatable=false)
    Pieza pieza;

    @Column(name="pieza_fk")
    private Long pieza_fk;

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

    public String getFileActionsHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"/piezaDownload/"+getId()+"\" class=\"btn btn-xs green\"><i class=\"icon-cloud-download\"></i> Descargar</a>");
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

}
