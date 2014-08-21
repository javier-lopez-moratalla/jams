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
import java.util.Queue;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

public class BusImpl implements Bus {

	private Map<ReceiverID, Receiver> receivers;
	private Map<Long,Map<ReceiverID,ConversationHandler>> conversationHandlers;
	
	private Object lockQueue;
	private Queue<Message> queue;
	
	public BusImpl() {
	
		receivers = new HashMap<ReceiverID, Receiver>();
		conversationHandlers = new HashMap<Long, Map<ReceiverID,ConversationHandler>>();
		
		lockQueue = new Object();
		queue = new LinkedList<>();
		
		Thread processingThread = new Thread(new ProcessingThread(1000));
		processingThread.setDaemon(true);
		processingThread.start();
	}
	
	@Override
	public Message createMessage(ReceiverID sender, List<ReceiverID> receivers) {
	
		Message message = new BasicMessageImpl();
		
		Headers headers = message.getHeaders();
		headers.setSender(sender);
		headers.setReceivers(receivers);
		
		return message;
	}
	
	@Override
	public Message createMessage(ReceiverID sender, ReceiverID receiver) {
	
		Message message = new BasicMessageImpl();
		
		List<ReceiverID> receivers = new LinkedList<ReceiverID>();
		receivers.add(receiver);
		
		Headers headers = message.getHeaders();
		headers.setSender(sender);
		headers.setReceivers(receivers);
		
		return message;
	}
	
	@Override
	public Message createResponse(Message message) {
	
		Message response = new BasicMessageImpl();
		
		List<ReceiverID> receivers = new LinkedList<>();
		receivers.add(message.getHeaders().getSender());

		response.getHeaders().setReceivers(receivers);
		response.getHeaders().setConversationId(message.getHeaders().getConversationId());
		
		return response;
	}
	
	@Override
	public void sendMessage(Message message) {
		
		synchronized (lockQueue) {
		
			queue.add(message);
		}
	}
	
	private void processMessage(Message message){

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
	}
	
	@Override
	public void sendMessage(Message message,
			ConversationHandler handler) {

		Headers headers = message.getHeaders();
		
		Long conversationId = headers.getConversationId();
		ReceiverID sender = headers.getSender();
		
		if(conversationId == null){
			conversationId = generateConversationId();
			headers.setConversationId(conversationId);
		}
		
		addHandler(conversationId, sender, handler);
		
		sendMessage(message);
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

	private class ProcessingThread implements Runnable{
		
		private long delay;
		;
		private boolean stop;
		private Object lockStop;
		
		public ProcessingThread(long delay) {
		
			this.delay = delay;
			stop = true;
			lockStop = new Object();
		}
		
		public boolean isStop(){
			
			synchronized (lockStop) {
			
				return stop;
			}
		}
		
		public void setStop(boolean newStop){
			
			synchronized (lockStop) {
			
				stop = newStop;
			}
		}
		
		@Override
		public void run() {
					
			setStop(false);
 
			boolean actualStop = isStop();
			
			while(!actualStop){	
				
				Message message;
				
				synchronized (lockQueue) {
				
					message = queue.poll();
				}
				
				if(message != null){
					
					processMessage(message);
					
				}
				else{
					
					try{
						Thread.sleep(delay);
					}
					catch(InterruptedException e){
						
						e.printStackTrace();
					}
				}
				
				actualStop = isStop();
			}
		}
	}
}