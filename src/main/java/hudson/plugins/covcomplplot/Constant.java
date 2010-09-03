package hudson.plugins.covcomplplot;

/**
 * Constants
 * 
 * @author JunHo Yoon
 */
public class Constant {
	/** Url name */
	public static final String URL_NAME = "covcomplplot";
	/** Icon name */
	public static final String ICON_NAME = null;
	/** Vertical tick unit */
	public static final int RANGE_AXIS_TICK_UNIT = 5;
	/** Horizontal tick unit*/
	public static final int DOMAIN_AXIS_TICK_UNIT = 10;
	/** Upper bound value of vertical line */
	public static final int RANGE_AXIS_UPPERBOUND = 35;
	/** Upper bound value of horizontal line */
	public static final int DOMAIN_AXIS_UPPERBOUND = 100;
	/** The count of how many ticks exists in the horizontal line */
	public static final int DOMAIN_AXIS_COUNT = DOMAIN_AXIS_UPPERBOUND / DOMAIN_AXIS_TICK_UNIT;
	/** The count of how many ticks exists in the vertical line */
	public static final int RANGE_AXIS_COUNT = RANGE_AXIS_UPPERBOUND / RANGE_AXIS_TICK_UNIT;
	/** Minimum graph point size */
	public static final int GRAPH_POINT_SIZE = 5;
	/** Each data file name containing covcomplplot data */
	public static final String RESULT_FILENAME = "covcomplplot.xml";
	/** Page size of detailed list of methods */
	public static final int PAGING_SIZE = 20;
	/** Constants indicating how many pages are shown in pagination tag */
	public static final int PAGING_RANGE = 10;
	
	/** Constants indicating how many times bigger the outliers in the graph should be drawn. */
	public static final int OUTLIER_MAXIMIZE_RATE = 15;

}
