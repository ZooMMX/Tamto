package hello;

import hello.calidad.Departamento;
import hello.calidad.DocumentoInterno;
import hello.calidad.ListaMaestraRepository;
import hello.calidad.ROLCS;
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

    @Autowired
    PiezaRepository piezaRepository;

    @Autowired
    ListaMaestraRepository listaMaestraRepository;

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
        log.info("TargetID: "+targetId+", targetType: "+targetType+", permission: "+permission);

        //Guardo los roles del usuario actual en un simple HashSet
        HashSet<Roles> roles = new HashSet<>();
        for(GrantedAuthority ga : authentication.getAuthorities()) {
            roles.add(Roles.valueOf( ga.getAuthority() ));
        }

        //Reviso los permisos por tipo de entidad ligada
        if(targetType.equalsIgnoreCase("ARCHIVO")) {
            //Permisos para las entidades Archivo
            return _permisosArchivos(roles, (Long)targetId, (String) permission);
        } else if(targetType.equalsIgnoreCase("PIEZA")) {
            return _permisosPiezas(roles, (Long) targetId, (String) permission);
        } else if(targetType.equalsIgnoreCase("LISTAMAESTRA")) {
            return _permisosListaMaestra(roles, (Long) targetId, (String) permission);
        }

        return false;
    }

    private boolean _permisosListaMaestra(HashSet<Roles> roles, Long targetId, String permission) {
        DocumentoInterno documento = null;
        if(targetId != null)
            documento = listaMaestraRepository.findOne(targetId);

        return permisosListaMaestra(roles, documento, permission);
    }

    /**
     * Determina los permisos de la lista maestra
     * Permisos pesimistas.
     *
     * 	                        Ver	    Editar  	    Descargar
     * Ventas	                Todo	Sólo sus docs.	Sólo sus docs.
     * Planeación	            Todo	Sólo sus docs.	Sólo sus docs.
     * Logística	            Todo	Sólo sus docs.	Sólo sus docs.
     * RH	                    Todo	Sólo sus docs.	Sólo sus docs.
     * Calidad	                Todo	Sólo sus docs.	Sólo sus docs.
     * Dirección	            Todo	Sólo sus docs.	Sólo sus docs.
     * Representante dirección	Todo	Sólo sus docs.	Sólo sus docs.
     * Admin                	Nada    Nada            Nada
     * Produccion               Nada    Nada            Nada

     * @param roles
     * @param documento
     * @param permission
     * @return Boolean
     */
    private Boolean permisosListaMaestra(HashSet<Roles> roles, DocumentoInterno documento, String permission) {

        //Verifico que tengan algún rol del módulo de calidad (cualquiera menos admin y produccion), experaso en el Enum hello.calidad.ROLCS
        Boolean contains = false;
        for(ROLCS rolc : ROLCS.values()) {
            //Convierto de ROLCS a ROLE; NOTA: ROLCS significa algo así como rol del sistema de calidad (yo no lo puse si no tamto)
            Roles requiredRole = Roles.valueOf("ROLE_" + rolc.toString());
            //**Usuarios del sistema de calidad.** Si la autoridad del usuario incluye este "ROLCS" entonces contains = true; Lo que significa que es un usuario con permiso de acceso al sistema de calidad
            contains = roles.contains(requiredRole);
            if(contains) break;
        }
        if(!contains) return false;
        // ------ FIN Verificación de cualquier rol de calidad ------

        //Ahora verifico el resto de las reglas
        switch(permission) {
            case "VER":
                return true;
            case "AGREGAR":
                return true;
            case "EDITAR":
                if(documento == null) return true; //TODO Nuevos documentos son permitidos sin considerar roles, aunque debería considerarse el ROLCS. Esto en el POST de agregar/editar
            case "DESCARGAR":
                Roles requiredRole = Roles.valueOf("ROLE_"+documento.getRolcs());
                return roles.contains(requiredRole); // TRUE si el usuario tiene al menos el mismo rol del documento
        }
        // ----------- FIN REGLAS PRINCIPALES -------------

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
     *  EDITAR -> PROGRAMA -> PC, PL, PE -> ROLE_PLANEACIÓN
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
                                if(roles.contains(Roles.ROLE_PLANEACION)) return true; break;
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
                                if(roles.contains(Roles.ROLE_PRODUCCION) || roles.contains(Roles.ROLE_PLANEACION)) return true; break;
                        }
                        break;
                }
                break;
        }

        //Comportamiento pesimista
        return false;
    }

    /**
     * Método wrapper para consultar permisos de una pieza por ID
     * @param roles
     * @param id
     * @param permiso
     * @return
     */
    private boolean _permisosPiezas(HashSet<Roles> roles, Long id, String permiso) {
        if(id==null && permiso.equalsIgnoreCase("AGREGAR")) return true;

        Pieza p = piezaRepository.findOne(id);
        return permisosPiezas(roles, p, permiso);
    }

    /**
     * Método para consultar los permisos de una Pieza dado el objeto Pieza.
     * Optimista a menos que:
     * VER,EDITAR -> ROLE_* -> ENABLED -> FALSE (Piezas inactivas)
     * @param roles
     * @param p
     * @param permiso
     * @return
     */
    private boolean permisosPiezas(HashSet<Roles> roles, Pieza p, String permiso) {
        switch(permiso) {
            case "AGREGAR": break;
            case "VER":
            case "EDITAR":
                if(!p.isEnabled()) return false;
                break;
        }
        return true;
    }
}