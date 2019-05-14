package deepSRL.mapping;

import java.io.Serializable;

public interface Mapping extends Serializable {

	Integer getDocumentStart();

	Integer getDocumentEnd();

	String getDocumentText();

}
