package hudson.plugins.covcomplplot.util;

import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class CustomGraph {
	protected final long timestamp;
	protected final int defaultW;
	protected final int defaultH;
	protected volatile JFreeChart graph;

	/**
	 * @param timestamp
	 *            Timestamp of this graph. Used for HTTP cache related headers.
	 *            If the graph doesn't have any timestamp to tie it to, pass -1.
	 */
	protected CustomGraph(long timestamp, int defaultW, int defaultH) {
		this.timestamp = timestamp;
		this.defaultW = defaultW;
		this.defaultH = defaultH;
	}

	protected CustomGraph(Calendar timestamp, int defaultW, int defaultH) {
		this(timestamp.getTimeInMillis(), defaultW, defaultH);
	}

	/**
	 * Actually render a chart.
	 */
	protected abstract JFreeChart createGraph();

	protected BufferedImage render(ChartRenderingInfo info) {
		if (graph == null)
			graph = createGraph();
		return graph.createBufferedImage(defaultW, defaultH, null);
	}

	protected BufferedImage render(StaplerRequest req, ChartRenderingInfo info) {
		String w = null;
		String h = null;
		if (req != null) {
			w = req.getParameter("width");
			if (w == null)
				w = String.valueOf(defaultW);
			h = req.getParameter("height");
			if (h == null)
				h = String.valueOf(defaultH);
		} else {
			w = String.valueOf(defaultW);
			h = String.valueOf(defaultH);
		}
		if (graph == null)
			graph = createGraph();
		return graph.createBufferedImage(Integer.parseInt(w), Integer.parseInt(h), null);
	}

	/**
	 * Renders a graph.
	 */
	public void doPng(StaplerRequest req, StaplerResponse rsp) throws IOException {
		if (req.checkIfModified(timestamp, rsp))
			return;

		try {
			BufferedImage image = render(req, null);
			rsp.setContentType("image/png");
			ServletOutputStream os = rsp.getOutputStream();
			ImageIO.write(image, "PNG", os);
			os.close();
		} catch (Error e) {

			if (e.getMessage().contains("Probable fatal error:No fonts found")) {
				rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
				return;
			}
			throw e; // otherwise let the caller deal with it
		} catch (HeadlessException e) {
			// not available. send out error message
			rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
		}
	}

}