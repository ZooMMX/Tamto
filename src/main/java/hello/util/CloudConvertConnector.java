package hello.util;

import hello.Archivo;
import org.aioobe.cloudconvert.CloudConvertService;
import org.aioobe.cloudconvert.ConvertProcess;
import org.aioobe.cloudconvert.ProcessStatus;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.ParseException;

/**
 * Proyecto gs-securing-web
 * User: octavioruizcastillo
 * Date: 16/09/15
 * Time: 22:02
 */
public class CloudConvertConnector {

    /**
     * Éste método auxiliar genera el PDF de un Archivo archivo
     * @param archivo
     * @throws Exception
     */
    public void addPdfPreview(Archivo archivo) throws Exception {

        //Si el archivo original ya es PDF, sólo lo clona
        if(archivo.getFileType().equals("application/pdf")) {
            archivo.setFileNamePdf(archivo.getFileName());
            archivo.setFileTypePdf(archivo.getFileType());
            archivo.setFileSizePdf(archivo.getFileSize());
            archivo.setBytesPdf(archivo.getBytes());
            return ;
        }

        try {
            //Se convierte el documento original en un PDF
            byte[] pdfBytes = toPDF(archivo);
            Blob blob = new javax.sql.rowset.serial.SerialBlob(pdfBytes);
            //Defino el nombre del PDF como el nombre original pero con extensión PDF
            String nombreDestino = FilenameUtils.getBaseName(archivo.getFileName())+".pdf";
            //Establezco todos los metadatos y el archivo en si mismo
            archivo.setFileNamePdf(nombreDestino);
            archivo.setFileSizePdf(String.valueOf(pdfBytes.length));
            archivo.setFileTypePdf("application/pdf");
            archivo.setBytesPdf(blob);
        } catch (URISyntaxException | InterruptedException | ParseException | SQLException | IOException e) {
            e.printStackTrace();
            throw new Exception( e.getMessage() + ", ocurrió un error generando vista previa del archivo");

        }

    }

    public byte[] toPDF(Archivo archivo) throws URISyntaxException, IOException, ParseException, InterruptedException, SQLException {
        String originalFilename = archivo.getFileName();
        InputStream inputStream =  archivo.getBytes().getBinaryStream();

        return toPDF(inputStream, originalFilename);
    }

    public byte[] toPDF(MultipartFile origen) throws URISyntaxException, IOException, ParseException, InterruptedException {

        return toPDF(origen.getInputStream(), origen.getOriginalFilename());

    }

    public byte[] toPDF(InputStream inputStream, String originalFilename) throws URISyntaxException, IOException, ParseException, InterruptedException {
        String tipoOrigen  = FilenameUtils.getExtension(originalFilename);
        String tipoDestino = "pdf";

        // Create service object
        CloudConvertService service = new CloudConvertService("9fTb6FvyfjORsM1fsh-0SPbKdXu-cpHopiWqRpFFkIzx9iFb2_Nzr85sCBPCU7PjgZXHFv_bRM1J3gZFbZawfw");

        // Create conversion process
        ConvertProcess process = service.startProcess(tipoOrigen, tipoDestino);

        // Perform conversion
        process.startConversion( inputStream, originalFilename );

        // Wait for result
        ProcessStatus status;
        waitLoop: while (true) {
            status = process.getStatus();

            switch (status.step) {
                case FINISHED: break waitLoop;
                case ERROR: throw new RuntimeException(status.message);
            }

            // Be gentle
            Thread.sleep(200);
        }

        // Download result
        InputStream is = service.download(status.output.url);
        byte[] bytes = IOUtils.toByteArray(is);

        // Clean up
        process.delete();

        return bytes;
    }
}
