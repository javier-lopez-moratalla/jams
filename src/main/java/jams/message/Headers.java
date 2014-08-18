package jams.message;

import java.util.List;

public interface Headers {

	public String getHeader(String header);	
	
	public List<ReceiverID>getReceivers();
	public ReceiverID getSender();
	
	public Long getConversationId();
	public void setConversationId(Long conversationId);

	public void setHeader(String header, String value);

	public void setSender(ReceiverID sender);

	public void setReceivers(List<ReceiverID> receivers);
}
