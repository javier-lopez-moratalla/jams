package jams.message.bus;

import jams.message.Message;
import jams.message.Receiver;
import jams.message.ReceiverID;
import jams.message.conversation.ConversationHandler;

import java.util.List;

public interface Bus {

	public Message createMessage(ReceiverID sender,List<ReceiverID> receivers);
	public Message createMessage(ReceiverID sender,ReceiverID receiver);
	
	public Message createResponse(Message message);
	
	public void sendMessage(Message message);
	public void sendMessage(Message message, ConversationHandler handler);
	
	public void addReceiver(ReceiverID id, Receiver receiver);

	public void removeReceiver(ReceiverID id);

}
