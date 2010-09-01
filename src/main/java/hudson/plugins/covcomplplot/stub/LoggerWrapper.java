package hudson.plugins.covcomplplot.stub;

import java.io.PrintStream;

public class LoggerWrapper {

	private final PrintStream out;
	private final boolean verbose;

	public LoggerWrapper() {
		this(System.out);
	}
	
	public LoggerWrapper(PrintStream printStream) {
		this(printStream, false);
	}

	public LoggerWrapper(PrintStream printStream, boolean verbose) {
		this.verbose = verbose;
		this.out = printStream;
	}

	public void printLineSeperation() {
		if (this.verbose)
			out.println("==================================================");
	}

	public void printlnVerbose(String msg) {
		if (verbose) {
			println(msg);
		}
	}

	public void println(String msg) {
		out.println("[COVCOMPLPLOT] " + msg);
	}

	public void println(Object msg) {
		out.println("[COVCOMPLPLOT] " + msg);
	}

	public void println(String msg, Object... args) {
		out.println("[COVCOMPLPLOT] " + String.format(msg, args));
	}

	public void printError(String msg) {
		out.println("[COVCOMPLPLOT ERROR] " + msg);
	}

	public void printError(String msg, Object... args) {
		out.println("[COVCOMPLPLOT ERROR] " + String.format(msg, args));
	}

	public void printStackTrace(Exception e) {
		if (verbose) {
			out.println("=======================================================");
			e.printStackTrace(out);
			out.println("=======================================================");
		}
	}
}
