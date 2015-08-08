package passio;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

import org.apache.commons.codec.binary.Base64;

public class DatastorePassioEntityFactory implements PassioEntityFactory {

	private final DatastoreService datastore;

	public DatastorePassioEntityFactory(DatastoreService datastore) {
		this.datastore = datastore;
	}

	private Key generateKey(String name) {
		return KeyFactory.createKey("passio", name);
	}

	public PassioEntity load(String name) {
		Key eKey = generateKey(name);
		Entity entity;
		try {
			entity = datastore.get(eKey);
		} catch (EntityNotFoundException e) {
			throw new PassioEntityNotFoundException("Passio entity " + name + " not found.", e);
		}

		String value = ((Text) entity.getProperty("value")).getValue();
		String signingKey64 = (String) entity.getProperty("signing-key");
		return new PassioEntity(name, value, Base64.decodeBase64(signingKey64));
	}

	public void save(PassioEntity passioEntity) {
		Entity entity = new Entity(generateKey(passioEntity.getName()));
		entity.setProperty("signing-key", Base64.encodeBase64String(passioEntity.getSigningKey()));
		entity.setProperty("value", new Text(passioEntity.getValue()));
		datastore.put(entity);
	}

}
