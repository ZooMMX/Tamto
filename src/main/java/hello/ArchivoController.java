package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Blob;
import java.util.List;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 13/12/14
 * Time: 19:18
 */
@Controller
public class ArchivoController {
    @Autowired
    ArchivoRepository archivoRepository;

    @PreAuthorize("hasPermission(#archivoId, 'archivo', 'EDITAR')")
    @RequestMapping(value = "/archivoEdicion/{archivoId}", method = RequestMethod.GET)
    public String archivoEdicion(
        @PathVariable Long archivoId,
        Model model)
    {
        Archivo archivo = archivoRepository.findOne(archivoId);

        model.addAttribute("selectedMenu", "piezas");
        model.addAttribute("archivo", archivo);
        return "archivo_editar";
    }

    @PreAuthorize("hasPermission(#archivoId, 'archivo', 'EDITAR')")
    @RequestMapping(value = "/archivoEdicion/{archivoId}", method = RequestMethod.POST)
    public @ResponseBody
    Archivo archivoEdicionPost(
        @PathVariable Long archivoId,
        @RequestParam("file") MultipartFile file,
        Model model)
    {
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
                        archivo.setBytes(blob);

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
}
