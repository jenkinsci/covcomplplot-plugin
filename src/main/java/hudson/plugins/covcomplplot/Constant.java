package hudson.plugins.covcomplplot;

public class Constant {
	public static final String URL_NAME = "covcomplplot";
	public static final String ICON_NAME = null;
	public static final String QD_NAME = "Quality Dashboard";
	public static final int RANGE_AXIS_TICK_UNIT = 5;
	public static final int DOMAIN_AXIS_TICK_UNIT = 10;
	public static final int RANGE_AXIS_UPPERBOUND = 35;
	public static final int DOMAIN_AXIS_UPPERBOUND = 100;
	public static final int DOMAIN_AXIS_COUNT = DOMAIN_AXIS_UPPERBOUND / DOMAIN_AXIS_TICK_UNIT;
	public static final int RANGE_AXIS_COUNT = RANGE_AXIS_UPPERBOUND / RANGE_AXIS_TICK_UNIT;
	public static final int GRAPH_POINT_SIZE = 5;
	
	public static final String RESULT_FILENAME = "covcomplplot.xml";
	public static final int PAGING_SIZE =20;
	public static final int PAGING_RANGE = 10;
	public static final int OUTLIER_MAXIMIZE_RATE = 15;
	
}
