package hello;

import org.apache.tomcat.util.ExceptionUtils;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 05/12/14
 * Time: 14:43
 */
@ControllerAdvice
class GlobalDefaultExceptionHandler implements ErrorController{
    public static final String DEFAULT_ERROR_VIEW = "500";

    private static final String PATH = "/error";

    @Override
    public String getErrorPath() {
        return PATH;
    }

    @ExceptionHandler(value = Exception.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {

        //Delego el tratamiento de la AccessDeniedException
        if(e instanceof AccessDeniedException) throw e;

        //if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
        //    throw e;

        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", e);
        mav.addObject("url", req.getRequestURL());
        mav.addObject("type", e.getClass().toGenericString());
        mav.addObject("error", e.getMessage());
        mav.addObject("selectedMenu", "dashboard");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        mav.addObject("message", sw.toString());
        mav.setViewName(DEFAULT_ERROR_VIEW);
        return mav;
    }

}