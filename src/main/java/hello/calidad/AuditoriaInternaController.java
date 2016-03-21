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
public class AuditoriaInternaController {

    @Autowired
    PropiedadRepository propiedadRepository;


    //El encabezado es inyectado por CalidadLayoutAdvice
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_CALIDAD')")
    @RequestMapping("/calidad/auditoria_interna/editor")
    public String verDialogoEditor(Model model) {
        Propiedad contenido = getContenido();

        model.addAttribute("contenido", contenido.getVal());
        model.addAttribute("selectedMenu", "calidad");

        return "calidad/auditoria_interna_editor";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN_CALIDAD')")
    @RequestMapping(value = "/calidad/auditoria_interna", method = RequestMethod.POST)
    public String updateDialogoOrganigrama(Model model, @RequestParam(value = "contenido") String contenidoTxt) {

        Propiedad contenido = new Propiedad();
        contenido.setKey("CONTENIDO-AUDITORIA-INTERNA");
        contenido.setVal(contenidoTxt);

        propiedadRepository.save(contenido);

        return "redirect:/calidad/auditoria_interna";
    }

    @RequestMapping("/calidad/auditoria_interna")
    public String verMapeoProcesos(Model model) {
        Propiedad contenido = getContenido();

        model.addAttribute("contenido", contenido.getVal());
        return "calidad/auditoria_interna";
    }

    private Propiedad getContenido() {
        //CONTENIDO-AUDITORIA-INTERNA es una propiedad constante alojada en la tabla propiedades de la BD,
        //  su función es almacenar el HTML que se mostrará como encabezado en toda la sección de calidad
        Propiedad contenido = propiedadRepository.findOne("CONTENIDO-AUDITORIA-INTERNA");

        //Null-safe, En caso de que la propiedad no sea encontrada se enviará el placeholder
        if(contenido == null) {
            contenido = new Propiedad();
            contenido.setKey("CONTENIDO-AUDITORIA-INTERNA");
            contenido.setVal("<h1>Sin contenido</h1><p>Por favor edita mi contenido, pasando el cursor por aquí y oprimiendo 'Editar contenido'.</p>");
        }

        return contenido;
    }

}