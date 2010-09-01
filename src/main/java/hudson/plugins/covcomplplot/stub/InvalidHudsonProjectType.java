package hudson.plugins.covcomplplot.stub;

import java.text.MessageFormat;

/**
 * Invalid Hudson Project Error Type.
 * @author nhn
 */
public enum InvalidHudsonProjectType {
	NONE {
		@Override
		public String getLogMessageTemplate() {
			return "";
		}

		@Override
		public String getScreenMessageTemplate() {
			return "";
		}
	},
	INTERNAL {
		@Override
		public String getLogMessageTemplate() {
			return "내부 에러입니다. {0}.";
		}

		@Override
		public String getScreenMessageTemplate() {
			return "Internal Error due to {0}.";
		}
	},
	INVALID_PLUGIN_RESULT {

		@Override
		public String getLogMessageTemplate() {
			return "[{0}] 플러그인 결과가 없습니다. Cov/Compl 그래프를 생성하기 위해서는 [{0}] 플러그인을 반드시 먼저 실행하여야 합니다.";
		}

		@Override
		public String getScreenMessageTemplate() {
			return "[{0}] 플러그인 결과가 없습니다. Cov/Compl 그래프를 생성하기 위해서는 [{0}] 플러그인을 반드시 먼저 실행하여야 합니다.";
		}
		
	}
;
	
	/**
	 * Get Template for Screen Message;
	 * @return
	 */
	abstract protected String getScreenMessageTemplate();

	/**
	 * Get Template for Log detailed message.
	 * @return
	 */
	abstract protected String getLogMessageTemplate();

	/**
	 * Get Message for screen.
	 * @param args arguments
	 * @return screen message
	 */
	public String getScreenMessage(Object... args) {
		return MessageFormat.format(getScreenMessageTemplate(), args);
	}
	/**
	 * Get Message for logger
	 * @param args arguments
	 * @return detailedMessage
	 */
	public String getLogMessage(Object... args) {
		return "[COVCOMPL] " + MessageFormat.format(getLogMessageTemplate(), args);
	}
}
