package hello.calidad;

import hello.Propiedad;
import hello.PropiedadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.ListResourceBundle;

/**
 * Proyecto tamto
 * User: octavioruizcastillo
 * Date: 25/04/15
 * Time: 16:07
 */
@Controller
public class ManualController {

    @Autowired
    ListaMaestraRepository repo;

    /**
     * Obtiene el primer documento con nivel 1. Considerando que hasta el momento sólo hay un documento con dicho
     * nivel y es el manual de calidad, además hasta donde se por definición siempre existirá sólo ese documento nivel 1.
     * @param model
     * @return
     */
    @RequestMapping("/calidad/manual")
    private String verManual(Model model) {
        List<DocumentoInterno> docs = repo.findByNivel(1);

        model.addAttribute("selectedMenu", "calidad");
        if(docs.size()>0)
            model.addAttribute("id", docs.get(0).getId());

        return "calidad/c_manual";
    }
}
