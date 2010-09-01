package hudson.plugins.covcomplplot;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.plugins.covcomplplot.annalyzer.Analyzer;
import hudson.plugins.covcomplplot.model.MethodInfo;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectException;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectType;
import hudson.plugins.covcomplplot.stub.LoggerWrapper;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 */
public class CovComplPlotPublisher extends Notifier {

	public final Analyzer analyzer;
	public final boolean verbose;
	public final boolean excludeGetterSetter;

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public CovComplPlotPublisher(Analyzer analyzer, boolean excludeGetterSetter, boolean verbose) {
		this.analyzer = analyzer;
		this.excludeGetterSetter = excludeGetterSetter;
		this.verbose = verbose;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

		LoggerWrapper logger = getLoggerWrapper(listener);
		try {
			logger.println("Collecting Data...");
			FilePath filePath = build.getModuleRoot();
			try {
				this.analyzer.getHandler().checkBuild(build);
			} catch (InvalidHudsonProjectException e) {
				logger.printError(e.getScreenMessage());
				return true;
			}
			String remoteDir = FilenameUtils.normalize(filePath.getRemote());
			List<MethodInfo> methods = getCovComplMethodInfoList(this.analyzer, build, excludeGetterSetter, remoteDir, logger);
			logger.println("Build CovComplPlotBuildAction...");
			CovComplPlotBuildAction buildAction = createCovComplScatterPlotBuildAction(build, methods);
			build.addAction(buildAction);
			logger.println("Complete CovCompPlotPlugIn.");
		} catch (Exception e) {
			logger.printError(e.toString());
			logger.printStackTrace(e);
		}

		return true;
	}

	/**
	 * Build Action 만든다.
	 * 
	 * @param build
	 * @param methods
	 * @return
	 * @throws IOException
	 */
	public CovComplPlotBuildAction createCovComplScatterPlotBuildAction(AbstractBuild<?, ?> build, List<MethodInfo> methods) throws IOException {
		CovComplPlotTaget cloverScatterPlotTaget = new CovComplPlotTaget(build, methods, analyzer, build.getTimestamp());
		CovComplPlotTaget.saveCloverScatterPlotTarget(build, cloverScatterPlotTaget);
		CovComplPlotBuildAction buildAction = new CovComplPlotBuildAction(build, cloverScatterPlotTaget);
		return buildAction;
	}

	/**
	 * Clover 에서 MethodInfo 객체 리스트를 가져온다.
	 * @param excludeGetterSetter2 
	 * 
	 * @param analyzer2
	 * 
	 * @param hudsonProject
	 * @param remoteDir
	 * @param logger
	 * @return
	 * @throws InvalidHudsonProjectException
	 */
	public List<MethodInfo> getCovComplMethodInfoList(Analyzer analyzer, AbstractBuild<?, ?> build, boolean excludeGetterSetter, String remoteDir, LoggerWrapper logger)
			throws InvalidHudsonProjectException {
		List<MethodInfo> methods = analyzer.getHandler().process(build, excludeGetterSetter, remoteDir, logger, analyzer);
		if (methods.size() == 0) {
			throw new InvalidHudsonProjectException(InvalidHudsonProjectType.INTERNAL, "모든 플러그인 처리기를 사용하더라도 처리할 수 없습니다.");
		}
		return methods;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return new CovComplPlotProjectAction(project);
	}

	protected void addBuildAction(AbstractBuild<?, ?> build, CovComplPlotBuildAction buildAction) {

	}

	protected LoggerWrapper getLoggerWrapper(BuildListener listener) {
		return new LoggerWrapper(listener.getLogger(), verbose);
	}

	// overrided for better type safety.
	// if your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Descriptor for {@link CovComplPlotPublisher}. Used as a singleton. The
	 * class is marked as public so that it can be accessed from views.
	 * 
	 * <p>
	 * See <tt>views/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension
	// this marker indicates Hudson that this is an implementation of an
	// extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		@SuppressWarnings("unchecked")
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Publish Coverage / Complexity Scatter Plot";
		}

	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}
}
