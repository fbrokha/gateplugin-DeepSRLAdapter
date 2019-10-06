package deepSRL.mapping;

public abstract class SimpleMapping implements Mapping {
	private static final long serialVersionUID = 1L;

	protected final Object documentId;
	protected final Integer documentStart;
	protected final Integer documentEnd;

	protected Document document;

	protected SimpleMapping(Object documentId, Integer documentStart, Integer documentEnd) {
		this(documentId, documentStart, documentEnd, null);
	}

	protected SimpleMapping(Object documentId, Integer documentStart, Integer documentEnd, Document document) {
		this.documentId = documentId;
		this.documentStart = documentStart;
		this.documentEnd = documentEnd;
		this.document = document;
	}

	@SuppressWarnings("unchecked")
	public <C> C getDocumentId() {
		return (C) documentId;
	}

	@Override
	public Integer getDocumentStart() {
		return documentStart;
	}

	@Override
	public Integer getDocumentEnd() {
		return documentEnd;
	}

	@Override
	public String getDocumentText() {
		return document.documentText.substring(documentStart, documentEnd);
	}

}
