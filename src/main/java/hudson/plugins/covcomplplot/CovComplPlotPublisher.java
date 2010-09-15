package hudson.plugins.covcomplplot;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.plugins.covcomplplot.analyzer.Analyzer;
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
 * {@link CovComplPlotPublisher} is the main class for this plugin.
 * It's a subclass of {@link Notifier} so that {@link CovComplPlotPublisher} should be executed 
 * after corresponding coverage plugin is performed.
 */
public class CovComplPlotPublisher extends Notifier {
	/** Analyzer to be used in this instance. */
	public final Analyzer analyzer;
	/** Verbose logging mode */
	public final boolean verbose;
	/** Exclude the getter / setter methods */
	public final boolean excludeGetterSetter;
	/** locate graph topmost of the page */
	private boolean locateTopMost = true;
	/**
	 * Constructor
	 * @param analyzer analyzer to be used
	 * @param excludeGetterSetter true if getter/setter methods are excluded.
	 * @param verbose true if verbose logging mode is on
	 */
	@DataBoundConstructor
	public CovComplPlotPublisher(Analyzer analyzer, boolean excludeGetterSetter, boolean verbose, boolean locateTopMost) {
		this.analyzer = analyzer;
		this.excludeGetterSetter = excludeGetterSetter;
		this.verbose = verbose;
		this.setLocateTopMost(locateTopMost);
	}

	/* (non-Javadoc)
	 * @see hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener)
	 */
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

		LoggerWrapper logger = getLoggerWrapper(listener);
		try {
			logger.println("Collecting Data...");
			// Firstly check if this build contains appropriate data for analyzer.
			try {
				this.analyzer.getHandler().checkBuild(build);
			} catch (InvalidHudsonProjectException e) {
				logger.printError(e.getLogMessage());
				return true;
			}

			FilePath filePath = build.getModuleRoot();
			String remoteDir = FilenameUtils.normalize(filePath.getRemote());
			
			// Get MethodInfo list 
			List<MethodInfo> methods = getCovComplMethodInfoList(this.analyzer, build, excludeGetterSetter, remoteDir, logger);
			logger.println("Build CovComplPlotBuildAction...");
			
			// Create BuildAction
			CovComplPlotBuildAction buildAction = createCovComplScatterPlotBuildAction(build, methods);
			build.addAction(buildAction);
			Action customAction = analyzer.getHandler().getCustomSourceViewBuildAction(build);
			if (customAction != null) {
				build.addAction(customAction);
			}
			logger.println("Complete CovCompPlotPlugIn.");
		} catch (InvalidHudsonProjectException e) {
			logger.printError(e.getLogMessage());
			logger.printStackTrace(e);
		} catch (Exception e) {
			logger.printError(e.toString());
			logger.printStackTrace(e);
		}

		return true;
	}

	/**
	 * Create {@link CovComplPlotBuildAction}
	 * 
	 * @param build
	 *            Current {@link AbstractBuild} instance.
	 *         
	 * @param methods
	 *            {@link MethodInfo} list which will be contained in
	 *            {@link CovComplPlotBuildAction} instance.
	 * @return {@link CovComplPlotBuildAction}
	 * @throws IOException
	 */
	public CovComplPlotBuildAction createCovComplScatterPlotBuildAction(AbstractBuild<?, ?> build, List<MethodInfo> methods) throws IOException {
		CovComplPlotTaget cloverScatterPlotTaget = new CovComplPlotTaget(build, methods, analyzer, build.getTimestamp());
		CovComplPlotTaget.saveCovComplScatterPlotTarget(cloverScatterPlotTaget);
		CovComplPlotBuildAction buildAction = new CovComplPlotBuildAction(build, cloverScatterPlotTaget);
		return buildAction;
	}

	/**
	 * Create {@link MethodInfo} list using given analyzer.
	 * 
	 * @param analyzer
	 *            analyzer
	 * @param build
	 *            current build
	 * @param excludeGetterSetter
	 *            true if getter/setter methods are excluded.
	 * @param rootDir
	 * @param logger
	 *            logger
	 * @return {@link MethodInfo} list
	 * @throws InvalidHudsonProjectException
	 *             occurs when the data extracting is failed by some problem in
	 *             project.
	 */
	public List<MethodInfo> getCovComplMethodInfoList(Analyzer analyzer, AbstractBuild<?, ?> build, boolean excludeGetterSetter, String rootDir,
			LoggerWrapper logger) throws InvalidHudsonProjectException {
		List<MethodInfo> methods = analyzer.getHandler().process(build, excludeGetterSetter, rootDir, logger, analyzer);
		if (methods.size() == 0) {
			throw new InvalidHudsonProjectException(InvalidHudsonProjectType.INTERNAL, "Method size is 0.");
		}
		return methods;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * hudson.tasks.BuildStepCompatibilityLayer#getProjectAction(hudson.model
	 * .AbstractProject)
	 */
	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return new CovComplPlotProjectAction(project);
	}

	/**
	 * Get {@link LoggerWrapper} instance from build listener.<br/>
	 * This method is a subject to be overridden for unit test
	 * 
	 * @param listener
	 *            listener from which the logger is extracted.
	 * @return {@link LoggerWrapper} instance
	 */
	protected LoggerWrapper getLoggerWrapper(BuildListener listener) {
		return new LoggerWrapper(listener.getLogger(), verbose);
	}

	/* (non-Javadoc)
	 * @see hudson.tasks.Notifier#getDescriptor()
	 */
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Descriptor for {@link CovComplPlotPublisher}. 
	 * Used as a singleton. The
	 * class is marked as public so that it can be accessed from views.
	 */
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		/* (non-Javadoc)
		 * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
		 */
		@SuppressWarnings("unchecked")
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}
		
		/* (non-Javadoc)
		 * @see hudson.model.Descriptor#getDisplayName()
		 */
		public String getDisplayName() {
			return "Publish Coverage / Complexity Scatter Plot";
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.tasks.BuildStep#getRequiredMonitorService()
	 */
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	/**
	 * Get analyzer
	 * 
	 * @return analyzer
	 */
	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setLocateTopMost(boolean locateTopMost) {
		this.locateTopMost = locateTopMost;
	}

	public boolean isLocateTopMost() {
		return locateTopMost;
	}
}
