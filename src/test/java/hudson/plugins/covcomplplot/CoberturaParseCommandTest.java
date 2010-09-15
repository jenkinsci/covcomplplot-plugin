package hudson.plugins.covcomplplot;

import java.io.File;
import java.util.List;

import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.plugins.covcomplplot.analyzer.Analyzer;
import hudson.plugins.covcomplplot.analyzer.CoberturaMethodHandler;
import hudson.plugins.covcomplplot.model.MethodInfo;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectException;
import hudson.plugins.covcomplplot.stub.LoggerWrapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith; 

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { FreeStyleBuild.class, BuildListener.class })
@PowerMockIgnore("javax.*")
public class CoberturaParseCommandTest {

	@Mock
	public FreeStyleBuild mockBuild;

	@Test
	public void testCoberturaParseCommand() throws InvalidHudsonProjectException {
		when(mockBuild.getRootDir()).thenReturn(new File("src/test/resources/sample_valid/builds/1"));
		CoberturaMethodHandler handler = new CoberturaMethodHandler();
		List<MethodInfo> process = handler.process(mockBuild, false, "", new LoggerWrapper(System.out), Analyzer.Cobertura);
		for (MethodInfo methodInfo : process) {
			assertThat(methodInfo.sig, not(""));
			assertThat(methodInfo.compl, not(0));
			assertThat(methodInfo.st, not(0));
			assertThat(methodInfo.path, not(""));
			System.out.println(methodInfo);
		}
	}

	@Test
	public void testCoberturaBranch() {
		CoberturaMethodHandler handler = new CoberturaMethodHandler();
		assertThat(handler.getBranchCount("50% (1/2)"), is(1));
		assertThat(handler.getBranchCount("33.22% (1/4)"), is(2));

	}
	
	@Test
	public void testArgumentTransform() {
		
	}
	
	@Test
	public void testParsePath() {
		when(mockBuild.getUrl()).thenReturn("10/url");
		CoberturaMethodHandler handler = new CoberturaMethodHandler();
		MethodInfo methodInfo = new MethodInfo("hudson/plugins/covcomplplot/util/ScatterPlotPointMapRenderer.java", "", 0, 0, 0, 0);
		System.out.println(handler.getMethodUrlLocation(mockBuild, methodInfo));;

	}
}
