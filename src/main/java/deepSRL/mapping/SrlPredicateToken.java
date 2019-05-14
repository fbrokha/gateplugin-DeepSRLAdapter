package deepSRL.mapping;

import java.util.ArrayList;
import java.util.List;

public class SrlPredicateToken extends Token {
	private static final long serialVersionUID = 1L;

	protected Token token;
	protected List<SrlArgumentToken> arguments = new ArrayList<>();

	protected SrlPredicateToken(Sentence sentence, Token token) {
		super(sentence, token.documentStart, token.documentEnd);
		this.token = token;
	}

	public Token getToken() {
		return token;
	}

	public List<SrlArgumentToken> getArguments() {
		return arguments;
	}

	protected void addArguments(List<SrlArgumentToken> arguments) {
		this.arguments.addAll(arguments);
		for (SrlArgumentToken argument : arguments) {
			argument.predicateToken = this;
		}
	}

}