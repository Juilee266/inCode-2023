package com.inDrive.plugin.voice;

import android.os.Bundle;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import opennlp.tools.doccat.BagOfWordsFeatureGenerator;
import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.doccat.FeatureGenerator;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatbotFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatbotFragment extends Fragment {

    private static Map<String, String> questionAnswer = new HashMap<>();
    private DoccatModel model;

    /*
     * Define answers for each given category.
     */
    static {
        questionAnswer.put("greeting", "Hello, how can I help you?");
        questionAnswer.put("book_cab_instruction",
                "book_cab_instruction");
        questionAnswer.put("location_inquiry", "location_inquiry");
        questionAnswer.put("driver_inquiry", "driver_inquiry");
        questionAnswer.put("time_for_driver", "time_for_driver");
        questionAnswer.put("vehicle_inquiry", "vehicle_inquiry");
        questionAnswer.put("time_to_reach", "time_to_reach");
        questionAnswer.put("change_source", "change_source");
        questionAnswer.put("change_destination", "change_destination");
        questionAnswer.put("otp_inquiry", "otp_inquiry");
        questionAnswer.put("start_instruction", "start_instruction");
        questionAnswer.put("all_okay", "okay");
        questionAnswer.put("stars", "stars");

    }

    static DocumentCategorizerME myDocCategorizer;
    static SentenceDetectorME mySenCategorizer;
    static TokenizerME tokenizer;
    static POSTaggerME myPOSCategorizer;
    static LemmatizerME myLemCategorizer;

    public ChatbotFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment
     *
     * @return A new instance of fragment ChatbotFragment.
     */
    public static ChatbotFragment newInstance() {
        ChatbotFragment fragment = new ChatbotFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vw = inflater.inflate(R.layout.fragment_chatbot, container, false);
        vw.findViewById(R.id.submit).setOnClickListener(view -> {
            TextView resp = vw.findViewById(R.id.response);
            EditText inp = vw.findViewById(R.id.input);
            try {
                resp.setText(getResponse(String.valueOf(inp.getText())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        // Train categorizer model to the training data we created.
        try {
            model = trainCategorizerModel();
            initializeSentenceModel();
            initializeDocumentCategorizer();
            initializePOSModel();
            initializeTokenizerModel();
            initializeLemmatizer();

        } catch (IOException e) {
            System.out.println(e.getStackTrace());
            throw new RuntimeException(e);
        }
        return vw;
    }

    private void initializeSentenceModel() throws IOException {
        InputStream is = null;
        SentenceModel sm;
        try {
            is = getContext().getAssets().open("en_sent.bin");
            sm = new SentenceModel(is);
            mySenCategorizer = new SentenceDetectorME(sm);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private void initializeTokenizerModel() throws IOException {
        InputStream is = null;
        TokenizerModel tm;
        try {
            is = getContext().getAssets().open("en_token.bin");
            tm = new TokenizerModel(is);
            tokenizer = new TokenizerME(tm);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    private void initializePOSModel() throws IOException {
        InputStream is = null;
        try {
            is = getContext().getAssets().open("en_pos_maxent.bin");
            // Initialize POS tagger tool
            myPOSCategorizer = new POSTaggerME(new POSModel(is));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    private void initializeLemmatizer() throws IOException {
        InputStream is = null;
        try {
            is = getContext().getAssets().open("en_lemmatizer.bin");
            // Tag sentence.
            myLemCategorizer = new LemmatizerME(new LemmatizerModel(is));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private void initializeDocumentCategorizer() throws IOException {
        // Initialize document categorizer tool
        myDocCategorizer = new DocumentCategorizerME(model);
    }

    private String getResponse(String input) throws IOException {
        // Break users chat input into sentences using sentence detection.
        String[] sentences = breakSentences(input);
        String answer = "";
        boolean conversationComplete = false;

        // Loop through sentences.
        for (String sentence : sentences) {

            // Separate words from each sentence using tokenizer.
            String[] tokens = tokenizeSentence(sentence);

            // Tag separated words with POS tags to understand their gramatical structure.
            String[] posTags = detectPOSTags(tokens);

            // Lemmatize each word so that its easy to categorize.
            String[] lemmas = lemmatizeTokens(tokens, posTags);

            // Determine BEST category using lemmatized tokens used a mode that we trained
            // at start.
            String category = detectCategory(model, lemmas);

            // Get predefined answer from given category & add to answer.
            answer = answer + " " + questionAnswer.get(category);

            // If category conversation-complete, we will end chat conversation.
            if ("conversation-complete".equals(category)) {
                conversationComplete = true;
            }
        }

        return answer;
    }

    /**
     * Train categorizer model as per the category sample training data we created.
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private DoccatModel trainCategorizerModel() throws FileNotFoundException, IOException {
        // faq-categorizer.txt is a custom training data with categories as per our chat
        // requirements.
        InputStream is = null;
        ObjectStream<DocumentSample> sampleStream = null;
        ObjectStream<String> lineStream = null;
        try {
            is = getContext().getAssets().open("faq_categorizer.txt");
            InputStream finalIs = is;
            InputStreamFactory inputStreamFactory = () -> finalIs;
            lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
            sampleStream = new DocumentSampleStream(lineStream);

            DoccatFactory factory = new DoccatFactory(new FeatureGenerator[]{new BagOfWordsFeatureGenerator()});

            TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
            params.put(TrainingParameters.CUTOFF_PARAM, 0);

            // Train a model with classifications from above file.
            DoccatModel model = DocumentCategorizerME.train("en", sampleStream, params, factory);
            return model;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (lineStream != null) {
                lineStream.close();
            }
            if (sampleStream != null) {
                sampleStream.close();
            }
            if (is != null) {
                is.close();
            }

        }

    }

    /**
     * Detect category using given token. Use categorizer feature of Apache OpenNLP.
     *
     * @param model
     * @param finalTokens
     * @return
     * @throws IOException
     */
    private static String detectCategory(DoccatModel model, String[] finalTokens) throws IOException {

        // Get best possible category.
        double[] probabilitiesOfOutcomes = myDocCategorizer.categorize(finalTokens);
        String category = myDocCategorizer.getBestCategory(probabilitiesOfOutcomes);
        System.out.println("Category: " + category);

        return category;

    }

    /**
     * Break data into sentences using sentence detection feature of Apache OpenNLP.
     *
     * @param data
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String[] breakSentences(String data) throws FileNotFoundException, IOException {
        String[] sentences = mySenCategorizer.sentDetect(data);

        System.out.println("Sentence Detection: " + Arrays.stream(sentences).collect(Collectors.joining(" | ")));

        return sentences;
    }

    /**
     * Break sentence into words & punctuation marks using tokenizer feature of
     * Apache OpenNLP.
     *
     * @param sentence
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String[] tokenizeSentence(String sentence) throws FileNotFoundException, IOException {
        String[] tokens = tokenizer.tokenize(sentence);
        System.out.println("Tokenizer : " + Arrays.stream(tokens).collect(Collectors.joining(" | ")));
        return tokens;
    }

    /**
     * Find part-of-speech or POS tags of all tokens using POS tagger feature of
     * Apache OpenNLP.
     *
     * @param tokens
     * @return
     * @throws IOException
     */
    private String[] detectPOSTags(String[] tokens) throws IOException {
        // Tag sentence.
        String[] posTokens = myPOSCategorizer.tag(tokens);
        System.out.println("POS Tags : " + Arrays.stream(posTokens).collect(Collectors.joining(" | ")));
        return posTokens;
    }

    /**
     * Find lemma of tokens using lemmatizer feature of Apache OpenNLP.
     *
     * @param tokens
     * @param posTags
     * @return
     * @throws IOException
     */
    private String[] lemmatizeTokens(String[] tokens, String[] posTags)
            throws IOException {

        String[] lemmaTokens = myLemCategorizer.lemmatize(tokens, posTags);
        System.out.println("Lemmatizer : " + Arrays.stream(lemmaTokens).collect(Collectors.joining(" | ")));
        return lemmaTokens;
    }
}