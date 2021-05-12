package at.networkexplorer.backend;

import at.networkexplorer.backend.api.ApiController;
import at.networkexplorer.backend.api.FileController;
import at.networkexplorer.backend.api.response.Result;
import at.networkexplorer.backend.beans.FileType;
import at.networkexplorer.backend.beans.NetworkFile;
import at.networkexplorer.backend.io.StorageService;
import at.networkexplorer.backend.io.ZipService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@WebMvcTest(value = FileController.class)
@WithMockUser
public class FileControllerTest {

    //https://www.springboottutorial.com/unit-testing-for-spring-boot-rest-services

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StorageService storageService;
    @MockBean
    private ZipService zipService;

    @Test
    public void listRoot() throws Exception{
        storageService.deleteAll();
        RequestBuilder builder = MockMvcRequestBuilders
                .get("/api/v1/folder")
                .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(builder).andReturn();

        System.out.println(result.getResponse().getContentAsString());

        Result expected = new Result(200, new NetworkFile("/", FileType.FOLDER, null));

        JSONAssert.assertEquals(mapper.writeValueAsString(expected), result.getResponse().getContentAsString(), false);
    }

    @Test
    public void deleteFolder() throws Exception {
        String path = "testFolder";
        storageService.mkdir(path);

        RequestBuilder builder = MockMvcRequestBuilders
                .delete("/api/v1/delete")
                .accept(MediaType.APPLICATION_JSON)
                .content("[ \"" + path + "\"]")
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(builder).andReturn();

        System.out.println(result.getResponse().getContentAsString());

        Result expected = new Result(201, null, "Deleted successfully");

        JSONAssert.assertEquals(mapper.writeValueAsString(expected), result.getResponse().getContentAsString(), false);
    }
}
