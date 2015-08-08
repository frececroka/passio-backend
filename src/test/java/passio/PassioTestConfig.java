package passio;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class PassioTestConfig {

	@Bean
	public LocalServiceTestHelper localServiceTestHelper() {
		return new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	}

}
