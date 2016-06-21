package hello;

import hello.calidad.DocumentoInterno;
import hello.util.CloudConvertConnector;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

/**
 * User: octavioruizcastillo
 * Date: 13/12/14
 * Time: 19:18
 */
@Controller
public class ArchivoController {
    @Autowired
    ArchivoRepository archivoRepository;

    @Autowired
    EntityManager em;

    /**
     * Modifica la categoría de un archivo
     * @param id
     * @param categoria
     * @param model
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_PLANEACION')")
    @RequestMapping("/archivoEdicion/{id}/categoria")
    public @ResponseBody String updateCategoria(
            @PathVariable Long id,
            @RequestParam Archivo.CategoriaArchivo categoria,
            Model model
    ) {
        Boolean exists = archivoRepository.exists(id);
        if(exists) {
            archivoRepository.updateCategory(id, categoria);
            return "ok";
        } else
            return "Archivo no encontrado";
    }

    /**
     * Formulario de edición de archivos
     * @param archivoId
     * @param model
     * @return
     */
    @PreAuthorize("hasPermission(#archivoId, 'archivo', 'EDITAR')")
    @RequestMapping(value = "/archivoEdicion/{archivoId}", method = RequestMethod.GET)
    public String archivoEdicion(
        @PathVariable Long archivoId,
        Model model)
    {
        Archivo archivo = archivoRepository.findOne(archivoId);
        List<Object> versiones = archivoRepository.getVersiones(archivoId);

        model.addAttribute("selectedMenu", "piezas");
        model.addAttribute("archivo", archivo);
        model.addAttribute("versiones", versiones);
        model.addAttribute("roles", getCurrentRoles());
        return "archivo_editar";
    }

    /**
     * Diálogo detalles archivo y versiones anteriores.
     * @param archivoId
     * @param model
     * @return
     */
    @PreAuthorize("hasPermission(#archivoId, 'archivo', 'EDITAR')")
    @RequestMapping(value = "/archivo/detalles/{archivoId}", method = RequestMethod.GET)
    public String archivoDetalles(
            @PathVariable Long archivoId,
            Model model)
    {
        Archivo archivo = archivoRepository.findOne(archivoId);
        List<Object> versiones = archivoRepository.getVersiones(archivoId);

        model.addAttribute("selectedMenu", "piezas");
        model.addAttribute("archivo", archivo);
        model.addAttribute("versiones", versiones);
        model.addAttribute("roles", getCurrentRoles());
        return "piezas/versiones_modal";
    }

    @PreAuthorize("hasPermission(#archivoId, 'archivo', 'EDITAR') && hasPermission(#file, 'archivo', 'UPLOAD')")
    @RequestMapping(value = "/archivoEdicion/{archivoId}", method = RequestMethod.POST)
    public @ResponseBody
    Archivo archivoEdicionPost(
        @PathVariable Long archivoId,
        @RequestParam("file") MultipartFile file,
        Model model)
    {
        CloudConvertConnector connector = new CloudConvertConnector();
        Archivo archivo = archivoRepository.findOne(archivoId);

        /* Guardar archivos */
        String fileName = null;
        String msg = "";
        Archivo fileMeta;

        if (file != null) {

                    try {
                        fileName = file.getOriginalFilename();
                        Blob blob = new javax.sql.rowset.serial.SerialBlob(file.getBytes());

                        archivo.setFileName(file.getOriginalFilename());
                        archivo.setFileSize(String.valueOf(file.getSize()));
                        archivo.setFileType(file.getContentType());
                        archivo.setTamtoType(TipoArchivo.fromFilename(file.getOriginalFilename()));
                        if(archivo.getTamtoType() == TipoArchivo.PROGRAMA) archivo.setCategoria(Archivo.CategoriaArchivo.PROGRAMAS);
                        archivo.setBytes(blob);
                        connector.addPdfPreview(archivo);

                        archivoRepository.save(archivo);

                        msg += "Archivo exitosamente cargado " + fileName +"<br/>";
                    } catch (Exception e) {
                        System.out.println( "Falla al cargar archivo " + fileName + ": " + e.getMessage() +"<br/>" );
                        e.printStackTrace();
                    }

                    System.out.println( msg );
                } else {
                    System.out.println( "No es posible subir el archivo ya que está vacío." );
                }

        /* Preparar html */

        fileMeta = archivoRepository.findOne(archivoId);
        return fileMeta;
    }

    @PreAuthorize("hasPermission(#archivoId, 'archivo', 'EDITAR')")
    @RequestMapping(value = "/archivoEliminacion/{archivoId}")
    public ModelAndView archivoEliminacion(
        @PathVariable Long archivoId,
        Model model)
    {
        Archivo archivo = archivoRepository.findOne(archivoId);

        /* Guardar archivos */
        archivoRepository.delete(archivo);

        return new ModelAndView("redirect:/piezaEdicion/"+archivo.getPieza_fk());
    }

    /**
     * DESCARGAR ARCHIVO
     * Recupera un archivo en una revisión específica
     * @param id
     * @param rev
     * @param response
     * @return
     * @throws SQLException
     * @throws IOException
     */
    @PreAuthorize("hasPermission(#id, 'archivo', 'VER')")
    @RequestMapping("/pieza/archivo/{rev}/{id}")
    public String archivoDescargar(@PathVariable("id") Long id,
                            @PathVariable("rev") Integer rev,
                            HttpServletResponse response) throws SQLException, IOException {

        Archivo documento;
        // la variable "rev" indica que se quiere rescatar de la revisión #"rev" anterior, si rev=0 -> se toma el PDF actual
        if(rev == 0)
            documento = archivoRepository.findOne(id);
        else {
            AuditReader reader = AuditReaderFactory.get(em);
            documento = reader.find(Archivo.class, id, rev);
        }
        try {
            response.setHeader("Content-Disposition", "attachment; filename=\"" +documento.getFileName()+ "\"");
            response.setContentType("application/force-download");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setContentLength((int) documento.getBytes().length());
            OutputStream out = response.getOutputStream();
            org.apache.commons.io.IOUtils.copy(documento.getBytes().getBinaryStream(), out);
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }

    @PreAuthorize("hasPermission(#piezaArchivoId, 'archivo', 'VER')")
    @RequestMapping("/archivo/view/{piezaArchivoId}.pdf")
    public String view(@PathVariable("piezaArchivoId")
            Long piezaArchivoId, HttpServletResponse response) {

        Archivo a = archivoRepository.findOne(piezaArchivoId);
        //Determina si existe la versión PDF, si no, reenvía al PDF dónde explica el error
        if(a.getBytesPdf() == null) return "redirect:/media/errpreview.pdf";

        try {
            //Determina los atributos del archivo a mostrar
            String filename = a.getFileNamePdf();
            long length = a.getBytesPdf().length();
            Blob bytes = a.getBytesPdf();
            //Prepara el response
            response.setHeader("Content-Disposition", "inline; filename=\"" +filename+ "\"");
            response.setContentType("application/pdf");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setContentLength((int) length);
            OutputStream out = response.getOutputStream();
            IOUtils.copy(bytes.getBinaryStream(), out);
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }

    @RequestMapping("/archivo/pdfViewer/{piezaArchivoId}")
    public String pdfViewer(@PathVariable("piezaArchivoId")
            Long piezaArchivoId, HttpServletResponse response) {


        return "pdf_viewer";
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
