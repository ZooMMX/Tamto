package hello;


//import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Proyecto tamto
 * User: octavioruizcastillo
 * Date: 11/05/15
 * Time: 17:32
 */
@Controller
public class ErrController implements org.springframework.boot.autoconfigure.web.ErrorController {
    // Error page
    @RequestMapping("/error")
    public String error(HttpServletRequest request, Model model) {
        Integer errorCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String  errorMsg  = (String)  request.getAttribute("javax.servlet.error.message");
        model.addAttribute("errorCode", errorCode);
        if(errorCode == 404) {
            return "404";
        } else if(errorCode == 403) {
            return "403";
        } else {
            Throwable throwable = (Throwable) request.getAttribute("org.springframework.web.servlet.DispatcherServlet.EXCEPTION");
            //throw safe
            if (throwable == null) {
                throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
                if(throwable == null) {
                    throwable = new Throwable("Error "+errorCode+". "+errorMsg);
                }
            }
            model.addAttribute("throwable", throwable);
            model.addAttribute("selectedMenu", "");
            return "500headless";
        }
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    //Sólo se llega desde la URL
    @RequestMapping("/403")
    public String forbidden(HttpServletRequest request, Model model) {
        model.addAttribute("selectedMenu", "");
        return "403";
    }

    //Sólo se llega desde la URL
    @RequestMapping("/404")
    public String notFound(HttpServletRequest request, Model model) {
        model.addAttribute("selectedMenu", "");
        return "404";
    }
}
