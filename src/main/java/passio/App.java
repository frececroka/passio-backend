package passio;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class App extends Application {

	@Override
	public Set<Object> getSingletons() {
		Set<Object> singletons = new HashSet<>();

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		DatastorePassioEntityFactory passioEntityFactory = new DatastorePassioEntityFactory();
		passioEntityFactory.setDatastoreService(datastore);

		PassioResource passioResource = new PassioResource();
		passioResource.setPassioEntityFactory(passioEntityFactory);

		singletons.add(passioResource);
		singletons.add(new CorsResponseFilter());

		return singletons;
	}

}
