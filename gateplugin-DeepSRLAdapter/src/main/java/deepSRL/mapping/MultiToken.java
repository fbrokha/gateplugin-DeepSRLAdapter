package main.java.deepSRL.mapping;

public class MultiToken implements Mapping {
	private static final long serialVersionUID = 1L;

	protected Sentence sentence;
	protected String type;
	protected Token startToken;
	protected Token endToken;

	protected MultiToken(Sentence sentence, String type, Token startToken) {
		this(sentence, type, startToken, null);
	}

	protected MultiToken(Sentence sentence, String type, Token startToken,
			Token endToken) {
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

	@Override
	public Document getDeepSRLDocument() {
		return sentence.deepSRLDocument;
	}

	@Override
	public Integer getDeepSRLStart() {
		return startToken.getDeepSRLStart();
	}

	@Override
	public Integer getDeepSRLEnd() {
		return endToken.getDeepSRLEnd();
	}

	@Override
	public String getDeepSRLText() {
		return sentence.deepSRLDocument.deepSRLText.substring(startToken.deepSRLStart, endToken.deepSRLEnd);
	}

}
