package jams.message.bus;

import jams.message.Message;
import jams.message.Receiver;
import jams.message.ReceiverID;
import jams.message.bus.device.Device;
import jams.message.bus.device.DeviceID;
import jams.message.conversation.ConversationHandler;

import java.util.List;

public interface Bus {

	public Message createMessage(ReceiverID sender,List<ReceiverID> receivers);
	public Message createMessage(ReceiverID sender,ReceiverID receiver);
	
	public Message createResponse(Message message);
	
	public void sendMessage(Message message);
	public void sendMessage(Message message, ConversationHandler handler);
	
	public void addReceiver(ReceiverID id, Receiver receiver);
	public void addReceiver(ReceiverID receiver, DeviceID device);
	
	public void removeReceiver(ReceiverID id);
	public void removeReceiver(ReceiverID receiver, DeviceID device);

	public void addDevice(DeviceID id, Device device);
	public void removeDevice(DeviceID id);
}
