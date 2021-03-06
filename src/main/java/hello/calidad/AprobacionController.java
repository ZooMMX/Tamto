package hello.calidad;

import hello.User;
import hello.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Proyecto gs-securing-web
 * User: octavioruizcastillo
 * Date: 06/09/15
 * Time: 12:38
 */
@Controller
public class AprobacionController {
    @Autowired
    ListaMaestraRepository listaMaestraRepository;

    @Autowired
    UserRepository userRepository;

    @PreAuthorize("hasPermission(#id, 'listamaestra', 'APROBAR')")
    @RequestMapping("/calidad/documento/0/{id}/aprobar")
    public String aprobar(
            @PathVariable Long id,
            Model model,
            HttpServletRequest request,
            RedirectAttributes redirectAttrs) throws InvocationTargetException, IllegalAccessException {

        DocumentoInterno di = listaMaestraRepository.findOne(id);
        User user = userRepository.findByUsername( request.getRemoteUser() );

        di.setAprobado(true);
        di.setAprobo( user );

        listaMaestraRepository.save( di );

        //Redirect success message
        redirectAttrs.addAttribute("successMsg", "Documento aprobado");

        return "redirect:/calidad/listaMaestra";
    }

    @PreAuthorize("hasPermission(#id, 'listamaestra', 'ELIMINAR')")
    @RequestMapping("/calidad/documento/0/{id}/eliminar")
    public String eliminar(
            @PathVariable Long id,
            Model model,
            HttpServletRequest request,
            RedirectAttributes redirectAttrs) throws InvocationTargetException, IllegalAccessException {

        DocumentoInterno di = listaMaestraRepository.findOne(id);

        listaMaestraRepository.delete(di);

        //Redirect success message
        redirectAttrs.addAttribute("successMsg", "Documento eliminado");

        return "redirect:/calidad/listaMaestra";
    }
}
