package jams.message;

public interface Receiver {

	public ReceiverID getId();
	public void receiveMesssage(Message message);
}
