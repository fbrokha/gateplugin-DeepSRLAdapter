package deepSRL.mapping;

import java.util.ArrayList;
import java.util.List;

public class Sentence extends SimpleMapping {
	private static final long serialVersionUID = 1L;

	protected final List<Token> tokens;
	protected final boolean predefinedVerbs;

	protected final List<SrlPredicateToken> srlPredicates = new ArrayList<>();

	public Sentence(Object documentId, Integer documentStart, Integer documentEnd, List<Token> tokens,
			boolean predefinedVerbs) {
		super(documentId, documentStart, documentEnd);

		this.tokens = DocumentBuilder.sort(tokens);
		for (Token token : this.tokens) {
			token.sentence = this;
		}

		this.predefinedVerbs = predefinedVerbs;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public List<SrlPredicateToken> getSrlPredicates() {
		return srlPredicates;
	}

	public boolean isPredefinedVerbs() {
		return predefinedVerbs;
	}

}