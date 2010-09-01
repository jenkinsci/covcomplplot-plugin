package hudson.plugins.covcomplplot.annalyzer;

import hudson.model.AbstractBuild;
import hudson.plugins.covcomplplot.model.MethodInfo;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectException;
import hudson.plugins.covcomplplot.stub.LoggerWrapper;
import hudson.plugins.covcomplplot.util.QDUtil;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

public class CloverMethodHandler extends AbstractMethodInfoHandler {
	
	@Override
	public List<MethodInfo> process(AbstractBuild<?, ?> build, boolean excludeGetterSetter, String remoteDir, LoggerWrapper logger,
			Analyzer analyzer) throws InvalidHudsonProjectException {
		Document clover = super.getBuildArtifact(build, "clover.xml", Analyzer.Clover);
		List<Element> domElement = null;
		domElement = QDUtil.getXPathNodeList(clover, "/coverage/project/package");
		if (domElement.size() == 0) {
			domElement = QDUtil.getXPathNodeList(clover, "/cl:coverage/cl:project/cl:package");
		}
		int maxComplexity = 0;
		ArrayList<MethodInfo> methods = new ArrayList<MethodInfo>();

		for (Element eachPackage : domElement) {
			String dirPath = eachPackage.attributeValue("name");
			for (Object eachFileObject : eachPackage.elements("file")) {
				Element eachFileElement = (Element) eachFileObject;
				String path = dirPath.replace(".", "/") + "/" + eachFileElement.attributeValue("name");
				MethodInfo cloverMethod = null;
				for (Object each : eachFileElement.elements("line")) {
					Element eachLine = (Element) each;
					String eachType = eachLine.attributeValue("type");
					if ("method".equals(eachType)) {
						// Remove the invalid method
						// I put this here to minimize the array search and array memory reallocation.
						if (!isMethodValid(cloverMethod, excludeGetterSetter)) {
							methods.remove(methods.size() - 1);
						}
						
						String signature = eachLine.attributeValue("signature");
						int complexity = Integer.parseInt(eachLine.attributeValue("complexity"));
						int lineno = Integer.parseInt(eachLine.attributeValue("num"));
						maxComplexity = Math.max(maxComplexity, complexity);
						cloverMethod = new MethodInfo(path, signature, complexity, lineno);
						methods.add(cloverMethod);
					} else if (("stmt").equals(eachType) && cloverMethod != null) {
						cloverMethod.increaseLine(!"0".equals(eachLine.attributeValue("count")));
					}
				}
				// Remove last method of this file if it's not valid.
				// I put this here to minimize the array search and array memory reallocation.
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



	@Override
	public String getCustomJavaScript() {
		return "";
	}

	@Override
	public String getMethodUrlLocation(AbstractBuild<?, ?> owner, MethodInfo methodInfo) {
		String cloverPath = methodInfo.path;
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
