package hudson.plugins.covcomplplot.stub;

import java.io.PrintStream;

/**
 * Wrapper class for logging
 * 
 * @author JunHo Yoon.
 */
public class LoggerWrapper {
	/** Logger print stream */
	private final PrintStream out;
	/** Verbose moe */
	private final boolean verbose;

	/**
	 * Default Constructor.
	 * It uses System.out as the print stream.
	 */
	public LoggerWrapper() {
		this(System.out);
	}

	/**
	 * Constructor with selective print stream
	 * @param printStream print stream to be used.
	 */
	public LoggerWrapper(PrintStream printStream) {
		this(printStream, false);
	}
	
	/**
	 * Constructor with selective print stream and verbose mode.
	 * 
	 * @param printStream print stream to be used.
	 * @param verbose true if the verbose message should be printed as well
	 */
	public LoggerWrapper(PrintStream printStream, boolean verbose) {
		this.verbose = verbose;
		this.out = printStream;
	}

	/** 
	 * Print line separation. 
	 * This method is actually performed when verboseMode is true.
	 */
	public void printLineSeperation() {
		if (this.verbose)
			out.println("==================================================");
	}

	/**
	 * Print verbose message.
	 * This method is actually performed when verboseMode is true.
	 * @param msg message to be printed.
	 */
	public void printlnVerbose(String msg) {
		if (verbose) {
			println(msg);
		}
	}

	/**
	 * Print message
	 * @param msg message to be printed
	 */
	public void println(Object msg) {
		out.println("[COVCOMPLPLOT] " + msg);
	}

	/**
	 * Print message with arguments (same as String.format())
	 * @param msg message format to be printed
	 * @param args arguments to be inserted in message
	 */
	public void println(String msg, Object... args) {
		out.println("[COVCOMPLPLOT] " + String.format(msg, args));
	}

	/**
	 * Print error message.
	 * @param msg message to be printed.
	 */
	public void printError(Object msg) {
		out.println("[COVCOMPLPLOT ERROR] " + msg);
	}

	/**
	 * Print error message with arguments
	 * @param msg message format to be printed
	 * @param args arguments to be inserted in message
	 */
	public void printError(String msg, Object... args) {
		out.println("[COVCOMPLPLOT ERROR] " + String.format(msg, args));
	}

	/** 
	 * Print exception stack trace. Only activated when verboseMode is true
	 * @param e Exception to be printed.
	 */
	public void printStackTrace(Exception e) {
		if (verbose) {
			out.println("=======================================================");
			e.printStackTrace(out);
			out.println("=======================================================");
		}
	}
}
