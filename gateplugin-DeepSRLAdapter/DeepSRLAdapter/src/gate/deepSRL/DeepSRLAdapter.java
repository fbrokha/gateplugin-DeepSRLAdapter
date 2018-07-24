package gate.deepSRL;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

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
import deepSRL.mapping.Token;
import deepSRL.DeepSRL;
import deepSRL.DeepSRLBuilder;
import deepSRL.mapping.Document;
import deepSRL.mapping.Sentence;
import deepSRL.mapping.SrlArgumentToken;
import deepSRL.mapping.SrlVerbToken;


/**
 * This class is the implementation of the resource DeepSRLAdapter.
 */
@CreoleResource(name = "DeepSRLAdapter", comment = "Integrate DeepSRL (https://github.com/luheng/deep_srl) as a Processing Resource")
public class DeepSRLAdapter extends AbstractLanguageAnalyser {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9182190380538490182L;

	private static final Integer MAX_INPUT_LENGTH = Integer.MAX_VALUE / 2;
	
	private static final String ANNOTATION_SRL_NAME = "SRL";
	private static final String ANNOTATION_SRL_FEATURE_VERB_NAME = "verb";
	private static final String ANNOTATION_SRL_FEATURE_TYPE_NAME = "type";
	private static final String ANNOTATION_SRL_FEATURE_ARGUMENT_JOIN = " [...] ";
	private static final String RELATION_SRL_NAME = "SRL";
	
	private boolean init = false;
	
	private static boolean userTokens;
	
	private URL executableFile;
	private URL modelPath;
	private URL propidModelPath;
	
	private String inputASName;
	private String inputSentenceType;
	private String inputTokenType;
	private String outputASName;
	private Boolean shutdownValue;
	
	private Document deepSRLDocument;
	
	private DeepSRL deepSRLprocess;

	
	@Override
	public Resource init() throws ResourceInstantiationException {
		try {
			deepSRLinit();
		} catch (Exception e) {
			System.out.println("Init unsuccessful");
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public void reInit() throws ResourceInstantiationException {
		init();
	}

	@Override
	public void execute() throws ExecutionException {
		
		if(shutdownValue) {
			if(deepSRLprocess.getProcess().isAlive() && !deepSRLprocess.getExec().isTerminated()) {
				deepSRLprocess.shutdownService();
				init =false;
				System.out.println("DeepSRL Script beendet.");
			}
			else {
				System.out.println("DeepSRL Script wurde bereits beendet.");
			}
		}
		else {
			AnnotationSet inputAnnotationSet = document.getAnnotations(inputASName);
			AnnotationSet outputAnnotationSet = document.getAnnotations(outputASName);
			
			if(!init) {
				try {
					deepSRLinit();
				} catch (Exception e) {
					System.out.println("Init unsuccessful");
					e.printStackTrace();
				}
			}

			try {
				executeContent(document.getContent(), inputAnnotationSet, outputAnnotationSet);
			} catch (Exception e) {
				throw new ExecutionException(e);
			}
			System.out.println("DeepSRL Computation finished");
		}
	}
	
	private void deepSRLinit() throws Exception {
		DeepSRLBuilder builder = new DeepSRLBuilder(urlToFile(executableFile), urlToFile(modelPath),
				urlToFile(propidModelPath));


		deepSRLprocess = builder.build();
		deepSRLprocess.init();
		init = true;
	}
	
	private void executeContent(DocumentContent documentContent, AnnotationSet inputAnnotationSet,
			AnnotationSet outputAnnotationSet) throws Exception {
		List<Sentence> sentences = new ArrayList<>();
		userTokens = hasValue(inputTokenType);
		boolean reuseAnnotations = equals(inputASName, outputASName);
		if (hasValue(inputSentenceType)) {

			Long documentOffset = 0l;
			Long lastSentenceEnd = 0l;

			AnnotationSet annotationSet = inputAnnotationSet.get(inputSentenceType);
			for (Annotation sentenceAnnotation : annotationSet.inDocumentOrder()) {
				Long sentenceStart = sentenceAnnotation.getStartNode().getOffset();
				Long sentenceEnd = sentenceAnnotation.getEndNode().getOffset();
				
//				if ((sentenceEnd - documentOffset) > MAX_INPUT_LENGTH.longValue()) {
//					String documentText = documentContent.getContent(documentOffset, lastSentenceEnd).toString();
//					//sentences leere liste?
//					Document document = new Document(documentText, sentences);
//					executeDeepSRL(documentOffset, document, outputAnnotationSet);
//
//					documentOffset = lastSentenceEnd;
//
//					sentences = new ArrayList<>();
//				}

				Integer id = reuseAnnotations ? sentenceAnnotation.getId() : null;
				int documentStart = (int) (sentenceStart - documentOffset);
				int documentEnd = (int) (sentenceEnd - documentOffset);
				Sentence sentence;

				if (userTokens) {
					List<Token> tokens = buildTokens(documentOffset, inputAnnotationSet, sentenceStart, sentenceEnd,
							reuseAnnotations);
					sentence = new Sentence(id, documentStart, documentEnd, tokens);
				} else {
					sentence = new Sentence(id, documentStart, documentEnd);
				}

				sentences.add(sentence);
				lastSentenceEnd = sentenceEnd;
			}
			if (!sentences.isEmpty()) {
				String documentText = documentContent.getContent(documentOffset, lastSentenceEnd).toString();
				Document deepSRLDocument = new Document(documentText, sentences);
				executeDeepSRL(documentOffset, deepSRLDocument, outputAnnotationSet);
			}
		}
		else if (documentContent.size() < MAX_INPUT_LENGTH.longValue()) {
			Sentence sentence;

			if (userTokens) {
				List<Token> tokens = buildTokens(0l, inputAnnotationSet, 0l, documentContent.size(), reuseAnnotations);
				sentence = new Sentence(null, 0, documentContent.size().intValue(), tokens);
			} else {
				sentence = new Sentence(null, 0, documentContent.size().intValue());
			}
				
			sentences.add(sentence);
			deepSRLDocument = new Document(documentContent.getContent(0l, documentContent.size()).toString(),
					sentences);
			executeDeepSRL(0l, deepSRLDocument, outputAnnotationSet);
		} else {
			throw new IllegalStateException();
		}
	}
	
	protected void executeDeepSRL(Long documentOffset, final Document document, AnnotationSet outputAnnotationSet)
			throws Exception {
		
		deepSRLprocess.execute(document);
		addSrlAnnotations(documentOffset, document, outputAnnotationSet);
	}
	
	private List<Token> buildTokens(Long documentOffset, AnnotationSet inputAnnotationSet, Long sentenceStart,
			Long sentenceEnd, boolean reuseAnnotations) throws InvalidOffsetException {
		List<Token> tokens = new ArrayList<>();
		AnnotationSet inputTokenSet = inputAnnotationSet.get(inputTokenType, sentenceStart, sentenceEnd);
		Iterator<Annotation> tokenAnnotationIterator = inputTokenSet.iterator();
		while (tokenAnnotationIterator.hasNext()) {
			Annotation tokenAnnotation = tokenAnnotationIterator.next();
			Long tokenStart = tokenAnnotation.getStartNode().getOffset();
			Long tokenEnd = tokenAnnotation.getEndNode().getOffset();
			Integer id = reuseAnnotations ? tokenAnnotation.getId() : null;
			tokens.add(new Token(id, (int) (tokenStart - documentOffset), (int) (tokenEnd - documentOffset)));
		}
		return tokens;
	}
	
	protected void addSrlAnnotations(Long documentOffset, Document document, AnnotationSet outputAnnotationSet)
			throws InvalidOffsetException {
		for (Sentence sentence : document.getSentences()) {
			for (SrlVerbToken verb : sentence.getMultiTokens()) {
				List<Integer> relationIds = new ArrayList<>();

				DocumentContent verbText = this.document.getContent()
						.getContent(documentOffset + verb.getDocumentStart(), documentOffset + verb.getDocumentEnd());

				FeatureMap verbFeatures = Factory.newFeatureMap();
				verbFeatures.put(ANNOTATION_SRL_FEATURE_TYPE_NAME, verb.getType());
				verbFeatures.put(ANNOTATION_SRL_FEATURE_VERB_NAME, verbText);

				for (SrlArgumentToken argument : verb.getArguments()) {
					DocumentContent argumentText = this.document.getContent().getContent(
							documentOffset + argument.getDocumentStart(), documentOffset + argument.getDocumentEnd());
					//was soll diese if abfrage??
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
	
	public static boolean getUserTokens() {
		return userTokens;
	}
	
	private static File urlToFile(URL url) throws URISyntaxException {
		return new File(url.toURI());
	}
	
	private static int[] toIntArray(List<Integer> list) {
		int[] ret = new int[list.size()];
		int i = 0;
		for (Integer e : list)
			ret[i++] = e.intValue();
		return ret;
	}
	
	private static <E> boolean equals(E e1, E e2) {
		if (e1 == null && e2 == null)
			return true;
		if (e1 == null || e2 == null)
			return false;
		return e1.equals(e2);
	}
	
	private static boolean hasValue(String string) {
		return string != null && string.length() > 0;
	}
	
	//@Optional(false)
	@CreoleParameter(comment = "DeepSRL executable", defaultValue = "file:/C:/SRL_Impl/deep_srl-master/python/gate_deepSRL.py")
	public void setExecutableFile(URL executableFile) {
		this.executableFile = executableFile;
	}

	public URL getExecutableFile() {
		return executableFile;
	}
	
	//@Optional(false)
	@CreoleParameter(comment = "DeepSRL ModelPath", defaultValue = "file:/C:/SRL_Impl/deep_srl-master/conll05_model/")
	public void setModelPath(URL PATH) {
		this.modelPath = PATH;
	}

	public URL getModelPath() {
		return modelPath;
	}
	
	//@Optional(false)
	@CreoleParameter(comment = "DeepSRL PropidModelPath", defaultValue = "file:/C:/SRL_Impl/deep_srl-master/conll05_propid_model/")
	public void setPropidModelPath(URL PATH) {
		this.propidModelPath = PATH;
	}

	public URL getPropidModelPath() {
		return propidModelPath;
	}

	@Optional
	@RunTime
	@CreoleParameter(comment = "Input annotation set name", defaultValue = "openNLP")
	public void setInputASName(String inputASName) {
		this.inputASName = inputASName;
	}

	public String getInputASName() {
		return inputASName;
	}
	
	@Optional
	@RunTime
	@CreoleParameter(comment = "Output annotation set name", defaultValue = "deepSRL")
	public void setOutputASName(String outputASName) {
		this.outputASName = outputASName;
	}

	public String getOutputASName() {
		return this.outputASName;
	}
	
	@Optional
	@RunTime
	@CreoleParameter(comment = "Input sentence annotation name", defaultValue = "Sentence")
	public void setInputSentenceType(String inputSentenceType) {
		this.inputSentenceType = inputSentenceType;
	}

	public String getInputSentenceType() {
		return inputSentenceType;
	}
	
	@Optional
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
	@CreoleParameter(comment = "Close the python script runnnig in background.", defaultValue = "false")
	public void setShutdown(Boolean shutdownValue) {
		this.shutdownValue = shutdownValue;
	}

	public Boolean getShutdown() {
		return shutdownValue;
	}

}
