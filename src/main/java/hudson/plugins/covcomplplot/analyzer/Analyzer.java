package hudson.plugins.covcomplplot.analyzer;

/**
 * Enum containing each Jenkins plugin to be used by this plugin. If you want to add more Jenkins
 * plugins used, please add enum value.
 * 
 * @author JunHo Yoon
 */
public enum Analyzer {
	Clover("Clover", "Clover(Statement)", new CloverMethodHandler()), CloverBranch(
					"Clover",
					"Clover(Branch)",
					new CloverBranchCoverageMethodHandler()), Cobertura(
					"Cobertura",
					"Cobertura(Statement)",
					new CoberturaMethodHandler()), Emma("Emma", "Emma(Block)", new EmmaMethodHandler());
	/** plugin name */
	private final String pluginName;
	/** real handler */
	private final AbstractMethodInfoHandler handler;
	private final String name;

	/**
	 * Analyzer Constructor
	 * 
	 * @param pluginName
	 *            Jenkins plugin name from which the each handler extracts information.
	 * @param handler
	 *            Real Jenkins plugin result handler.
	 */
	Analyzer(String pluginName, String name, AbstractMethodInfoHandler handler) {
		this.pluginName = pluginName;
		this.name = name;
		this.handler = handler;
	}

	/**
	 * Get Jenkins plugin name from which each analyzer extracts information.
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

	/**
	 * Get the {@link Analyzer} which use the given handler.
	 * 
	 * @param abstractMethodInfoHandler
	 *            handler
	 * @return analyzer
	 */
	public static Analyzer getAnalyzerForthisHandler(AbstractMethodInfoHandler abstractMethodInfoHandler) {
		for (Analyzer each : values()) {
			if (each.getHandler() == abstractMethodInfoHandler) {
				return each;
			}
		}
		return Analyzer.Clover;
	}

}
