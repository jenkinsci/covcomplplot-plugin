package hudson.plugins.covcomplplot.util;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;

/**
 * Dummy renderer which extracts only dataarea size.
 * 
 * @author JunHo Yoon
 */
public class ScatterPlotPointMapRenderer extends XYDotRenderer {
	public Rectangle2D dataArea;
	/** UUID */
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jfree.chart.renderer.xy.AbstractXYItemRenderer#drawDomainMarker(java
	 * .awt.Graphics2D, org.jfree.chart.plot.XYPlot,
	 * org.jfree.chart.axis.ValueAxis, org.jfree.chart.plot.Marker,
	 * java.awt.geom.Rectangle2D)
	 */
	@Override
	public void drawDomainMarker(Graphics2D g2, XYPlot plot, ValueAxis domainAxis, Marker marker, Rectangle2D dataArea) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jfree.chart.renderer.xy.AbstractXYItemRenderer#drawAnnotations(java
	 * .awt.Graphics2D, java.awt.geom.Rectangle2D,
	 * org.jfree.chart.axis.ValueAxis, org.jfree.chart.axis.ValueAxis,
	 * org.jfree.ui.Layer, org.jfree.chart.plot.PlotRenderingInfo)
	 */
	@Override
	public void drawAnnotations(Graphics2D g2, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, Layer layer, PlotRenderingInfo info) {
		this.dataArea = dataArea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jfree.chart.renderer.xy.XYDotRenderer#drawItem(java.awt.Graphics2D,
	 * org.jfree.chart.renderer.xy.XYItemRendererState,
	 * java.awt.geom.Rectangle2D, org.jfree.chart.plot.PlotRenderingInfo,
	 * org.jfree.chart.plot.XYPlot, org.jfree.chart.axis.ValueAxis,
	 * org.jfree.chart.axis.ValueAxis, org.jfree.data.xy.XYDataset, int, int,
	 * org.jfree.chart.plot.CrosshairState, int)
	 */
	@Override
	public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis,
			ValueAxis rangeAxis, XYDataset prevDataset, int series, int item, CrosshairState crosshairState, int pass) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jfree.chart.renderer.xy.AbstractXYItemRenderer#drawItemLabel(java
	 * .awt.Graphics2D, org.jfree.chart.plot.PlotOrientation,
	 * org.jfree.data.xy.XYDataset, int, int, double, double, boolean)
	 */
	@Override
	protected void drawItemLabel(Graphics2D g2, PlotOrientation orientation, XYDataset dataset, int series, int item, double x, double y,
			boolean negative) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jfree.chart.renderer.xy.AbstractXYItemRenderer#drawRangeMarker(java
	 * .awt.Graphics2D, org.jfree.chart.plot.XYPlot,
	 * org.jfree.chart.axis.ValueAxis, org.jfree.chart.plot.Marker,
	 * java.awt.geom.Rectangle2D)
	 */
	@Override
	public void drawRangeMarker(Graphics2D g2, XYPlot plot, ValueAxis rangeAxis, Marker marker, Rectangle2D dataArea) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jfree.chart.renderer.xy.AbstractXYItemRenderer#drawDomainGridLine
	 * (java.awt.Graphics2D, org.jfree.chart.plot.XYPlot,
	 * org.jfree.chart.axis.ValueAxis, java.awt.geom.Rectangle2D, double)
	 */
	@Override
	public void drawDomainGridLine(Graphics2D g2, XYPlot plot, ValueAxis axis, Rectangle2D dataArea, double value) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.jfree.chart.renderer.xy.AbstractXYItemRenderer#drawRangeLine(java.awt.Graphics2D, org.jfree.chart.plot.XYPlot, org.jfree.chart.axis.ValueAxis, java.awt.geom.Rectangle2D, double, java.awt.Paint, java.awt.Stroke)
	 */
	@Override
	public void drawRangeLine(Graphics2D g2, XYPlot plot, ValueAxis axis, Rectangle2D dataArea, double value, Paint paint, Stroke stroke) {
		// do nothing
	}
}
