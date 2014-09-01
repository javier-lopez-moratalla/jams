package jams.message.bus.impl;

import jams.message.Headers;
import jams.message.Message;
import jams.message.Receiver;
import jams.message.ReceiverID;
import jams.message.bus.Bus;
import jams.message.bus.SendingError;
import jams.message.bus.device.Device;
import jams.message.bus.device.DeviceID;
import jams.message.conversation.ConversationHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


public class BusImpl implements Bus {

	private Map<ReceiverID, Receiver> receivers;
	private Map<Long,Map<ReceiverID,ConversationHandler>> conversationHandlers;
	
	private Map<DeviceID, Device> devices;
	private Map<ReceiverID, DeviceID> externalReceivers;
	private Map<DeviceID,List<ReceiverID>> deviceContent;
	
	private Object lockQueue;
	private Queue<Message> queue;
	
	public BusImpl() {
	
		receivers = new HashMap<ReceiverID, Receiver>();
		conversationHandlers = new HashMap<Long, Map<ReceiverID,ConversationHandler>>();
		
		devices = new HashMap<DeviceID, Device>();
		externalReceivers = new HashMap<ReceiverID, DeviceID>();
		deviceContent = new HashMap<DeviceID, List<ReceiverID>>();
		
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
		
		Map<DeviceID,List<ReceiverID>> receiversByDevice = new HashMap<DeviceID, List<ReceiverID>>();
		
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
				
				DeviceID device = externalReceivers.get(id);
				
				if(device != null){
					
					List<ReceiverID> deviceReceivers = receiversByDevice.get(device);
					if(deviceReceivers== null){
						
						deviceReceivers = new LinkedList<ReceiverID>();
						receiversByDevice.put(device, deviceReceivers);
					}
					
					deviceReceivers.add(id);
				}
				else{
				
					SendingError error = new ReceiverNotFoundSendingError(id);
					result.add(error);
				}
			}
			
			for(Map.Entry<DeviceID, List<ReceiverID>> entry:receiversByDevice.entrySet()){
				
				DeviceID deviceID = entry.getKey();
				List<ReceiverID> deviceReceivers = entry.getValue();
				
				Device device = devices.get(deviceID);
				device.deliverMessage(message, deviceReceivers);
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

	@Override
	public void addDevice(DeviceID id, Device device) {
	
		devices.put(id, device);
		deviceContent.put(id, new LinkedList<ReceiverID>());
		device.setBus(this);
	}
	
	@Override
	public void addReceiver(ReceiverID receiver, DeviceID device) {
	
		if(!devices.containsKey(device)){
			
			throw new DeviceNotFoundException(device);
		}
		
		externalReceivers.put(receiver, device);
		deviceContent.get(device).add(receiver);
	}
	
	@Override
	public void removeDevice(DeviceID id) {
	
		devices.remove(id);
		List<ReceiverID> deviceReceivers = deviceContent.remove(id);
		
		for(ReceiverID receiverID:deviceReceivers){
			
			externalReceivers.remove(receiverID);
		}
	}
	
	@Override
	public void removeReceiver(ReceiverID receiver, DeviceID device) {
	
		externalReceivers.remove(receiver);
		deviceContent.get(device).remove(receiver);
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