package passio;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

import java.io.UnsupportedEncodingException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.List;

import javax.crypto.Mac;

import javax.crypto.spec.SecretKeySpec;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;

@Path("/{username}")
public class PassioResource {

	private DatastoreService datastore;

	public PassioResource() throws NoSuchAlgorithmException {
		this.datastore = DatastoreServiceFactory.getDatastoreService();
	}

	private Key generateKey(String username) {
		return KeyFactory.createKey("passio", username);
	}

	private boolean verifyMac(String text, byte[] expectedMac, byte[] key)
	throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
		SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA1");

		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(keySpec);

		byte[] actualMac = mac.doFinal(text.getBytes("UTF-8"));

		return Arrays.equals(expectedMac, actualMac);
	}

	private String getSingleHeader(HttpHeaders headers, String key) {
		List<String> keyHeaders = headers.getRequestHeaders().get(key);
		if (keyHeaders != null && keyHeaders.size() == 1) {
			return keyHeaders.get(0);
		} else {
			return null;
		}
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

	@PUT
	public Response createEntity(
		@PathParam("username") String username,
		@Context HttpHeaders headers) {

		String signingKey64 = getSingleHeader(headers, "X-Signing-Key");
		if (signingKey64 == null) {
			return Response.status(Response.Status.BAD_REQUEST)
				.entity("No signing key provided")
				.build();
		}

		Entity user = new Entity(generateKey(username));
		user.setProperty("signing-key", signingKey64);
		user.setProperty("value", new Text(""));
		datastore.put(user);

		return Response.ok().build();
	}

	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	public Response updateEntity(
	String requestBody, @PathParam("username") String username, @Context HttpHeaders headers)
	throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
		String mac64 = getSingleHeader(headers, "X-MAC");
		if (mac64 == null) {
			return Response.status(Response.Status.BAD_REQUEST)
				.entity("No MAC provided")
				.build();
		}

		byte[] mac = Base64.decodeBase64(mac64);

		Entity user;
		try {
			user = datastore.get(generateKey(username));
		} catch (EntityNotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		if (user.hasProperty("signing-key")) {
			// Make use of signing key
			String signingKey64 = (String) user.getProperty("signing-key");
			byte[] signingKey = Base64.decodeBase64(signingKey64);
			if (!verifyMac(requestBody, mac, signingKey)) {
				return Response.status(Response.Status.FORBIDDEN).build();
			}
		} else {
			String signingKey64 = getSingleHeader(headers, "X-Signing-Key");
			if (signingKey64 == null) {
				return Response.status(Response.Status.BAD_REQUEST)
					.entity("A signing key is required for legacy accounts")
					.build();
			}

			user.setProperty("signing-key", signingKey64);
		}

		user.setProperty("value", new Text(requestBody));
		this.datastore.put(user);

		return Response.ok().build();
	}

}
