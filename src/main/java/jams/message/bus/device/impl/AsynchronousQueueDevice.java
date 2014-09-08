package jams.message.bus.device.impl;

import java.util.LinkedList;
import java.util.List;

import jams.message.Message;
import jams.message.ReceiverID;
import jams.message.bus.Bus;
import jams.message.bus.device.Device;
import jams.message.bus.device.DeviceID;

public class AsynchronousQueueDevice implements Device {

	private DeviceID id;
	private Bus bus;
	private List<Message> queue;
	
	public AsynchronousQueueDevice(DeviceID id) {
		
		this.id = id;
		this.queue = new LinkedList<Message>();
	}

	@Override
	public DeviceID getId() {
		return id;
	}

	@Override
	public void setBus(Bus bus) {
	
		this.bus = bus;
	}
	
	@Override
	public void deliverMessage(Message message, List<ReceiverID> receivers) {

		queue.add(message);
	}
	
	public List<Message> pollAll(){
		
		List<Message> result = queue;
		queue = new LinkedList<Message>();
		
		return result;
	}
	
	public boolean isEmpty(){
		
		return queue.isEmpty();
	}
	
	public void sendMessage(Message message){
		
		
		bus.sendMessage(message);
	}

	public void registerReceiver(ReceiverID receiverID){
		
		bus.addReceiver(receiverID, id);
	}
	
	public void unregisterReceiver(ReceiverID receiverID){
		
		bus.removeReceiver(receiverID, id);
	}
	
	public boolean isRegistered(ReceiverID receiver){
		
		return bus.isRegistered(receiver, id);
	}
}
