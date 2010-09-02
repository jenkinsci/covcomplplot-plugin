package hudson.plugins.covcomplplot.util;

import hudson.FilePath;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;

/**
 * Utility Class for various data process
 * @author JunHo Yoon
 */
public class CovComplPlotUtil {
	static Pattern pattern = Pattern.compile("\\.(java|c|cpp|hpp|h|hxx|cxx)$");

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
	
	public static Map<String, MessageFormat> templateCache = new HashMap<String, MessageFormat>();

	public synchronized static MessageFormat getTemplate(String location) {
		if (!templateCache.containsKey(location)) {
			try {
				ClassPathResource resource = new ClassPathResource(location, CovComplPlotUtil.class.getClassLoader());
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
		return getXmlFileDocument(stream, CovComplPlotUtil.factory);
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
}