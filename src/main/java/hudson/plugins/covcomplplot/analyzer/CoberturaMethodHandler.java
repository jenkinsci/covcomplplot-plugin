package hudson.plugins.covcomplplot.analyzer;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.plugins.covcomplplot.model.MethodInfo;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectException;
import hudson.plugins.covcomplplot.stub.InvalidHudsonProjectType;
import hudson.plugins.covcomplplot.stub.LoggerWrapper;
import hudson.plugins.covcomplplot.util.CovComplPlotUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
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
public class CoberturaMethodHandler extends AbstractMethodInfoHandler {
	Pattern branchBase = Pattern.compile("\\([0-9]*/([0-9]*)\\)");
	Pattern signaturePattern =  Pattern.compile("\\((.*)\\)(.*)");
	Pattern argumentPattern = Pattern.compile("\\[*([TL][^\\;]*\\;)|([ZCBSIFJDV])");
	@Override
	public List<MethodInfo> process(AbstractBuild<?, ?> build, boolean excludeGetterSetter, String remoteDir, LoggerWrapper logger, Analyzer analyzer)
			throws InvalidHudsonProjectException {
		Document clover = super.getBuildArtifact(build, "coverage.xml", Analyzer.Cobertura);
		List<Element> elementList = null;
		Element rootElement = clover.getRootElement();
		try {
			elementList = CovComplPlotUtil.getXPathNodeList(rootElement, "//packages/package/classes/class");
		} catch (Exception e) {
			throw new InvalidHudsonProjectException(
					InvalidHudsonProjectType.INTERNAL,
					"coverage.xml doesn't contain the detailed result. Please check if coverage.xml has method level infomration.");
		}
		ArrayList<MethodInfo> methods = new ArrayList<MethodInfo>();

		for (Element eachClass : elementList) {
			String path = eachClass.attributeValue("filename");
			for (Object eachMethodObject : eachClass.selectNodes("methods/method")) {
				Element eachMethodElement = (Element) eachMethodObject;
				String name = eachMethodElement.attributeValue("name");
				if (name == null || "<clinit>".equals(name) || "<init>".equals(name) || name.startsWith("__CLR")) {
					continue;
				}

				int complexity = 1;
				int line = 1;
				int covered = 0;
				int size = 0;
				String signature = eachMethodElement.attributeValue("signature");
				boolean first = true;
				for (Object eachLineObject : eachMethodElement.selectNodes("lines/line")) {
					Element eachLineElement = (Element) eachLineObject;
					if (first) {
						String number = eachLineElement.attributeValue("number");
						line = Math.max(1, Integer.parseInt(number) - 2);
						first = false;
					}
					size++;
					if (!"0".equals(eachLineElement.attributeValue("hits"))) {
						covered++;
					}

					if ("true".equals(eachLineElement.attributeValue("branch"))) {
						complexity += getBranchCount(eachLineElement.attributeValue("condition-coverage"));
					}
				}
				MethodInfo methodInfo = new MethodInfo(path, buildMethodName(name, signature), complexity, line, covered, size);
				if (isMethodValid(methodInfo, excludeGetterSetter)) {
					methods.add(methodInfo);
				}
			}

		}

		return methods;
	}

	/**
	 * Build readable method name from name signature
	 * @param name method name
	 * @param signature signature
	 * @return readable method name
	 */
	private String buildMethodName(String name, String signature) {
		Matcher signatureMatcher = signaturePattern.matcher(signature);
		StringBuilder methodName = new StringBuilder();
		if (signatureMatcher.matches()) {
			Pattern argMatcher = argumentPattern;
			String returnType = signatureMatcher.group(2);
			Matcher matcher = argMatcher.matcher(returnType);
			if (matcher.matches()) {
				methodName.append(parseMethodArg(matcher.group()));
				methodName.append(' ');
			}
			methodName.append(name);
			String args = signatureMatcher.group(1);
			matcher = argMatcher.matcher(args);
			methodName.append('(');
			boolean first = true;
			while (matcher.find()) {
				if (!first) {
					methodName.append(',');
				}
				methodName.append(parseMethodArg(matcher.group()));
				first = false;
			}
			methodName.append(')');
		} else {
			methodName.append(name);
		}
		return methodName.toString();
	}
	
	/**
	 * parse cobertura method arg into readable form.
	 * @param s cobertura method arg
	 * @return readable form of method arg
	 */
	private String parseMethodArg(String s) {
		char c = s.charAt(0);
		int end;
		switch (c) {
		case 'Z':
			return "boolean";
		case 'C':
			return "char";
		case 'B':
			return "byte";
		case 'S':
			return "short";
		case 'I':
			return "int";
		case 'F':
			return "float";
		case 'J':
			return "";
		case 'D':
			return "double";
		case 'V':
			return "void";
		case '[':
			return parseMethodArg(s.substring(1)) + "[]";
		case 'T':
		case 'L':
			end = s.indexOf(';');
			String eachArg = s.substring(1, end).replace('/', '.');
			int index = eachArg.lastIndexOf(".") + 1;
			return eachArg.substring(Math.min(eachArg.length() - 1, index));

		}
		return s;
	}

	public int getBranchCount(String text) {
		Matcher matcher = branchBase.matcher(text);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1)) / 2;
		}
		return 0;
	}

	@Override
	public String getCustomJavaScript() {
		return "";
	}
	
	/**
	 * URL Transform 
	 * @param name 
	 * @return
	 */
	public String urlTransform(String name) {
		StringBuilder buf = new StringBuilder(name.length());
		for (int i = 0; i < name.length(); i++) {
			final char c = name.charAt(i);
			if (('0' <= c && '9' >= c) || ('A' <= c && 'Z' >= c) || ('a' <= c && 'z' >= c)) {
				buf.append(c);
			} else {
				buf.append('_');
			}
		}
		return buf.toString();
	}

	@Override
	public String getMethodUrlLocation(AbstractBuild<?, ?> owner, MethodInfo methodInfo) {
		String fullPath = methodInfo.getPath();
		String path = FilenameUtils.getPath(fullPath);
		if (StringUtils.isNotEmpty(path))
			path = path.substring(0, path.length() - 1);
		path = urlTransform(path);
		String name = urlTransform(FilenameUtils.getName(fullPath));
		return String.format("%s/cobeturasourceview/%s/%s#%d", owner.getUrl(), path, name, methodInfo.line);
	}

	@Override
	public void checkBuild(AbstractBuild<?, ?> build) throws InvalidHudsonProjectException {
		checkBuildContainningBuildAction(build, "cobertura");
	}

	@Override
	public String getDescription() {
		return "Cobertura hudson plugin result is used for generating this plot.<br/> In this case, the coverage means statement coverage.";
	}

	public Action getCustomSourceViewBuildAction(AbstractBuild<?, ?> build) {
		return new CoberturaSourceViewBuildAction(build);
	}
}
