package api;

import java.util.HashMap;
import java.util.Map;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;
import org.loopa.comm.message.MessageType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Service {

	@PostMapping("/plan")
	public ResponseEntity<String> createPlanDataEntry(@RequestBody String dataForplan) {
		if (dataForplan != null) {
			Map<String, String> messageBody = new HashMap<>();
			messageBody.put("plan", dataForplan);
			IMessage planMssg = new Message("PLAN", Application.p.getElementId(),
					Integer.parseInt(Application.p.getElementPolicy().getPolicyContent()
							.get(LoopAElementMessageCode.MSSGINFL.toString())),
					MessageType.REQUEST.toString(), messageBody);
			Application.p.getReceiver().doOperation(planMssg);
			return ResponseEntity.status(HttpStatus.CREATED).build();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}
}
