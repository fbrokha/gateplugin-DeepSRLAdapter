package deepSRL.mapping;

public class MultiToken implements Mapping {
	private static final long serialVersionUID = 1L;

	protected Sentence sentence;
	protected String type;
	protected Token startToken;
	protected Token endToken;

	protected MultiToken(Sentence sentence, String type, Token startToken) {
		this(sentence, type, startToken, null);
	}

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
		return sentence.deepSRLDocument.documentText.substring(startToken.documentStart, endToken.documentEnd);
	}

	public Sentence getSentence() {
		return sentence;
	}

}
