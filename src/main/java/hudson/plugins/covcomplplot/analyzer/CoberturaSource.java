package hudson.plugins.covcomplplot.analyzer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data Class for an coverage info rendered source code by cobertura.
 * 
 * @author JunHo Yoon
 */
public class CoberturaSource {
	/** file name */
	private String fileName;
	/** file content */
	private String content;
	/** pattern to extract number from line td */
	public static final Pattern linePattern = Pattern.compile("^<td class=\\\"line\\\">(\\d*)<\\/td>\\W*$");

	/** default constructor */
	public CoberturaSource() {

	}

	/**
	 * Set content. It will add HTML anchor on the source content.
	 * @param content content to be set
	 */
	public void setContent(String content) {
		String[] splitted = content.split("\n");
		StringBuilder builder = new StringBuilder();
		for (String each : splitted) {
			if (each.startsWith("<td class=\"line\">")) {
				Matcher matcher = linePattern.matcher(each);
				if (matcher.find()) {
					String linenumber = matcher.group(1);
					builder.append("<td class=\"line\"><a name='").append(linenumber).append("'/>").append(linenumber).append("</td>\n");
					continue;
				}
			}
			builder.append(each).append("\n");
		}
		this.content = builder.toString();
	}

	/**
	 * Get content
	 * @return content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Set file name
	 * @param fileName file name to be set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Get file name
	 * @return file name
	 */
	public String getFileName() {
		return fileName;
	}

}
