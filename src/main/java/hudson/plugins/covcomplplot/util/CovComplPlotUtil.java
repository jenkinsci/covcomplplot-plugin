package hudson.plugins.covcomplplot.util;

import hudson.FilePath;

import java.io.IOException;
import java.io.InputStream;
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
import org.xml.sax.SAXException;

/**
 * Utility Class for various data process
 * 
 * @author JunHo Yoon
 */
public class CovComplPlotUtil {

	/** DocumentFactory to handler namespace in the xml file */
	public static DocumentFactory factory = new DocumentFactory();
	static {
		factory.setXPathNamespaceURIs(new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("cl", "http://schemas.atlassian.com/clover3/report");
			}
		});
	}

	/**
	 * Get first matching string value from the given node by searching given
	 * xpath.
	 * 
	 * @param node
	 *            the node from which xpath search starts.
	 * @param xpathStr
	 *            XPath String
	 * @return found string
	 * @throws NoXPathContentException
	 *             occurs XPath is incorrect or nothing can be found.
	 */
	public static String getXPathNodeText(Node node, String xpathStr) throws NoXPathContentException {
		return getXPathNode(node, xpathStr).getText();
	}

	/**
	 * Get all xpath matching nodes.
	 * 
	 * @param node
	 *            node from which xpath search starts
	 * @param xpathStr
	 *            XPath String
	 * @return found node list
	 */
	@SuppressWarnings("unchecked")
	public static List<Element> getXPathNodeList(Node node, String xpathStr) {
		List selectNodes = node.selectNodes(xpathStr);
		return (List<Element>) selectNodes;
	}

	/**
	 * Get single xpath matching nodes.
	 * 
	 * @param node
	 *            node from which xpath search starts
	 * @param xpathStr
	 *            XPath String
	 * @return found node
	 * @throws NoXPathContentException
	 *             occurs when no matching.
	 */
	public static Element getXPathNode(Node node, String xpathStr) throws NoXPathContentException {
		Node selectSingleNode = node.selectSingleNode(xpathStr);
		if (selectSingleNode == null) {
			throw new NoXPathContentException(xpathStr + " is not avaliable");
		}
		return (Element) selectSingleNode;
	}

	/**
	 * Get {@link Document} instance by parsing given stream.
	 * 
	 * @param stream
	 *            inputStream
	 * @param factory
	 *            {@link DocumentFactory} instance to handle namespace
	 * @return {@link Document} instance
	 * @throws DocumentException
	 *             occurs when wrong xml is provided.
	 */
	public static Document getXmlFileDocument(InputStream stream, DocumentFactory factory) throws DocumentException {
		SAXReader reader = new SAXReader(factory);
		reader.setValidation(false);
		try {
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (SAXException e) {
			e.printStackTrace();
		}
      
		Document doc = reader.read(stream);
		IOUtils.closeQuietly(stream);
		return doc;
	}

	/**
	 * Get {@link Document} instance by parsing given stream.
	 * 
	 * @param stream
	 *            inputStream
	 * @return {@link Document} instance
	 * @throws DocumentException
	 *             occurs when wrong xml is provided.
	 */
	public static Document getXmlFileDocument(InputStream stream) throws DocumentException {
		return getXmlFileDocument(stream, CovComplPlotUtil.factory);
	}

	/**
	 * Find files matching given pattern from given base path
	 * 
	 * @param basePath
	 *            base path from which the search starts
	 * @param pattern
	 *            file matching pattern
	 * @return matched {@link FilePath} array.
	 */
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
	
	/**
	 * get corresponding class resource file path from the folder where the class is in.
	 * @param clazz class
	 * @param extension file extension to be added
	 * @return resource path
	 */
	public static String getClassResourcePath(Class<?> clazz, String extension) {
		return clazz.getCanonicalName().replace(".", "/").concat(".").concat(extension);
	}
	
	/**
	 * Compare version string
	 * @param v1 version1
	 * @param v2 version2
	 * @return -1 if less, 0 if same, 1 if more
	 */
	public static int compareVersion(String v1, String v2) {
		String s1 = normalisedVersion(v1);
		String s2 = normalisedVersion(v2);
		return s1.compareTo(s2);
	}

	/**
	 * Normalize version string
	 * @param version verion string
	 * @return normalized version string
	 */
	private static String normalisedVersion(String version) {
		return normalisedVersion(version, ".", 4);
	}

	/**
	 * Normalize version string
	 * @param version version string
	 * @param sep version separator
	 * @param maxWidth max version width
	 * @return normalized version string
	 */
	private static String normalisedVersion(String version, String sep, int maxWidth) {
		String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
		StringBuilder sb = new StringBuilder();
		for (String s : split) {
			sb.append(String.format("%" + maxWidth + 's', s));
		}
		return sb.toString();
	}

	public static Map<String, String> templateCache = new HashMap<String, String>();

	/**
	 * Read file from given location and return string.
	 * In this method, the cache is used if more than 1 time.
	 * @param location file location
	 * @return file content as string
	 */
	public synchronized static String getFileAsString(String location) {
		if (!templateCache.containsKey(location)) {
			try {
				ClassPathResource resource = new ClassPathResource(location, CovComplPlotUtil.class.getClassLoader());
				InputStream inputStream = resource.getInputStream();
				String template = IOUtils.toString(inputStream, "UTF-8");
				templateCache.put(location, template);
				IOUtils.closeQuietly(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String template = templateCache.get(location);
		return template;
	}
}