package main.java.deepSRL.mapping;


public class SrlArgumentToken extends MultiToken {
	private static final long serialVersionUID = 1L;

	protected SrlVerbToken verbToken;

	protected SrlArgumentToken(Sentence sentence, String type, Token startToken, Token endToken) {
		super(sentence, type, startToken, endToken);
	}

	@Override
	public String getType() {
		return type;
	}

	public SrlVerbToken getVerb() {
		return verbToken;
	}

}