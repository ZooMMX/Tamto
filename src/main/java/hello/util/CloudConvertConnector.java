package hello.util;

import org.aioobe.cloudconvert.CloudConvertService;
import org.aioobe.cloudconvert.ConvertProcess;
import org.aioobe.cloudconvert.ProcessStatus;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * Proyecto gs-securing-web
 * User: octavioruizcastillo
 * Date: 16/09/15
 * Time: 22:02
 */
public class CloudConvertConnector {
    public byte[] toPDF(MultipartFile origen) throws URISyntaxException, IOException, ParseException, InterruptedException {
        String tipoOrigen  = FilenameUtils.getExtension(origen.getOriginalFilename());
        String tipoDestino = "pdf";

        // Create service object
        CloudConvertService service = new CloudConvertService("9fTb6FvyfjORsM1fsh-0SPbKdXu-cpHopiWqRpFFkIzx9iFb2_Nzr85sCBPCU7PjgZXHFv_bRM1J3gZFbZawfw");

        // Create conversion process
        ConvertProcess process = service.startProcess(tipoOrigen, tipoDestino);

        // Perform conversion
        process.startConversion( origen.getInputStream(), origen.getOriginalFilename() );

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
