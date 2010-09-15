package hudson.plugins.covcomplplot.model;

import java.text.DecimalFormat;

/**
 * Value class which contains each method info.
 * 
 * @author JunHo Yoon
 */
public class MethodInfo implements Comparable<MethodInfo> {

	/** Signature */
	public String sig;

	/** Complexity */
	public int compl;

	/** Line no */
	public int line;

	/** Covered Statement */
	public int cst;

	/** Statement */
	public int st;

	/** File path */
	public String path;

	/**
	 * whether this method is called
	 */
	public boolean covered;

	/**
	 * Constructor
	 * 
	 * @param path
	 *            file path in which this method exists
	 * @param signature
	 *            method signature
	 * @param complexity
	 *            method complexity
	 * @param lineno
	 *            method lines
	 */
	public MethodInfo(String path, String signature, int complexity, int lineno) {
		this.path = path;
		this.sig = signature;
		this.compl = complexity;
		this.line = lineno;
	}
	
	/**
	 * Constructor
	 * 
	 * @param path
	 *            file path in which this method exists
	 * @param signature
	 *            method signature
	 * @param complexity
	 *            method complexity
	 * @param lineno
	 *            method lines
	 */
	public MethodInfo(String path, String signature, int complexity, int lineno, int covered, int size) {
		this.path = path;
		this.sig = signature;
		this.compl = complexity;
		this.line = lineno;
		this.cst = covered;
		this.st = size;
	}

	/**
	 * Get coverage ratio.
	 * 
	 * @return Coverage ratio
	 */
	public float getCoverageRatio() {
		if (this.st == 0) {
			if (this.covered)
				return 100;
			return Float.NaN;
		}
		return ((float) cst) / st * 100;
	}

	private static DecimalFormat formatter = new DecimalFormat("#.##");

	/**
	 * Get coverage ratio formatted by "#.##"
	 * 
	 * @return formatted coverage ratio.
	 */
	public String getFormattedCoverageRatio() {
		float ratio = getCoverageRatio();
		return (ratio == Float.NaN) ? "0" : formatter.format(ratio);
	}

	/**
	 * Return signature string cutting off more than 80 characters.
	 * 
	 * @return signature
	 */
	public String getDisplaySignature() {
		if (sig.length() > 80) {
			return sig.substring(0, 77) + "...";
		}
		return sig;
	}

	/**
	 * Increase lines of code in this method. If the true is passed, the covered
	 * line is increased as well.
	 * 
	 * @param covered
	 *            true if the line is covered.
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(MethodInfo o) {
		int compare = o.compl - compl;
		if (compare == 0) {
			compare = o.st - st;
		}
		return compare;
	}

	public void setCompl(int compl) {
		this.compl = compl;
	}

	public int getCompl() {
		return compl;
	}

	public void setSig(String sig) {
		this.sig = sig;
	}

	public String getSig() {
		return sig;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void increaseSizeAndCovered(int size, int covered) {
		st += size;
		cst += covered;
	}
	
	public void increaseComplexity(int count) {
		compl += count;
	}
}
