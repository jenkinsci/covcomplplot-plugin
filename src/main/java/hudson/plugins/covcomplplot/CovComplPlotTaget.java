package hudson.plugins.covcomplplot;

import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.plugins.covcomplplot.analyzer.Analyzer;
import hudson.plugins.covcomplplot.model.MethodInfo;
import hudson.plugins.covcomplplot.util.CustomGraph;
import hudson.plugins.covcomplplot.util.ScatterPlotPointMapRenderer;
import hudson.plugins.covcomplplot.util.ScatterPlotPointRenderer;
import hudson.util.XStream2;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Target containing coverage and complexity methods. This class y mostlhandles graph
 * and map generation. This class handles detailed method list generation when
 * the detailed view is shown. 
 * {@link CovComplPlotTaget} is a top level information holder. 
 * It defers the detailed method list view responsibility to {@link CovComplPlotMethods}.
 * 
 * @author JunHo Yoon
 */

public class CovComplPlotTaget implements Serializable {

	/** UID */
	private static final long serialVersionUID = 1L;

	/** Method occurrence count matrix */
	private transient int[][] methodOccuranceMatrix;

	/** Method list per each cell */
	@SuppressWarnings("unchecked")
	private transient List[][] methodMapMatrix;

	/** Cache for graph map shown each build page */
	private transient String graphMapWithBuildNo = null;

	/** Cache for graph map shown in project page */
	private transient String graphMap = null;

	/** Timestamp */
	private final Calendar ownerTimestamp;

	/** Method list in total */
	private final List<MethodInfo> methodInfoList;

	/** Owner */
	private transient AbstractBuild<?, ?> owner;

	/** Analyzer from which this object is created */
	private final Analyzer analyzer;

	/** Synchronization lock */
	public Object lock = new Object();

	/**
	 * Get the {@link AbstractBuild} instance which owns this instance.
	 * 
	 * @return {@link AbstractBuild} instance.
	 */
	public AbstractBuild<?, ?> getOwner() {
		return owner;
	}

	/**
	 * Constructor 
	 * 
	 * @param owner
	 *            {@link AbstractBuild} instance which owns this instance.
	 * @param methodInfoList
	 *            {@link MethodInfo} list
	 * @param ownersTimeStamp
	 *            timestamp of owner.
	 */
	public CovComplPlotTaget(AbstractBuild<?, ?> owner, List<MethodInfo> methodInfoList, Analyzer analyzer, Calendar ownersTimeStamp) {
		this.owner = owner;
		this.methodInfoList = methodInfoList;
		this.analyzer = analyzer;
		this.ownerTimestamp = ownersTimeStamp;
	}

	/**
	 * Get graph. This method is called to show a graph from jelly.
	 * <pre>
	 * projectName/build/graph/map
	 * </pre>
	 * @return GraphImpl object
	 */
	public GraphImpl getGraph() {
		return new GraphImpl();
	}

	/**
	 * Dynamic handling of Stapler request. This method create appropriate
	 * {@link CovComplPlotMethods} object based on the given parameters and
	 * redirect the {@link StaplerRequest} to the created object.
	 * 
	 * @param req
	 *            stapler request
	 * @param rsp
	 *            stapler response
	 * @param cov
	 *            coverage range start value
	 * @param compl
	 *            complexity range start value
	 * @param page
	 *            page
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void doDynamic(StaplerRequest req, StaplerResponse rsp, @QueryParameter("cov") int cov, @QueryParameter("compl") int compl,
			@QueryParameter("page") int page) throws ServletException, IOException {
		page = Math.max(page, 1);
		cov = Math.min(cov, Constant.DOMAIN_AXIS_COUNT - 1);
		compl = Math.min(compl, Constant.RANGE_AXIS_COUNT - 1);
		List[][] methodMapMatrix = createMapGridMatrix(getMethodInfoList());
		List obj = methodMapMatrix[compl][cov];
		int totalSize = 0;
		if (obj == null) {
			obj = Collections.emptyList();
		} else {
			totalSize = obj.size();
			int startIndex = ((page - 1) * Constant.PAGING_SIZE);
			int retrieveSize = Math.min(totalSize - ((page - 1) * Constant.PAGING_SIZE), Constant.PAGING_SIZE);
			obj = obj.subList(startIndex, startIndex + retrieveSize);
		}
		req.getView(new CovComplPlotMethods(owner, (List<MethodInfo>) obj, analyzer, cov, compl, page, totalSize), "index.jelly").forward(req, rsp);
	}

	/**
	 * Create a method occurrence matrix. a method occurrence matrix is
	 * a 2 dimensional array and each cell represents the corresponding coverage and complexity value.
	 * The reason why uses array is to save memories and regulate the graph rendering speed.
	 * 
	 * This method is subject to be synchronized. Because internally it uses a cache not to generate matrix more than once.
	 * @param methodInfoList
	 *            {@link MethodInfo} list from which method occurrence matrix is created
	 * @return method occurrence matrix
	 */
	private int[][] createMatrix(List<MethodInfo> methodInfoList) {
		synchronized (lock) {
			if (this.methodOccuranceMatrix == null) {
				int[][] matrix = new int[Constant.RANGE_AXIS_UPPERBOUND][Constant.DOMAIN_AXIS_UPPERBOUND];
				for (MethodInfo methodInfo : methodInfoList) {
					int complexityIndex = Math.min(methodInfo.compl, Constant.RANGE_AXIS_UPPERBOUND - 1);
					int coverageIndex = Math.min(Math.round(methodInfo.getCoverageRatio()), Constant.DOMAIN_AXIS_UPPERBOUND - 1);
					matrix[complexityIndex][coverageIndex]++;
				}
				this.methodOccuranceMatrix = matrix;
			}
		}
		return this.methodOccuranceMatrix;
	}
	
	/**
	 * Get custom java script
	 * @return custom java script string
	 */
	public String getCustomJavaScript() {
		return analyzer.getHandler().getCustomJavaScript();
	}
	/**
	 * Get the description of the graph. The description is different according to which analyzer is used.
	 * For example, coverage in clover processing means statement coverage.
	 * @return description
	 */
	public String getDescription() {
		return analyzer.getHandler().getDescription();
	}

	/**
	 * Get the snap value of the passed x. For example, if the snap is 5, the
	 * value returned for 6 is 5. In case that x is 11, the snap value is 10.
	 * 
	 * @param x
	 *            value to be converted
	 * @param snap
	 *            snap to be applied
	 * @return snaped value
	 */
	private int getSnapValue(double x, int snap) {
		return ((int) (x / snap)) * snap;
	}

	/**
	 * Create {@link MethodInfo} list per each grid cell. {@link MethodInfo}
	 * list is subject to be cached. So there is synchronized block inside.
	 * 
	 * @param methodInfoList
	 *            all {@link MethodInfo} list to be reorganized/
	 * @return 2 dimensional array of {@link MethodInfo} instances.
	 */
	@SuppressWarnings("unchecked")
	private List[][] createMapGridMatrix(List<MethodInfo> methodInfoList) {
		synchronized (lock) {
			if (this.methodMapMatrix == null) {
				ArrayList[][] matrix = new ArrayList[Constant.RANGE_AXIS_COUNT][Constant.DOMAIN_AXIS_COUNT];
				for (MethodInfo methodInfo : methodInfoList) {
					int complexityIndex = Math.min(getSnapValue(methodInfo.compl, Constant.RANGE_AXIS_TICK_UNIT), Constant.RANGE_AXIS_UPPERBOUND
							- Constant.RANGE_AXIS_TICK_UNIT)
							/ Constant.RANGE_AXIS_TICK_UNIT;
					int coverageIndex = Math.min(getSnapValue(methodInfo.getCoverageRatio(), Constant.DOMAIN_AXIS_TICK_UNIT),
							Constant.DOMAIN_AXIS_UPPERBOUND - Constant.DOMAIN_AXIS_TICK_UNIT)
							/ Constant.DOMAIN_AXIS_TICK_UNIT;
					ArrayList<MethodInfo> each = matrix[complexityIndex][coverageIndex];
					if (each == null) {
						each = new ArrayList<MethodInfo>();
					}
					matrix[complexityIndex][coverageIndex] = each;
					each.add(methodInfo);
				}

				for (ArrayList[] matrixRow : matrix) {
					for (ArrayList<MethodInfo> matrixCol : matrixRow) {
						if (matrixCol != null) {
							Collections.sort(matrixCol);
						}
					}
				}
				this.methodMapMatrix = matrix;
			}
		}
		return this.methodMapMatrix;
	}

	/**
	 * Create {@link DefaultXYZDataset} instance from methodOccurenceMatrix
	 * 
	 * @return {@link DefaultXYZDataset} instance.
	 */
	public DefaultXYZDataset generateXYDataset() {
		this.methodOccuranceMatrix = createMatrix(this.getMethodInfoList());
		DefaultXYZDataset dataSet = new DefaultXYZDataset();
		// At first check the total count of point to create dataset array.
		int totalPoint = 0;
		for (int[] eachRow : methodOccuranceMatrix) {
			for (int eachValue : eachRow) {
				if (eachValue != 0) {
					totalPoint++;
				}
			}
		}
		double[][] eachPoint = new double[3][totalPoint];
		int count = 0;
		// Create the dataset
		for (int eachRowCount = 0; eachRowCount < methodOccuranceMatrix.length; eachRowCount++) {
			for (int eachColumCount = 0; eachColumCount < methodOccuranceMatrix[eachRowCount].length; eachColumCount++) {
				int methodCount = methodOccuranceMatrix[eachRowCount][eachColumCount];
				if (methodCount != 0) {
					eachPoint[0][count] = eachColumCount;
					eachPoint[1][count] = eachRowCount;
					eachPoint[2][count] = methodCount;
					count++;
				}
			}
		}
		dataSet.addSeries(1, eachPoint);
		return dataSet;
	}

	/**
	 * Implementation of scatter plot graph
	 * 
	 * @author JunHo Yoon
	 */
	public class GraphImpl extends CustomGraph {
		/** Renderer to be used */
		XYDotRenderer renderer = null;
		/** Timestamp */
		public final long timeInMillis;

		/**
		 * Default constructor
		 */
		protected GraphImpl() {
			super(ownerTimestamp, 500, 200);
			timeInMillis = ownerTimestamp.getTimeInMillis();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * hudson.plugins.covcomplplot.util.CustomGraph#doPng(org.kohsuke.stapler
		 * .StaplerRequest, org.kohsuke.stapler.StaplerResponse)
		 */
		@Override
		public void doPng(StaplerRequest req, StaplerResponse rsp) throws IOException {
			this.renderer = new ScatterPlotPointRenderer();
			super.doPng(req, rsp);
		}

		/**
		 * Create image map string. This method uses caching inside.
		 * 
		 * @param withBuildNo
		 *            "true" if URL should contains build NO.
		 * @return Image Map String
		 */
		public synchronized String getMapString(String withBuildNo) {
			if ("true".equals(withBuildNo)) {
				if (graphMapWithBuildNo == null) {
					graphMapWithBuildNo = getMapStringInternal(withBuildNo);
				}
				return graphMapWithBuildNo;
			} else {
				if (graphMap == null) {
					graphMap = getMapStringInternal(withBuildNo);
				}
				return graphMap;
			}
		}

		/**
		 * Create image map string
		 * 
		 * @param withBuildNo
		 *            true if URL should contains build NO.
		 * @return Image Map String
		 */
		private String getMapStringInternal(String withBuildNo) {
			// To be minimize the rendering processing, assign an almost empty
			// renderer.
			this.renderer = new ScatterPlotPointMapRenderer();
			// Only reason why rendering here is to get the dataArea.
			render(null);
			Rectangle2D dataArea = ((ScatterPlotPointMapRenderer) this.renderer).dataArea;

			ChartRenderingInfo info = new ChartRenderingInfo();
			XYPlot xyPlot = this.graph.getXYPlot();
			ValueAxis domainAxis = xyPlot.getDomainAxis();
			ValueAxis rangeAxis = xyPlot.getRangeAxis();
			double rangeTickSize = dataArea.getHeight() / Constant.RANGE_AXIS_COUNT;
			double domainTickSize = dataArea.getWidth() / Constant.DOMAIN_AXIS_COUNT;
			RectangleEdge domainAxisEdge = xyPlot.getDomainAxisEdge();
			RectangleEdge rangeAxisEdge = xyPlot.getRangeAxisEdge();
			int buildNo = owner.number;
			List<?>[][] mapGridMatrix = createMapGridMatrix(getMethodInfoList());
			// Create mapString per each grid cell
			for (int x = 0; x < Constant.DOMAIN_AXIS_COUNT; x++) {
				for (int y = 0; y < Constant.RANGE_AXIS_COUNT; y++) {
					if (mapGridMatrix[y][x] != null) {
						int valueX = x * Constant.DOMAIN_AXIS_TICK_UNIT;
						int valueY = y * Constant.RANGE_AXIS_TICK_UNIT;
						double realX = domainAxis.valueToJava2D(valueX, dataArea, domainAxisEdge);
						double realY = rangeAxis.valueToJava2D(valueY, dataArea, rangeAxisEdge);
						String complexityRangeString = "";
						if (valueY == Constant.RANGE_AXIS_UPPERBOUND - 1) {
							complexityRangeString = String.format("%d~", valueY);
						} else {
							complexityRangeString = String.format("%d~%d", valueY, valueY + Constant.RANGE_AXIS_TICK_UNIT - 1);
						}
						String tooltip = String.format("%d methods - Complexity : %s, Coverage : %d~%d%%", mapGridMatrix[y][x].size(),
								complexityRangeString, valueX, valueX + Constant.DOMAIN_AXIS_TICK_UNIT - 1);
						String url = String.format("%s/?cov=%d&compl=%d", Constant.URL_NAME, x, y);
						if ("true".equals(withBuildNo)) {
							url = buildNo + "/" + url;
						}
						info.getEntityCollection().add(
								new ChartEntity(new Rectangle((int) realX, (int) (realY - rangeTickSize), (int) domainTickSize - 1,
										(int) rangeTickSize - 1), tooltip, url));
					}
				}
			}
			String mapString = ChartUtilities.getImageMap("covcomplmap", info);
			return mapString;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see hudson.plugins.covcomplplot.util.CustomGraph#createGraph()
		 */
		@Override
		protected JFreeChart createGraph() {
			final XYZDataset dataset = generateXYDataset();
			final JFreeChart chart = ChartFactory.createScatterPlot("", "coverage(%)", "complexity", dataset, PlotOrientation.VERTICAL, false, false,
					false);
			chart.setBackgroundPaint(Color.white);
			final XYPlot plot = chart.getXYPlot();
			// set up paint
			plot.setBackgroundPaint(Color.WHITE);
			plot.setOutlinePaint(null);
			plot.setForegroundAlpha(0.4f);
		
			// set up grid line
			plot.setRangeGridlinesVisible(true);
			plot.setRangeGridlinePaint(Color.GRAY);
			plot.setRangeGridlinesVisible(true);
			plot.setDomainGridlinePaint(Color.GRAY);
			
			
			// Set up axis
			plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));

			ValueAxis rangeAxis = plot.getRangeAxis();
			rangeAxis.setAutoRange(false);
			rangeAxis.setUpperBound(Constant.RANGE_AXIS_UPPERBOUND);
			rangeAxis.setLowerBound(0);
			((NumberAxis) rangeAxis).setTickUnit(new NumberTickUnit(Constant.RANGE_AXIS_TICK_UNIT));

			ValueAxis domainAxis = plot.getDomainAxis();
			domainAxis.setAutoRange(false);
			domainAxis.setUpperBound(Constant.DOMAIN_AXIS_UPPERBOUND);
			domainAxis.setLowerBound(0);
			((NumberAxis) domainAxis).setTickUnit(new NumberTickUnit(Constant.DOMAIN_AXIS_TICK_UNIT));

			plot.setInsets(new RectangleInsets(5, 0, 0, 10));
			renderer.setSeriesPaint(0, Color.BLUE);
			plot.setRenderer(renderer);

			return chart;
		}
	}

	/**
	 * Set owner
	 * 
	 * @param owner
	 *            owner
	 */
	public void setOwner(AbstractBuild<?, ?> owner) {
		this.owner = owner;
	}

	/**
	 * Get datafile in the {@link AbstractBuild} root.
	 * 
	 * @param build
	 *            build
	 * @return {@link XmlFile}
	 */
	private static XmlFile getDataFile(AbstractBuild<?, ?> build) {
		File dir = build == null ? new File(System.getProperty("java.io.tmpdir")) : build.getRootDir();
		return new XmlFile(new XStream2(), new File(dir, Constant.RESULT_FILENAME));
	}

	/**
	 * Load {@link CovComplPlotTaget} instance from datafile
	 * 
	 * @param build
	 *            the build where the datafile is located.
	 * @return {@link CovComplPlotTaget} instance
	 * @throws IOException
	 *             occurs if there is file system failure.
	 */
	public static CovComplPlotTaget loadCovComplScatterPlotTarget(AbstractBuild<?, ?> build) throws IOException {
		CovComplPlotTaget target = (CovComplPlotTaget) getDataFile(build).read();
		target.setOwner(build);
		return target;
	}

	/**
	 * Save {@link CovComplPlotTaget} in the datafile named.
	 * 
	 * @param target
	 *            {@link CovComplPlotTaget} instance to be saved.
	 * @throws IOException
	 *             occurs if there is file system failure.
	 */
	public static void saveCovComplScatterPlotTarget(CovComplPlotTaget target) throws IOException {
		getDataFile(target.getOwner()).write(target);
	}

	/**
	 * Get all {@link MethodInfo} list.
	 * 
	 * @return {@link MethodInfo} list
	 */
	public List<MethodInfo> getMethodInfoList() {
		return methodInfoList;
	}

}
