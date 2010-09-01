package hudson.plugins.covcomplplot.annalyzer;


/**
 * Enum containing each hudson plugin to be used by this plugin.
 * If you want to add more hudson plugins used, Please add enum value.
 */
public enum Analyzer {
	Clover("Clover", new CloverMethodHandler());

	private final String pluginName;
	private final AbstractMethodInfoHandler handler;
	
	/**
	 * Analyzer Constructor
	 * @param pluginName 
	 * @param handler Real hudson plugin result hanlder.
	 */
	Analyzer(String pluginName, AbstractMethodInfoHandler handler) {
		this.pluginName = pluginName;
		this.handler = handler;
	}

	public String getPluginName() {
		return pluginName;
	}

	public AbstractMethodInfoHandler getHandler() {
		return handler;
	}


}
