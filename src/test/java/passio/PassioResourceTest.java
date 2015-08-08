package passio;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PassioConfig.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PassioResourceTest extends AbstractAppengineTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Before
	public void setUpMockMvc() {
		mockMvc = webAppContextSetup(webApplicationContext).build();
	}

	@Test
	public void thatNonexistentEntitiesReturn404() throws Exception {
		mockMvc.perform(get("/notauser")).andExpect(status().isNotFound());
	}

	@Test
	public void thatNewEntityCanBeSavedOnlyWithCorrectMac() throws Exception {
		mockMvc.perform(put("/soonauser").header("X-Signing-Key", Base64.encodeBase64String(new byte[]{1, 2, 3})))
				.andExpect(status().isCreated());

		RequestBuilder requestWithWrongMac = post("/soonauser")
				.header("X-MAC", "nDmBNnVjXNRmQhwPuxWYumYkBDc=")
				.contentType(MediaType.TEXT_PLAIN)
				.content("allmypasswords");
		mockMvc.perform(requestWithWrongMac).andExpect(status().isForbidden());

		RequestBuilder requestWithCorrectMac = post("/soonauser")
				.header("X-MAC", "LuQcvDHavhDBdxKoEKdomgQlf3Q=")
				.contentType(MediaType.TEXT_PLAIN)
				.content("allmypasswords");
		mockMvc.perform(requestWithCorrectMac).andExpect(status().isNoContent());

		mockMvc.perform(get("/soonauser"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.TEXT_PLAIN))
				.andExpect(content().string("allmypasswords"));
	}

	@Test
	public void thatExistingEntityCannotBeOverwritten() throws Exception {
		mockMvc.perform(put("/soonauser").header("X-Signing-Key", Base64.encodeBase64String(new byte[]{1, 2, 3})))
				.andExpect(status().isCreated());

		RequestBuilder requestWithCorrectMac = post("/soonauser")
				.header("X-MAC", "LuQcvDHavhDBdxKoEKdomgQlf3Q=")
				.contentType(MediaType.TEXT_PLAIN)
				.content("allmypasswords");
		mockMvc.perform(requestWithCorrectMac).andExpect(status().isNoContent());

		mockMvc.perform(put("/soonauser").header("X-Signing-Key", Base64.encodeBase64String(new byte[]{1, 2, 3})))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/soonauser"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.TEXT_PLAIN))
				.andExpect(content().string("allmypasswords"));
	}

}
