package logic;

import org.loopa.comm.message.IMessage;
import org.loopa.element.sender.messagesender.AMessageSender;

import client.ExecuterClient;

public class PlannerMessageSender extends AMessageSender {
	private ExecuterClient ecli = new ExecuterClient();
	
	@Override
	public void processMessage(IMessage mssg) {
		if (mssg.getMessageBody().get("type").equals("EXECUTE_Executer")) {
			ecli.requestAdaptationExecution((String) this.getComponent().getComponentRecipient("Executer").getRecipient(), mssg);
		}
	}
}
