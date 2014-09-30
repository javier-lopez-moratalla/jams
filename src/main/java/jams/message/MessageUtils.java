package jams.message;

import jams.message.bus.impl.BasicMessageImpl;

import java.util.LinkedList;
import java.util.List;

public class MessageUtils {

	public static Message createMessage(ReceiverID sender, List<ReceiverID> receivers) {
	
		Message message = new BasicMessageImpl();
		
		Headers headers = message.getHeaders();
		headers.setSender(sender);
		headers.setReceivers(receivers);
		
		return message;
	}
	
	public static Message createMessage(ReceiverID sender, ReceiverID receiver) {
	
		Message message = new BasicMessageImpl();
		
		List<ReceiverID> receivers = new LinkedList<ReceiverID>();
		receivers.add(receiver);
		
		Headers headers = message.getHeaders();
		headers.setSender(sender);
		headers.setReceivers(receivers);
		
		return message;
	}

	public static Message createResponse(Message message) {
	
		Message response = new BasicMessageImpl();
		
		List<ReceiverID> receivers = new LinkedList<>();
		receivers.add(message.getHeaders().getSender());

		response.getHeaders().setReceivers(receivers);
		response.getHeaders().setConversationId(message.getHeaders().getConversationId());
		
		return response;
	}
	
	public static Message createResponse(Message message, ReceiverID sender, String semantics, Object content){
		
		Message response = new BasicMessageImpl();
		
		List<ReceiverID> receivers = new LinkedList<>();
		receivers.add(message.getHeaders().getSender());

		response.getHeaders().setReceivers(receivers);
		response.getHeaders().setConversationId(message.getHeaders().getConversationId());
		response.getHeaders().setSender(sender);
		response.getBody().setSemantica(semantics);
		response.getBody().setContent(content);
				
		return response;
	}

}
