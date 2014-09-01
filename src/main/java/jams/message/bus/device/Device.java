package jams.message.bus.device;

import jams.message.Message;
import jams.message.ReceiverID;
import jams.message.bus.Bus;

import java.util.List;

public interface Device {

	public DeviceID getId();
	
	public void deliverMessage(Message message, List<ReceiverID> receivers);
	public void setBus(Bus bus);
}
