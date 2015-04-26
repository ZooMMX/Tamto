package hello.calidad;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Proyecto tamto
 * User: octavioruizcastillo
 * Date: 25/04/15
 * Time: 16:07
 */
@Controller
public class ManualController {
    @RequestMapping("/calidad/manual")
    private String verManual(Model model) {
        model.addAttribute("selectedMenu", "calidad");
        return "calidad/c_manual";
    }
}
