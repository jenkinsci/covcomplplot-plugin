package hudson.plugins.covcomplplot;

import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Build Action. This redirects {@link StaplerRequest} to
 * {@link CovComplPlotTaget} by {@link StaplerProxy}
 * 
 * @author JunHo Yoon
 */
public class CovComplPlotBuildAction implements Action, StaplerProxy {

	/** Weak reference to {@link CovComplPlotTaget} to be gabage collected */
	public transient WeakReference<CovComplPlotTaget> target = null;
	/** owner */
	private final AbstractBuild<?, ?> owner;

	/**
	 * Constructor
	 * 
	 * @param owner
	 *            build object
	 * @param target
	 *            target to be used.
	 */
	public CovComplPlotBuildAction(AbstractBuild<?, ?> owner, CovComplPlotTaget target) {
		this.owner = owner;
		this.target = new WeakReference<CovComplPlotTaget>(target);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Action#getDisplayName()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.kohsuke.stapler.StaplerProxy#getTarget()
	 */
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

	/**
	 * Get owner
	 * 
	 * @return owner
	 */
	public AbstractBuild<?, ?> getOwner() {
		return this.owner;
	}

}
