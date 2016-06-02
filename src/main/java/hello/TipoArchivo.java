package hello;

import org.apache.commons.io.FilenameUtils;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 13/12/14
 * Time: 17:25
 */
public enum TipoArchivo {
    DIBUJO,
    ITEM,
    PROGRAMA,
    OTRO;

    public static TipoArchivo fromFilename(String filename) {

        String ext = FilenameUtils.getExtension(filename);
        ext = ext.toUpperCase();

        switch(ext) {
            case "XLS":
            case "XLSX":
            case "DOC":
            case "DOCX":
                return TipoArchivo.ITEM;
            case "DWG":
            case "PDF":
            case "PNG":
            case "TIF":
            case "TIFF":
                return TipoArchivo.DIBUJO;
            case "TXT":
            case "DAT":
            case "NC":
                return TipoArchivo.PROGRAMA;
            default:
                return TipoArchivo.OTRO;
        }
    }
}
