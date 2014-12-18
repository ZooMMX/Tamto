package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.util.*;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 04/12/14
 * Time: 13:52
 */
@Controller
public class UsuariosController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/usuarios")
    public String usuarios(Model model) {

        List usuarios = (List) userRepository.findActiveUsers();

        model.addAttribute("selectedMenu", "usuarios");
        model.addAttribute("users", usuarios);
        model.addAttribute("noUsuarios", usuarios.size());
        return "usuarios";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/usuario/{username}")
    public String usuarioVer(@PathVariable("username") String username, Model model) {
        User user = userRepository.findByUsername(username);

        //Construyo un HashSet con los roles actuales
        HashSet userRoles = new HashSet();
        for(UserRole ur : user.getUserRole())
            userRoles.add(ur.getRole());

        model.addAttribute("selectedMenu", "usuarios");
        model.addAttribute("user", user);
        model.addAttribute("roleTypes", Roles.values());
        model.addAttribute("userRoles", userRoles);

        return "usuario";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/usuarioNuevo")
    public String usuarioNuevo(Model model) {

        model.addAttribute("selectedMenu", "usuarios");
        model.addAttribute("user", new User());
        model.addAttribute("roleTypes", Roles.values());
        return "usuario_nuevo";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/usuarioNuevo", method = RequestMethod.POST)
    public String usuarioNuevoPost(
            @ModelAttribute User user,
            @RequestParam("roles") String roles,
            Model model) {

        /* Encriptar password */

        user.setPassword( passwordEncoder.encode(user.getPassword()) );

        /* Asignar roles */

        for( String role : roles.split(",") ) {
                    user.addUserRole(role);
                }

        /* Guardar registro */

        userRepository.save(user);

        /* Preparar html */

        model.addAttribute("username", user.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("selectedMenu", "usuarios");
        model.addAttribute("roleTypes", Roles.values());

        return "usuario_nuevo";

    }

    @RequestMapping(value = "/usuarioCheckUsername")
    public @ResponseBody String usuarioCheckUsername(@RequestParam("username") String username) {

        if(username == null)
            return "nombre inválido";
        else
            return String.valueOf(!userRepository.exists(username));
    }

    /**
     * Editar usuario. Primer paso GET
     * @param username
     * @param model
     * @return
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/usuarioEdicion/{username}")
    public String usuarioNuevo(@PathVariable String username, Model model) {

        User user = userRepository.findByUsername(username);

        //Construyo un HashSet con los roles actuales
        HashSet userRoles = new HashSet();
        for(UserRole ur : user.getUserRole())
            userRoles.add(ur.getRole());

        model.addAttribute("selectedMenu", "usuarios");
        model.addAttribute("user", user);
        model.addAttribute("roleTypes", Roles.values());
        model.addAttribute("userRoles", userRoles);

        return "usuario_edicion";
    }

    /**
     * Editar usuario. 2do Paso POST
     * @param username
     * @param user
     * @param roles
     * @param model
     * @return
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/usuarioEdicion/{username}", method = RequestMethod.POST)
        public String usuarioEdicionPost(
                @PathVariable String username,
                @ModelAttribute User user,
                @RequestParam("roles") String roles,
                Model model) {

            User userFromBd = userRepository.findByUsername(user.getUsername());

            //Construyo un HashSet con los roles actuales
            HashSet userRoles = (HashSet) user.getUserRole();

            /* Actualizar campos modificados */
            userFromBd.setEnabled( user.isEnabled() );
            userFromBd.setFullname(user.getFullname());

            /* Cambiar y Encriptar password si ha sido cambiado*/
            if(!user.getPassword().isEmpty())
                userFromBd.setPassword( passwordEncoder.encode(user.getPassword()) );

            /* Limpiar y reasignar roles */
            // Para cada rol marcado en el formulario
            for( String role : roles.split(",") ) {
                //Para cada rol del usuario en la BD
                Boolean alreadyExists = false;
                for( UserRole ur : userFromBd.getUserRole() ) {
                    //Verifico si ya existía ese rol
                    if( ur.getRole().equals(role) )
                        alreadyExists = true;
                }
                //Agregar rol si no lo tiene
                if(alreadyExists)  { /* Does nothing */ } else
                    userFromBd.addUserRole(role);
            }

            //Quitar rol: si roles en el formulario no lo contiene y la BD sí
            ArrayList<UserRole> quitarRoles = new ArrayList<UserRole>();
            for( UserRole ur : userFromBd.getUserRole() ) {
                if(!Arrays.asList( roles.split(",") ).contains(ur.getRole()))  {
                    quitarRoles.add(ur);
                }
            }
            for(UserRole ur : quitarRoles)
                userFromBd.getUserRole().remove(ur);

            /* Guardar registro */
            modificarUsuario(userFromBd);

            /* Preparar html */

            //Construyo un HashSet con los roles actuales
            for(UserRole ur : userFromBd.getUserRole())
                userRoles.add(ur.getRole());

            model.addAttribute("username", user.getUsername());
            model.addAttribute("user", userFromBd);
            model.addAttribute("selectedMenu", "usuarios");
            model.addAttribute("roleTypes", Roles.values());
            model.addAttribute("userRoles", userRoles);

            return "usuario_edicion";

        }

    @Transactional
    public void modificarUsuario(User user) {
        userRepository.save(user);
    }

}
