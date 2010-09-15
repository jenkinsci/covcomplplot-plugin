package hudson.plugins.covcomplplot.analyzer;

/**
 * Enum containing each hudson plugin to be used by this plugin. If you want to
 * add more hudson plugins used, Please add enum value.
 * 
 * @author JunHo Yoon
 */
public enum Analyzer {
	Clover("Clover", "Clover(Statement)", new CloverMethodHandler()), 
	CloverBranch("Clover", "Clover(Branch)", new CoberturaMethodHandler()),
	Cobertura("Cobertura", "Cobertura", new CoberturaMethodHandler());

	/** plugin name */
	private final String pluginName;
	/** real handler */
	private final AbstractMethodInfoHandler handler;
	private final String name;

	/**
	 * Analyzer Constructor
	 * 
	 * @param pluginName
	 *            hudson plugin name from which the each handler extracts
	 *            information.
	 * @param handler
	 *            Real hudson plugin result hanlder.
	 */
	Analyzer(String pluginName, String name, AbstractMethodInfoHandler handler) {
		this.pluginName = pluginName;
		this.name = name;
		this.handler = handler;
	}

	/**
	 * Get hudson plugin name from which each analyzer extracts information.
	 * 
	 * @return plugin name
	 */
	public String getPluginName() {
		return pluginName;
	}

	/**
	 * Get real handler
	 * 
	 * @return handler
	 */
	public AbstractMethodInfoHandler getHandler() {
		return handler;
	}

	public String getName() {
		return name;
	}

}
