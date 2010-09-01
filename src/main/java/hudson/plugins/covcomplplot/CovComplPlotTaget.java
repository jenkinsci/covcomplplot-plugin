package hudson.plugins.covcomplplot;

import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.plugins.covcomplplot.annalyzer.Analyzer;
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
 * Target containing coverage and complexity methods. This class handles graph
 * and map generation in default and This class handles method list output when
 * it is used in the detailed view. {@link CovComplPlotTaget} is used as a top
 * level information holder. It defers the detailed information to
 * {@link CovComplPlotMethods}.
 * 
 * @author nhn
 */

public class CovComplPlotTaget implements Serializable {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	private transient int[][] methodOccuranceMatrix;
	@SuppressWarnings("unchecked")
	private transient List[][] methodMapMatrix;
	private transient String graphMapWithBuildNo = null;
	private transient String graphMap = null;

	private final Calendar ownerTimestamp;
	private final List<MethodInfo> methodInfoList;

	private transient AbstractBuild<?, ?> owner;

	private final Analyzer analyzer;
	public Object lock = new Object(); 
	/**
	 * Get the build which owns this information.
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
	 * @param methodInfoList
	 * @param ownersTimeStamp
	 */
	public CovComplPlotTaget(AbstractBuild<?, ?> owner, List<MethodInfo> methodInfoList, Analyzer analyzer, Calendar ownersTimeStamp) {
		this.owner = owner;
		this.methodInfoList = methodInfoList;
		this.analyzer = analyzer;
		this.ownerTimestamp = ownersTimeStamp;
	}

	public String getMethodUrlLocation(MethodInfo methodInfo) {
		return analyzer.getHandler().getMethodUrlLocation(owner, methodInfo);
	}

	/**
	 * Get graph. This will be called to show graph from jelly.
	 * 
	 * @return GraphImpl object
	 */
	public GraphImpl getGraph() {
		return new GraphImpl();
	}

	/**
	 * 
	 * @param token
	 * @param req
	 * @param rsp
	 * @param cov
	 * @param compl
	 * @param start
	 * @param size
	 * @return
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

	public String getDescription() {
		return analyzer.getHandler().getDescription();
	}

	private int getSnapValue(double x, int snap) {
		return ((int) (x / snap)) * snap;
	}

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

	public DefaultXYZDataset generateXYDataset() {
		this.methodOccuranceMatrix = createMatrix(this.getMethodInfoList());
		DefaultXYZDataset dataSet = new DefaultXYZDataset();

		// 먼저 전체 데이터 개수를 확인한다.
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

		// 
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

	public class GraphImpl extends CustomGraph {
		XYDotRenderer renderer = null;
		public final long timeInMillis;

		protected GraphImpl() {
			super(ownerTimestamp, 500, 200);
			timeInMillis = ownerTimestamp.getTimeInMillis();
		}

		@Override
		public void doPng(StaplerRequest req, StaplerResponse rsp) throws IOException {
			this.renderer = new ScatterPlotPointRenderer();
			super.doPng(req, rsp);
		}

		public synchronized String getMapString(String withBuildNo) {
			if ("true".equals(withBuildNo)) {
				if (graphMapWithBuildNo == null) {
					graphMapWithBuildNo = getMapString(withBuildNo, null);
				}
				return graphMapWithBuildNo;
			} else {
				if (graphMap == null) {
					graphMap = getMapString(withBuildNo, null);
				}
				return graphMap;
			}
		}

		private String getMapString(String withBuildNo, StaplerRequest req) {
			this.renderer = new ScatterPlotPointMapRenderer();
			ChartRenderingInfo info = new ChartRenderingInfo();
			render(req, info);
			Rectangle2D dataArea = ((ScatterPlotPointMapRenderer) this.renderer).dataArea;

			XYPlot xyPlot = this.graph.getXYPlot();
			ValueAxis domainAxis = xyPlot.getDomainAxis();
			ValueAxis rangeAxis = xyPlot.getRangeAxis();
			double rangeTickSize = dataArea.getHeight() / Constant.RANGE_AXIS_COUNT;
			double domainTickSize = dataArea.getWidth() / Constant.DOMAIN_AXIS_COUNT;
			RectangleEdge domainAxisEdge = xyPlot.getDomainAxisEdge();
			RectangleEdge rangeAxisEdge = xyPlot.getRangeAxisEdge();
			int buildNo = owner.number;
			List<?>[][] mapGridMatrix = createMapGridMatrix(getMethodInfoList());
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

		@Override
		protected JFreeChart createGraph() {
			final XYZDataset dataset = generateXYDataset();
			final JFreeChart chart = ChartFactory.createScatterPlot("", "coverage(%)", "complexity", dataset, PlotOrientation.VERTICAL, false, false,
					false);
			chart.setBackgroundPaint(Color.white);
			final XYPlot plot = chart.getXYPlot();
			plot.setBackgroundPaint(Color.WHITE);
			plot.setOutlinePaint(null);
			plot.setForegroundAlpha(0.4f);
			plot.setRangeGridlinesVisible(true);
			plot.setRangeGridlinePaint(Color.GRAY);
			plot.setRangeGridlinesVisible(true);
			plot.setDomainGridlinePaint(Color.GRAY);
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

	public void setOwner(AbstractBuild<?, ?> owner) {
		this.owner = owner;
	}

	private static XmlFile getDataFile(AbstractBuild<?, ?> build) {
		File dir = build == null ? new File(System.getProperty("java.io.tmpdir")) : build.getRootDir();
		return new XmlFile(new XStream2(), new File(dir, Constant.RESULT_FILENAME));
	}

	public static CovComplPlotTaget loadCloverScatterPlotTarget(AbstractBuild<?, ?> build) throws IOException {
		CovComplPlotTaget target = (CovComplPlotTaget) getDataFile(build).read();
		target.setOwner(build);
		return target;
	}

	public static void saveCloverScatterPlotTarget(AbstractBuild<?, ?> build, CovComplPlotTaget target) throws IOException {
		getDataFile(build).write(target);
	}

	public List<MethodInfo> getMethodInfoList() {
		return methodInfoList;
	}

}
