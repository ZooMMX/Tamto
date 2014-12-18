package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 14/12/14
 * Time: 19:44
 */
public class TamtoPermissionEvaluator implements PermissionEvaluator {

    Logger log = LoggerFactory.getLogger(TamtoPermissionEvaluator.class);

    @Autowired
    ArchivoRepository archivoRepository;

    @Override
    public boolean hasPermission(Authentication authentication,
            Object targetDomainObject, Object permission) {
        log.info("hasPermission(Authentication, Object, Object) called");
        return true;
    }

    @Override
    public boolean hasPermission(Authentication authentication,
            Serializable targetId, String targetType, Object permission) {
        log.info("hasPermission(Authentication, Serializable, String, Object) called");
        System.out.println("TargetID: "+targetId+", targetType: "+targetType+", permission: "+permission);

        //Guardo los roles del usuario actual en un simple HashSet
        HashSet<Roles> roles = new HashSet<>();
        for(GrantedAuthority ga : authentication.getAuthorities()) {
            roles.add(Roles.valueOf( ga.getAuthority() ));
        }

        //Reviso los permisos por tipo de entidad ligada
        if(targetType.equalsIgnoreCase("ARCHIVO")) {
            //Permisos para las entidades Archivo
            return _permisosArchivos(roles, (Long)targetId, (String) permission);
        }

        return false;
    }

    private boolean _permisosArchivos(HashSet<Roles> roles, Long id, String permiso) {
        Archivo a = archivoRepository.findOne(id);
        return permisosArchivos(roles, a, permiso);
    }

    /**
     * Permisos por default falsos, excepto en los siguientes casos.
     * Permisos concedidos:
     *  EDITAR -> DIBUJO -> PIEZA DE CLIENTE -> Sólo PDF -> ROLE_VENTAS
     *  EDITAR -> DIBUJO -> PIEZA DE CLIENTE -> ROLE_PLANEACIÓN
     *  EDITAR -> DIBUJO -> PIEZA DE LÍNEA -> ROLE_PLANEACIÓN
     *  EDITAR -> DIBUJO -> PIEZA ESPECIAL -> ROLE_PLANEACIÓN
     *  EDITAR -> DIBUJO -> PIEZA ESPECIAL -> ROLE_PRODUCCIÓN
     *
     *  EDITAR -> ITEM -> PIEZA DE CLIENTE -> ROLE_PLANEACIÓN
     *  EDITAR -> ITEM -> PIEZA DE LÍNEA -> ROLE_PLANEACIÓN
     *  EDITAR -> ITEM -> PIEZA ESPECIAL -> ROLE_PLANEACIÓN
     *  EDITAR -> ITEM -> PIEZA ESPECIAL -> ROLE_PRODUCCIÓN
     *
     *  EDITAR -> PROGRAMA -> PC, PL, PE -> ROLE_PRODUCCIÓN
     *
     *  VER -> DIBUJO -> PC, PL, PE -> * -> ROLE_PLANEACIÓN, ROLE_PRODUCCIÓN
     *  VER -> DIBUJO -> PC, PL, PE -> PDF -> ROLE_VENTAS
     *  VER -> ITEM -> PC, PL, PE -> ROLE_*
     *  VER -> PROGRAMA -> PC, PL, PE -> ROLE_*
     *
     * @param roles
     * @param a
     * @param permiso
     * @return
     */
    public boolean permisosArchivos(HashSet<Roles> roles, Archivo a, String permiso) {

        //Para determinar el permiso debemos saber que tipo de pieza es
        Pieza p = a.getPieza();

        switch(permiso) {
            case "EDITAR":
                //Hay que saber que tipo de archivo es ¿Programa, Dibujo o Item?
                switch(a.getTamtoType()) {
                    case DIBUJO:
                        /* PERMISOS PARA UN DIBUJO */
                        switch (p.getTipoPieza()) {
                            case PC:
                                if(roles.contains(Roles.ROLE_VENTAS) && a.getFileType().contains("application/pdf")) return true;
                            case PL:
                                if(roles.contains(Roles.ROLE_PLANEACION)) return true; break;
                            case PE:
                                if(roles.contains(Roles.ROLE_PLANEACION)) return true;
                                if(roles.contains(Roles.ROLE_PRODUCCION)) return true; break;
                        }
                        break;
                    case ITEM:
                        /* PERMISOS PARA UN ITEM DE CONFIGURACIÓN */
                        switch(p.getTipoPieza()) {
                            case PC:
                            case PL:
                                if(roles.contains(Roles.ROLE_PLANEACION)) return true; break;
                            case PE:
                                if(roles.contains(Roles.ROLE_PLANEACION)) return true;
                                if(roles.contains(Roles.ROLE_PRODUCCION)) return true; break;
                        }
                        break;
                    case PROGRAMA:
                        /* PERMISOS PARA UN PROGRAMA CNC */
                        switch(p.getTipoPieza()) {
                            case PC:
                            case PL:
                            case PE:
                                if(roles.contains(Roles.ROLE_PRODUCCION)) return true; break;
                        }
                        break;
                }
                break;
            case "VER":
                //Hay que saber que tipo de archivo es ¿Programa, Dibujo o Item?
                switch(a.getTamtoType()) {
                    case DIBUJO:
                        /* PERMISOS PARA UN DIBUJO */
                        switch (p.getTipoPieza()) {
                            case PC:
                            case PL:
                            case PE:
                                if(roles.contains(Roles.ROLE_PLANEACION) || roles.contains(Roles.ROLE_PRODUCCION))
                                    return true;
                                else if(roles.contains(Roles.ROLE_VENTAS) && a.getFileType().contains("application/pdf"))
                                    return true;

                        }
                        break;
                    case ITEM:
                        /* PERMISOS PARA UN ITEM DE CONFIGURACIÓN */
                        switch(p.getTipoPieza()) {
                            case PC:
                            case PL:
                            case PE:
                                return true;
                        }
                        break;
                    case PROGRAMA:
                        /* PERMISOS PARA UN PROGRAMA CNC */
                        switch(p.getTipoPieza()) {
                            case PC:
                            case PL:
                            case PE:
                                if(roles.contains(Roles.ROLE_PRODUCCION)) return true; break;
                        }
                        break;
                }
                break;
        }

        //Comportamiento pesimista
        return false;
    }
}