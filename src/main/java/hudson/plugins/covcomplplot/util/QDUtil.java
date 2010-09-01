package hudson.plugins.covcomplplot.util;

import hudson.FilePath;
import hudson.model.Hudson;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;

public class QDUtil {
	static Pattern pattern = Pattern.compile("\\.(java|c|cpp|hpp|h|hxx|cxx)$");

	public static boolean isKlocworkRecognizedFile(String file) {
		if (file == null)
			return false;
		return pattern.matcher(file).find();
	}

	public static DocumentFactory factory = new DocumentFactory();
	static {
		factory.setXPathNamespaceURIs(new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("n", "nhncorp.koreabusinesssupport.nhninformationsystem.productiongroup.QualityDashboard.getprojectcodequality");
				put("cl", "http://schemas.atlassian.com/clover3/report");
			}
		});
	}


	public static String getXPathNodeText(Node node, String xpathStr) throws NoXPathContentException {
		return getXPathNode(node, xpathStr).getText();
	}

	@SuppressWarnings("unchecked")
	public static List<Element> getXPathNodeList(Node node, String xpathStr) {
		List selectNodes = node.selectNodes(xpathStr);
		return (List<Element>) selectNodes;
	}

	public static int compareVersion(String v1, String v2) {
		String s1 = normalisedVersion(v1);
		String s2 = normalisedVersion(v2);
		return s1.compareTo(s2);
	}

	public static String normalisedVersion(String version) {
		return normalisedVersion(version, ".", 4);
	}

	public static String normalisedVersion(String version, String sep, int maxWidth) {
		String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
		StringBuilder sb = new StringBuilder();
		for (String s : split) {
			sb.append(String.format("%" + maxWidth + 's', s));
		}
		return sb.toString();
	}

	public static String getClassResourcePath(Class<?> clazz, String extension) {
		return clazz.getCanonicalName().replace(".", "/").concat(".").concat(extension);
	}

	public static Map<String, MessageFormat> templateCache = new HashMap<String, MessageFormat>();

	public synchronized static MessageFormat getTemplate(String location) {
		if (!templateCache.containsKey(location)) {
			try {
				ClassPathResource resource = new ClassPathResource(location, QDUtil.class.getClassLoader());
				InputStream inputStream = resource.getInputStream();
				String template = IOUtils.toString(inputStream, "UTF-8");
				MessageFormat stringTemplate = new MessageFormat(template);
				templateCache.put(location, stringTemplate);
				IOUtils.closeQuietly(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		MessageFormat template = templateCache.get(location);
		return template;
	}

	public static Element getXPathNode(Node node, String xpathStr) throws NoXPathContentException {
		Node selectSingleNode = node.selectSingleNode(xpathStr);
		if (selectSingleNode == null) {
			throw new NoXPathContentException(xpathStr + " is not avaliable");
		}
		return (Element) selectSingleNode;
	}

	public static Document getXmlFileDocument(InputStream stream, DocumentFactory factory) throws DocumentException {
		SAXReader reader = new SAXReader(factory);
		Document doc = reader.read(stream);
		IOUtils.closeQuietly(stream);
		return doc;
	}

	public static Document getXmlFileDocument(InputStream stream) throws DocumentException {
		return getXmlFileDocument(stream, QDUtil.factory);
	}

	public static FilePath[] findFile(FilePath basePath, String pattern) {
		try {
			return basePath.list(pattern);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getIcons(String iconName) {
		if (StringUtils.isEmpty(iconName)) {
			iconName = "blank";
		}
		return getPlugInDir() + "icons/" + iconName.toUpperCase() + ".gif";
	}

	public static String getPlugInDir() {
		String rootDir = "http://localhost:8080";
		try {
			rootDir = Hudson.getInstance().getRootUrlFromRequest();
		} catch (Exception e) {
		}
		return rootDir + "plugin/qd/";
	}
}