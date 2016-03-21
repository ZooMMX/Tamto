package hello;

import hello.productos.Product;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by octavioruiz on 16/03/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IntegrationTestApplication.class)
@WebAppConfiguration

public class ArchivoControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    ArchivoRepository archivoRepository;

    private MockMvc mockMvc;

    Archivo archivoPrueba;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();

        //Busco el archivo que voy a utilizar
        Pageable pageable = new PageRequest(0, 1);  //start from -1 to match page.page
        List<Archivo> p = archivoRepository.findAll(pageable).getContent();

        if(p.size()>0) {
            archivoPrueba = p.get(0);
        } else {
            throw new Exception("Es necesario cargar datos de prueba o crear un dataset para esta prueba");
        }
    }

    /**
     * Cambia la categoría de un archivo dos veces y comprueba el resultado
     * @throws Exception
     */
    @Test
    @WithMockUser(roles="PLANEACION")
    public void testUpdateCategoria() throws Exception {
        //********** Prueba 1 ************
        /* ** Realiza el cambio de categoría ** */
        mockMvc.perform(MockMvcRequestBuilders.post("/archivoEdicion/"+archivoPrueba.getId()+"/categoria")
                .param("categoria", String.valueOf(Archivo.CategoriaArchivo.CALIDAD))
                .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(content().string("ok"));

        //** Comprueba el cambio **
        Archivo a = archivoRepository.findOne(archivoPrueba.getId());

        assertEquals(Archivo.CategoriaArchivo.CALIDAD, a.getCategoria());

        //********** Prueba 2 ************
        /* ** Realiza el cambio de categoría ** */
        mockMvc.perform(MockMvcRequestBuilders.post("/archivoEdicion/"+archivoPrueba.getId()+"/categoria")
                .param("categoria", String.valueOf(Archivo.CategoriaArchivo.ESPECIFICACIONES))
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        //** Comprueba el cambio **
        a = archivoRepository.findOne(archivoPrueba.getId());

        assertEquals(Archivo.CategoriaArchivo.ESPECIFICACIONES, a.getCategoria());
    }

    /**
     * Prueba de errores, intenta con un id erróneo
     */
    @Test
    @WithMockUser(roles="admin")
    public void testUpdateCategoria2() throws Exception {


        mockMvc.perform(MockMvcRequestBuilders.post("/archivoEdicion/-1/categoria")
                .param("categoria", String.valueOf(Archivo.CategoriaArchivo.CALIDAD))
                .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(content().string("Archivo no encontrado"));
    }


}