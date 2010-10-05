package hudson.plugins.covcomplplot;

import hudson.plugins.covcomplplot.model.MethodInfo;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

public class CloverBranchParseTest {
	@Test
	public void testMethodInfo() {
		MethodInfo info = new MethodInfo("wew", "ew", 1, 11);
		info.covered = false;
		assertThat(info.getFormattedCoverageRatio(), is("0"));
		info.covered = true;
		assertThat(info.getFormattedCoverageRatio(), is("100"));
	}
}
