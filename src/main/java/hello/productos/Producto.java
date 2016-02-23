package hello.productos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hello.Pieza;
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
@JsonIgnoreProperties({"controlDeCambios"})
public class Producto {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    String name;

    @Column(name = "code", length = 32)
    String code;

    @Column(name = "notes")
    String notes;

    /**
     * Almacena una imagen que representa al producto o una fotografía del mismo
     */
    @Lob
    @Column(name = "image")
    private Blob imagen;

    /**
     * Una pieza puede estar en ninguno, uno o muchos productos.
     */
    @ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    private List<Pieza> piezas;
}
