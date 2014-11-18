package passio;

import java.util.List;

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

	private PassioEntityFactory passioEntityFactory;

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
		try {
			PassioEntity e = passioEntityFactory.load(username);
		} catch (PassioEntityNotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		return Response.ok(e.getValue()).build();
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

		PassioEntity passioEntity = new PassioEntity(username, Base64.decodeBase64(signingKey64));
		passioEntityFactory.save(passioEntity);

		return Response.ok().build();
	}

	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	public Response updateEntity(
	String requestBody, @PathParam("username") String username, @Context HttpHeaders headers) {
		String mac64 = getSingleHeader(headers, "X-MAC");
		if (mac64 == null) {
			return Response.status(Response.Status.BAD_REQUEST)
				.entity("No MAC provided")
				.build();
		}

		byte[] mac = Base64.decodeBase64(mac64);

		try {
			PassioEntity passioEntity = passioEntityFactory.load(username);
		} catch (PassioEntityNotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		if (!passioEntity.setValue(requestBody, mac)) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

		passioEntityFactory.save(passioEntity);
		return Response.ok().build();
	}

	public void setPassioEntityFactory(PassioEntityFactory f) {
		passioEntityFactory = f;
	}

}
