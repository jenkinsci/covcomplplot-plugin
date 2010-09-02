package hudson.plugins.covcomplplot.stub;

import java.text.MessageFormat;

/**
 * Invalid Hudson Project Error Type.
 * 
 * @author JunHo Yoon
 */
public enum InvalidHudsonProjectType {
	/**
	 * No Error.
	 */
	NONE {
		@Override
		public String getLogMessageTemplate() {
			return "";
		}
	},
	/**
	 * Internal Error Type
	 */
	INTERNAL {
		@Override
		public String getLogMessageTemplate() {
			return "Internal Error due to {0}.";
		}
	},
	/**
	 * Invalid Plugin Result Error Type
	 */
	INVALID_PLUGIN_RESULT {

		@Override
		public String getLogMessageTemplate() {
			return "No [{0}] plugin result. [{0}] plugin should be peformed first to generate Cov/Compl plot.";
		}

	};

	/**
	 * Get Template for Log detailed message.
	 * 
	 * @return
	 */
	abstract protected String getLogMessageTemplate();

	/**
	 * Get Message for logger
	 * 
	 * @param args
	 *            arguments
	 * @return detailedMessage
	 */
	public String getLogMessage(Object... args) {
		return "[COVCOMPL] " + MessageFormat.format(getLogMessageTemplate(), args);
	}
}
