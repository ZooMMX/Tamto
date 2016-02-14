package hello.importer;

import hello.Pieza;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by octavioruizcastillo on 06/02/16.
 */
@Service
public interface ImportService {
    public void importPiezasFromFile(FileBean fileBean, Consumer<Pieza> consumeElements) throws Exception;
    public void importPiezas(Workbook workbook, Consumer<Pieza> consumeElements) throws Exception;
}
