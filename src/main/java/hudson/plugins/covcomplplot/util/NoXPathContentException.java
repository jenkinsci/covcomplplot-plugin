package hudson.plugins.covcomplplot.util;

/**
 * Exception occurring when the XPATH is invalid.
 * 
 * @author JunHo Yoon
 */
public class NoXPathContentException extends Exception {

	/** Constructor */
	public NoXPathContentException(String xpathStr) {
		super(xpathStr);
	}

	/** UUID */
	private static final long serialVersionUID = 1L;

}
