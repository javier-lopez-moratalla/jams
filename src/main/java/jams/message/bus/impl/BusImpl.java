package jams.message.bus.impl;

import jams.message.Message;
import jams.message.Receiver;
import jams.message.ReceiverID;
import jams.message.bus.Bus;
import jams.message.bus.SendingError;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BusImpl implements Bus {

	private Map<ReceiverID, Receiver> receivers;
	
	public BusImpl() {
	
		receivers = new HashMap<ReceiverID, Receiver>();
	}
	
	@Override
	public List<SendingError> sendMessage(Message message) {

		List<SendingError> result = new LinkedList<SendingError>();
		
		List<ReceiverID> receiverIDs = message.getHeaders().getReceivers();
		for(ReceiverID id:receiverIDs){
			
			Receiver receiver = receivers.get(id);
			
			if(receiver != null){
				
				receiver.receiveMesssage(message);
			}
			else{
				
				SendingError error = new ReceiverNotFoundSendingError(id);
				result.add(error);
			}
		}
		
		return result;
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
