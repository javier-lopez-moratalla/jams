package jams.message.bus.impl;

import jams.message.Headers;
import jams.message.Message;
import jams.message.Receiver;
import jams.message.ReceiverID;
import jams.message.bus.Bus;
import jams.message.bus.SendingError;
import jams.message.conversation.ConversationHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BusImpl implements Bus {

	private Map<ReceiverID, Receiver> receivers;
	private Map<Long,Map<ReceiverID,ConversationHandler>> conversationHandlers;
	
	public BusImpl() {
	
		receivers = new HashMap<ReceiverID, Receiver>();
		conversationHandlers = new HashMap<Long, Map<ReceiverID,ConversationHandler>>();
	}
	
	@Override
	public List<SendingError> sendMessage(Message message) {

		List<SendingError> result = new LinkedList<SendingError>();
		
		Headers headers = message.getHeaders();
		
		List<ReceiverID> receiverIDs = headers.getReceivers();
		Long conversationId = headers.getConversationId();
		Map<ReceiverID,ConversationHandler> conversation = null;
		
		if(conversationId != null){
		
			 conversation = conversationHandlers.get(conversationId);
		}
		
		for(ReceiverID id:receiverIDs){
			
			Receiver receiver = null;
			ConversationHandler handler = null; 
					
			if(conversation != null){
			
				handler = conversation.get(id);
			}
			
			if(handler != null){
				
				receiver = handler; 
			}
			else{
				
				receiver = receivers.get(id);
			}
			
			if(receiver != null){
				
				receiver.receiveMessage(message);
				
				if(handler != null && handler.conversationEnded()){
		
					removeHandler(conversationId, id, handler);
				}
			}
			else{
				
				SendingError error = new ReceiverNotFoundSendingError(id);
				result.add(error);
			}
		}
		
		return result;
	}
	
	@Override
	public List<SendingError> sendMessage(Message message,
			ConversationHandler handler) {

		Headers headers = message.getHeaders();
		
		Long conversationId = headers.getConversationId();
		ReceiverID sender = headers.getSender();
		
		if(conversationId == null){
			conversationId = generateConversationId();
			headers.setConversationId(conversationId);
		}
		
		addHandler(conversationId, sender, handler);
		
		return sendMessage(message);
	}

	private Long generateConversationId(){
		
		return System.currentTimeMillis();
	}
	
	private void removeHandler(Long conversationId,ReceiverID receiver, ConversationHandler handler){
	
		Map<ReceiverID, ConversationHandler> conversation = conversationHandlers.get(conversationId);
		conversation.remove(receiver);
		
		if(conversation.isEmpty()){
			
			conversationHandlers.remove(conversationId);
		}
	}
	
	private void addHandler(Long conversationId,ReceiverID receiver,ConversationHandler handler){
		
		Map<ReceiverID, ConversationHandler> conversation = conversationHandlers.get(conversationId);
		if(conversation == null){
			
			conversation = new HashMap<ReceiverID, ConversationHandler>();
			conversationHandlers.put(conversationId, conversation);
		}
		
		conversation.put(receiver, handler);
	}
	
	@Override
	public void addReceiver(ReceiverID id, Receiver receiver) {

		receivers.put(id, receiver);
	}

	@Override
	public void removeReceiver(ReceiverID id) {

		receivers.remove(id);
	}

}