package hello.productos;

import hello.IntegrationTestApplication;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by octavioruiz on 25/02/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IntegrationTestApplication.class)
@WebAppConfiguration
public class ProductsControllerTest {
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
        repo.deleteAll();
        for(int i = 0; i < 10; i++) createTestRow();
    }

    /**
     * Test listado simple de productos
     */
    @Test
    @WithMockUser(roles="ADMIN")
    public void testListado() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/products")
                .param("length", "10")
                .param("start" , "0")
        )
                .andDo(print())
                .andExpect(status().isOk());
    }

    public void createTestRow() throws Exception {
        String code = RandomStringUtils.randomAlphanumeric(7);

        MultipartFile file = new MockMultipartFile("image","avatar1.png","image/png", downloadTestImage(new URL("http://identicon.org?t="+code+"&s=256")));

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/product/add")
                .file((MockMultipartFile) file)
                .param("name", "Prodct Name "+ RandomStringUtils.randomAlphabetic(5))
                .param("code", code)
                .param("notes", "notes here")
                .param("pieza_id", "1")
                .param("pieza_id", "2")
                .param("pieza_id", "3")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf())
        )
                .andDo(print())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attributeExists("isError"))
                .andExpect(flash().attribute("isError", false));

    }

    public byte[] downloadTestImage(URL url) throws IOException {
        InputStream is = null;
        try {
            is = url.openStream ();
            byte[] imageBytes = IOUtils.toByteArray(is);
            return imageBytes;
        }
        catch (IOException e) {
            System.err.printf ("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
            e.printStackTrace ();
            return null;
            // Perform any other exception handling that's appropriate.
        }
        finally {
            if (is != null) { is.close(); }
        }
    }
}
