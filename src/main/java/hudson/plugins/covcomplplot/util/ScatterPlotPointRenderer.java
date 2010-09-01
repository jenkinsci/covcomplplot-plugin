package hudson.plugins.covcomplplot.util;

import hudson.plugins.covcomplplot.Constant;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;

/**
 * Bubble chart renderer.
 * 
 * @author junoyoon@gmail.com
 */
public class ScatterPlotPointRenderer extends XYDotRenderer {
	private static final long serialVersionUID = 1L;

	public int calcPointSize(int size) {
		return Math.max((int) Math.sqrt(Constant.GRAPH_POINT_SIZE * size), 3);
	}

	public int getDotHeight(int size) {
		return calcPointSize(size);
	}

	public int getDotWidth(int size) {
		return calcPointSize(size);
	}

	@Override
	public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis,
			ValueAxis rangeAxis, XYDataset prevDataset, int series, int item, CrosshairState crosshairState, int pass) {
		// get the data point...
		XYZDataset dataset = (XYZDataset) prevDataset;
		double x = dataset.getXValue(series, item);
		double y = dataset.getYValue(series, item);
		int z = ((int) dataset.getZValue(series, item));
		int dotSize = getDotHeight(z);
		double adjx = ((double)dotSize) / 2.0;
		double adjy = ((double)dotSize) / 2.0;
		if (!Double.isNaN(y)) {
			RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
			RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
			if (y >= Constant.RANGE_AXIS_UPPERBOUND - 1) {
				g2.setPaint(Color.RED);		
				dotSize += 3;
				dotSize += 3;
			} else {
				g2.setPaint(getItemPaint(series, item));
			}
			double transX = domainAxis.valueToJava2D(x, dataArea, xAxisLocation) - adjx + 2;
			double transY = rangeAxis.valueToJava2D(y, dataArea, yAxisLocation) - adjy;

			PlotOrientation orientation = plot.getOrientation();
			if (orientation == PlotOrientation.HORIZONTAL) {
				g2.fillOval((int) transY, (int) transX, dotSize, dotSize);
			} else if (orientation == PlotOrientation.VERTICAL) {
				g2.fillOval((int) transX, (int) transY, dotSize, dotSize);
			}

			int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
			int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
			updateCrosshairValues(crosshairState, x, y, domainAxisIndex, rangeAxisIndex, transX, transY, orientation);
		}
	}
}
