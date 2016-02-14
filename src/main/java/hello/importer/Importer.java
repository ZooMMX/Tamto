package hello.importer;

import hello.Pieza;
import hello.TipoPieza;
import hello.WrongImportFormatException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by octavioruizcastillo on 06/02/16.
 */
@Service
public class Importer implements ImportService {


    @Override
    public void importPiezasFromFile(FileBean fileBean, Consumer<Pieza> processElementsWithFunction) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(fileBean.getFileData().getBytes());
        Workbook workbook;

        if (fileBean.getFileData().getOriginalFilename().endsWith("xls")) {
            workbook = new HSSFWorkbook(bis);
        } else if (fileBean.getFileData().getOriginalFilename().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(bis);
        } else {
            throw new IllegalArgumentException("El archivo recibido no se reconoce como uno de excel");
        }

        importPiezas(workbook, processElementsWithFunction);
    }

    @Override
    public void importPiezas(Workbook workbook, Consumer<Pieza> processElementsWithFunction) throws Exception {

        int fila = 0;
        int col = 0;
        try {
            for (Row row : workbook.getSheetAt(0)) {
                if (row.getRowNum() > 0) {
                    col = 0;
                    Pieza p = new Pieza("", "");

                    p.setId((long) row.getCell(col++).getNumericCellValue());
                    p.setDescripcion(row.getCell(col++).getStringCellValue());
                    p.setNombreSap(row.getCell(col++).getStringCellValue());
                    p.setUniversalCode(row.getCell(col++).getStringCellValue());
                    p.setCliente(row.getCell(col++).getStringCellValue());
                    p.setTipoPieza(TipoPieza.valueOf(row.getCell(col++).getStringCellValue()));
                    p.setWorkOrderDate(row.getCell(col++).getDateCellValue());
                    p.setWorkOrderNo((long) row.getCell(col++).getNumericCellValue());
                    p.setEnabled(row.getCell(col++).getBooleanCellValue());
                    p.setNotas(row.getCell(col++).getStringCellValue());

                    if (p != null) processElementsWithFunction.accept(p);
                    fila++;
                }
            }
        } catch(Exception e) {
            fila += 2;
            throw new WrongImportFormatException("Error en la fila "+fila+", en la columna  "+col+". Se rechazó toda la importación.", e);
        }

    }
}
