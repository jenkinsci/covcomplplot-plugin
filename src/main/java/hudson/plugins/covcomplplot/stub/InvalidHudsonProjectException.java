package hudson.plugins.covcomplplot.stub;

/**
 * Invalid Hudson Project Exception.<br/> 
 * This exception is used to minimize the count of exception classes which shows different message. 
 * When the constructor is invoked, the appropriate enum value is passed to specify the exact error.
 * @author JunHo Yoon
 */
public class InvalidHudsonProjectException extends Exception {
	/** UUID */
	private static final long serialVersionUID = 1L;
	/** Exception Type */
	private final InvalidHudsonProjectType invalidHudsonProjectType;
	/** Message */
	private final String logMessage;

	/**
	 * Constructor
	 * 
	 * @param invalidHudsonProjectType
	 *            exception type.
	 * @param args
	 *            arguments which {@link InvalidHudsonProjectType} needs.
	 */
	public InvalidHudsonProjectException(InvalidHudsonProjectType invalidHudsonProjectType, Object... args) {
		this(invalidHudsonProjectType, null, args);
	}

	/**
	 * Constructor
	 * 
	 * @param invalidHudsonProjectType
	 *            exception type
	 * @param throwable
	 *            internal exception
	 * @param args
	 *            arguments which {@link InvalidHudsonProjectType} needs.
	 */
	public InvalidHudsonProjectException(InvalidHudsonProjectType invalidHudsonProjectType, Throwable throwable, Object... args) {
		super(throwable);
		this.invalidHudsonProjectType = invalidHudsonProjectType;
		this.logMessage = invalidHudsonProjectType.getLogMessage(args);
	}

	/**
	 * Get message constructed from {@link InvalidHudsonProjectType}.
	 * 
	 * @return log message
	 */
	public String getLogMessage() {
		return logMessage;
	}

	/**
	 * Get exception type.
	 * 
	 * @return exception type
	 */
	public InvalidHudsonProjectType getInvalidHudsonProjectType() {
		return invalidHudsonProjectType;
	}
}
