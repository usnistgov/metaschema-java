package gov.nist.secauto.metaschema.model;

public class MetaschemaException extends Exception {

	/**
	 * The serial version UID
	 */
	private static final long serialVersionUID = 1L;

	public MetaschemaException() {
	}

	public MetaschemaException(String message) {
		super(message);
	}

	public MetaschemaException(Throwable cause) {
		super(cause);
	}

	public MetaschemaException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetaschemaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
