package passio;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/{username}")
public class PassioResource {

	@Autowired
	private PassioEntityFactory passioEntityFactory;

	@RequestMapping(method = RequestMethod.GET, produces = "text/plain")
	@ResponseStatus(HttpStatus.OK)
	public String getEntity(@PathVariable("username") String username) {
		PassioEntity passioEntity = requirePassioEntity(username);
		return passioEntity.getValue();
	}

	@RequestMapping(method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.CREATED)
	public void createEntity(@PathVariable("username") String username, @RequestHeader("X-Signing-Key") String signingKey64) {
		PassioEntity passioEntity = new PassioEntity(username, Base64.decodeBase64(signingKey64));
		if (!passioEntityFactory.create(passioEntity)) {
			throw new EntityCreationForbidden(username);
		}
	}

	@RequestMapping(method = RequestMethod.POST, consumes = "text/plain")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateEntity(@RequestBody String requestBody, @PathVariable("username") String username, @RequestHeader("X-MAC") String mac64) {
		byte[] mac = Base64.decodeBase64(mac64);

		PassioEntity passioEntity = requirePassioEntity(username);

		if (!passioEntity.setValue(requestBody, mac)) {
			throw new EntityUpdateForbidden(username);
		}

		passioEntityFactory.update(passioEntity);
	}

	private PassioEntity requirePassioEntity(String username) {
		try {
			return passioEntityFactory.load(username);
		} catch (PassioEntityNotFoundException e) {
			throw new RequiredEntityNotFound(e, username);
		}
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	private static class RequiredEntityNotFound extends ClientError {

		private final String entityName;

		public RequiredEntityNotFound(Throwable cause, String entityName) {
			super(cause);
			this.entityName = entityName;
		}

		@Override
		public String getCode() {
			return "Passio_Entity_Not_Found";
		}

		@Override
		public String getMessage() {
			return String.format("Entity with name %s not found.", entityName);
		}
	}

	@ResponseStatus(HttpStatus.FORBIDDEN)
	private static class EntityCreationForbidden extends ClientError {

		private final String entityName;

		public EntityCreationForbidden(String entityName) {
			this.entityName = entityName;
		}

		@Override
		public String getCode() {
			return "Entity_Creating_Forbidden";
		}

		@Override
		public String getMessage() {
			return String.format("Entity with name '%s' already exists.", entityName);
		}

	}

	@ResponseStatus(HttpStatus.FORBIDDEN)
	private static class EntityUpdateForbidden extends ClientError {

		private final String entityName;

		public EntityUpdateForbidden(String entityName) {
			this.entityName = entityName;
		}

		@Override
		public String getCode() {
			return "Entity_Update_Forbidden";
		}

		@Override
		public String getMessage() {
			return String.format("Provided MAC is invalid for entity '%s'.", entityName);
		}

	}

}
