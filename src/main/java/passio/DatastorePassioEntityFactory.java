package passio;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

import com.google.appengine.api.datastore.Transaction;
import org.apache.commons.codec.binary.Base64;

import java.util.logging.Logger;

public class DatastorePassioEntityFactory implements PassioEntityFactory {

	private static final Logger logger = Logger.getLogger(DatastorePassioEntityFactory.class.getCanonicalName());

	private final DatastoreService datastore;

	public DatastorePassioEntityFactory(DatastoreService datastore) {
		this.datastore = datastore;
	}

	private Key generateKey(String name) {
		return KeyFactory.createKey("passio", name);
	}

	public PassioEntity load(String name) {
		Key entityKey = generateKey(name);
		logger.info("Loading entity with key " + entityKey + ".");

		Entity entity;
		try {
			entity = datastore.get(entityKey);
		} catch (EntityNotFoundException e) {
			logger.info("Entity not found.");
			throw new PassioEntityNotFoundException("Passio entity " + name + " not found.", e);
		}

		String value = ((Text) entity.getProperty("value")).getValue();
		String signingKey64 = (String) entity.getProperty("signing-key");
		return new PassioEntity(name, value, Base64.decodeBase64(signingKey64));
	}

	public boolean create(PassioEntity passioEntity) {
		Key entityKey = generateKey(passioEntity.getName());
		logger.info("Trying to create entity with key " + entityKey + ".");

		Transaction txn = datastore.beginTransaction();
		try {
			datastore.get(entityKey);
			logger.info("Entity already exists. Rejecting create.");
			return false;
		} catch (EntityNotFoundException e) {
			logger.info("Entity not found. Creating.");
			Entity entity = new Entity(entityKey);
			entity.setProperty("signing-key", Base64.encodeBase64String(passioEntity.getSigningKey()));
			entity.setProperty("value", new Text(passioEntity.getValue()));
			datastore.put(entity);
			return true;
		} finally {
			logger.info("Finalizing create by committing transaction.");
			txn.commit();
		}
	}

	public boolean update(PassioEntity passioEntity) {
		Key entityKey = generateKey(passioEntity.getName());
		logger.info("Updating entity with key " + entityKey + ".");

		Transaction txn = datastore.beginTransaction();
		try {
			Entity entity = datastore.get(entityKey);
			entity.setProperty("value", new Text(passioEntity.getValue()));
			datastore.put(entity);
			return true;
		} catch (EntityNotFoundException e) {
			logger.info("Entity does not exist.");
			return false;
		} finally {
			logger.info("Finalizing update by committing transaction.");
			txn.commit();
		}
	}

}
