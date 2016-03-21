package hello.productos;

import hello.IntegrationTestApplication;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.multipart.MultipartFile;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by octavioruiz on 23/02/16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IntegrationTestApplication.class)
@WebAppConfiguration
public class ProductControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    ProductRepository repo;

    @Autowired
    ProductController controller;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
        //Elimino el código que voy a utilizar
        Product p = repo.findByCode("PCode1");
        if(p != null) repo.delete(p);
    }

    /**
     * Simple prueba de un alta de producto con las piezas con id 1 y 2
     * @throws Exception
     */
    @Test
    @WithMockUser(roles="PLANEACION")
    public void testAddProduct1() throws Exception {


        mockMvc.perform(MockMvcRequestBuilders.post("/product/add")
                .param("name", "Prodct Name 1")
                .param("code", "PCode1")
                .param("notes", "notes here")
                .param("pieza_id", "1")
                .param("pieza_id", "2")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf())
        )
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attributeExists("isError"));
    }

    /**
     * Prueba de inserción de código repetido, debe enviar error controlado
     */
    @Test
    @WithMockUser(roles="PLANEACION")
    public void testAddProduct2() throws Exception {

        testAddProduct1();
        mockMvc.perform(MockMvcRequestBuilders.post("/product/add")
                .param("name", "Prodct Name 1")
                .param("code", "PCode1")
                .param("notes", "notes here")
                .param("pieza_id", "1")
                .param("pieza_id", "2")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf())
        )
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attributeExists("isError"))
                .andExpect(flash().attribute("isError", true));
    }

    /**
     * Prueba de inserción con producto inválido
     */
    @Test
    @WithMockUser(roles="PLANEACION")
    public void testAddProduct3() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/product/add")
                .param("name", "Prodct Name 1")
                .param("code", "PCode1")
                .param("notes", "notes here")
                .param("pieza_id[]", "1")
                .param("pieza_id[]", "200")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf())
        )
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attributeExists("isError"))
                .andExpect(flash().attribute("isError", true));
    }

    /**
     * Prueba de inserción con imagen
     */
    @Test
    @WithMockUser(roles="PLANEACION")
    public void testAddProduct4() throws Exception {

        MultipartFile file = new MockMultipartFile("image","avatar1.png","image/png","".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/product/add")
                .file((MockMultipartFile) file)
                .param("name", "Prodct Name 1")
                .param("code", "PCode1")
                .param("notes", "notes here")
                .param("pieza_id", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf())
        )
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attributeExists("isError"))
                .andExpect(flash().attribute("isError", false));

        Product p = repo.findByCode("PCode1");
        Assert.assertNotNull( p );
        Assert.assertNotNull( p.getImage() );
    }

    /**
     * Test agregar Producto sin piezas
     * @throws Exception
     */
    @Test
    @WithMockUser(roles="PLANEACION")
    public void testAddProduct5() throws Exception {


        mockMvc.perform(MockMvcRequestBuilders.post("/product/add")
                .param("name", "Prodct Name 1")
                .param("code", "PCode1")
                .param("notes", "notes here")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf())
        )
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attributeExists("isError"))
                .andExpect(flash().attribute("isError", false));
    }

    /**
     * Prueba de la búsqueda de piezas, asumo que la pieza ID 1 si existe y la ID 200 no
     */

    @Test
    @WithMockUser(roles="admin")
    public void testGetPiezas() {
        try {
            // No existe
            controller.getPiezas(new Long[] {200l});
            Assert.assertTrue(false);
        } catch (ProductController.PiezaNotFoundException e) {
            Assert.assertTrue(true);
        }

        try {
            // Sí existe
            controller.getPiezas(new Long[] {1l});
            Assert.assertTrue(true);
        } catch (ProductController.PiezaNotFoundException e) {
            Assert.assertTrue(false);
        }
    }

    /**
     * Prueba simple de actualización de un atributo
     */
    @Test
    @WithMockUser(roles="PLANEACION")
    public void testUpdateProduct1() throws Exception {

        testAddProduct1();
        Product p = repo.findByCode("PCode1");
        Assert.assertNotEquals("Product Name 1 - UPDATED -", p.getName());
        Long id = p.getId();
        mockMvc.perform(MockMvcRequestBuilders.post("/product/update")
                .param("id", String.valueOf(id))
                .param("name", "Product Name 1 - UPDATED -")
                .param("code", "PCode1")
                .param("notes", "notes here")
                .param("pieza_id", "1")
                .param("pieza_id", "2")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf())
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attributeExists("isError"))
                .andExpect(flash().attribute("isError", false));

        p = repo.findByCode("PCode1");
        Assert.assertEquals("Product Name 1 - UPDATED -", p.getName());
    }


    /**
     * Prueba de actualización de la imagen
     * @throws Exception
     */
    @Test
    @WithMockUser(roles="PLANEACION")
    public void testUpdateProduct2() throws Exception {

        MultipartFile file = new MockMultipartFile("product_image","avatar1.png","image/png","abcd".getBytes());

        testAddProduct1();
        Product p = repo.findByCode("PCode1");
        //Afirma que el producto tiene el nombre origina
        Assert.assertNotEquals("Product Name 1 - UPDATED -", p.getName());
        Long id = p.getId();
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/product/update")
                .file((MockMultipartFile) file)
                .param("id", String.valueOf(id))
                .param("name", "Product Name 1 - UPDATED -")
                .param("code", "PCode1")
                .param("notes", "notes here")
                .param("pieza_id", "1")
                .param("pieza_id", "2")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf())
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attributeExists("isError"))
                .andExpect(flash().attribute("isError", false));

        p = repo.findByCode("PCode1");
        //Afirma que el producto ya cambió de nombre y tiene asignada la nueva imagen
        Assert.assertEquals("Product Name 1 - UPDATED -", p.getName());
        Assert.assertArrayEquals(file.getBytes(), p.getImage());
    }

    /**
     * Método que agrega un Producto de pruebas
     */
    public void addMockProduct() {
        Product p = new Product();
        p.setName("Product Name 1");
        p.setCode("PCode1");
        p.setNotes("notes here");

        repo.save(p);
    }

    /**
     * Prueba niveles de acceso
     */

    @Test
    @WithMockUser(roles="ADMIN")
    public void testSecurity() throws Exception {
        addMockProduct();
        Product p = repo.findByCode("PCode1");
        Long id = p.getId();

        UserRequestPostProcessor userAdmin           = user("admin").password("abcd1234").roles("ADMIN");
        UserRequestPostProcessor userPlaneacion      = user("admin").password("abcd1234").roles("PLANEACION");
        UserRequestPostProcessor userAdminPlaneacion = user("admin").password("abcd1234").roles("ADMIN_PLANEACION");

        mockMvc.perform(MockMvcRequestBuilders.get("/product/"+id).with(userAdmin)).andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.get("/product/"+id).with(userPlaneacion)).andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.get("/product/"+id+"/delete").with(userAdminPlaneacion)).andExpect(status().is3xxRedirection());
    }

    /**
     * Prueba simple de view
     */
    @Test
    @WithMockUser(roles="ADMIN")
    public void testView1() throws Exception {

        addMockProduct();
        Product p = repo.findByCode("PCode1");
        Long id = p.getId();

        mockMvc.perform(MockMvcRequestBuilders.get("/product/"+id)

        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("product", notNullValue()));


    }

    /**
     * Prueba simple de delete
     */
    @Test
    @WithMockUser(roles="ADMIN_PLANEACION")
    public void testDelete1() throws Exception {

        addMockProduct();
        Product p = repo.findByCode("PCode1");
        Long id = p.getId();

        mockMvc.perform(MockMvcRequestBuilders.get("/product/"+id+"/delete")

        )
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attributeExists("isError"))
                .andExpect(flash().attribute("isError", false));

    }

    /**
     * Delete de un producto inexistente. Asumo que el producto 1 000 000 no existe.
     */
    @Test
    @WithMockUser(roles="ADMIN_PLANEACION")
    public void testDelete2() throws Exception {

        Long id = 1_000_000l;

        mockMvc.perform(MockMvcRequestBuilders.get("/product/"+id+"/delete")

        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attributeExists("isError"))
                .andExpect(flash().attribute("isError", true));

    }
}
