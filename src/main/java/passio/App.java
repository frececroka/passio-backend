package passio;

import java.security.NoSuchAlgorithmException;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class App extends Application {

	@Override
	public Set<Object> getSingletons() {
		Set<Object> singletons = new HashSet<>();

		try {
			singletons.add(new PassioResource());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		singletons.add(new CorsResponseFilter());

		return singletons;
	}

}
