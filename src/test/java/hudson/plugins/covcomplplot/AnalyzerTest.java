package hudson.plugins.covcomplplot;

import hudson.plugins.covcomplplot.analyzer.Analyzer;

import org.junit.Test;

public class AnalyzerTest {
	@Test
	public void testAnalyzerJavascript() {
		System.out.println(Analyzer.Clover.getHandler().getCustomJavaScript());
	}
}
