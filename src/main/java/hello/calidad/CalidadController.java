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
 * Proyecto tamto
 * User: octavioruizcastillo
 * Date: 25/04/15
 * Time: 10:09
 */
@Controller
public class CalidadController {

    @Autowired
    PropiedadRepository propiedadRepository;

    //El encabezado es inyectado por CalidadLayoutAdvice
    /* Controlador en lista maestra
    @RequestMapping("/calidad")
    private String verPortadaCalidad(Model model) {
        model.addAttribute("selectedMenu", "calidad");

        return "calidad_layout";
    } */

    //El encabezado es inyectado por CalidadLayoutAdvice
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CALIDAD')")
    @RequestMapping("/calidad/encabezado")
    public String verDialogoEncabezado(Model model) {
        model.addAttribute("selectedMenu", "calidad");

        return "calidad/encabezado_calidad_edicion";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CALIDAD')")
    @RequestMapping(value = "/calidad/encabezado", method = RequestMethod.POST)
    public String updateDialogoEncabezado(Model model, @RequestParam(value = "encabezado") String encabezadoTxt) {

        if(encabezadoTxt == null)
            return "redirect:/calidad";

        Propiedad encabezado = new Propiedad();
        encabezado.setKey("ENCABEZADO-CALIDAD");
        encabezado.setVal(encabezadoTxt);

        propiedadRepository.save(encabezado);

        return "redirect:/calidad";
    }

}
