package jams.message.conversation.state;

import jams.message.Message;

public interface State {

	public State execute(Message input);
	public boolean isFinalState();
}
