package deepSRL.mapping;

public abstract class MultiToken implements Mapping {
	private static final long serialVersionUID = 1L;

	protected final Sentence sentence;
	protected final String type;
	protected final Token startToken;
	protected final Token endToken;

	protected MultiToken(Sentence sentence, String type, Token startToken, Token endToken) {
		this.sentence = sentence;
		this.type = type;
		this.startToken = startToken;
		this.endToken = endToken;
	}

	public String getType() {
		return type;
	}

	@Override
	public Integer getDocumentStart() {
		return startToken.getDocumentStart();
	}

	@Override
	public Integer getDocumentEnd() {
		return endToken.getDocumentEnd();
	}

	@Override
	public String getDocumentText() {
		return sentence.document.documentText.substring(startToken.documentStart, endToken.documentEnd);
	}

	public Sentence getSentence() {
		return sentence;
	}

}
