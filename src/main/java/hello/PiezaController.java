package hello;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 20/11/14
 * Time: 19:28
 */
@Controller
public class PiezaController {

    @Autowired
    PiezaRepository piezaRepository;

    @Autowired
    ArchivoRepository archivoRepository;

    @PreAuthorize("isAuthenticated() and hasPermission(#piezaId, 'somePermissionName')")
    @RequestMapping("/pieza/{piezaId}")
    public String pieza(@PathVariable Long piezaId, Model model) {
        Pieza p = piezaRepository.findOne(piezaId);
        model.addAttribute("descripcion", p.getDescripcion());
        model.addAttribute("barcode", p.getBarcode());
        model.addAttribute("cliente", p.getCliente());
        model.addAttribute("descripcion", p.getDescripcion());
        model.addAttribute("nombreSap", p.getNombreSap());
        model.addAttribute("notas", p.getNotas());
        model.addAttribute("tipo", p.getTipoPieza());
        model.addAttribute("workOrderDate", p.getWorkOrderDateString());
        model.addAttribute("workOrderNo", p.getWorkOrderNo());
        model.addAttribute("pieza", p);
        model.addAttribute("selectedMenu", "piezas");
        model.addAttribute("roles", getCurrentRoles());
        return "pieza";
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#piezaId, 'somePermissionName')")
    @RequestMapping(value = "/piezaEdicion/{piezaId}", method = RequestMethod.GET)
    public String piezaEdicion(@PathVariable Long piezaId, Model model) {
        Pieza p = piezaRepository.findOne(piezaId);
        model.addAttribute("piezaId", piezaId);
        model.addAttribute("pieza", p);
        model.addAttribute("selectedMenu", "piezas");
        model.addAttribute("roles", getCurrentRoles());
        return "pieza_edicion";
    }

    @RequestMapping(value = "/piezaEdicion/{piezaId}", method = RequestMethod.POST)
    public String editar(
            @PathVariable Long piezaId,
            @ModelAttribute Pieza pieza,
            Model model) {

        /* Guardar registro */

        Pieza p = piezaRepository.findOne(piezaId);
        p.setDescripcion( pieza.getDescripcion() );
        p.setNombreSap( pieza.getNombreSap() );
        p.setCliente( pieza.getCliente() );
        p.setTipoPieza( pieza.getTipoPieza() );
        p.setWorkOrderDate( pieza.getWorkOrderDate() );
        p.setWorkOrderNo( pieza.getWorkOrderNo() );
        p.setNotas(pieza.getNotas());

        piezaRepository.save(p);

        /* Preparar html */

        model.addAttribute("piezaId", piezaId);
        model.addAttribute("pieza", p);
        model.addAttribute("selectedMenu", "piezas");

        return "pieza_edicion";

    }

    @RequestMapping(value = "/piezaNueva", method = RequestMethod.POST)
    public String piezaNuevaPost(
            @ModelAttribute Pieza pieza,
            @RequestParam("file") MultipartFile[]  file,
            @RequestParam("tipo0") TipoArchivo tipo0,
            @RequestParam("tipo1") TipoArchivo tipo1,
            @RequestParam("tipo2") TipoArchivo tipo2,
            @RequestParam("tipo3") TipoArchivo tipo3,
            Model model) {

        TipoArchivo[] tipos = new TipoArchivo[] { tipo0,tipo1,tipo2,tipo3 };
        /* Preparar archivos para su carga */
        StringBuilder msg = new StringBuilder();
        int i = 0;
        for(MultipartFile f : file) {
            try {
                //Si el archivo está vacío, se lo salta
                if(f.isEmpty()) continue;

                Blob blob = new javax.sql.rowset.serial.SerialBlob(f.getBytes());

                Archivo archivo = new Archivo();
                archivo.setFileName(f.getOriginalFilename());
                archivo.setFileSize(String.valueOf(f.getSize()));
                archivo.setFileType(f.getContentType());
                archivo.setTamtoType( tipos[i] );
                archivo.setBytes(blob);

                pieza.addArchivo(archivo);

                msg.append( "Archivo exitosamente cargado " + f.getOriginalFilename() +"<br/>" );
            } catch (Exception e) {
                msg.append( "Falla al cargar archivo " + f.getOriginalFilename() + ": " + e.getMessage() +"<br/>" );
                e.printStackTrace();
            }
            i++;
        }
        /* Guardar registro */

        savePiezaDeeply(pieza);

        /* Preparar html */

        model.addAttribute("piezaId", pieza.getId());
        model.addAttribute("pieza", pieza);
        model.addAttribute("selectedMenu", "piezas");

        return "pieza_edicion";

    }

    @RequestMapping(value = "/piezaNueva", method = RequestMethod.GET)
    public String piezaNueva(Model model) {
        model.addAttribute("selectedMenu", "piezas");
        model.addAttribute("pieza", new Pieza());
        return "pieza_nueva";
    }

    @Transactional
    private void savePiezaDeeply(Pieza p) {
        //Guardo primero la pieza para obtener un ID
        p = piezaRepository.save(p);
        //Si hay archivos asociados los guardo, ligándolos con el ID de la pieza
        if(p.getArchivos() != null)
            for(Archivo a : p.getArchivos()) {
                a.setPieza_fk(p.getId());
                archivoRepository.save(a);
            }
    }

    /**
     * Descarga un archivo. Aplica los permisos de VER un archivo.
     * @param piezaArchivoId
     * @param response
     * @return
     */
    //TODO Pasar a ArchivoController y cambiar nomenclatura
    @PreAuthorize("hasPermission(#piezaArchivoId, 'archivo', 'VER')")
    @RequestMapping("/piezaDownload/{piezaArchivoId}")
    public String download(@PathVariable("piezaArchivoId")
            Long piezaArchivoId, HttpServletResponse response) {

        Archivo a = archivoRepository.findOne(piezaArchivoId);
        try {
            response.setHeader("Content-Disposition", "attachment; filename=\"" +a.getFileName()+ "\"");
            response.setContentType("application/force-download");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setContentLength((int) a.getBytes().length());
            OutputStream out = response.getOutputStream();
            IOUtils.copy(a.getBytes().getBinaryStream(), out);
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }

    /**
     * Agrega un nuevo archivo a una pieza existente.
     * @param piezaId
     * @param pieza
     * @param files
     * @param tipo
     * @param model
     * @return
     */
    //TODO Pasar a ArchivoController y cambiar nomenclatura
    @RequestMapping(value = "/piezaUpload/{piezaId}", method = RequestMethod.POST)
    public @ResponseBody
    List<Archivo> upload(
            @PathVariable Long piezaId,
            @ModelAttribute Pieza pieza,
            @RequestParam("file") MultipartFile[] files,
            @RequestParam("tipo") TipoArchivo tipo,
            Model model) {

        /* Guardar archivos */
        String fileName = null;
        String msg = "";
        List<Archivo> fileMetas;

        if (files != null && files.length >0) {
                    for(int i =0 ;i< files.length; i++){
                        try {
                            fileName = files[i].getOriginalFilename();
                            Blob blob = new javax.sql.rowset.serial.SerialBlob(files[i].getBytes());

                            Archivo archivo = new Archivo();
                            archivo.setFileName(files[i].getOriginalFilename());
                            archivo.setFileSize(String.valueOf(files[i].getSize()));
                            archivo.setFileType(files[i].getContentType());
                            archivo.setTamtoType(tipo);
                            archivo.setBytes(blob);
                            archivo.setPieza_fk(piezaId);

                            archivoRepository.save(archivo);

                            msg += "Archivo exitosamente cargado " + fileName +"<br/>";
                        } catch (Exception e) {
                            System.out.println( "Falla al cargar archivo " + fileName + ": " + e.getMessage() +"<br/>" );
                            e.printStackTrace();
                        }
                    }
                    System.out.println( msg );
                } else {
                    System.out.println( "No es posible subir el archivo ya que está vacío." );
                }

        /* Preparar html */
        fileMetas = piezaRepository.findOne(piezaId).getArchivos();

        return fileMetas;

    }

    @RequestMapping(value = "/piezaEdicion/{piezaId}", method = RequestMethod.HEAD)
    public @ResponseBody String metodoDisponible(@PathVariable Long piezaId) {
        return "ok";

    }

    /**
     * Método interno para obtener los roles del usuario que tiene sesión abierta. Lo utilizan los métodos del controlador.
     * @return
     */
    private HashSet<Roles> getCurrentRoles() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();

        HashSet<Roles> roles = new HashSet<>();
        for(GrantedAuthority ga : a.getAuthorities()) {
            roles.add(Roles.valueOf( ga.getAuthority() ));
        }

        return roles;
    }
}
