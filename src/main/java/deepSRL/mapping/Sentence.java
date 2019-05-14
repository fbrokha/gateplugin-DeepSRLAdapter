package deepSRL.mapping;

import java.util.ArrayList;
import java.util.List;

public class Sentence extends SimpleMapping {
	private static final long serialVersionUID = 1L;

	protected List<Token> tokens = new ArrayList<>();
	protected List<SrlPredicateToken> srlPredicates = new ArrayList<>();

	public Sentence(Object documentId, Integer documentStart, Integer documentEnd) {
		super(null);
		this.documentId = documentId;
		this.documentStart = documentStart;
		this.documentEnd = documentEnd;
	}

	public Sentence(Object documentId, Integer documentStart, Integer documentEnd, List<Token> tokens) {
		super(null);
		this.documentId = documentId;
		this.documentStart = documentStart;
		this.documentEnd = documentEnd;

		this.tokens = DocumentBuilder.sort(tokens);

		for (Token token : this.tokens) {
			token.sentence = this;
		}
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public List<SrlPredicateToken> getSrlPredicates() {
		return srlPredicates;
	}

	protected void addToken(Token token) {
		tokens.add(token);
		token.sentence = this;
	}

}