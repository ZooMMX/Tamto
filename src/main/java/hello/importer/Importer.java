package hello.importer;

import hello.Pieza;
import hello.TipoPieza;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by octavioruizcastillo on 06/02/16.
 */
public class Importer implements ImportService {


    @Override
    public void importPiezasFromFile(FileBean fileBean, Consumer<Pieza> processElementsWithFunction) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(fileBean.getFileData().getBytes());
        Workbook workbook;

        if (fileBean.getFileData().getOriginalFilename().endsWith("xls")) {
            workbook = new HSSFWorkbook(bis);
        } else if (fileBean.getFileData().getOriginalFilename().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(bis);
        } else {
            throw new IllegalArgumentException("Received file does not have a standard excel extension.");
        }

        importPiezas(workbook, processElementsWithFunction);
    }

    @Override
    public void importPiezas(Workbook workbook, Consumer<Pieza> processElementsWithFunction) throws IOException {

        for (Row row : workbook.getSheetAt(0)) {
            if (row.getRowNum() > 0) {
                Pieza p = new Pieza("", "");

                p.setId(            (long) row.getCell(0).getNumericCellValue());
                p.setDescripcion(   row.getCell(1).getStringCellValue());
                p.setNombreSap(     row.getCell(2).getStringCellValue());
                p.setUniversalCode( row.getCell(3).getStringCellValue());
                p.setCliente(       row.getCell(4).getStringCellValue());
                p.setTipoPieza(     TipoPieza.valueOf(row.getCell(5).getStringCellValue()));
                p.setWorkOrderDate( row.getCell(6).getDateCellValue());
                p.setWorkOrderNo(   (long) row.getCell(7).getNumericCellValue());
                p.setEnabled(       row.getCell(8).getBooleanCellValue());
                p.setNotas(         row.getCell(9).getStringCellValue());

                if(p!=null) processElementsWithFunction.accept(p);
            }
        }

    }
}
