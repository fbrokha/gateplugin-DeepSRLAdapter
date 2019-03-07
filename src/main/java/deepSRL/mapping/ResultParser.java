package deepSRL.mapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ResultParser {

	private static final String SENTENCE_START_REGEX = "\\[\\[\\[";
	private static final String PREDICATE_SPLIT_REGEX = "\\]\\], \\[\\[";
	private static final String TOKEN_SPLIT_REGEX = "\\], \\[";
	private static final String LABEL_SPLIT_REGEX = ", ";
	private static final String SRL_VERB_TYPE = "V";
	private static final String CHAR_MARKS = "'";
	private static final String BACKSLASH = "\\";
	private static final String EMPTY_STRING = "";
	private static final String COMP_DONE = "computationdone";
	private static final String EMPTY_SRL = "empty";
	private static final int SRL_ARGUMENT_TYPE = 0;
	private static final int SRL_ARG_START = 1;
	private static final int SRL_ARG_END = 2;

	public static void extractSRL(Document document, InputStream inputStream) throws IOException {
		int sentenceNumber = 0;
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.matches(COMP_DONE)) {
				break;
			} else if (line.contentEquals(EMPTY_SRL)) {
				sentenceNumber += 1;
				continue;
			} else if (!line.startsWith(SENTENCE_START_REGEX.replace(BACKSLASH, EMPTY_STRING))) {
				continue;
			} else if (sentenceNumber <= document.getSentences().size()) {
				Sentence sentence = document.getSentences().get(sentenceNumber);
				parseSRL(sentence, line);
				sentenceNumber += 1;
			} else {
				throw new IllegalStateException(
						"Number of extracted sentences does not match number of sentences in document");
			}
		}
	}

	private static void parseSRL(Sentence sentence, String line) {
		List<String> sentence_predicates = new ArrayList<String>();
		List<String> tokens = new ArrayList<String>();
		List<String> tokenLabels = new ArrayList<String>();
		int verbcount = 0;
		line = line.substring(3, line.length() - 3);
		sentence_predicates.addAll(Arrays.asList(line.split(PREDICATE_SPLIT_REGEX)));
		Iterator<String> predicateIterator = sentence_predicates.iterator();
		while (predicateIterator.hasNext()) {
			tokens.addAll(Arrays.asList(predicateIterator.next().split(TOKEN_SPLIT_REGEX)));
			Iterator<String> tokenIterator = tokens.iterator();
			List<SrlArgumentToken> arguments = new ArrayList<SrlArgumentToken>();
			while (tokenIterator.hasNext()) {
				tokenLabels.addAll(Arrays.asList(tokenIterator.next().split(LABEL_SPLIT_REGEX)));
				if (tokenLabels.get(SRL_ARGUMENT_TYPE).replaceAll(CHAR_MARKS, "").contentEquals(SRL_VERB_TYPE)) {
					SrlVerbToken verb = new SrlVerbToken(sentence, SRL_VERB_TYPE,
							sentence.getTokens().get(Integer.valueOf(tokenLabels.get(SRL_ARG_START))),
							sentence.getTokens().get(Integer.valueOf(tokenLabels.get(SRL_ARG_END))));
					sentence.srlVerbs.add(verb);
				} else {
					SrlArgumentToken argument = new SrlArgumentToken(sentence,
							tokenLabels.get(SRL_ARGUMENT_TYPE).replaceAll(CHAR_MARKS, ""),
							sentence.getTokens().get(Integer.valueOf(tokenLabels.get(SRL_ARG_START))),
							sentence.getTokens().get(Integer.valueOf(tokenLabels.get(SRL_ARG_END))));
					arguments.add(argument);
				}
				for (int tokennumber = Integer.valueOf(tokenLabels.get(SRL_ARG_START)); tokennumber <= Integer
						.valueOf(tokenLabels.get(SRL_ARG_END)); tokennumber++) {
					sentence.getTokens().get(tokennumber).srlValues
							.add(tokenLabels.get(SRL_ARGUMENT_TYPE).replaceAll(CHAR_MARKS, ""));
				}
				tokenLabels.clear();
			}
			sentence.getSrlVerbs().get(verbcount).addArguments(arguments);
			tokens.clear();
			arguments.clear();
			verbcount += 1;
		}
		sentence_predicates.clear();
	}

}
