package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageBody;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;
import org.loopa.comm.message.MessageType;
import org.loopa.element.functionallogic.enactor.planner.IPlannerFleManager;
import org.loopa.generic.element.component.ILoopAElementComponent;
import org.loopa.policy.IPolicy;
import org.loopa.policy.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.DeltaIoTPlannerFLPolicy;
import model.MotesInfo;
import model.PlanDocument;
import model.PlannerLink;
import model.PlannerMote;
import model.PlanningStep;

public class FunctionalLogicEnactorManager implements IPlannerFleManager {
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
	private IPolicy allPolicy = new Policy(this.getClass().getName(), new HashMap<String, String>());
	private DeltaIoTPlannerFLPolicy policy; // only contains motes ids
	private ILoopAElementComponent owner;

	@Override
	public void setConfiguration(Map<String, String> config) {
		LOGGER.info("Component (re)configured");
		if (config.containsKey("config")) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				this.policy = mapper.readValue(config.get("config"), DeltaIoTPlannerFLPolicy.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.allPolicy.update(new Policy(this.allPolicy.getPolicyOwner(), config));
	}

	@Override
	public void processLogicData(Map<String, String> data) {
		LOGGER.info("Data received");
		if (data.keySet().contains("plan")) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				MotesInfo motesMessage = mapper.readValue(data.get("plan"), MotesInfo.class);
				List<PlanningStep> steps = new LinkedList<>();
				boolean powerChanging = false;
				PlannerLink left, right;
				for (PlannerMote mote : new ArrayList<>(motesMessage.getMotes().values())) {
					for (PlannerLink link : mote.getLinks()) {
						powerChanging = false;
						if (link.getSNR() > 0 && link.getPower() > 0) {
							steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() - 1));
							powerChanging = true;
							LOGGER.info("Change power for mote" + mote.getMoteid() + " link " + link.getSource() + "->"
									+ link.getDest());
						} else if (link.getSNR() < 0 && link.getPower() < 15) {
							steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() + 1));
							powerChanging = true;
							LOGGER.info("Change power for mote" + mote.getMoteid() + " link " + link.getSource() + "->"
									+ link.getDest());
						}
					}
					if (mote.getLinks().size() == 2 && powerChanging == false) {
						left = mote.getLinks().get(0);
						right = mote.getLinks().get(1);
						if (left.getPower() != right.getPower()) {
							// If distribution of all links is 100 then change it to 50
							// 50
							if (left.getDistribution() == 100 && right.getDistribution() == 100) {
								left.setDistribution(50);
								right.setDistribution(50);
							}
							if (left.getPower() > right.getPower() && left.getDistribution() < 100) {
								steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() + 10));
								steps.add(new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() - 10));
								LOGGER.info("Change dist for mote" + mote.getMoteid());
							} else if (right.getDistribution() < 100) {
								steps.add(new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() + 10));
								steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() - 10));
								LOGGER.info("Change dist for mote" + mote.getMoteid());
							}
						}
					}
				}

				if (steps.size() > 0) {
					PlanDocument outInfo = new PlanDocument();
					outInfo.setMotes(new ArrayList<>(motesMessage.getMotes().values()));
					outInfo.setSteps(steps);
					sendPlanToExecute(mapper.writeValueAsString(outInfo));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendPlanToExecute(String plan) {
		LoopAElementMessageBody messageContent = new LoopAElementMessageBody("EXECUTE", plan);
		messageContent.getMessageBody().put("contentType", "adaptationPlan");
		String code = this.getComponent().getElement().getElementPolicy().getPolicyContent()
				.get(LoopAElementMessageCode.MSSGOUTFL.toString());
		IMessage mssg = new Message(this.owner.getComponentId(), this.allPolicy.getPolicyContent().get(code),
				Integer.parseInt(code), MessageType.REQUEST.toString(), messageContent.getMessageBody());
		((ILoopAElementComponent) this.owner.getComponentRecipient(mssg.getMessageTo()).getRecipient())
				.doOperation(mssg);
	}

	@Override
	public void setComponent(ILoopAElementComponent c) {
		this.owner = c;
	}

	@Override
	public ILoopAElementComponent getComponent() {
		return this.owner;
	}
}
