package hudson.plugins.covcomplplot;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.Hudson;
import hudson.model.ProminentProjectAction;
import hudson.plugins.covcomplplot.CovComplPlotPublisher.DescriptorImpl;

/**
 * Project Action. This action just passes the request to the latest
 * {@link CovComplPlotBuildAction} instance.
 * 
 * @author JunHo Yoon
 */
public class CovComplPlotProjectAction extends Actionable implements ProminentProjectAction {
	/** {@link AbstractProject} which owns this action */
	private transient AbstractProject<?, ?> owner;

	/**
	 * Constructor
	 * 
	 * @param project
	 *            owner
	 */
	public CovComplPlotProjectAction(AbstractProject<?, ?> project) {
		this.owner = project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.ModelObject#getDisplayName()
	 */
	public String getDisplayName() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Action#getIconFileName()
	 */
	public String getIconFileName() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Action#getUrlName()
	 */
	public String getUrlName() {
		return Constant.URL_NAME;
	}

	/**
	 * Get the Last {@link CovComplPlotBuildAction} whose owner is completed and not failed
	 * 
	 * @return Last Build Action
	 */
	public CovComplPlotBuildAction getLastBuildAction() {
		for (AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) owner.getLastCompletedBuild(); build != null; build = build
				.getPreviousNotFailedBuild()) {
			CovComplPlotBuildAction action = build.getAction(CovComplPlotBuildAction.class);
			if (action != null) {
				return action;
			}
		}
		return null;
	}
	
	public boolean isTopMostSectionInProjectPage() {
		CovComplPlotPublisher publisher = owner.getPublishersList().get(CovComplPlotPublisher.class);
		if (publisher != null) {
			return publisher.isLocateTopMost();
		}
		return false;
		//(arg0)owner.getDescriptorByName(DescriptorImpl.class.getName())
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.search.SearchItem#getSearchUrl()
	 */
	public String getSearchUrl() {
		return null;
	}
}
