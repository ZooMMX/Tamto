package hello;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by octavioruizcastillo on 07/02/16.
 */
@Controller
public class ExternalServicesController {
    @RequestMapping(value = "/external_services")
    public String externalServicesHome(
            Model model)
    {

        return "external_services_home";
    }
}
