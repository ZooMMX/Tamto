package hello;

import hello.productos.Product;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by octavioruizcastillo on 17/03/16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IntegrationTestApplication.class)
@WebAppConfiguration
public class PiezaControllerTest {

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
     * Test simple de cambio de atributo, se pondrá el atributo para después probar que fue cambiado.
     * @throws Exception
     */
    @Test
    @WithMockUser(roles="ADMIN")
    public void testUpdateAttribute() throws Exception {

        UserRequestPostProcessor userAdmin           = user("admin").password("abcd1234").roles("ADMIN");
        UserRequestPostProcessor userPlaneacion      = user("admin").password("abcd1234").roles("PLANEACION");
        UserRequestPostProcessor userAdminPlaneacion = user("admin").password("abcd1234").roles("ADMIN_PLANEACION");

        /* Sin permiso */
        mockMvc.perform(MockMvcRequestBuilders.get("/piezasJSON/")
                .param("length", "1").param("start", "0").param("draw", "2").param("id[]", String.valueOf(piezaTest.getId())).param("customActionType", "group_action").param("customActionName", "softdelete")
                .with(userAdmin)).andExpect(status().isOk())
                .andExpect(content().json("{'softdeleted':false}"));

        mockMvc.perform(MockMvcRequestBuilders.get("/piezasJSON/")
                .param("length", "1").param("start", "0").param("draw", "2").param("id[]", String.valueOf(piezaTest.getId())).param("customActionType", "group_action").param("customActionName", "softdelete")
                .with(userPlaneacion)).andExpect(status().isOk())
                .andExpect(content().json("{'softdeleted':false}"));

        /* Con permiso */
        mockMvc.perform(MockMvcRequestBuilders.get("/piezasJSON/")
                .param("length", "1").param("start", "0").param("draw", "2").param("id[]", String.valueOf(piezaTest.getId())).param("customActionType", "group_action").param("customActionName", "softdelete")
                .with(userAdminPlaneacion)).andExpect(status().isOk())
                .andExpect(content().json("{'softdeleted':true}"));
    }


}