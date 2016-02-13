package hello.exporter;

import hello.Pieza;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by octavioruizcastillo on 03/02/16.
 */
@Component
public class PiezasExcelView extends AbstractExcelView {

    @Override
    protected void buildExcelDocument(Map model, HSSFWorkbook workbook,
                                      HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        HSSFSheet excelSheet = workbook.createSheet("Piezas");
        setExcelHeader(excelSheet);

        List piezas = (List) model.get("piezas");
        setExcelRows(excelSheet, piezas);

    }

    public void setExcelHeader(HSSFSheet excelSheet) {
        HSSFRow excelHeader = excelSheet.createRow(0);
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

    }

    public void setExcelRows(HSSFSheet excelSheet, List<Pieza> piezas){

        CellStyle cellStyle = excelSheet.getWorkbook().createCellStyle();
        CreationHelper createHelper = excelSheet.getWorkbook().getCreationHelper();
        cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("dd/mm/yy"));

        int record = 1;
        for (Pieza pieza : piezas) {
            HSSFRow excelRow = excelSheet.createRow(record++);
            excelRow.createCell(0).setCellValue( pieza.getId() );
            excelRow.createCell(1).setCellValue( pieza.getDescripcion() );
            excelRow.createCell(2).setCellValue( pieza.getNombreSap() );
            excelRow.createCell(3).setCellValue( pieza.getUniversalCode() );
            excelRow.createCell(4).setCellValue( pieza.getCliente() );
            excelRow.createCell(5).setCellValue( pieza.getTipoPieza().toString() );
            HSSFCell celdaWODate = excelRow.createCell(6);
            celdaWODate.setCellStyle( cellStyle );
            celdaWODate.setCellValue( pieza.getWorkOrderDate() );
            excelRow.createCell(7).setCellValue( pieza.getWorkOrderNo() );
            excelRow.createCell(8).setCellValue( pieza.isEnabled() );
            excelRow.createCell(9).setCellValue( pieza.getNotas() );
        }
    }
}