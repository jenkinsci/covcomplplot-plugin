package hudson.plugins.covcomplplot.util;

import hudson.util.Graph;

import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Reimplementation of {@link Graph} class in hudson lib. lazyMap logic on which
 * {@link Graph} class is based is not properly working in IE. Related doMap()
 * method in the class is removed.
 * 
 * @author JunHo Yoon
 */
public abstract class CustomGraph {
	protected final long timestamp;
	protected final int defaultW;
	protected final int defaultH;
	protected volatile JFreeChart graph;

	/**
	 * Constructor
	 * 
	 * @param timestamp
	 *            Timestamp of this graph. Used for HTTP cache related headers.
	 *            If the graph doesn't have any timestamp to tie it to, pass -1.
	 * @param defaultW
	 *            default width
	 * @param defaultH
	 *            default height
	 */
	protected CustomGraph(long timestamp, int defaultW, int defaultH) {
		this.timestamp = timestamp;
		this.defaultW = defaultW;
		this.defaultH = defaultH;
	}

	/**
	 * Constructor
	 * 
	 * @param timestamp
	 *            Timestamp of this graph. Used for HTTP cache related headers.
	 * @param defaultW
	 *            default width
	 * @param defaultH
	 *            default height
	 */
	protected CustomGraph(Calendar timestamp, int defaultW, int defaultH) {
		this(timestamp.getTimeInMillis(), defaultW, defaultH);
	}

	/**
	 * Prepare {@link JFreeChart} instance to render image
	 * 
	 * @return chart prepared {@link JFreeChart} instance
	 */
	protected abstract JFreeChart createGraph();

	/**
	 * Actually render a chart.
	 * 
	 * @param req
	 *            the {@link StaplerRequest} instance from which the width and
	 *            height attributes are extracted.
	 */
	protected BufferedImage render(StaplerRequest req) {
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
	 * 
	 * @param req
	 *            request from which the some attribute is extracted (width /
	 *            height)
	 * @param rsp
	 *            response to which the image is written.
	 * @throws IOException
	 *             occurs when there is IO problems
	 */
	public void doPng(StaplerRequest req, StaplerResponse rsp) throws IOException {
		if (req.checkIfModified(timestamp, rsp))
			return;

		try {
			BufferedImage image = render(req);
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