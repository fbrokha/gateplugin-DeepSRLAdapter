package gate.deepSRL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import deepSRL.DeepSRL;
import deepSRL.mapping.Document;
import deepSRL.mapping.Sentence;
import deepSRL.mapping.SrlArgumentToken;
import deepSRL.mapping.SrlPredicateToken;
import deepSRL.mapping.Token;
import gate.Annotation;
import gate.AnnotationSet;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.GateRuntimeException;
import gate.util.InvalidOffsetException;

public abstract class DeepSRLAnalyser extends AbstractLanguageAnalyser {
	private static final long serialVersionUID = -9182190380538490182L;
	private static Logger logger = Logger.getLogger(DeepSRLAnalyser.class);

	public static final String ANNOTATION_TYPE_PREDICATE = "predicate";
	public static final String ANNOTATION_TYPE_ARGUMENT = "argument";
	public static final String FEATURE_ARGUMENT_TYPE = "type";
	public static final String FEATURE_ARGUMENT_PREDICATE = "predicateId";
	public static final String RELATION_SRL_NAME = "SRL";

	private String inputASName;
	private String inputSentenceType;
	private String inputTokenType;
	private Object verbFeatureKey;
	private Set<Object> verbFeatureValues;
	private String outputASName;

	protected DeepSRL deepSRL;

	protected void initDeepSRL(DeepSRL deepSRL) {
		this.deepSRL = deepSRL;
	}

	@Override
	public void reInit() throws ResourceInstantiationException {
		init();
	}

	@Override
	public void cleanup() {
		try {
			deepSRL.close();
		} catch (IOException e) {
			throw new GateRuntimeException(e);
		}
		super.cleanup();
	}

	@Override
	public void execute() throws ExecutionException {
		AnnotationSet inputAnnotationSet = document.getAnnotations(inputASName);
		AnnotationSet outputAnnotationSet = document.getAnnotations(outputASName);

		try {
			executeContent(document.getContent(), inputAnnotationSet, outputAnnotationSet);
		} catch (Exception e) {
			if (e instanceof IOException) {
				try {
					deepSRL.close();
				} catch (IOException e1) {
					e.addSuppressed(e1);
				}
			}
			throw new ExecutionException(e);
		}
	}

	private void executeContent(DocumentContent documentContent, AnnotationSet inputAnnotationSet,
			AnnotationSet outputAnnotationSet) throws IOException, InvalidOffsetException {
		List<Sentence> sentences = new ArrayList<>();

		Long documentOffset = 0l;
		Long lastSentenceEnd = 0l;

		boolean predefinedVerbs = verbFeatureKey != null && verbFeatureValues != null && !verbFeatureValues.isEmpty();

		AnnotationSet annotationSet = inputAnnotationSet.get(inputSentenceType);
		for (Annotation sentenceAnnotation : annotationSet.inDocumentOrder()) {
			Long sentenceStart = sentenceAnnotation.getStartNode().getOffset();
			Long sentenceEnd = sentenceAnnotation.getEndNode().getOffset();

			int documentStart = (int) (sentenceStart - documentOffset);
			int documentEnd = (int) (sentenceEnd - documentOffset);
			Sentence sentence;

			List<Token> tokens = buildTokens(documentOffset, inputAnnotationSet, sentenceStart, sentenceEnd,
					predefinedVerbs);
			sentence = new Sentence(sentenceAnnotation.getId(), documentStart, documentEnd, tokens, predefinedVerbs);

			sentences.add(sentence);
			lastSentenceEnd = sentenceEnd;
		}
		if (!sentences.isEmpty()) {
			String documentText = documentContent.getContent(documentOffset, lastSentenceEnd).toString();
			Document deepSRLDocument = new Document(documentText, sentences);
			executeDeepSRL(documentOffset, deepSRLDocument, outputAnnotationSet);
		}
	}

	protected void executeDeepSRL(Long documentOffset, final Document document, AnnotationSet outputAnnotationSet)
			throws IOException, InvalidOffsetException {

		deepSRL.execute(document);
		addSrlAnnotations(documentOffset, document, outputAnnotationSet);
	}

	private List<Token> buildTokens(Long documentOffset, AnnotationSet inputAnnotationSet, Long sentenceStart,
			Long sentenceEnd, boolean predefinedVerbs) throws InvalidOffsetException {
		List<Token> tokens = new ArrayList<>();
		AnnotationSet inputTokenSet = inputAnnotationSet.get(inputTokenType, sentenceStart, sentenceEnd);
		Iterator<Annotation> tokenAnnotationIterator = inputTokenSet.iterator();
		while (tokenAnnotationIterator.hasNext()) {
			Annotation tokenAnnotation = tokenAnnotationIterator.next();
			Long tokenStart = tokenAnnotation.getStartNode().getOffset();
			Long tokenEnd = tokenAnnotation.getEndNode().getOffset();
			boolean predefinedVerb = false;
			if (predefinedVerbs) {
				predefinedVerb = verbFeatureValues.contains(tokenAnnotation.getFeatures().get(verbFeatureKey));
			}
			tokens.add(new Token(tokenAnnotation.getId(), (int) (tokenStart - documentOffset),
					(int) (tokenEnd - documentOffset), predefinedVerb));
		}
		return tokens;
	}

	protected void addSrlAnnotations(Long documentOffset, Document document, AnnotationSet outputAnnotationSet)
			throws InvalidOffsetException {
		for (Sentence sentence : document.getSentences()) {
			for (SrlPredicateToken predicateToken : sentence.getSrlPredicates()) {

				FeatureMap predicateFeatures = Factory.newFeatureMap();
				Integer predicateId = outputAnnotationSet.add(documentOffset + predicateToken.getDocumentStart(),
						documentOffset + predicateToken.getDocumentEnd(), ANNOTATION_TYPE_PREDICATE, predicateFeatures);

				List<Integer> relationIds = new ArrayList<>();
				for (SrlArgumentToken argumentToken : predicateToken.getArguments()) {
					FeatureMap argumentFeatures = Factory.newFeatureMap();
					argumentFeatures.put(FEATURE_ARGUMENT_TYPE, argumentToken.getType());
					argumentFeatures.put(FEATURE_ARGUMENT_PREDICATE, predicateId);
					Integer argumentId = outputAnnotationSet.add(documentOffset + argumentToken.getDocumentStart(),
							documentOffset + argumentToken.getDocumentEnd(), ANNOTATION_TYPE_ARGUMENT,
							argumentFeatures);
					relationIds.add(argumentId);
				}

				if (!relationIds.isEmpty()) {
					relationIds.add(0, predicateId);
					outputAnnotationSet.getRelations().addRelation(RELATION_SRL_NAME, toIntArray(relationIds));
				}
			}
		}
	}

	private static int[] toIntArray(List<Integer> list) {
		int[] ret = new int[list.size()];
		int i = 0;
		for (Integer e : list)
			ret[i++] = e.intValue();
		return ret;
	}

	@Optional
	@RunTime
	@CreoleParameter(comment = "Input annotation set name", defaultValue = "")
	public void setInputASName(String inputASName) {
		this.inputASName = inputASName;
	}

	public String getInputASName() {
		return inputASName;
	}

	@Optional
	@RunTime
	@CreoleParameter(comment = "Output annotation set name", defaultValue = "")
	public void setOutputASName(String outputASName) {
		this.outputASName = outputASName;
	}

	public String getOutputASName() {
		return this.outputASName;
	}

	@RunTime
	@CreoleParameter(comment = "Input sentence annotation name", defaultValue = "Sentence")
	public void setInputSentenceType(String inputSentenceType) {
		this.inputSentenceType = inputSentenceType;
	}

	public String getInputSentenceType() {
		return inputSentenceType;
	}

	@RunTime
	@CreoleParameter(comment = "Use tokens by GATE Annotation instead of DeepSRL tokenizer.", defaultValue = "Token")
	public void setInputTokenType(String inputTokenType) {
		this.inputTokenType = inputTokenType;
	}

	public String getInputTokenType() {
		return inputTokenType;
	}

	@Optional
	@RunTime
	@CreoleParameter(comment = "Feature name for tokens, which are predefined as verbs (if not set, DeepSRL determines verbs using pidmodel)")
	public void setVerbFeatureKey(Object verbFeatureKey) {
		this.verbFeatureKey = verbFeatureKey;
	}

	public Object getVerbFeatureKey() {
		return verbFeatureKey;
	}

	@Optional
	@RunTime
	@CreoleParameter(comment = "Feature values for tokens, which are predefined as verbs (if not set or empty, DeepSRL determines verbs using pidmodel)")
	public void setVerbFeatureValues(Set<Object> verbFeatureValues) {
		this.verbFeatureValues = verbFeatureValues;
	}

	public Set<Object> getVerbFeatureValues() {
		return verbFeatureValues;
	}

}
