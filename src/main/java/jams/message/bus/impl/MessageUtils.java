package jams.message.bus.impl;

import jams.message.Message;
import jams.message.ReceiverID;
import jams.message.bus.Bus;

public class MessageUtils {

	public static Message createResponse(Message message, Bus bus, ReceiverID sender, String semantics, Object content){
		
		Message response = bus.createResponse(message);
		
		response.getHeaders().setSender(sender);
				
		response.getBody().setSemantica(semantics);
		response.getBody().setContent(content);
		
		return response;
	}
}
