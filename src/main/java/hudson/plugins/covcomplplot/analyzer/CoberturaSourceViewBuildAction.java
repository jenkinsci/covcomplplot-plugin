package hudson.plugins.covcomplplot.analyzer;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.plugins.cobertura.CoberturaBuildAction;
import hudson.plugins.cobertura.targets.CoverageResult;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class CoberturaSourceViewBuildAction implements Action {

	private final AbstractBuild<?, ?> owner;

	public CoberturaSourceViewBuildAction(AbstractBuild<?, ?> owner) {
		this.owner = owner;
	}

	public String getDisplayName() {
		return null;
	}

	public String getIconFileName() {
		return null;
	}

	public String getUrlName() {
		return "cobeturasourceview";
	}

	public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
		String[] paths = req.getRestOfPath().split("/");
		CoberturaSource source = new CoberturaSource();
		source.setFileName(req.getRestOfPath());
		try {
			CoberturaBuildAction action = owner.getAction(CoberturaBuildAction.class);
			CoverageResult target = ((CoverageResult) action.getTarget());
			CoverageResult result = target;
			for (String token : paths) {
				if (StringUtils.isNotEmpty(token)) {
					result = (CoverageResult) result.getDynamic(token, req, rsp);
				}
			}
			source.setFileName(result.getRelativeSourcePath());
			source.setContent(result.getSourceFileContent());
			req.getView(source, "index.jelly").forward(req, rsp);
		} catch (Exception e) {
			req.getView(source, "nosourcecode.jelly").forward(req, rsp);
		}
	}
}
