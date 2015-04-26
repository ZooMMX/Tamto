package hello;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.sql.Blob;

/**
 * Proyecto tamto
 * User: octavioruizcastillo
 * Date: 26/04/15
 * Time: 7:49
 */
@Entity
public class Propiedad {
    @Id
    @Column(name = "username", unique = true,
    		nullable = false, length = 45)
    private String key;

    @Lob
    private String val;

    @Lob
    private Blob attachment;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public Blob getAttachment() {
        return attachment;
    }

    public void setAttachment(Blob attachment) {
        this.attachment = attachment;
    }
}
