package passio;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PassioDataConfig {

	@Bean
	public DatastoreService datastoreService() {
		return DatastoreServiceFactory.getDatastoreService();
	}

	@Bean
	public PassioEntityFactory datastorePassioEntityFactory(DatastoreService datastoreService) {
		return new DatastorePassioEntityFactory(datastoreService);
	}

}
