package jams.message.bus;

import jams.message.Message;
import jams.message.Receiver;
import jams.message.ReceiverID;

import java.util.List;

public interface Bus {

	public List<SendingError> sendMessage(Message message);
	
	public void addReceiver(ReceiverID id, Receiver receiver);
	public void removeReceiver(ReceiverID id);
}
