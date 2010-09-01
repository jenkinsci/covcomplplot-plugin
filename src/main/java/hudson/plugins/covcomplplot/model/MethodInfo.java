package hudson.plugins.covcomplplot.model;

import java.text.DecimalFormat;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class MethodInfo implements Comparable<MethodInfo> {

	/** Signature */
	@XStreamAsAttribute
	public final String sig;

	/** Complexity */
	@XStreamAsAttribute
	public final int compl;

	/** Line no */
	@XStreamAsAttribute
	public final int line;

	/** Covered Statement */
	@XStreamAsAttribute
	public int cst;

	/** Statement */
	@XStreamAsAttribute
	public int st;

	/** File path */
	@XStreamAsAttribute
	public final String path;

	public MethodInfo(String path, String signature, int complexity, int lineno) {
		this.path = path;
		this.sig = signature;
		this.compl = complexity;
		this.line = lineno;
	}

	public float getCoverageRatio() {
		if (this.st == 0) {
			return Float.NaN;
		}
		return ((float) cst) / st * 100;
	}

	public static DecimalFormat formatter = new DecimalFormat("#.##");

	public String getFormattedCoverageRatio() {
		float ratio = getCoverageRatio();
		return (ratio == Float.NaN) ? "0" : formatter.format(ratio);
	}

	public String getDisplaySignature() {
		if (sig.length() > 80) {
			return sig.substring(0, 77) + "...";
		}
		return sig;
	}

	public void increaseLine(boolean covered) {
		if (covered) {
			cst++;
		}
		st++;
	}

	@Override
	public String toString() {
		return String.format("%s -- %s, %d, %d / %d, %d", path, sig, line, cst, st, (int) ((float) cst / st * 100));
	}

	public int compareTo(MethodInfo o) {
		int compare = o.compl - compl;
		if (compare == 0) {
			compare = o.st - st;
		}
		return compare;
	}
}
