package hudson.plugins.covcomplplot;

import hudson.model.Hudson;

/**
 * Class for rendering paging information in jelly script.
 * 
 * @author JunHo Yoon
 */
public class Pagination {
	private static final long serialVersionUID = 116765475669773985L;
	private int totalCount = 0;
	private int currentPage = 1;
	private int prevPage = 0;
	private int nextPage = 0;
	private int totalPage = 0;
	private int count = 0;
	private int pageSize = 10;
	private final String moveLink;
	private String rootUrl = "";

	/**
	 * Contructor
	 * @param currentPage
	 *            Current Page
	 * @param totalCount
	 *            total Count
	 * @param pagingSize Page Size
	 * @param moveLink  
	 */
	public Pagination(int currentPage, int totalCount, int pagingSize, String moveLink) {
		this.currentPage = currentPage;
		this.totalCount = totalCount;
		this.pageSize = pagingSize;
		this.moveLink = moveLink;
		try {
			rootUrl = Hudson.getInstance().getRootUrlFromRequest();
		} catch (Exception e) {

		}
	}

	/**
	 * Get paging tag
	 * @return tag string
	 */
	public String getTag() {
		if (pageSize == 0)
			pageSize = 10;
		setPageNation();
		StringBuilder sb = new StringBuilder();
		sb.append("<table summary='page navigation' class='Nnavi'>\n").append("<tr>\n");
		if (prevPage + 1 > Constant.PAGING_RANGE) {
			setPrePage(sb);
		} else {
			setBlankTd(sb);
		}
		setIndex(sb);
		if (totalPage > (prevPage + Constant.PAGING_RANGE)) {
			setNextPage(sb);
		}
		sb.append("</tr>\n").append("</table>\n");
		return sb.toString();
	}

	private void setNextPage(StringBuilder sb) {
		sb.append("<td nowrap class='pgR' style='border-right:0px'>\n").append("<a href='").append(moveLink).append(nextPage).append("'>\n").append(
				"Next").append("<img src='").append(rootUrl).append(
				"/plugin/covcomplplot/icons/nextbutton.gif' width='3' height='5' alt='' border='0'>\n").append("</a>\n");
		sb.append("</td>");
	}

	private void setPrePage(StringBuilder sb) {
		sb.append("<td nowrap class='pgR'>\n").append("<a href='").append(moveLink).append(prevPage).append("'>\n").append("<img src='").append(
				rootUrl).append("/plugin/covcomplplot/icons/nextbutton.gif' width='3' height='5' alt='' border='0'>\n").append("Prev").append(
				"</a>\n");
		sb.append("</td>");
	}

	private void setBlankTd(StringBuilder sb) {
		sb.append("<td class='pgR'>\n");
		sb.append("</td>");
	}

	private void setIndex(StringBuilder sb) {
		for (count = prevPage + 1; count < nextPage && count <= totalPage; count++) {
			sb.append("<td");
			if (count == currentPage) {
				sb.append(" class='on'>\n");
				sb.append("<a href='#'>").append(count).append("</a>\n");
			} else {
				sb.append(">\n");
				sb.append("<a href='").append(moveLink).append(count).append("'>").append(count).append("</a>\n");
			}
			sb.append("</td>\n");
		}

	}

	private void setPageNation() {
		int current = (currentPage - 1) / Constant.PAGING_RANGE + 1;
		prevPage = (current - 1) * Constant.PAGING_RANGE;
		nextPage = current * Constant.PAGING_RANGE + 1;
		totalPage = ((totalCount - 1) / pageSize) + 1;
	}

	/**
	 * Get total item count
	 * @return total tem count
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * Set total item count
	 * @param totalCount total item count
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	/**
	 * Get current page
	 * @return current page
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * Set current page
	 * @param currentPage current page
	 */
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * Get each page size
	 * @return the pageSize
	 */

	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Set page size
	 * @param pageSize
	 *            the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

}
