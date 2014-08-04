package jams.message.bus.impl;

import jams.message.ReceiverID;
import jams.message.bus.SendingError;

public class ReceiverNotFoundSendingError implements SendingError {

	private ReceiverID receiverID;

	public ReceiverID getReceiverID() {
		return receiverID;
	}
	
	public ReceiverNotFoundSendingError(ReceiverID receiverID) {
		super();
		this.receiverID = receiverID;
	}
}
