package hello.productos;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.sun.org.apache.xpath.internal.operations.Bool;
import hello.*;
import hello.calidad.ItemAuditoria;
import hello.calidad.ROLCS;
import hello.productos.identicons.IdenticonGenerator;
import hello.util.NullAwareBeanUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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

    @Autowired
    EntityManager em;

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
        model.addAttribute("product"        , p);
        model.addAttribute("titulo"         , "Producto");
        model.addAttribute("action"         , "/product/update");
        model.addAttribute("showEliminarTab", true);
        model.addAttribute("selectedMenu"   , "product");
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
            //int blobLength = (int) p.getImage().length();
            //bytes = p.getImage().getBytes(1, blobLength);
            bytes = p.getImage();
        } catch (NullPointerException e ) {
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
     * Add producto (View)
     * @param model
     * @return
     */
    @RequestMapping(value = "/product/add")
    public String addProductGet(Model model) {

        Product p = new Product();
        model.addAttribute("product"     , p);
        model.addAttribute("titulo"      , "Nuevo Producto");
        model.addAttribute("action"      , "/product/add");
        model.addAttribute("selectedMenu", "product");
        return "products/product";
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

        /*
        Blob imageBlob = image == null || image.length < 1 || image[0].isEmpty() ?
                getImageBlobFromBytes( new IdenticonGenerator().generateIdenticonBytes(code) ) :
                getImageBlobFromMultipartFile(image);
                */
        byte[] imageBlob = image == null || image.length < 1 || image[0].isEmpty() ?
                new IdenticonGenerator().generateIdenticonBytes(code) :
                image[0].getBytes();

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

    private Blob getImageBlobFromBytes(byte[] bytes) throws SQLException {
        Blob imageBlob = null;
        if(bytes.length >0)
            imageBlob = new javax.sql.rowset.serial.SerialBlob(bytes);

        return imageBlob;
    }

    /**
     * Update Product
     * @param image
     * @param pieza_id
     * @param redirectAttributes
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/product/update", method = RequestMethod.POST)
    public ModelAndView updateProductPost(
            /*@RequestParam Long id,
            @RequestParam String name,
            @RequestParam String code,
            @RequestParam String notes,*/
            @ModelAttribute Product product,
            @RequestParam(value = "product_image", required = false) MultipartFile[] image,
            @RequestParam(value = "pieza_id[]", required = false) Long[] pieza_id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            Model model) throws IOException, SQLException, InvocationTargetException, IllegalAccessException {

        //Cambio opcional de imagen
        if(image != null && image.length > 0 && image[0] != null && !image[0].isEmpty()) {
            //Blob imageBlob = getImageBlobFromMultipartFile(image);
            byte[] imageBlob = image[0].getBytes();
            product.setImage(imageBlob);
        } else {
            //Por defecto debo extraer la imagen original y añadirla de nuevo
            //  Nota: Si no hago esto image queda null
            //  Nota 2: Intenté hacer un UPDATE sin imagen pero no pude hacer
            //      update de la relación many-to-many
            Product productFromBd = repo.findOne(product.getId());
            product.setImage(productFromBd.getImage());
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
    public void saveProduct(Long[] piezas_ids, RedirectAttributes redirectAttributes, Product product) {
        Long[] pieza_id = piezas_ids;
        try {
            if(piezas_ids != null && piezas_ids.length > 0) { //¿Hay piezas que agregar al productos?
                //Recupero las piezas que contiene este producto
                List<Pieza> piezaList = getPiezas(pieza_id);

                //Añado las nuevas piezas
                if(product.getPiezas() == null)
                    product.setPiezas(piezaList);
                else
                    product.getPiezas().addAll(piezaList);
            }

            //Persisto el producto
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

    @RequestMapping("/product/{id}/audit")
    public String verAuditoria(@PathVariable("id") Long id, Model model) {

        model.addAttribute("revisiones", createRevisions(id));

        return "products/audit";
    }

    /**
     * Genera una lista de auditoría con los últimos cambios realizados a un producto, características:
     *  - Especifica quien realizó el cambio
     *  - Especifica cuando se realizó
     *  - Especifica cada campo que fue modificado
     *  - Especifica el nuevo valor de cada campo modificado
     *  - Sólo considera las últimas 100 revisiones
     *  - Siempre agrega el autor y la fecha de creación del registro (1ra revisión) en el último elemento de la lista
     * @param id
     * @return
     */
    private List<ItemAuditoria> createRevisions(Long id) {

        // ******** Datos sobre la creación de la entidad (primera revisión) ******
        Query q0 = em.createNativeQuery("SELECT u.fullname, timestamp FROM product_aud doc JOIN revision r ON doc.rev = r.id JOIN user u ON r.username = u.username where doc.id = "+id+" ORDER BY timestamp ASC LIMIT 1");
        Object[] firstRev = (Object[]) q0.getSingleResult();
        //La fecha de creación también puede ser tomada directo de DocumentoInterno.created
        Date created = (Date) Date.from(Instant.ofEpochMilli(((BigInteger) firstRev[1]).longValue()));
        String autor = (String) firstRev[0];

        // ******** Datos sobre actualizaciones a la entidad *******
        //  (Índices del array)--------->      [0]   [1]   [2]    [3]   [4]    [5]       [6]        [7]       [8]        [9]         [10]        [11]
        Query q = em.createNativeQuery("SELECT p.id, code, image, name, notes, code_mod, image_mod, name_mod, notes_mod, piezas_mod, u.fullname, timestamp FROM product_aud p JOIN revision r ON p.rev = r.id JOIN user u ON r.username = u.username where p.id = "+id+" ORDER BY timestamp ASC LIMIT 100 OFFSET 1");
        List rl = q.getResultList();
        //Hago SELECT en orden inverso para poder utilizar OFFSET y quitar el registro de creación de la entidad
        //Con Collections.reverse pongo la lista en el sentido que requiero de más actual a más viejo
        Collections.reverse(rl);

        List<ItemAuditoria> resultados = new ArrayList<>();

        for(Object object : rl) {
            Object[] r = (Object[]) object;
            // Mapeo manual de campos
            String code       = (String) r[1];
            // No obtengo la imagen
            String  name      = (String) r[3];
            // No obtengo las notas por ser muy largas para mostrar a detalle
            Boolean codeMod   = (Boolean) r[5];
            Boolean imageMod  = (Boolean) r[6];
            Boolean nameMod   = (Boolean) r[7];
            Boolean notesMod  = (Boolean) r[8];
            Boolean piezasMod = (Boolean) r[9];
            String usuario    = (String) r[10];
            Date revisionDate = Date.from(Instant.ofEpochMilli(((BigInteger) r[11]).longValue()));

            // Ejemplo de la descripción de la revisión: "Juan modificó el código universal a 1009, cliente a Tamto, descripción a 'Nueva Descripción' hace 3 días"
            StringBuilder descripcionRev = new StringBuilder();
            descripcionRev.append("modificó ");

            ArrayList<String> msgs = new ArrayList<>();
            Boolean coma = false;
            if(codeMod != null && codeMod) {
                msgs.add("el código a \""+ code +"\"");
            }
            // Cancelado por defecto, siempre se muestra modificado, el problema reside en que
            //      hibernate siempre altera el contenido de este campo aunque sea igual al contenido
            //      anterior, al parecer no lo diferencía
            /*
            if(imageMod != null && imageMod) {
                msgs.add("la imagen o fotografía");
            }*/
            if(nameMod != null && nameMod) {
                msgs.add("el nombre del producto por \""+name+"\"");
            }
            if(notesMod != null && notesMod) {
                msgs.add("las notas");
            }
            if(piezasMod != null && piezasMod) {
                msgs.add("las piezas que contiene");
            }


            if(msgs.size() > 1) { //Varios cambios
                //Agregar " , " para concatenar frases
                descripcionRev.append( String.join(", ", msgs.subList(0, msgs.size()-1)) );
                //Agregar palabra " Y " para concatenar últimas dos frases
                descripcionRev.append( " y " );
                descripcionRev.append( msgs.get(msgs.size()-1) );
            } else if(msgs.size() == 1) //Un cambio
                descripcionRev.append( msgs.get(0) );
            else    //Ningún cambio conocido, skip
                continue;

            /* Modificó otro atributo como fileType, fileSize, updated, created
            if(!coma) {
                descripcionRev.append("un atributo interno");
            }*/

            ItemAuditoria item = new ItemAuditoria(usuario, descripcionRev.toString(), Util.getElapsedTimeString(revisionDate));

            resultados.add( item );
        }

        DateFormat df = SimpleDateFormat.getDateInstance();
        resultados.add( new ItemAuditoria(autor, "registró por primera vez este producto", df.format(created)));

        return resultados;
    }
}
