package model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class PlanDocument {
	private List<PlannerMote> motes;
	private List<PlanningStep> steps;

	public  List<PlannerMote>  getMotes() {
		return motes;
	}

	public void setMotes( List<PlannerMote>  motes) {
		this.motes = motes;
	}

	public List<PlanningStep> getSteps() {
		return steps;
	}

	public void setSteps(List<PlanningStep> steps) {
		this.steps = steps;
	}
}
