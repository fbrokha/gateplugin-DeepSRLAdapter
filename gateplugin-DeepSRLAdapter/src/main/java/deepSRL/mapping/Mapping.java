package main.java.deepSRL.mapping;

import java.io.Serializable;

public interface Mapping extends Serializable {

	Integer getDocumentStart();

	Integer getDocumentEnd();

	String getDocumentText();

	Document getDeepSRLDocument();

	Integer getDeepSRLStart();

	Integer getDeepSRLEnd();

	String getDeepSRLText();

}
