package jams.message.conversation;

import jams.message.Receiver;

public interface ConversationHandler extends Receiver{

	public boolean conversationEnded();
}
