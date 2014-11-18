package passio;

public class PassioException extends RuntimeException {

	public PassioException(String msg) {
		super(msg);
	}

	public PassioException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
