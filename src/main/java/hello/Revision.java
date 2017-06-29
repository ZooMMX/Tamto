package hello;

import javax.persistence.Entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.DefaultTrackingModifiedEntitiesRevisionEntity;
import org.hibernate.envers.RevisionEntity;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 21/11/14
 * Time: 19:17
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@RevisionEntity(CustomRevisionListener.class)
public class Revision extends DefaultTrackingModifiedEntitiesRevisionEntity {

    private static final long serialVersionUID = 3775550420286576001L;

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}