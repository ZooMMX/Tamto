package hello;

import hello.exporter.PiezasExcelView;
import hello.importer.FileBean;
import hello.importer.Importer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

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

    @RequestMapping(value = "/external_services")
    public String externalServicesHome(
            Model model)
    {
        model.addAttribute("selectedMenu", "piezas");
        return "external_services_home";
    }

    @RequestMapping(value = "/external_services/export")
    public ModelAndView export() {
        Iterable<Pieza> piezas = repository.findAll();

        HashMap<String, Object> model = new HashMap<>();
        model.put("piezas", piezas);

        return new ModelAndView(new PiezasExcelView(), model);
    }

    @Transactional
    @RequestMapping(value = "/external_services/import")
    public String importXls(@RequestParam("file") MultipartFile file, Model model) {
        FileBean fileBean = new FileBean();
        fileBean.setFileData( file);

        Importer importer = new Importer();
        try {
            importer.importPiezasFromFile(fileBean, pieza -> {
                repository.save(pieza);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        model.addAttribute("selectedMenu", "dashboard");

        return "external_services_home";
    }
}
