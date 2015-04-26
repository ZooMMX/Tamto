package hello.calidad;

import hello.Propiedad;
import hello.PropiedadRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping("/calidad")
    private String verPortadaCalidad(Model model) {
        model.addAttribute("selectedMenu", "calidad");
        Propiedad encabezado = getEncabezado();
        model.addAttribute("encabezado", encabezado.getVal());

        return "calidad_layout";
    }

    @RequestMapping("/calidad/encabezado")
    private String verDialogoEncabezado(Model model) {
        model.addAttribute("selectedMenu", "calidad");
        Propiedad encabezado = getEncabezado();
        model.addAttribute("encabezado", encabezado.getVal());

        return "calidad/encabezado_calidad_edicion";
    }

    @RequestMapping(value = "/calidad/encabezado", method = RequestMethod.POST)
    private String updateDialogoEncabezado(Model model, @RequestParam(value = "encabezado") String encabezadoTxt) {

        if(encabezadoTxt == null)
            return "redirect:/calidad";

        Propiedad encabezado = new Propiedad();
        encabezado.setKey("ENCABEZADO-CALIDAD");
        encabezado.setVal(encabezadoTxt);

        propiedadRepository.save(encabezado);

        return "redirect:/calidad";
    }

    private Propiedad getEncabezado() {
        //ENCABEZADO-CALIDAD es una propiedad constante alojada en la tabla propiedades de la BD,
        //  su función es almacenar el HTML que se mostrará como encabezado en toda la sección de calidad
        Propiedad encabezado = propiedadRepository.findOne("ENCABEZADO-CALIDAD");

        //Null-safe, En caso de que la propiedad no sea encontrada se enviará el placeholder
        if(encabezado == null) {
            encabezado = new Propiedad();
            encabezado.setKey("ENCABEZADO-CALIDAD");
            encabezado.setVal("<h1>Sin encabezado</h1><p>Por favor edita mi contenido, pasando el cursor por aquí y oprimiendo 'Editar encabezado'.</p>");
        }
        return encabezado;
    }
}
