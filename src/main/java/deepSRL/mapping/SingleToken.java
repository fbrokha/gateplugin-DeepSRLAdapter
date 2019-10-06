package deepSRL.mapping;

public abstract class SingleToken extends SimpleMapping {
	private static final long serialVersionUID = 1L;

	protected Sentence sentence;

	protected SingleToken(Object documentId, Integer documentStart, Integer documentEnd, Document document) {
		super(documentId, documentStart, documentEnd, document);
	}

	protected SingleToken(Object documentId, Integer documentStart, Integer documentEnd) {
		super(documentId, documentStart, documentEnd);
	}

	public Sentence getSentence() {
		return sentence;
	}

	public Integer getDocumentSentenceStart() {
		return documentStart - sentence.documentStart;
	}

	public Integer getDocumentSentenceEnd() {
		return documentEnd - sentence.documentEnd;
	}

}