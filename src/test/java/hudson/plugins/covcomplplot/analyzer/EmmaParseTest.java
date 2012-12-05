package hudson.plugins.covcomplplot.analyzer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import hudson.model.AbstractBuild;
import hudson.plugins.covcomplplot.model.MethodInfo;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectException;
import hudson.plugins.covcomplplot.util.CovComplPlotUtil;

import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class EmmaParseTest {
	@Test
	public void testMethodInfo() {
		EmmaMethodHandler handler = new EmmaMethodHandler();
		MethodInfo methodInfo = new MethodInfo("hello", "world", 0, 0);
		handler.setCoverage(methodInfo, "2%   (3/199)");
		handler.setBlockCount(methodInfo, "2%   (3/199)");
		assertThat(methodInfo.getCoverageRatio(), is((3 / 199f) * 100));
		assertThat(methodInfo.getCompl(), is(20));
	}

	@Test
	public void testEmmaParse() throws InvalidHudsonProjectException {
		EmmaMethodHandler handler = new EmmaMethodHandler() {
			@Override
			protected Document getEmmaReport(AbstractBuild<?, ?> build) throws InvalidHudsonProjectException {
				try {
					return CovComplPlotUtil.getXmlFileDocument(new ClassPathResource(
							"/sample_valid/builds/1/emma/coverage.xml").getInputStream());
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected String getProjectUrl(AbstractBuild<?, ?> build) {
				return "http://localhost/helloworld/";
			}

			@Override
			protected boolean hasFolder(AbstractBuild<?, ?> build, String path) {
				return true;
			}

			@Override
			protected String getModuleRoot(AbstractBuild<?, ?> build) {
				return "trunk";
			}
		};
		MethodInfo last = null;
		for (MethodInfo each : handler.process(null, false, null, null, Analyzer.Emma)) {
			assertThat(each.getSig(), CoreMatchers.notNullValue());
			assertThat(each.getCompl(), CoreMatchers.not(0));
			assertThat(each.getCoverageRatio() > 100, is(false));
			last = each;
		}
		String methodUrlLocation = handler.getMethodUrlLocation(null, last);
		assertThat(methodUrlLocation, is("http://localhost/helloworld/ws/trunk/src/main/java/" + last.getPath()
				+ "/*view*/"));

	}
}
