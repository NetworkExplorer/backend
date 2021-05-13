package at.networkexplorer.backend;

import at.networkexplorer.backend.api.ApiController;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@WebMvcTest(value = ApiController.class)
@WithMockUser
class ApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	/**
	 * Tests whether the server is pingable or not.
	 * @throws Exception
	 */
	@Test
	public void isPingable() throws Exception {
		RequestBuilder builder = MockMvcRequestBuilders.get("/api/v1/ping").accept(MediaType.ALL);
		MvcResult result = mockMvc.perform(builder).andReturn();

		System.out.println(result.getResponse());

		assertEquals(302,result.getResponse().getStatus());
	}

}
