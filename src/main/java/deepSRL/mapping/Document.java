package deepSRL.mapping;

import java.util.List;

public class Document extends SimpleMapping {
	private static final long serialVersionUID = 1L;

	protected final String documentText;
	protected final List<Sentence> sentences;

	public Document(String documentText, List<Sentence> sentences) {
		super(null, 0, documentText.length());
		this.document = this;
		this.documentText = documentText;
		this.sentences = DocumentBuilder.sort(sentences);

		for (Sentence sentence : this.sentences) {
			sentence.document = this;
			for (Token token : sentence.tokens) {
				token.document = this;
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
