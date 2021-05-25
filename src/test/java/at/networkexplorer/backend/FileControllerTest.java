package at.networkexplorer.backend;

import at.networkexplorer.backend.api.ApiController;
import at.networkexplorer.backend.api.FileController;
import at.networkexplorer.backend.api.response.Result;
import at.networkexplorer.backend.beans.FileType;
import at.networkexplorer.backend.beans.NetworkFile;
import at.networkexplorer.backend.io.FileSystemStorageService;
import at.networkexplorer.backend.io.StorageService;
import at.networkexplorer.backend.io.ZipService;
import at.networkexplorer.backend.messages.Messages;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@WebMvcTest(value = FileController.class)
@WithMockUser
public class FileControllerTest {

    /* HOW TO TEST
     * https://www.springboottutorial.com/unit-testing-for-spring-boot-rest-services
    */
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StorageService storageService;

    @MockBean
    private ZipService zipService;

    /**
     * Tests the `/api/v1/folder` Endpoint. Deletes all of the contents of the shared folder first.
     * @throws Exception
     */
    @Test
    public void listRoot() throws Exception{
        NetworkFile[] empty = {};
        Mockito.when(storageService.loadAll(Mockito.anyString())).thenReturn(empty);

        RequestBuilder builder = MockMvcRequestBuilders
                .get("/api/v1/folder")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(builder).andReturn();

        System.out.println(result.getResponse().getContentAsString());

        Result expected = new Result(200, new NetworkFile("/", FileType.FOLDER, empty));

        JSONAssert.assertEquals(mapper.writeValueAsString(expected), result.getResponse().getContentAsString(), false);
    }

    

    /**
     * Tests the `/api/v1/delete` Endpoint. Creates a directory on the shared folder first.
     * @throws Exception
     */
    @Test
    public void deleteFolder() throws Exception {
        String path = "testFolder";

        RequestBuilder builder = MockMvcRequestBuilders
                .delete("/api/v1/delete")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("[ \"" + path + "\"]");

        Result expected = new Result(201, null, "Deleted successfully");

        mockMvc.perform(builder).andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(expected))).andDo(MockMvcResultHandlers.print());
    }

    /**
     * Test the `/api/v1/upload` Endpoint. Uploads a pseudo file to the root directory.
     * @throws Exception
     */
    @Test
    public void uploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "application/json", "This is a test".getBytes());

        RequestBuilder builder = MockMvcRequestBuilders
                .multipart("/api/v1/upload")
                .file(file)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param("path", "/");

        Result expected = new Result(201, true, String.format(Messages.UPLOAD_SUCCESS, file.getOriginalFilename(), "/"));

        mockMvc.perform(builder)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(expected)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void deleteFile() throws Exception {
        String[] path = {"test.txt"};

        RequestBuilder builder = MockMvcRequestBuilders
                .delete("/api/v1/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(path));

        Result expected = new Result(201, null, Messages.DELETE_SUCCESS);

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(expected)))
                .andDo(MockMvcResultHandlers.print());
    }

}
