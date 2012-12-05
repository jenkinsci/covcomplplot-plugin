package hudson.plugins.covcomplplot.analyzer;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.plugins.covcomplplot.model.MethodInfo;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectException;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectType;
import hudson.plugins.covcomplplot.stub.LoggerWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

public class EmmaMethodHandler extends AbstractMethodInfoHandler {
	private Pattern coverage = Pattern.compile("[0-9]*%\\s*\\(([0-9]*)/([0-9]*)\\)");
	private Pattern blockCount = Pattern.compile("[0-9]*%\\s*\\([0-9]*/([0-9]*)\\)");
	private static final Logger LOGGER = Logger.getLogger(EmmaMethodHandler.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<MethodInfo> process(AbstractBuild<?, ?> build, boolean excludeGetterSetter, String rootDir,
			LoggerWrapper logger, Analyzer analyzer) throws InvalidHudsonProjectException {
		Document emma = getEmmaReport(build);
		ArrayList<MethodInfo> methods = new ArrayList<MethodInfo>();
		Element eachElement = emma.getRootElement();
		try {
			eachElement = eachElement.element("data");
			eachElement = eachElement.element("all");
			List<Element> elementList = (List<Element>) eachElement.elements("package");
			for (Element eachPackage : elementList) {
				String packagePath = eachPackage.attributeValue("name").replace(".", "/");
				for (Element eachSrcFile : (List<Element>) eachPackage.elements("srcfile")) {
					String srcFileName = packagePath + "/" + eachSrcFile.attributeValue("name");
					for (Element eachClass : (List<Element>) eachSrcFile.elements("class")) {
						for (Element eachMethod : (List<Element>) eachClass.elements("method")) {
							MethodInfo each = new MethodInfo(srcFileName, eachMethod.attributeValue("name").replace(
									" (", "("), 0, 0);
							for (Element eachCoverage : (List<Element>) eachMethod.elements("coverage")) {
								String coverageType = eachCoverage.attributeValue("type");
								if (coverageType.startsWith("block")) {
									setBlockCount(each, eachCoverage.attributeValue("value"));
								} else if (coverageType.startsWith("line")) {
									setCoverage(each, eachCoverage.attributeValue("value"));
								}
							}
							if (excludeGetterSetter) {
								if ((each.getSig().startsWith("get") || each.getSig().startsWith("set"))
										&& each.st <= 2) {
									continue;
								}
							}
							methods.add(each);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new InvalidHudsonProjectException(
					InvalidHudsonProjectType.INTERNAL,
					"clover.xml doesn't contain the detailed result.\n You may use clover 2.X version or summary='true' attribute ant clover report task. Please use clover 3.X and remove summary='true' in the ant clover report task.");
		}
		return methods;
	}

	protected Document getEmmaReport(AbstractBuild<?, ?> build) throws InvalidHudsonProjectException {
		return super.getBuildArtifact(build, "emma/coverage.xml", Analyzer.Emma);
	}

	void setBlockCount(MethodInfo methodInfo, String value) {
		Matcher matcher = blockCount.matcher(value);
		if (matcher.matches()) {
			// Provide 0.1 weight
			methodInfo.setCompl(NumberUtils.toInt(matcher.group(1), 0) / 10 + 1);
		}
	}

	void setCoverage(MethodInfo methodInfo, String value) {
		Matcher matcher = coverage.matcher(value);
		if (matcher.matches()) {
			methodInfo.increaseSizeAndCovered(NumberUtils.toInt(matcher.group(2), 0),
					NumberUtils.toInt(matcher.group(1), 0));
		}
	}

	@Override
	public String getMethodUrlLocation(AbstractBuild<?, ?> build, MethodInfo methodInfo) {
		String base = "";

		if (hasFolder(build, "src/main/java")) {
			base = "src/main/java/";
		} else if (hasFolder(build, "src")) {
			base = "src/";
		}
		String moduleRoot = getModuleRoot(build);

		String path = String.format("ws/%s/%s/*view*/", moduleRoot,
				hudson.Functions.encode(base + methodInfo.getPath()));
		path = path.replace("//", "/");
		return getProjectUrl(build) + path;
	}

	protected String getModuleRoot(AbstractBuild<?, ?> build) {
		FilePath moduleRoot = build.getModuleRoot();
		if (build.getWorkspace().getRemote().equals(moduleRoot.getRemote())) {
			return "";
		} else {
			return moduleRoot.getName();
		}
	}

	protected boolean hasFolder(AbstractBuild<?, ?> build, String path) {
		try {
			return build.getModuleRoot().child(path).exists();
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return false;
	}

	protected String getProjectUrl(AbstractBuild<?, ?> build) {
		return build.getProject().getUrl();
	}

	@Override
	public void checkBuild(AbstractBuild<?, ?> build) throws InvalidHudsonProjectException {
		checkBuildContainningBuildAction(build, "emma");
	}

	@Override
	public String getDescription() {
		return "Emma plugin result is used for generating this plot.<br/> Comlextity axis represents (block count / 10) <br/>and Coverage axis represents line coverage.";
	}

}
