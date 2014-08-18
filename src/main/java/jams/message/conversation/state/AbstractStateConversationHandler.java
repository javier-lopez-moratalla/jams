package jams.message.conversation.state;

import jams.message.Message;
import jams.message.bus.Bus;
import jams.message.conversation.ConversationHandler;

public abstract class AbstractStateConversationHandler implements ConversationHandler{
	
	private Bus messageBus;
	private State state;
	protected abstract State getInitialState(Bus bus);
	
	public AbstractStateConversationHandler() {
		super();
	
		state = getInitialState(messageBus);
	}

	@Override
	public void receiveMessage(Message message) {
		
		state = state.execute(message);
	}
	
	@Override
	public boolean conversationEnded() {
	
		return state.isFinalState();
	}
}
