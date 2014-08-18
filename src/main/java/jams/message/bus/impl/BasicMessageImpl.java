package jams.message.bus.impl;

import jams.message.Body;
import jams.message.Headers;
import jams.message.Message;
import jams.message.ReceiverID;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BasicMessageImpl implements Message {

	private Headers headers;
	private Body body;
	
	public BasicMessageImpl(){
		
		headers = new HeadersImpl();
		body = new BodyImpl();
	}
	
	@Override
	public Headers getHeaders() {
	
		return headers;
	}

	@Override
	public Body getBody() {

		return body;
	}
	
	private class HeadersImpl implements Headers{
		
		private Map<String,String> headers;
		private Long conversation;
		private ReceiverID sender;
		private List<ReceiverID> receivers;
		
		public HeadersImpl(){
			
			headers = new HashMap<String, String>();
			conversation = null;
			sender = null;
			receivers = new LinkedList<ReceiverID>();
		}
		
		@Override
		public Long getConversationId() {

			return conversation;
		}
		
		@Override
		public void setConversationId(Long conversationId) {
		
			this.conversation = conversationId;
		}
		
		@Override
		public String getHeader(String header) {
		
			return headers.get(header);
		}
		
		@Override
		public List<ReceiverID> getReceivers() {
		
			return receivers;
		}
		
		@Override
		public ReceiverID getSender() {
		
			return sender;
		}
		
		@Override
		public void setReceivers(List<ReceiverID> receivers) {
			this.receivers = receivers;
		}
		
		@Override
		public void setSender(ReceiverID sender) {
			this.sender = sender;
		}
		
		@Override
		public void setHeader(String header,String value){
			
			headers.put(header, value);
		}
	}

	private class BodyImpl implements Body{
		
		private String semantica;
		private Object content;
		
		public String getSemantica() {
			return semantica;
		}
		@Override
		public void setSemantica(String semantica) {
			this.semantica = semantica;
		}
		public Object getContent() {
			return content;
		}
		@Override
		public void setContent(Object content) {
			this.content = content;
		}
	}
}
