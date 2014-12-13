package hello;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 08/12/14
 * Time: 11:29
 */
@Controller
public class HomeController {
    @RequestMapping("/")
        public String pieza(Model model) {
            model.addAttribute("selectedMenu", "dashboard");
            return "home";
        }
}
