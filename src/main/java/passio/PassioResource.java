package passio;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;

@Path("/{username}")
public class PassioResource {

	private MessageDigest messageDigest;
	private DatastoreService datastore;

	public PassioResource() throws NoSuchAlgorithmException {
		this.datastore = DatastoreServiceFactory.getDatastoreService();
		this.messageDigest = MessageDigest.getInstance("SHA-256");
	}

	private Key generateKey(String username) {
		return KeyFactory.createKey("passio", username);
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getEntity(@PathParam("username") String username) {
		Entity user;
		Key userKey = this.generateKey(username);

		try {
			user = datastore.get(userKey);
		} catch (EntityNotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		return Response.ok(((Text) user.getProperty("value")).getValue()).build();
	}

	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	public Response updateEntity(
		String requestBody,
		@PathParam("username") String username,
		@Context HttpHeaders headers) {

		List<String> authHeader = headers.getRequestHeaders().get("Authorization");
		if (authHeader == null) {
			return Response.status(Response.Status.BAD_REQUEST)
				.entity("No authorization provided")
				.build();
		} else if (authHeader.size() > 1) {
			return Response.status(Response.Status.BAD_REQUEST)
				.entity("Ambiguous authorization provided")
				.build();
		}

		// we obtain the authentication token from the request headers and hash it again.
		String auth64 = authHeader.get(0);
		byte[] auth = Base64.decodeBase64(auth64);
		auth = this.messageDigest.digest(auth);
		auth64 = new String(Base64.encodeBase64(auth));

		Key userKey = this.generateKey(username);
		Entity user;

		try {
			user = this.datastore.get(userKey);
		} catch (EntityNotFoundException e) {
			// user doesn't exist. we create a new entity for this user and set the authentication token to the one
			// provided by the client
			user = new Entity(userKey);
			user.setProperty("auth", auth64);
		}

		if (!((String) user.getProperty("auth")).equals(auth64)) {
			// if the authentication token stored with the user entity is different from the authentication token
			// provided in the request, the client is not allowed to change this data.
			return Response.status(Response.Status.FORBIDDEN).build();
		}

		user.setProperty("value", new Text(requestBody));
		this.datastore.put(user);

		return Response.ok().build();
	}

}
