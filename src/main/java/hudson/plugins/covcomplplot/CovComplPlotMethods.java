package hudson.plugins.covcomplplot;

import hudson.model.AbstractBuild;
import hudson.plugins.covcomplplot.annalyzer.Analyzer;
import hudson.plugins.covcomplplot.model.MethodInfo;

import java.io.Serializable;
import java.util.List;

/**
 * Target containing coverage and complexity methods. This class handles graph
 * and map generation in default and This class handles method list output when
 * it is used in the detailed view. {@link CovComplPlotMethods} is used as a top
 * level information holder and detailed information holder as well.
 * 
 * @author nhn
 */

public class CovComplPlotMethods implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final List<MethodInfo> methodInfoList;

	private transient AbstractBuild<?, ?> owner;

	/** Coverage Lower Bound */
	public final int cov;

	/** Complexity Lower Bound */
	public final int compl;

	private final Analyzer analyzer;

	private final int page;

	private final int totalCount;

	/**
	 * Get the build which owns this information.
	 * 
	 * @return build object
	 */
	public AbstractBuild<?, ?> getOwner() {
		return owner;
	}

	public String getUrl(MethodInfo methodInfo) {
		return analyzer.getHandler().getMethodUrlLocation(owner, methodInfo);
	}

	/**
	 * Constructor for detailed level information holder
	 * 
	 * @param owner
	 * @param methodInfoList
	 * @param cov
	 * @param compl
	 */
	public CovComplPlotMethods(AbstractBuild<?, ?> owner, List<MethodInfo> methodInfoList, Analyzer analyzer, int cov, int compl, int page,
			int totalCount) {
		this.owner = owner;
		this.methodInfoList = methodInfoList;
		this.analyzer = analyzer;
		this.cov = cov;
		this.compl = compl;
		this.page = page;
		this.totalCount = totalCount;
	}

	public String getTitle() {
		
		
		int covLowerBound = cov * Constant.DOMAIN_AXIS_TICK_UNIT;
		int covUpperBound = (cov+1) * Constant.DOMAIN_AXIS_TICK_UNIT - 1;
		if (covUpperBound == Constant.DOMAIN_AXIS_UPPERBOUND-1) {
			covUpperBound = Constant.DOMAIN_AXIS_UPPERBOUND;
		}
		
		int complLowerBound = compl * Constant.RANGE_AXIS_TICK_UNIT;
		int complUpperBound = (compl+1) * Constant.RANGE_AXIS_TICK_UNIT - 1;
		// when it's topmost complexity range.
		if (complUpperBound == Constant.RANGE_AXIS_UPPERBOUND-1) {
			return String.format("%d method(s) in the range of coverage (%d%%~%d%%) and complexity (%d~)", totalCount, covLowerBound, covUpperBound, complLowerBound);
		}
		
		return String.format("%d method(s) in the range of coverage (%d%%~%d%%) and complexity (%d~%d)", totalCount, covLowerBound, covUpperBound, complLowerBound, complUpperBound);
	}

	/**
	 * Constructor for top level information holder.
	 * 
	 * @param owner
	 * @param methodInfoList
	 * @param totalSize
	 * @param page
	 * @param compl2
	 * @param cov2
	 * @param ownersTimeStamp
	 */
	public CovComplPlotMethods(AbstractBuild<?, ?> owner, List<MethodInfo> methodInfoList, Analyzer analyzer) {
		this(owner, methodInfoList, analyzer, 0, 0, 1, 0);
	}

	public String getMethodUrlLocation(MethodInfo methodInfo) {
		return analyzer.getHandler().getMethodUrlLocation(owner, methodInfo);
	}

	public Pagination getPagination() {
		return new Pagination(this.page, this.getTotalCount(), Constant.PAGING_SIZE, String.format("?cov=%d&compl=%d&page=", cov, compl));
	}

	public void setOwner(AbstractBuild<?, ?> owner) {
		this.owner = owner;
	}

	public List<MethodInfo> getMethodInfoList() {
		return methodInfoList;
	}

	public int getTotalCount() {
		return totalCount;
	}

}
