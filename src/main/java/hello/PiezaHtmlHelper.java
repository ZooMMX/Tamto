package hello;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashSet;

/**
 * Proyecto gs-securing-web
 * User: octavioruizcastillo
 * Date: 17/09/15
 * Time: 12:26
 */
public class PiezaHtmlHelper {
    Pieza parent;
    private HttpServletRequest httpServletRequest;

    public void setPermissionEvaluator(TamtoPermissionEvaluator permissionEvaluator) {
        this.permissionEvaluator = permissionEvaluator;
    }

    TamtoPermissionEvaluator permissionEvaluator;

    public void setParent(Pieza di) {
        parent = di;
    }

    public String getHtmlAction() {
        StringBuilder builder = new StringBuilder();

        //Sólo muestra el botón ver si tiene permiso para editar
        if (permissionEvaluator.permisosPiezas(getRoles(), parent, "VER"))
            builder.append(new String("<a href=\"pieza/"+ parent.getId() +"\" data-target=\"#ajax\" data-toggle=\"modal\" class=\"btn btn-xs blue\"><i class=\"fa fa-search\"></i> Ver</a>"));

        //Sólo muestra el botón editar si tiene permiso para editar
        if (permissionEvaluator.permisosPiezas(getRoles(), parent, "EDITAR"))
            builder.append(new String("<a href=\"piezaEdicion/"+ parent.getId() + "\" class=\"btn btn-xs default\"><i class=\"icon-pencil\"></i> Editar</a>"));
        return builder.toString();
    }

    public String getHtmlCheckbox() {
        String ret = new String("<input type=\"checkbox\" name=\"id[]\" value=\"" + parent.getId() + "\">");
        return ret;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    private HashSet<Roles> getRoles() {

        Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        //Guardo los roles del usuario actual en un simple HashSet
        HashSet<Roles> roles = new HashSet<>();
        for(GrantedAuthority ga : authorities) {
            roles.add(Roles.valueOf( ga.getAuthority() ));
        }

        return roles;
    }
}
