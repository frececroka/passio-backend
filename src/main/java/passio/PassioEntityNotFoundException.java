package passio;

public class PassioEntityNotFoundException extends PassioEntityFactoryException {

	public PassioEntityNotFoundException(String msg) {
		super(msg);
	}

	public PassioEntityNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
