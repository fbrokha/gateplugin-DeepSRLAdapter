package deepSRL.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class DocumentBuilder {

	public static final Integer DEEPSRL_MAX_SENTENCE_SIZE = 1024;
	public static final String DEEPSRL_SENTENCESPLIT = "\n";
	public static final String DEEPSRL_TOKENSPLIT = " ";
	public static final String DEEPSRL_DOCUMENT_END = "\n\n";

	private static Comparator<SimpleMapping> MAPPING_COMPARATOR = new Comparator<SimpleMapping>() {
		@Override
		public int compare(SimpleMapping o1, SimpleMapping o2) {
			if (o1.getDocumentEnd() < o2.getDocumentStart()) {
				return -1;
			} else if (o1.getDocumentStart() > o2.getDocumentEnd()) {
				return 1;
			}
			return 0;
		}
	};

	protected static void calculateDeepSRLTextAndOffsets(Document document) {
		StringBuilder deepSRLText = new StringBuilder();
		Integer sentenceDeepSRLOffset = 0;
		Sentence previousSentence = null;
		for (Sentence sentence : document.sentences) {
			StringBuilder sentenceDeepSRLText = new StringBuilder();
			if (!sentence.tokens.isEmpty()) {
				Integer tokenDeepSRLOffset = sentenceDeepSRLOffset;
				Token previousToken = null;
				Iterator<Token> tokenIterator = sentence.tokens.iterator();
				while (tokenIterator.hasNext()) {
					Token token = tokenIterator.next();
					String tokenDeepSRLText = token.getDocumentText().replaceAll(DEEPSRL_TOKENSPLIT, "")
							.replaceAll(DEEPSRL_SENTENCESPLIT, "");
					if (previousToken != null) {
						sentenceDeepSRLText.append(DEEPSRL_TOKENSPLIT);
					}
					sentenceDeepSRLText.append(tokenDeepSRLText);

					token.deepSRLStart = tokenDeepSRLOffset;
					token.deepSRLEnd = token.deepSRLStart + tokenDeepSRLText.length();
					tokenDeepSRLOffset = token.deepSRLEnd + DEEPSRL_TOKENSPLIT.length();

					previousToken = token;
				}
			} else {
				sentenceDeepSRLText.append(sentence.getDocumentText().replaceAll(DEEPSRL_SENTENCESPLIT, ""));
			}
			if (previousSentence != null) {
				deepSRLText.append(DEEPSRL_SENTENCESPLIT);
			}
//			if (sentenceDeepSRLText.length() > DEEPSRL_MAX_SENTENCE_SIZE) {
//				throw new IllegalStateException(
//						"Sentence too long, max size is " + DEEPSRL_MAX_SENTENCE_SIZE + "\n" + sentenceDeepSRLText);
//			}
			deepSRLText.append(sentenceDeepSRLText.toString());

			sentence.deepSRLStart = sentenceDeepSRLOffset;
			sentence.deepSRLEnd = sentence.deepSRLStart + sentenceDeepSRLText.length();
			sentenceDeepSRLOffset = sentence.deepSRLEnd + DEEPSRL_SENTENCESPLIT.length();
			previousSentence = sentence;
		}
		document.deepSRLText = deepSRLText.append(DEEPSRL_SENTENCESPLIT).toString();
	}

	protected static <M extends SimpleMapping> List<M> sort(Collection<M> mappings) {
		List<M> sortedMappings = new ArrayList<>(mappings);
		Collections.sort(sortedMappings, MAPPING_COMPARATOR);
		return sortedMappings;
	}

}
