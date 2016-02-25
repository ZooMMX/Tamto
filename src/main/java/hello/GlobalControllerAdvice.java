package hello;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Created by octavioruiz on 25/02/16.
 */
@ControllerAdvice
public class GlobalControllerAdvice {
    @ModelAttribute
    public void addSelectedMenu(Model model) {
        if(!model.containsAttribute("selectedMenu"))
            model.addAttribute("selectedMenu", "");
    }
}
