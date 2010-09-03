package hudson.plugins.covcomplplot;

import hudson.model.AbstractBuild;
import hudson.plugins.covcomplplot.analyzer.Analyzer;
import hudson.plugins.covcomplplot.model.MethodInfo;

import java.io.Serializable;
import java.util.List;

/**
 * Class containing coverage and complexity methods in the given value range and
 * corresponding paging. This class handles detailed list of methods.
 * 
 * @author JunHo Yoon
 */
public class CovComplPlotMethods implements Serializable {

	/** UID */
	private static final long serialVersionUID = 1L;

	/** Method list on current page of methods */
	private final List<MethodInfo> methodInfoList;

	/** {@link AbstractBuild} which owns this instance */
	private transient AbstractBuild<?, ?> owner;

	/** Coverage lower bound value */
	public final int cov;

	/** Complexity lower bound value */
	public final int compl;

	/**
	 * Analyzer used to generate graph. This is used for generating some data
	 * which depends on the analyzer type
	 */
	private final Analyzer analyzer;

	/** Current Page */
	private final int page;

	/** Total Item count */
	private final int totalCount;

	/**
	 * Get the {@link AbstractBuild} instance which owns this information.
	 * 
	 * @return build object
	 */
	public AbstractBuild<?, ?> getOwner() {
		return owner;
	}

	/**
	 * Constructor for detailed level information holder
	 * 
	 * @param owner
	 *            {@link AbstractBuild} owning this instance.
	 * @param methodInfoList
	 *            {@link MethodInfo} list
	 * @param cov
	 *            coverage lower bound value
	 * @param compl
	 *            complexity lower bound value
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

	/**
	 * Get the title shown in the detailed method list page.
	 * 
	 * @return title string
	 */
	public String getTitle() {
		int covLowerBound = cov * Constant.DOMAIN_AXIS_TICK_UNIT;
		int covUpperBound = (cov + 1) * Constant.DOMAIN_AXIS_TICK_UNIT - 1;
		if (covUpperBound == Constant.DOMAIN_AXIS_UPPERBOUND - 1) {
			covUpperBound = Constant.DOMAIN_AXIS_UPPERBOUND;
		}

		int complLowerBound = compl * Constant.RANGE_AXIS_TICK_UNIT;
		int complUpperBound = (compl + 1) * Constant.RANGE_AXIS_TICK_UNIT - 1;
		// when it's topmost complexity range.
		if (complUpperBound == Constant.RANGE_AXIS_UPPERBOUND - 1) {
			return String.format("%d method(s) in the range of coverage (%d%%~%d%%) and complexity (%d~)", totalCount, covLowerBound, covUpperBound,
					complLowerBound);
		}

		return String.format("%d method(s) in the range of coverage (%d%%~%d%%) and complexity (%d~%d)", totalCount, covLowerBound, covUpperBound,
				complLowerBound, complUpperBound);
	}

	

	/**
	 * Return the Hudson URL in which the source code is viewed.
	 * 
	 * @param methodInfo
	 *            {@link MethodInfo} instance to be resolved
	 * @return Source code URL
	 */
	public String getMethodUrl(MethodInfo methodInfo) {
		return analyzer.getHandler().getMethodUrlLocation(owner, methodInfo);
	}

	/**
	 * Get pagination object so that jelly can render the pagination.
	 * 
	 * @return {@link Pagination} instance
	 */
	public Pagination getPagination() {
		return new Pagination(this.page, this.getTotalCount(), Constant.PAGING_SIZE, String.format("?cov=%d&compl=%d&page=", cov, compl));
	}

	/**
	 * Set {@link AbstractBuild} which owns this build
	 * 
	 * @param owner
	 *            {@link AbstractBuild}
	 */
	public void setOwner(AbstractBuild<?, ?> owner) {
		this.owner = owner;
	}

	/**
	 * Get {@link MethodInfo} list contained this object
	 * 
	 * @return {@link MethodInfo} list
	 */
	public List<MethodInfo> getMethodInfoList() {
		return methodInfoList;
	}

	/**
	 * Get total item count
	 * 
	 * @return total item count
	 */
	public int getTotalCount() {
		return totalCount;
	}
}
