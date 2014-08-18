package jams.message;

public interface Body {

	public String getSemantica();
	public Object getContent();
	public void setContent(Object content);
	public void setSemantica(String semantica);
}
