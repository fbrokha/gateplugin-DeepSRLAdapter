package deepSRL.mapping;

import java.util.List;

public class Document extends SimpleMapping {
	private static final long serialVersionUID = 1L;

	protected String documentText;
	protected List<Sentence> sentences;

	public Document(String documentText, List<Sentence> sentences) {
		this(documentText);
		setSentences(sentences);
	}

	protected Document(String documentText) {
		super(null);
		this.deepSRLDocument = this;

		this.documentText = documentText;
		this.documentStart = 0;
		this.documentEnd = documentText.length();
	}

	protected void setSentences(List<Sentence> sentences) {
		this.sentences = DocumentBuilder.sort(sentences);

		for (Sentence sentence : this.sentences) {
			sentence.deepSRLDocument = this;
			sentence.tokens = DocumentBuilder.sort(sentence.tokens);
			for (Token token : sentence.tokens) {
				token.deepSRLDocument = this;
			}
		}
	}

	public List<Sentence> getSentences() {
		return sentences;
	}

	@Override
	public String getDocumentText() {
		return documentText;
	}

}
