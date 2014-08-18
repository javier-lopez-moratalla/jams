package jams.message;

public interface Receiver {

	public ReceiverID getId();
	public void receiveMessage(Message message);
}
