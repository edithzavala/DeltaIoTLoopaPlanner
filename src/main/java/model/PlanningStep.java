package model;

import logic.Step;

public class PlanningStep {
	Step step;
	PlannerLink link;
	int value;
	
	public PlanningStep (Step step, PlannerLink link, int value){
		this.step = step;
		this.link = link;
		this.value = value;
	}
	
	public Step getStep() {
		return step;
	}
	
	public void setStep(Step step) {
		this.step = step;
	}
	
	public PlannerLink getLink() {
		return link;
	}
	
	public void setLink(PlannerLink link) {
		this.link = link;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return String.format("%s, %s, %d", step, link, value);
	}
}
