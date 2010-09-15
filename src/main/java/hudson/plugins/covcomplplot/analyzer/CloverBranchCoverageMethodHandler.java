package hudson.plugins.covcomplplot.analyzer;

import hudson.model.AbstractBuild;
import hudson.plugins.covcomplplot.model.MethodInfo;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectException;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectType;
import hudson.plugins.covcomplplot.stub.LoggerWrapper;
import hudson.plugins.covcomplplot.util.CovComplPlotUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Clover result handler. This class is responsible to read the clover result
 * and make the {@link MethodInfo} list. In addition, some clover specific
 * actions.
 * 
 * @author JunHo Yoon
 */
public class CloverBranchCoverageMethodHandler extends AbstractMethodInfoHandler {

	@SuppressWarnings("unchecked")
	@Override
	public List<MethodInfo> process(AbstractBuild<?, ?> build, boolean excludeGetterSetter, String remoteDir, LoggerWrapper logger, Analyzer analyzer)
			throws InvalidHudsonProjectException {
		Document clover = super.getBuildArtifact(build, "clover.xml", Analyzer.Clover);
		List<Element> elementList = null;
		Element eachElement = clover.getRootElement();
		try {
			if (CovComplPlotUtil.compareVersion(eachElement.attributeValue("clover"), "3.0.0") < 0) {
				throw new InvalidHudsonProjectException(InvalidHudsonProjectType.INTERNAL, "Clover version should be over 3.0.0.");
			}
			eachElement = eachElement.element("project");
			elementList = (List<Element>) eachElement.elements("package");
		} catch (Exception e) {
			throw new InvalidHudsonProjectException(InvalidHudsonProjectType.INTERNAL, "clover.xml doesn't contain valid result.");
		}
		ArrayList<MethodInfo> methods = new ArrayList<MethodInfo>();

		for (Element eachPackage : elementList) {
			String dirPath = eachPackage.attributeValue("name");
			for (Object eachFileObject : eachPackage.elements("file")) {
				Element eachFileElement = (Element) eachFileObject;
				String path = dirPath.replace(".", "/") + "/" + eachFileElement.attributeValue("name");
				MethodInfo cloverMethod = null;
				boolean methodCalled = false;
				for (Object each : eachFileElement.elements("line")) {
					Element eachLine = (Element) each;
					String eachType = eachLine.attributeValue("type");
					if ("method".equals(eachType)) {
						// Remove the invalid method
						// I put this here to minimize the array search and
						// array memory reallocation.
						if (!isMethodValid(cloverMethod, excludeGetterSetter)) {
							methods.remove(methods.size() - 1);
						} else {
							if (cloverMethod != null) {
								cloverMethod.covered = methodCalled;
							}
						}
						methodCalled = !("0".equals(eachLine.attributeValue("count")));
						String signature = eachLine.attributeValue("signature");
						int complexity = Integer.parseInt(eachLine.attributeValue("complexity"));
						int lineno = Integer.parseInt(eachLine.attributeValue("num"));
						cloverMethod = new MethodInfo(path, signature, complexity, lineno);

						methods.add(cloverMethod);
					} else if (("cond").equals(eachType) && cloverMethod != null) {
						int count = ("0".equals(eachLine.attributeValue("truecount")) ? 0 : 1);
						count += ("0".equals(eachLine.attributeValue("falsecount")) ? 0 : 1);
						cloverMethod.increaseSizeAndCovered(2, count);
					}
				}

				// Remove last method of this file if it's not valid.
				// I put this here to minimize the array search and array memory
				// reallocation.
				if (methods.size() > 0) {
					MethodInfo lastMehodInfo = methods.get(methods.size() - 1);
					if (!isMethodValid(lastMehodInfo, excludeGetterSetter)) {
						methods.remove(methods.size() - 1);
					}
				}
			}
		}

		return methods;
	}

	/**
	 * Check if the give method is valid
	 * 
	 * @param method
	 *            method to be checked
	 * @return true if given method is valid
	 */
	protected boolean isGetterSetter(MethodInfo method) {
		if (method.getCompl() == 1) {
			return StringUtils.startsWithIgnoreCase(method.getSig(), "get") || StringUtils.startsWithIgnoreCase(method.getSig(), "set");
		}
		return false;
	}

	@Override
	public String getCustomJavaScript() {
		return CovComplPlotUtil.getFileAsString(CovComplPlotUtil.getClassResourcePath(getClass(), "js"));
	}

	@Override
	public String getMethodUrlLocation(AbstractBuild<?, ?> owner, MethodInfo methodInfo) {
		String cloverPath = methodInfo.getPath();
		if (cloverPath.endsWith(".java")) {
			cloverPath = cloverPath.replaceAll("\\.java$", ".html");
		}

		return String.format("%s/clover-report/%s#%d", owner.getUrl(), hudson.Functions.encode(cloverPath), methodInfo.line);
	}

	@Override
	public void checkBuild(AbstractBuild<?, ?> build) throws InvalidHudsonProjectException {
		checkBuildContainningBuildAction(build, "clover");
	}

	@Override
	public String getDescription() {
		return "Clover hudson plugin result is used for generating this plot.<br/> In this case, the coverage means statement coverage.";
	}

}
