package hello;

import org.hibernate.envers.Audited;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Audited(withModifiedFlag = true)
@Entity
public class Pieza {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String descripcion;
    @Column(name = "nombre_sap")
    private String nombreSap;
    private String cliente;
    @Column(name = "work_order_no")
    private Long workOrderNo;         //Número de orden de trabajo
    //@Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern="dd/MM/yyyy")
    @Column(name = "work_order_date")
    private Date workOrderDate;         //Fecha de orden de trabajo
    @Column(name = "universal_code")
    private String universalCode;
    @Enumerated(EnumType.STRING)
    private TipoPieza tipoPieza;        //Tipo de pieza
    @OneToMany(cascade = CascadeType.ALL , fetch = FetchType.LAZY, mappedBy = "pieza")
    List<Archivo> archivos;
    @Column
    private String notas;
    @Column(nullable = false)
    private boolean enabled = true;

    protected Pieza() {}

    public Pieza(String descripcion, String nombreSap) {
        this.setDescripcion(descripcion);
        this.setNombreSap(nombreSap);
    }

    public List<Archivo> getArchivos() {
        return archivos;
    }

    public void setArchivos(List<Archivo> archivos) {
        this.archivos = archivos;
    }

    @Override
    public String toString() {
        return String.format(
                "Pieza[id=%d, descripcion='%s', nombreSap='%s']",
                getId(), getDescripcion(), getNombreSap());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombreSap() {
        return nombreSap;
    }

    public void setNombreSap(String nombreSap) {
        this.nombreSap = nombreSap;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public Long getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(Long workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public Date getWorkOrderDate() {
        return workOrderDate;
    }

    public void setWorkOrderDate(Date workOrderDate) {
        this.workOrderDate = workOrderDate;
    }

    public String getUniversalCode() {
        return universalCode;
    }

    public void setUniversalCode(String universalCode) {
        this.universalCode = universalCode;
    }

    public TipoPieza getTipoPieza() {
        if(tipoPieza == null) return TipoPieza.PL;
        return tipoPieza;
    }

    public void setTipoPieza(TipoPieza tipoPieza) {
        this.tipoPieza = tipoPieza;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getWorkOrderDateString() {
        DateFormat sdf = SimpleDateFormat.getDateInstance();
        String fechaString;
        if(getWorkOrderDate() == null)
                return "01/01/2000";
            else
                return sdf.format(getWorkOrderDate());

    }

    public void addArchivo(Archivo a) {
        if(getArchivos() == null)
            archivos = new ArrayList<Archivo>();

        archivos.add(a);
    }

    @Deprecated
    public String getHtmlAction() {
        StringBuilder builder = new StringBuilder();
        builder.append(new String("<a href=\"pieza/"+ getId() +"\" data-target=\"#ajax\" data-toggle=\"modal\" class=\"btn btn-xs blue\"><i class=\"fa fa-search\"></i> Ver</a>"));
        builder.append(new String("<a href=\"piezaEdicion/"+ getId() + "\" class=\"btn btn-xs default\"><i class=\"icon-pencil\"></i> Editar</a>"));
        return builder.toString();
    }

    public String getHtmlCheckbox() {
       String ret = new String("<input type=\"checkbox\" name=\"id[]\" value=\"" + getId() + "\">");
        return ret;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}