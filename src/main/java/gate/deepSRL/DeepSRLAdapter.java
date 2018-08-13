package gate.deepSRL;

import static gate.util.Files.fileFromURL;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import deepSRL.DeepSRL;
import deepSRL.DeepSRLBuilder;
import deepSRL.mapping.Document;
import deepSRL.mapping.Sentence;
import deepSRL.mapping.SrlArgumentToken;
import deepSRL.mapping.SrlVerbToken;
import deepSRL.mapping.Token;
import gate.Annotation;
import gate.AnnotationSet;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;

/**
 * This class is the implementation of the resource DeepSRLAdapter.
 */
@CreoleResource(name = "DeepSRLAdapter", comment = "Integrate DeepSRL (https://github.com/luheng/deep_srl) as a Processing Resource")
public class DeepSRLAdapter extends AbstractLanguageAnalyser {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9182190380538490182L;

	private static final String ANNOTATION_SRL_NAME = "SRL";
	private static final String ANNOTATION_SRL_FEATURE_VERB_NAME = "verb";
	private static final String ANNOTATION_SRL_FEATURE_TYPE_NAME = "type";
	private static final String ANNOTATION_SRL_FEATURE_ARGUMENT_JOIN = " [...] ";
	private static final String RELATION_SRL_NAME = "SRL";

	private URL pythonExecutable;
	private URL deepSRLScript;
	private URL modelPath;
	private URL propidModelPath;

	private String inputASName;
	private String inputSentenceType;
	private String inputTokenType;
	private String outputASName;

	private DeepSRL deepSRLprocess;

	@Override
	public Resource init() throws ResourceInstantiationException {
		try {
			DeepSRLBuilder builder = new DeepSRLBuilder(fileFromURL(pythonExecutable), fileFromURL(deepSRLScript),
					fileFromURL(modelPath), fileFromURL(propidModelPath));

			deepSRLprocess = builder.build();
		} catch (Exception e) {
			throw new ResourceInstantiationException(e);
		}
		return this;
	}

	@Override
	public void reInit() throws ResourceInstantiationException {
		init();
	}

	@Override
	public void cleanup() {
		try {
			deepSRLprocess.shutdownService();
		} catch (IOException e) {
			e.printStackTrace();
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
			throw new ExecutionException(e);
		}
	}

	private void executeContent(DocumentContent documentContent, AnnotationSet inputAnnotationSet,
			AnnotationSet outputAnnotationSet) throws Exception {
		List<Sentence> sentences = new ArrayList<>();

		Long documentOffset = 0l;
		Long lastSentenceEnd = 0l;

		AnnotationSet annotationSet = inputAnnotationSet.get(inputSentenceType);
		for (Annotation sentenceAnnotation : annotationSet.inDocumentOrder()) {
			Long sentenceStart = sentenceAnnotation.getStartNode().getOffset();
			Long sentenceEnd = sentenceAnnotation.getEndNode().getOffset();

			int documentStart = (int) (sentenceStart - documentOffset);
			int documentEnd = (int) (sentenceEnd - documentOffset);
			Sentence sentence;

			List<Token> tokens = buildTokens(documentOffset, inputAnnotationSet, sentenceStart, sentenceEnd);
			sentence = new Sentence(sentenceAnnotation.getId(), documentStart, documentEnd, tokens);

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
			throws Exception {

		deepSRLprocess.execute(document);
		addSrlAnnotations(documentOffset, document, outputAnnotationSet);
	}

	private List<Token> buildTokens(Long documentOffset, AnnotationSet inputAnnotationSet, Long sentenceStart,
			Long sentenceEnd) throws InvalidOffsetException {
		List<Token> tokens = new ArrayList<>();
		AnnotationSet inputTokenSet = inputAnnotationSet.get(inputTokenType, sentenceStart, sentenceEnd);
		Iterator<Annotation> tokenAnnotationIterator = inputTokenSet.iterator();
		while (tokenAnnotationIterator.hasNext()) {
			Annotation tokenAnnotation = tokenAnnotationIterator.next();
			Long tokenStart = tokenAnnotation.getStartNode().getOffset();
			Long tokenEnd = tokenAnnotation.getEndNode().getOffset();
			tokens.add(new Token(tokenAnnotation.getId(), (int) (tokenStart - documentOffset),
					(int) (tokenEnd - documentOffset)));
		}
		return tokens;
	}

	protected void addSrlAnnotations(Long documentOffset, Document document, AnnotationSet outputAnnotationSet)
			throws InvalidOffsetException {
		for (Sentence sentence : document.getSentences()) {
			for (SrlVerbToken verb : sentence.getSrlVerbs()) {
				List<Integer> relationIds = new ArrayList<>();

				DocumentContent verbText = this.document.getContent()
						.getContent(documentOffset + verb.getDocumentStart(), documentOffset + verb.getDocumentEnd());

				FeatureMap verbFeatures = Factory.newFeatureMap();
				verbFeatures.put(ANNOTATION_SRL_FEATURE_TYPE_NAME, verb.getType());
				verbFeatures.put(ANNOTATION_SRL_FEATURE_VERB_NAME, verbText);

				for (SrlArgumentToken argument : verb.getArguments()) {
					DocumentContent argumentText = this.document.getContent().getContent(
							documentOffset + argument.getDocumentStart(), documentOffset + argument.getDocumentEnd());

					if (verbFeatures.get(argument.getType()) != null) {
						verbFeatures.put(argument.getType(), verbFeatures.get(argument.getType())
								+ ANNOTATION_SRL_FEATURE_ARGUMENT_JOIN + argumentText);
					} else {
						verbFeatures.put(argument.getType(), argumentText);
					}
					FeatureMap features = Factory.newFeatureMap();
					features.put(ANNOTATION_SRL_FEATURE_TYPE_NAME, argument.getType());
					features.put(ANNOTATION_SRL_FEATURE_VERB_NAME, verbText);
					Integer argumentId = outputAnnotationSet.add(documentOffset + argument.getDocumentStart(),
							documentOffset + argument.getDocumentEnd(), ANNOTATION_SRL_NAME, features);
					relationIds.add(argumentId);
				}

				Integer verbId = outputAnnotationSet.add(documentOffset + verb.getDocumentStart(),
						documentOffset + verb.getDocumentEnd(), ANNOTATION_SRL_NAME, verbFeatures);
				if (!relationIds.isEmpty()) {
					relationIds.add(0, verbId);
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

	@CreoleParameter(comment = "python executable, version 2.x.x required", defaultValue = "")
	public void setPythonExecutable(URL pythonExecutable) {
		this.pythonExecutable = pythonExecutable;
	}

	public URL getPythonExecutable() throws MalformedURLException {
		return pythonExecutable;
	}

	@CreoleParameter(comment = "DeepSRL Script, name: \"gate_deepSRL.py\"", defaultValue = "")
	public void setDeepSRLScript(URL deepSRLScript) {
		this.deepSRLScript = deepSRLScript;
	}

	public URL getDeepSRLScript() {
		return deepSRLScript;
	}

	@CreoleParameter(comment = "DeepSRL ModelPath, name: \\conll05_model", defaultValue = "")
	public void setModelPath(URL PATH) {
		this.modelPath = PATH;
	}

	public URL getModelPath() {
		return modelPath;
	}

	@CreoleParameter(comment = "DeepSRL PropidModelPath, name: \\conll05_propid_model", defaultValue = "")
	public void setPropidModelPath(URL PATH) {
		this.propidModelPath = PATH;
	}

	public URL getPropidModelPath() {
		return propidModelPath;
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

}
