package hello.importer;

import hello.TipoPieza;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.Assert.*;

/**
 * Created by octavioruizcastillo on 06/02/16.
 */
public class ImporterTest {
    public Workbook workbook;

    @Before
    public void setUp() throws ParseException {
        workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("id");
        row.createCell(1).setCellValue("descripcion");
        row.createCell(2).setCellValue("nombre sap");
        row.createCell(3).setCellValue("código universal");
        row.createCell(4).setCellValue("cliente");
        row.createCell(5).setCellValue("tipo de pieza");
        row.createCell(6).setCellValue("wo date");
        row.createCell(7).setCellValue("wo id");
        row.createCell(8).setCellValue("habilitado");
        row.createCell(9).setCellValue("notas");


        row = sheet.createRow(2);
        row.createCell(0).setCellValue(159l);
        row.createCell(1).setCellValue("Descripción pieza");
        row.createCell(2).setCellValue("nombre sap");
        row.createCell(3).setCellValue("código universal");
        row.createCell(4).setCellValue("cliente");
        row.createCell(5).setCellValue(TipoPieza.PC.toString());
        row.createCell(6).setCellValue(SimpleDateFormat.getDateInstance().parse("01/01/2016"));
        row.createCell(7).setCellValue(500l);
        row.createCell(8).setCellValue(true);
        row.createCell(9).setCellValue("Las notas aquí estan");
    }
    /**
     excelHeader.createCell(0).setCellValue("ID");
     excelHeader.createCell(1).setCellValue("Descripción");
     excelHeader.createCell(2).setCellValue("Nombre SAP");
     excelHeader.createCell(3).setCellValue("Universal code");
     excelHeader.createCell(4).setCellValue("Cliente");
     excelHeader.createCell(5).setCellValue("Tipo de pieza");
     excelHeader.createCell(6).setCellValue("Work order date");
     excelHeader.createCell(7).setCellValue("Work order ID");
     excelHeader.createCell(8).setCellValue("Habilitado");
     excelHeader.createCell(9).setCellValue("Notas");
     * @throws Exception
     */
    @Test
    public void testImportPiezas() throws Exception {
        ImportService importService = new Importer();

        importService.importPiezas(workbook, pieza ->
            {
                //Sólo hay un row así que pongo aquí las pruebas sin importar la fila
                assertEquals(159l, (long) pieza.getId());
                assertEquals("Descripción pieza", pieza.getDescripcion());
                assertEquals("nombre sap", pieza.getNombreSap());
                assertEquals("código universal", pieza.getUniversalCode());
                assertEquals("cliente", pieza.getCliente());
                assertEquals(TipoPieza.PC.toString(), pieza.getTipoPieza().toString());
                try {
                    assertEquals(SimpleDateFormat.getDateInstance().parse("01/01/2016"), pieza.getWorkOrderDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                assertEquals(500l, (long) pieza.getWorkOrderNo());
                assertEquals(true, pieza.isEnabled());
                assertEquals("Las notas aquí estan", pieza.getNotas());
            }
        );
    }

    @Test
    public void testImportPiezasOnError() {
        ImportService importService = new Importer();
        workbook.getSheetAt(0).getRow(2).getCell(0).setCellValue("aquí debe ir un ID tipo numérico");

        //Debe generar una excepción
        try {
            importService.importPiezas(workbook, pieza ->
                    System.out.println("-->"+pieza.getId())
            );
            assertTrue("Se permitió una operación inválida", false);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("OK", true);
        }

    }
}