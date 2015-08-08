package passio;

public abstract class ClientError extends RuntimeException {

	public ClientError() {
	}

	public ClientError(Throwable cause) {
		super(cause);
	}

	public abstract String getCode();
	public abstract String getMessage();

}
