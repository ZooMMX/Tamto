package hello.productos;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import hello.*;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Null;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by octavioruiz on 23/02/16.
 */
@Controller
public class ProductController {
    @Autowired
    ProductRepository repo;

    @Autowired
    PiezaRepository piezaRepository;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * View product
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "/product/{id}")
    public String viewProductGet(@PathVariable Long id, Model model) {

        Product p = repo.findOne(id);
        model.addAttribute("product", p);
        return "products/product";
    }

    /**
     * View product's image
     * @return
     * @throws IOException
     */
    @ResponseBody
    @RequestMapping(value = "/product/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[]  viewImage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Product p = repo.findOne(id);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        byte[] bytes = {};
        try {
            int blobLength = (int) p.getImage().length();
            bytes = p.getImage().getBytes(1, blobLength);
        } catch (SQLException | NullPointerException e ) {
            log.error(e.getLocalizedMessage(), e);
            addFeedbackMessage(redirectAttributes, e.getLocalizedMessage(), true);
        }

        return bytes;
    }

    /**
     * Delete Product
     */
    @RequestMapping(value = "/product/{id}/delete")
    public ModelAndView deleteProductGet(@PathVariable Long id, RedirectAttributes redirectAttributes, Model model) {

        Product p = repo.findOne(id);
        if(p == null) {
            //Establezco la retroalimentación al usuario
            addFeedbackMessage(redirectAttributes, "No es posible eliminar el producto pues no existe.", true);
        } else {
            try {
                repo.delete(p);
                addFeedbackMessage(redirectAttributes, "Producto eliminado correctamente.", false);
            } catch (Exception e) {
                addFeedbackMessage(redirectAttributes, "Error al eliminar el producto: "+e.getMessage()+".", true);
            }
        }

        return new ModelAndView("redirect:/products");
    }

    /**
     * Add Product
     * @param name
     * @param code
     * @param notes
     * @param image
     * @param pieza_id
     * @param redirectAttributes
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/product/add", method = RequestMethod.POST)
    public ModelAndView addProductPost(
            @RequestParam String name,
            @RequestParam String code,
            @RequestParam String notes,
            @RequestParam("image") MultipartFile[] image,
            @RequestParam(name = "pieza_id[]", required = false) Long[] pieza_id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            Model model) throws IOException, SQLException {

        Blob imageBlob = getImageBlobFromMultipartFile(image);

        Product product = new Product();
        product.setName(name);
        product.setCode(code);
        product.setNotes(notes);
        product.setImage(imageBlob);

        saveProduct(pieza_id, redirectAttributes, product);

        return new ModelAndView("redirect:/products");
    }

    private Blob getImageBlobFromMultipartFile(@RequestParam("image") MultipartFile[] image) throws SQLException, IOException {
        Blob imageBlob = null;
        if(image.length > 0) {
            imageBlob = new javax.sql.rowset.serial.SerialBlob(image[0].getBytes());
        }
        return imageBlob;
    }

    /**
     * Update Product
     * @param name
     * @param code
     * @param notes
     * @param image
     * @param pieza_id
     * @param redirectAttributes
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/product/update", method = RequestMethod.POST)
    public ModelAndView updateProductPost(
            @RequestParam Long id,
            @RequestParam String name,
            @RequestParam String code,
            @RequestParam String notes,
            @RequestParam("image") MultipartFile[] image,
            @RequestParam("pieza_id[]") Long[] pieza_id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            Model model) throws IOException, SQLException {


        Product product = repo.findOne(id);
        product.setName(name);
        product.setCode(code);
        product.setNotes(notes);

        //Cambio opcional de imagen
        if(image != null && image.length > 0 && image[0] != null && !image[0].isEmpty()) {
            Blob imageBlob = getImageBlobFromMultipartFile(image);
            product.setImage(imageBlob);
        }

        saveProduct(pieza_id, redirectAttributes, product);

        return new ModelAndView("redirect:/products");
    }

    /**
     * Agrega o actualiza un Producto
     * @param piezas_ids
     * @param redirectAttributes
     * @param product
     */
    private void saveProduct(@RequestParam Long[] piezas_ids, RedirectAttributes redirectAttributes, Product product) {
        Long[] pieza_id = piezas_ids;
        try {
            //Recupero las piezas que contiene este producto
            List<Pieza> piezaList = getPiezas(pieza_id);

            //Preparo el producto para su persistencia
            product.setPiezas(piezaList);
            repo.save(product);

            //Establezco la retroalimentación al usuario
            addFeedbackMessage(redirectAttributes, "Operación realizada con éxito", false);

        } catch(DataIntegrityViolationException e) {

            //Establezco la retroalimentación al usuario
            if(e.getCause() instanceof MySQLIntegrityConstraintViolationException)
                addFeedbackMessage(redirectAttributes, "Código repetido, no se pudo completar la operación", true);
            else
                addFeedbackMessage(redirectAttributes, "Alguno de los campos se encuentra fuera de los parámetros permitidos", true);
        } catch (PiezaNotFoundException e) {
            //Establezco la retroalimentación al usuario
            addFeedbackMessage(redirectAttributes, e.getMessage()+". No se pudo completar la operación", true);
        }
    }


    public List<Pieza> getPiezas(@RequestParam Long[] pieza_id) throws PiezaNotFoundException {
        List<Pieza> piezaList = new ArrayList<>();
        for (Long p_id :
                pieza_id) {
            Pieza p = piezaRepository.findOne(p_id);
            if(p == null) throw new PiezaNotFoundException("No existe la pieza con ID "+p_id);
            piezaList.add(p);
        }
        return piezaList;
    }

    public void addFeedbackMessage(RedirectAttributes redirectAttributes, String message, Boolean isError) {
        redirectAttributes.addFlashAttribute("message", message);
        redirectAttributes.addFlashAttribute("isError", isError);
    }

    public class PiezaNotFoundException extends Exception {
        public PiezaNotFoundException(String msg) {
            super(msg);
        }
    }
}
