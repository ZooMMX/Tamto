package hello;

import com.sun.security.auth.UserPrincipal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.security.Principal;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Created by octavioruizcastillo on 07/02/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IntegrationTestApplication.class)
@WebAppConfiguration

public class ExternalServicesControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    @Test
    //@WithMockCustomUser
    @WithMockUser(roles="ADMIN")
    public void testExternalServicesHome() throws Exception {


        mockMvc.perform(MockMvcRequestBuilders.get("/external_services")

                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    //@WithMockCustomUser
    @WithMockUser(roles="ADMIN")
    public void testExport() throws Exception {


        mockMvc.perform(MockMvcRequestBuilders.get("/external_services/export")

        )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    //@WithMockCustomUser
    @WithMockUser(username = "admin", roles="ADMIN")
    public void testImport() throws Exception {

        FileInputStream fis = new FileInputStream("src/test/resources/exportTest.xls");
        MockMultipartFile file = new MockMultipartFile( "file", "exportTest.xls", "application/vnd.ms-excel", fis);


        mockMvc.perform(
                MockMvcRequestBuilders.fileUpload("/external_services/import")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                       .with(csrf())


        )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    //@WithMockCustomUser
    @WithMockUser(username = "admin", roles="ADMIN")
    public void testImportXLSX() throws Exception {

        FileInputStream fis = new FileInputStream("src/test/resources/exportTestXLSX.xlsx");
        MockMultipartFile file = new MockMultipartFile( "file", "exportTestXLSX.xlsx", "application/vnd.ms-excel", fis);


        mockMvc.perform(
                MockMvcRequestBuilders.fileUpload("/external_services/import")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles="ADMIN")
    public void testImportFail() throws Exception {

        FileInputStream fis = new FileInputStream("src/test/resources/exportFailTest.xls");
        MockMultipartFile file = new MockMultipartFile( "file", "exportFailTest.xls", "application/vnd.ms-excel", fis);

        //El archivo de pruebas exportFailTest debe contener un error en la fila 4 para que este test pase.
        mockMvc.perform(
                MockMvcRequestBuilders.fileUpload("/external_services/import")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())


        )
                .andExpect(MockMvcResultMatchers.content().string("Error (Cannot get a numeric value from a text cell) en la fila 4, en la columna  1"));
    }
}
