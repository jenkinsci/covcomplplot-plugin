package hudson.plugins.covcomplplot.stub;

public class InvalidHudsonProjectException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final InvalidHudsonProjectType invalidHudsonProjectType;
	private final String screenMessage;
	private final String logMessage;

	public InvalidHudsonProjectException(InvalidHudsonProjectType invalidHudsonProjectType, Object... args) {
		this(invalidHudsonProjectType, null, args);
	}

	public InvalidHudsonProjectException(InvalidHudsonProjectType invalidHudsonProjectType, Throwable throwable, Object... args) {
		super(throwable);
		this.invalidHudsonProjectType = invalidHudsonProjectType;
		this.screenMessage = invalidHudsonProjectType.getScreenMessage(args);
		this.logMessage = invalidHudsonProjectType.getLogMessage(args);
	}

	public String getScreenMessage() {
		return screenMessage;
	}

	public String getLogMessage() {
		return logMessage;
	}

	public InvalidHudsonProjectType getInvalidHudsonProjectType() {
		return invalidHudsonProjectType;
	}
}
