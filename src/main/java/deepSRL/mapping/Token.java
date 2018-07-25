package deepSRL.mapping;

import java.util.ArrayList;
import java.util.List;


public class Token extends SimpleMapping {
	private static final long serialVersionUID = 1L;

	protected Sentence sentence;

	protected List<String> srlValues = new ArrayList<String>();

	public Token(Object documentId, Integer documentStart, Integer documentEnd) {
		super(null);
		this.documentId = documentId;
		this.documentStart = documentStart;
		this.documentEnd = documentEnd;
	}

	protected Token(Sentence sentence, Integer deepSRLStart, Integer deepSRLEnd, Integer documentStart,
			Integer documentEnd) {
		super(sentence.deepSRLDocument);
		this.sentence = sentence;
		this.deepSRLStart = deepSRLStart;
		this.deepSRLEnd = deepSRLEnd;
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

	public Integer getDeepSRLSentenceStart() {
		return deepSRLStart - sentence.deepSRLStart;
	}

	public Integer getDeepSRLSentenceEnd() {
		return deepSRLEnd - sentence.deepSRLEnd;
	}

	public List<String> getSrlValues() {
		return srlValues;
	}

	public String getSrlValue(Integer verbNumber) {
		return srlValues.get(verbNumber);
	}

}