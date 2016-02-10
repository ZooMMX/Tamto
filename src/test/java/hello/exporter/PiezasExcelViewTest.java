package hello.exporter;

import hello.Application;
import hello.Pieza;
import hello.TestApplication;
import hello.TipoPieza;
import hello.exporter.PiezasExcelView;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.ServletContext;
import java.util.*;


/**
 * Created by octavioruizcastillo on 03/02/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
public class PiezasExcelViewTest {


    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Before
    public void setUp() {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

    }

    @org.junit.Test
    public void testBuildExcelDocument() throws Exception {

        Pieza pieza = new Pieza("descripción", "nombre sap");
        pieza.setId(1l);
        pieza.setDescripcion("descripción");
        pieza.setCliente("cliente");
        pieza.setEnabled(true);
        pieza.setNombreSap("nombre sap");
        pieza.setNotas("las notas");
        pieza.setTipoPieza(TipoPieza.PC);
        pieza.setUniversalCode("código universal");
        pieza.setWorkOrderNo(1l);
        pieza.setWorkOrderDate(Calendar.getInstance().getTime());

        List<Pieza> piezas = new ArrayList<>();
        piezas.add(pieza);

        HashMap<String, Object> model = new HashMap<>();
        model.put("piezas", piezas);

        PiezasExcelView piezasExcelView = new PiezasExcelView();
        piezasExcelView.render(model, request, response);
    }
}