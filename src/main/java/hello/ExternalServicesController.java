package hello;

import hello.exporter.PiezasExcelView;
import hello.importer.FileBean;
import hello.importer.ImportService;
import hello.importer.Importer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by octavioruizcastillo on 07/02/16.
 */
@Controller
public class ExternalServicesController {

    @Autowired
    PiezaRepository repository;

    @Autowired
    ImportService importService;

    @PreAuthorize("isAuthenticated() and hasAnyRole('VENTAS', 'PLANEACION', 'PRODUCCION')")
    @RequestMapping(value = "/external_services")
    public String externalServicesHome(
            Model model)
    {
        model.addAttribute("selectedMenu", "piezas");
        return "external_services_home";
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/external_services/export")
    public ModelAndView export() {
        Iterable<Pieza> piezas = repository.findAll();

        HashMap<String, Object> model = new HashMap<>();
        model.put("piezas", piezas);

        return new ModelAndView(new PiezasExcelView(), model);
    }

    @PreAuthorize("isAuthenticated() and hasAnyRole('VENTAS', 'PLANEACION', 'PRODUCCION')")
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping(value = "/external_services/import")
    public @ResponseBody
    String importXls(@RequestParam("file") MultipartFile file, Model model) throws Exception {

        FileBean fileBean = new FileBean();
        fileBean.setFileData( file);

        Importer importer = (Importer) importService;

        importer.importPiezasFromFile(fileBean, pieza -> {
            repository.save(pieza);
        });

        model.addAttribute("selectedMenu", "dashboard");

        return "ok";
    }

    @ExceptionHandler(WrongImportFormatException.class)
    public @ResponseBody String handleWrongImportFormatException(WrongImportFormatException we, HttpServletRequest request) {
        return we.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public @ResponseBody String handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        return e.getMessage();
    }
}
