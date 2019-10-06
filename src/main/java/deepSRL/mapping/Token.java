package deepSRL.mapping;

public class Token extends SingleToken {
	private static final long serialVersionUID = 1L;

	protected final boolean predefinedVerb;

	public Token(Object documentId, Integer documentStart, Integer documentEnd, boolean predefinedVerb) {
		super(documentId, documentStart, documentEnd);
		this.predefinedVerb = predefinedVerb;
	}

	public Token(Object documentId, Integer documentStart, Integer documentEnd) {
		this(documentId, documentStart, documentEnd, false);
	}

	public boolean isPredefinedVerb() {
		return predefinedVerb;
	}

}