package main.java.deepSRL.mapping;

public abstract class SimpleMapping implements Mapping {
	private static final long serialVersionUID = 1L;

	protected Object documentId;
	protected Integer documentStart;
	protected Integer documentEnd;

	protected Document deepSRLDocument;
	protected Integer deepSRLStart;
	protected Integer deepSRLEnd;

	protected SimpleMapping(Document document) {
		this.deepSRLDocument = document;
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
		return deepSRLDocument.documentText.substring(documentStart, documentEnd);
	}

	@Override
	public Document getDeepSRLDocument() {
		return deepSRLDocument;
	}

	@Override
	public Integer getDeepSRLStart() {
		return deepSRLStart;
	}

	@Override
	public Integer getDeepSRLEnd() {
		return deepSRLEnd;
	}

	@Override
	public String getDeepSRLText() {
		return deepSRLDocument.deepSRLText.substring(deepSRLStart, deepSRLEnd);
	}

}
