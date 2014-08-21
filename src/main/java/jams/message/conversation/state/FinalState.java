package jams.message.conversation.state;

import jams.message.Message;

public class FinalState implements State {

	@Override
	public State execute(Message input) {
		
		return this;
	}

	@Override
	public boolean isFinalState() {

		return true;
	}

}
