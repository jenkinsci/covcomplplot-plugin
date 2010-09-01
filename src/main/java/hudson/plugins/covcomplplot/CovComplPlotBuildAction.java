package hudson.plugins.covcomplplot;

import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.kohsuke.stapler.StaplerProxy;

public class CovComplPlotBuildAction implements Action, StaplerProxy {

	public transient WeakReference<CovComplPlotTaget> target = null;
	private final AbstractBuild<?,?> owner;

	public CovComplPlotBuildAction(AbstractBuild<?,?> owner, CovComplPlotTaget target) {
		this.owner = owner;
		this.target = new WeakReference<CovComplPlotTaget>(target);
	}

	public String getDisplayName() {
		return null;
	}

	public String getIconFileName() {
		return null;
	}
	
	public String getUrlName() {
		return "covcomplplot";
	}

	public Object getTarget() {
		if (this.target != null) {
			CovComplPlotTaget cloverTarget = this.target.get();

			if (cloverTarget != null) {
				cloverTarget.setOwner(getOwner());
				return cloverTarget;
			}
		}
		

		CovComplPlotTaget cloverTarget = null;

		synchronized (this) {
			try {
				cloverTarget = CovComplPlotTaget.loadCloverScatterPlotTarget(owner);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (cloverTarget != null) {
				target = new WeakReference<CovComplPlotTaget>(cloverTarget);
				cloverTarget.setOwner(getOwner());
				return cloverTarget;
			} else {
				return null;
			}
		}
	}

	public AbstractBuild<?, ?> getOwner() {
		return this.owner;
	}

}
