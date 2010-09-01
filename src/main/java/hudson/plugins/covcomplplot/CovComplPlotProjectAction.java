package hudson.plugins.covcomplplot;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class CovComplPlotProjectAction extends Actionable implements ProminentProjectAction {

	private transient AbstractProject<?, ?> owner;

	public CovComplPlotProjectAction(AbstractProject<?, ?> project) {
		this.owner = project;
	}

	public String getDisplayName() {
		return null;
	}

	public String getIconFileName() {
		return null;
	}

	public String getUrlName() {
		return Constant.URL_NAME;
	}

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

	public String getSearchUrl() {
		// TODO Auto-generated method stub
		return null;
	}
}
