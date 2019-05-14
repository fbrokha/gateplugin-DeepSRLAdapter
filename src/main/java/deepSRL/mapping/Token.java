package deepSRL.mapping;

public class Token extends SimpleMapping {
	private static final long serialVersionUID = 1L;

	protected Sentence sentence;

	public Token(Object documentId, Integer documentStart, Integer documentEnd) {
		super(null);
		this.documentId = documentId;
		this.documentStart = documentStart;
		this.documentEnd = documentEnd;
	}

	protected Token(Sentence sentence, Integer documentStart, Integer documentEnd) {
		super(sentence.deepSRLDocument);
		this.sentence = sentence;
		this.documentStart = documentStart;
		this.documentEnd = documentEnd;
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