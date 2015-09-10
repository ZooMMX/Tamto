package hello.calidad;

import hello.Propiedad;
import hello.PropiedadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Proyecto gs-securing-web
 * User: octavioruizcastillo
 * Date: 23/05/15
 * Time: 14:43
 */
@Controller
public class OrganigramaController {

    @Autowired
    PropiedadRepository propiedadRepository;


    //El encabezado es inyectado por CalidadLayoutAdvice
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_CALIDAD')")
    @RequestMapping("/calidad/organigrama/editor")
    public String verDialogoEditor(Model model) {
        Propiedad contenido = getContenido();

        model.addAttribute("contenido", contenido.getVal());
        model.addAttribute("selectedMenu", "calidad");

        return "calidad/organigrama_editor";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN_CALIDAD')")
    @RequestMapping(value = "/calidad/organigrama", method = RequestMethod.POST)
    public String updateDialogoOrganigrama(Model model, @RequestParam(value = "contenido") String contenidoTxt) {

        Propiedad contenido = new Propiedad();
        contenido.setKey("CONTENIDO-ORGANIGRAMA");
        contenido.setVal(contenidoTxt);

        propiedadRepository.save(contenido);

        return "redirect:/calidad/organigrama";
    }

    @RequestMapping("/calidad/organigrama")
    public String verMapeoProcesos(Model model) {
        Propiedad contenido = getContenido();

        model.addAttribute("contenido", contenido.getVal());
        return "calidad/organigrama";
    }

    private Propiedad getContenido() {
        //CONTENIDO-MAPEOPROCESOS es una propiedad constante alojada en la tabla propiedades de la BD,
        //  su función es almacenar el HTML que se mostrará como encabezado en toda la sección de calidad
        Propiedad contenido = propiedadRepository.findOne("CONTENIDO-ORGANIGRAMA");

        //Null-safe, En caso de que la propiedad no sea encontrada se enviará el placeholder
        if(contenido == null) {
            contenido = new Propiedad();
            contenido.setKey("CONTENIDO-ORGANIGRAMA");
            contenido.setVal("<h1>Sin contenido</h1><p>Por favor edita mi contenido, pasando el cursor por aquí y oprimiendo 'Editar contenido'.</p>");
        }

        return contenido;
    }

}