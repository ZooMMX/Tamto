package hello;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 21/11/14
 * Time: 19:20
 */

public class CustomRevisionListener implements RevisionListener {

    public void newRevision(Object revisionEntity) {

        Revision revision = (Revision) revisionEntity;
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        revision.setUsername(userDetails.getUsername());
    }

}