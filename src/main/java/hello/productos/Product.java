package hello.productos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hello.Pieza;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.sql.Blob;
import java.util.List;

/**
 * En este contexto un producto es un conjunto de piezas, por lo tanto sus principales propiedades
 * se encuentran definidas dentro de cada pieza.
 * Introducido en la versión 2.4
 * Created by octavioruizcastillo on 23/02/16.
 */
@Audited(withModifiedFlag = true)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Product {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "client")
    private String client;

    @Column(name = "code", length = 32)
    private String code;

    @Column(name = "notes")
    private String notes;

    /**
     * Almacena una imagen que representa al producto o una fotografía del mismo
     */
    @Lob
    @Column(name = "image")
    private byte[] image;

    /**
     * Una pieza puede estar en ninguno, uno o muchos productos.
     */
    @ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    private List<Pieza> piezas;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public List<Pieza> getPiezas() {
        return piezas;
    }

    public void setPiezas(List<Pieza> piezas) {
        this.piezas = piezas;
    }

    public String getClient() { return client; }

    public void setClient(String client) { this.client = client; }
}
