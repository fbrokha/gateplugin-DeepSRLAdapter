package deepSRL.mapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Parser {

	private static final int SRL_ARGUMENT_TYPE = 0;
	private static final int SRL_ARG_START = 1;
	private static final int SRL_ARG_END = 2;

	private static final ObjectMapper MAPPER;
	static {
		JsonFactory jf = new JsonFactory();
		jf.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
		jf.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
		MAPPER = new ObjectMapper(jf);
	}

	public static boolean writeDocument(OutputStream outputStream, Document document) throws IOException {
		List<Map<String, Object>> request = new ArrayList<>();

		for (Sentence sentence : document.getSentences()) {
			Map<String, Object> sentenceValues = new LinkedHashMap<>();
			List<String> tokens = new ArrayList<>();
			for (Token token : sentence.getTokens()) {
				String tokenText = token.getDocumentText();
				if (tokenText.length() > 0) {
					tokens.add(tokenText);
				}
			}
			if (!tokens.isEmpty()) {
				sentenceValues.put("tokens", tokens);
				if (sentence.isPredefinedVerbs()) {
					List<Integer> predefinedVerbIndexes = new ArrayList<>();
					for (int i = 0; i < sentence.getTokens().size(); i++) {
						if (sentence.getTokens().get(i).isPredefinedVerb()) {
							predefinedVerbIndexes.add(i);
						}
					}
					sentenceValues.put("verbs", predefinedVerbIndexes);
				}
			}
			if (!sentenceValues.isEmpty()) {
				request.add(sentenceValues);
			}
		}
		if (request.isEmpty()) {
			return false;
		}
		MAPPER.writeValue(outputStream, request);
		outputStream.write('\n');
		outputStream.flush();
		return true;
	}

	public static void extractSRL(InputStream inputStream, Document document) throws IOException {
		Object response = MAPPER.readValue(inputStream, Object.class);
		if (response instanceof List) {
			@SuppressWarnings("unchecked")
			List<List<Map<String, List<List<?>>>>> sentences = (List<List<Map<String, List<List<?>>>>>) response;
			for (int sentenceIndex = 0; sentenceIndex < sentences.size(); sentenceIndex++) {
				List<Map<String, List<List<?>>>> sentence = sentences.get(sentenceIndex);
				Sentence documentSentence = document.getSentences().get(sentenceIndex);
				for (Map<String, List<List<?>>> predicates : sentence) {
					for (Entry<String, List<List<?>>> entry : predicates.entrySet()) {
						Token token = documentSentence.getTokens().get(Integer.valueOf(entry.getKey()));
						SrlPredicateToken predicateToken = new SrlPredicateToken(documentSentence, token);

						List<SrlArgumentToken> argumentTokens = new ArrayList<SrlArgumentToken>();
						for (List<?> srlValue : entry.getValue()) {
							String type = (String) srlValue.get(SRL_ARGUMENT_TYPE);
							Integer startTokenIndex = (Integer) srlValue.get(SRL_ARG_START);
							Integer endTokenIndex = (Integer) srlValue.get(SRL_ARG_END);
							Token startToken = documentSentence.getTokens().get(startTokenIndex);
							Token endToken = documentSentence.getTokens().get(endTokenIndex);
							SrlArgumentToken argumentToken = new SrlArgumentToken(documentSentence, type, startToken,
									endToken);
							argumentTokens.add(argumentToken);
						}

						predicateToken.addArguments(argumentTokens);
						documentSentence.srlPredicates.add(predicateToken);
					}
				}
			}
		} else {
			throw new IllegalStateException("malformed response: " + response);
		}
	}

}
