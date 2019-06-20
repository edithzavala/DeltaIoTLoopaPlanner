package api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;
import org.loopa.comm.message.MessageType;
import org.loopa.comm.message.PolicyConfigMessageBody;
import org.loopa.element.functionallogic.enactor.IFunctionalLogicEnactor;
import org.loopa.element.functionallogic.enactor.planner.PlannerFunctionalLogicEnactor;
import org.loopa.element.sender.messagesender.IMessageSender;
import org.loopa.planner.IPlanner;
import org.loopa.planner.Planner;
import org.loopa.policy.IPolicy;
import org.loopa.policy.Policy;
import org.loopa.recipient.Recipient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;

import logic.FunctionalLogicEnactorManager;
import logic.PlannerMessageSender;
import model.DeltaIoTPlannerSenderPolicy;

@SpringBootApplication
public class Application {
	public static String PLANNER_ID;
	public static IPlanner p;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		PLANNER_ID = args[0];
		String policyFilePath = "/tmp/config/" + args[1];

		/** Init policy (MANDATORY) **/
		Map<String, String> initPolicy = new HashMap<>();
		initPolicy.put(LoopAElementMessageCode.MSSGINFL.toString(), "1");
		initPolicy.put(LoopAElementMessageCode.MSSGINAL.toString(), "2");
		initPolicy.put(LoopAElementMessageCode.MSSGADAPT.toString(), "3");
		initPolicy.put(LoopAElementMessageCode.MSSGOUTFL.toString(), "4");
		initPolicy.put(LoopAElementMessageCode.MSSGOUTAL.toString(), "5");

		/****** Create planner ***/
		// System.out.println(policyContent.toString());
		IMessageSender sMS = new PlannerMessageSender();
		IFunctionalLogicEnactor flE = new PlannerFunctionalLogicEnactor(new FunctionalLogicEnactorManager());
		IPolicy pp = new Policy(PLANNER_ID, initPolicy);
		p = new Planner(PLANNER_ID, pp, flE, sMS);
		p.construct();

		/***** Add logic policies ****/
		String policyString = "";
		Map<String, String> policyContent = new HashMap<>();
		try {
			policyString = new String(Files.readAllBytes(Paths.get(policyFilePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		policyContent.put("config", policyString);
		PolicyConfigMessageBody messageContentFL = new PolicyConfigMessageBody(p.getFunctionalLogic().getComponentId(),
				policyContent);
		IMessage mssgAdaptFL = new Message(PLANNER_ID, p.getReceiver().getComponentId(), 2,
				MessageType.REQUEST.toString(), messageContentFL.getMessageBody());
		p.getReceiver().doOperation(mssgAdaptFL);

		/*** Add recipients and corresponding policies ****/
		DeltaIoTPlannerSenderPolicy plannerRecipients;
		ObjectMapper mapper = new ObjectMapper();
		try {
			plannerRecipients = mapper.readValue(policyString, DeltaIoTPlannerSenderPolicy.class);
			plannerRecipients.getRecipients().forEach(recipient -> {
				p.addElementRecipient(
						new Recipient(recipient.getId(), recipient.getTypeOfData(), recipient.getRecipient()));
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
