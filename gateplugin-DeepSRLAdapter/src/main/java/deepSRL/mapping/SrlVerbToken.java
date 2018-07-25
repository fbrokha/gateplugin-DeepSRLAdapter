package main.java.deepSRL.mapping;

import java.util.ArrayList;
import java.util.List;


public class SrlVerbToken extends MultiToken {
	private static final long serialVersionUID = 1L;

	protected List<SrlArgumentToken> arguments = new ArrayList<>();

	protected SrlVerbToken(Sentence sentence, String type, Token startToken, Token endToken) {
		super(sentence, type, startToken, endToken);
	}

	public List<SrlArgumentToken> getArguments() {
		return arguments;
	}

	protected void addArguments(List<SrlArgumentToken> arguments) {
		this.arguments.addAll(arguments);
		for (SrlArgumentToken argument : arguments) {
			argument.verbToken = this;
		}
	}

}