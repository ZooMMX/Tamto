package hello.importer;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * Created by octavioruizcastillo on 06/02/16.
 */
public class FileBean {
    private CommonsMultipartFile fileData;

    public CommonsMultipartFile getFileData()
    {
        return fileData;
    }

    public void setFileData(CommonsMultipartFile fileData)
    {
        this.fileData = fileData;
    }
}
