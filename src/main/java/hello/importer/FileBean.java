package hello.importer;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * Created by octavioruizcastillo on 06/02/16.
 */
public class FileBean {
    private MultipartFile fileData;

    public MultipartFile getFileData()
    {
        return fileData;
    }

    public void setFileData(MultipartFile fileData)
    {
        this.fileData = fileData;
    }
}
