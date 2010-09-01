package hudson.plugins.covcomplplot;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.when;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.plugins.covcomplplot.CovComplPlotTaget.GraphImpl;
import hudson.plugins.covcomplplot.annalyzer.Analyzer;
import hudson.plugins.covcomplplot.annalyzer.CloverMethodHandler;
import hudson.plugins.covcomplplot.model.MethodInfo;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectException;
import hudson.plugins.covcomplplot.stub.LoggerWrapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { FreeStyleBuild.class, BuildListener.class })
@PowerMockIgnore("javax.*")
public class CloverParseCommandTest {

	class AddAction implements Answer<CovComplPlotBuildAction> {
		CovComplPlotBuildAction action;

		public CovComplPlotBuildAction answer(InvocationOnMock invocation) throws Throwable {
			Object[] args = invocation.getArguments();
			action = ((CovComplPlotBuildAction) args[0]);
			return action;
		}
	}

	@Mock
	FreeStyleBuild mockBuild;

	@Mock
	FreeStyleBuild prevBuildWithoutAction;

	@Mock
	FreeStyleBuild prevBuildWithAction;

	@Mock
	BuildListener mockListener;

	@Mock
	StaplerRequest mockStReq;

	@Mock
	StaplerResponse mockStRsp;

	@Mock
	FreeStyleProject mockProject;

	/**
	 * 정상적인 프로젝트를 사용하여 그래프가 잘 그려지는지 확인한다.
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws UnsupportedLookAndFeelException
	 */
	@Test
	public void testCloverWithValidProject() throws InterruptedException, IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		CovComplPlotPublisher publisher = new CovComplPlotPublisher(Analyzer.Clover, false, true);
		AddAction addAction = prepareValidSample1("src/test/resources/sample_valid/", 500, 200);

		publisher.perform((AbstractBuild<?, ?>) mockBuild, null, mockListener);
		CovComplPlotTaget cloverScatterPlotTaget = (CovComplPlotTaget) addAction.action.getTarget();
		assertThat(addAction.action, is(CovComplPlotBuildAction.class));

		GraphImpl graph = cloverScatterPlotTaget.getGraph();
		final FileOutputStream fileOutputStream = new FileOutputStream("a.png");
		when(mockStRsp.getOutputStream()).thenReturn(new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				fileOutputStream.write(b);
			}
		});
		graph.doPng(mockStReq, mockStRsp);
		fileOutputStream.flush();
		GraphImpl graph2 = cloverScatterPlotTaget.getGraph();
		assertThat(graph2.getMapString("false"), containsString("area shape"));
		assertThat(new File("a.png").length(), not(is((long) 0)));
	}

	/**
	 * Sample1 Mock 을 준비
	 * 
	 * @param moduleRoot
	 * @param width
	 * @param height
	 * @return
	 */
	private AddAction prepareValidSample1(String moduleRoot, int width, int height) {
		when(mockBuild.getModuleRoot()).thenReturn(new FilePath(new File(moduleRoot)));
		when(mockBuild.getRootDir()).thenReturn(new File(moduleRoot + "builds/1"));
		when(mockBuild.getTimestamp()).thenReturn(Calendar.getInstance());
		when(mockBuild.getActions()).thenReturn(new ArrayList<Action>() {
			{
				add(new Action() {

					public String getUrlName() {
						return "clover";
					}

					public String getIconFileName() {
						return null;
					}

					public String getDisplayName() {
						return null;
					}
				});
			}
		});
		AddAction addAction = new AddAction();
		doAnswer(addAction).when(mockBuild).addAction(org.mockito.Matchers.any(Action.class));
		when(mockListener.getLogger()).thenReturn(System.out);
		when(mockStReq.getParameter("width")).thenReturn(String.valueOf(width));
		when(mockStReq.getParameter("height")).thenReturn(String.valueOf(height));
		when(mockBuild.getUrl()).thenReturn("http://localhost/test/1");
		return addAction;
	}

	@Test
	public void testCloverLink() throws InvalidHudsonProjectException {
		AddAction addAction = prepareValidSample1("src/test/resources/sample_valid/", 500, 200);
		CloverMethodHandler handler = new CloverMethodHandler();
		List<MethodInfo> t = handler.process(mockBuild, false, "", new LoggerWrapper(System.out), Analyzer.Clover);
		for (MethodInfo each : t) {
			assertThat(Analyzer.Clover.getHandler().getMethodUrlLocation(mockBuild, each), containsString("http://localhost/test/1/clover-report/"
					+ each.path.substring(0, each.path.length() - 10)));
		}
	}

	@Test
	public void testPlotWithOneElement() throws IOException {
		List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
		MethodInfo methodInfo = new MethodInfo("", "test", 1, 10);

		methodInfoList.add(methodInfo);
		methodInfo.increaseLine(false);
		MethodInfo methodInfo2 = new MethodInfo("", "test", 1, 10);
		methodInfoList.add(methodInfo2);
		methodInfo2.increaseLine(false);

		CovComplPlotTaget cloverScatterPlotTaget = new CovComplPlotTaget(mockBuild, methodInfoList, Analyzer.Clover, Calendar.getInstance());
		GraphImpl graph = cloverScatterPlotTaget.getGraph();

		final FileOutputStream fileOutputStream = new FileOutputStream("d:\\a2.png");
		when(mockStRsp.getOutputStream()).thenReturn(new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				fileOutputStream.write(b);
			}
		});
		graph.doPng(mockStReq, mockStRsp);
		GraphImpl graph2 = cloverScatterPlotTaget.getGraph();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter systemOuputWriter = new PrintWriter(bos);
		when(mockStRsp.getWriter()).thenReturn(systemOuputWriter);
		assertEquals(3, graph2.getMapString("false").split("\n").length);
	}

	Action action;

	public void setAction(Action action) {
		this.action = action;
	}

	@Test
	public void testPlotWithOneElement2() throws IOException {
		CovComplPlotPublisher publisher = new CovComplPlotPublisher(Analyzer.Clover, true, false);
		when(mockBuild.getModuleRoot()).thenReturn(new FilePath(new File("src/test/resources/sample_valid/")));
		AddAction addAction = new AddAction();
		doAnswer(addAction).when(mockBuild).addAction(org.mockito.Matchers.any(Action.class));
		when(mockListener.getLogger()).thenReturn(System.out);
		publisher.perform((AbstractBuild<?, ?>) mockBuild, null, mockListener);
	}

	@Test
	public void testPrevBuildRetrival() {
		preparePrevBuildMock();
		CovComplPlotProjectAction action = new CovComplPlotProjectAction(mockProject);
		assertThat(action.getLastBuildAction().getOwner().getId(), is("33"));
	}

	@Test
	public void testPrevBuildRetrivalUntilEverythingFailed() {
		preparePrevBuildMock();
		when(prevBuildWithoutAction.getPreviousNotFailedBuild()).thenReturn(null);
		CovComplPlotProjectAction action = new CovComplPlotProjectAction(mockProject);
		assertEquals(action.getLastBuildAction(), null);
	}

	void preparePrevBuildMock() {
		when(prevBuildWithoutAction.getAction(CovComplPlotBuildAction.class)).thenReturn(null);
		when(prevBuildWithAction.getAction(CovComplPlotBuildAction.class)).thenReturn(new CovComplPlotBuildAction(prevBuildWithAction, null));
		when(prevBuildWithAction.getId()).thenReturn("33");
		when(mockProject.getLastCompletedBuild()).thenReturn(prevBuildWithoutAction);
		when(prevBuildWithoutAction.getPreviousNotFailedBuild()).thenReturn(prevBuildWithAction);
	}

	@Test
	public void testCloverPathConversion() {
		when(mockBuild.getUrl()).thenReturn("wewe");
		System.out.println(Analyzer.Clover.getHandler().getMethodUrlLocation(mockBuild, new MethodInfo("ewe/ewew/ss.java", "aaa", 1, 1)));
		;
	}

	@Test
	public void testExclusionGetterSetter() throws InvalidHudsonProjectException {
		AddAction addAction = prepareValidSample1("src/test/resources/sample_valid/", 500, 200);
		CloverMethodHandler handler = new CloverMethodHandler();
		List<MethodInfo> methodInfoList = handler.process(mockBuild, true, "", new LoggerWrapper(System.out), Analyzer.Clover);
		for (MethodInfo methodInfo : methodInfoList) {
			if (methodInfo.compl == 1 && methodInfo.st == 1) {
				assertThat(methodInfo.sig, not(startsWith("get")));
				assertThat(methodInfo.sig, not(startsWith("set")));
			}
		}
	}

	@Test
	public void testInclusionGetterSetter() throws InvalidHudsonProjectException {
		AddAction addAction = prepareValidSample1("src/test/resources/sample_valid/", 500, 200);
		CloverMethodHandler handler = new CloverMethodHandler();
		List<MethodInfo> methodInfoList = handler.process(mockBuild, false, "", new LoggerWrapper(System.out), Analyzer.Clover);
		int count = 0;
		for (MethodInfo methodInfo : methodInfoList) {
			if (methodInfo.compl == 1 && methodInfo.st == 1) {
				if (StringUtils.startsWithIgnoreCase(methodInfo.sig, "get") ||StringUtils.startsWithIgnoreCase(methodInfo.sig, "set")) {
					count++;
				}
			}
		}
		assertThat(count, not(0));
	}
}
