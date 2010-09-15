package hudson.plugins.covcomplplot.analyzer;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.plugins.covcomplplot.model.MethodInfo;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectException;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectType;
import hudson.plugins.covcomplplot.stub.LoggerWrapper;
import hudson.plugins.covcomplplot.util.CovComplPlotUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;

/**
 * Abstract handler which is the superclass of Hudson plugin handlers.
 * 
 * @author junoyoon@gmail.com
 */
public abstract class AbstractMethodInfoHandler {

	/**
	 * Parse the Hudson plugin result and return the {@link MethodInfo} list.
	 * 
	 * @param build
	 *            build info
	 * @param excludeGetterSetter
	 *            whether getter/setter methods are included or not.
	 * @param rootDir
	 *            base directory of build.
	 * @param logger
	 *            logger to be used
	 * @param analyzer
	 *            information which analyzer is used.
	 * @return {@link MethodInfo} list parsed.
	 * @throws InvalidHudsonProjectException
	 */

	public abstract List<MethodInfo> process(AbstractBuild<?, ?> build, boolean excludeGetterSetter, String rootDir, LoggerWrapper logger,
			Analyzer analyzer) throws InvalidHudsonProjectException;

	/**
	 * Get build xml artifact
	 * 
	 * @param build
	 *            build
	 * @param fileName
	 *            file to be retrieve
	 * @param forWhat
	 *            analyzer enum to be used for logging.
	 * @return xml document
	 * @throws InvalidHudsonProjectException
	 */
	public Document getBuildArtifact(AbstractBuild<?, ?> build, String fileName, Analyzer forWhat) throws InvalidHudsonProjectException {
		Document document = null;
		File artifactFile = new File(build.getRootDir(), fileName);
		InputStream is = null;
		try {
			is = new FileInputStream(artifactFile);
			document = CovComplPlotUtil.getXmlFileDocument(is);
		} catch (Exception e) {
			throw new InvalidHudsonProjectException(InvalidHudsonProjectType.INVALID_PLUGIN_RESULT, e, forWhat.getPluginName());
		} finally {
			IOUtils.closeQuietly(is);
		}
		return document;
	}

	/**
	 * Check if the method is valid.
	 * 
	 * @param method
	 *            method to checked. if it's null, it's valid.
	 * @param excludeGetterSetter
	 *            true if the getter/setter should be excluded.
	 * @return true if the method is valid
	 */
	protected boolean isMethodValid(MethodInfo method, boolean excludeGetterSetter) {
		if (method == null) {
			return true;
		}

		if (method.st == 0) {
			return false;
		}

		if (excludeGetterSetter) {
			return !isGetterSetter(method);
		}
		return true;
	}

	/**
	 * Check if the give method is valid
	 * 
	 * @param method
	 *            method to be checked
	 * @return true if given method is valid
	 */
	protected boolean isGetterSetter(MethodInfo method) {
		if (method.st == 1 && method.getCompl() == 1) {
			return StringUtils.startsWithIgnoreCase(method.getSig(), "get") || StringUtils.startsWithIgnoreCase(method.getSig(), "set");
		}
		return false;
	}

	public String getCustomJavaScript() {
		return "";
	}

	/**
	 * Get method content URL. Each handler should implements this method to get
	 * the appropriate URL for each method source code.
	 * 
	 * @param build
	 *            {@link AbstractBuild} instance against each methodInfo.
	 * @param methodInfo
	 *            method.
	 * @return URL string from each hudson job URL base.
	 */
	abstract public String getMethodUrlLocation(AbstractBuild<?, ?> build, MethodInfo methodInfo);

	/**
	 * Check if passed {@link AbstractBuild} contains valid hudson plugin result
	 * for this handler processing. This method is invoked before the process
	 * method is called.
	 * 
	 * @param build
	 * @throws InvalidHudsonProjectException
	 */
	abstract public void checkBuild(AbstractBuild<?, ?> build) throws InvalidHudsonProjectException;

	/**
	 * Get the description of Hudson plugin from which this handler get information
	 * 
	 * @return description string
	 */
	abstract public String getDescription();

	protected void checkBuildContainningBuildAction(AbstractBuild<?, ?> build, String buildActionName) throws InvalidHudsonProjectException {
		for (Action eachAction : build.getActions()) {
			if (buildActionName.equals(eachAction.getUrlName())) {
				return;
			}
		}
		throw new InvalidHudsonProjectException(InvalidHudsonProjectType.INVALID_PLUGIN_RESULT, Analyzer.Clover);
	}

	public Action getCustomSourceViewBuildAction(AbstractBuild<?, ?> build) {
		return null;
	}
}
