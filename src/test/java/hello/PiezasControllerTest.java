package hello;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by octavioruizcastillo on 17/03/16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IntegrationTestApplication.class)
@WebAppConfiguration
public class PiezasControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PiezaRepository piezaRepository;

    private MockMvc mockMvc;

    private Pieza piezaTest;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();

        //Busco una pieza para hacer pruebas
        Pageable pageable = new PageRequest(0, 1);  //start from -1 to match page.page
        List<Pieza> piezas = piezaRepository.findAll(pageable).getContent();
        if(piezas.isEmpty())
            throw new Exception("No hay piezas para realizar el test. Agrega una pieza de test o implementa un dataset de pruebas.");

        piezaTest = piezas.get(0);
    }

    /**
     * Test de seguridad, eliminar piezas
     * @throws Exception
     */
    @Test
    @WithMockUser(roles="PLANEACION")
    public void testSecurity() throws Exception {

        piezaTest.setAtributo("ACABADO", null);
        piezaRepository.save(piezaTest);

        mockMvc.perform(MockMvcRequestBuilders.post("/piezaEdicion/atributo")
                .param("pk", String.valueOf(piezaTest.getId()))
                .param("name", "ACABADO")
                .param("value", "niquelado")
                .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(content().string("ok"));

        Pieza p = piezaRepository.findOne(piezaTest.getId());
        assertEquals("niquelado", p.getAtributo("ACABADO"));
    }
}