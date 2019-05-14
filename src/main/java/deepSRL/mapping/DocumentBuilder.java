package deepSRL.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DocumentBuilder {

	public static final Integer DEEPSRL_MAX_SENTENCE_SIZE = 1024;
	public static final String DEEPSRL_SENTENCESPLIT = "\n";
	public static final String DEEPSRL_TOKENSPLIT = " ";
	public static final String DEEPSRL_DOCUMENT_END = "\n\n";

	private static Comparator<SimpleMapping> MAPPING_COMPARATOR = new Comparator<SimpleMapping>() {
		@Override
		public int compare(SimpleMapping o1, SimpleMapping o2) {
			if (o1.getDocumentEnd() == o2.getDocumentStart()) {
				return 0;
			} else if (o1.getDocumentEnd() > o2.getDocumentStart()) {
				return 1;
			} else if (o1.getDocumentStart() < o2.getDocumentEnd()) {
				return -1;
			}
			return 0;
		}
	};

	protected static <M extends SimpleMapping> List<M> sort(Collection<M> mappings) {
		List<M> sortedMappings = new ArrayList<>(mappings);
		Collections.sort(sortedMappings, MAPPING_COMPARATOR);
		return sortedMappings;
	}

}
