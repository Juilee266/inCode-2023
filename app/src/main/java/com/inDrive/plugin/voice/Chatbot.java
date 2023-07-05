package com.inDrive.plugin.voice;

import android.content.Context;
import android.util.Log;

import com.inDrive.plugin.entities.Driver;
import com.inDrive.plugin.entities.Location;
import com.inDrive.plugin.entities.Passenger;
import com.inDrive.plugin.entities.Ride;
import com.inDrive.plugin.entities.Vehicle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
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

public class Chatbot {

    private Context context;
    private Passenger passenger;
    private Ride ride;
    private Location pickupPoint;
    private Location DropPoint;

    private String status = "NOT_BOOKED";
    static final String GREETING = "greeting";
    static final String BOOK_CAB_INSTR = "book_cab_instruction";
    static final String LOCATION_INQUIRY = "location_inquiry";
    static final String DRIVER_INQUIRY = "driver_inquiry";
    static final String VEHICLE_INQUIRY = "vehicle_inquiry";
    static final String TIME_FOR_DRIVER = "time_for_driver";
    static final String TIME_TO_REACH = "time_to_reach";
    static final String CHANGE_SOURCE = "change_source";
    static final String CHANGE_DESTINATION = "change_destination";
    static final String OTP_INQUIRY = "otp_inquiry";
    static final String START_RIDE = "start_instruction";
    static final String ALL_GOOD = "all_okay";
    static final String RATING = "stars";
    static final String AFFIRMATION = "affirmation";
    static final String NEGATION = "negation";

    static final String STOP_PROCESS = "stop_process";
    static final String CANCEL_RIDE = "cancel_ride";

    private DoccatModel model;


    static DocumentCategorizerME myDocCategorizer;
    static SentenceDetectorME mySenCategorizer;
    static TokenizerME tokenizer;
    static POSTaggerME myPOSCategorizer;
    static LemmatizerME myLemCategorizer;

    public Chatbot(Context context, Passenger passenger) {
        try {
            System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");

            this.context = context;
            model = trainCategorizerModel();
            initializeSentenceModel();
            initializeDocumentCategorizer();
            initializePOSModel();
            initializeTokenizerModel();
            initializeLemmatizer();

        } catch (IOException e) {
            Log.e("ChatbotFragment", Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    private void initializeSentenceModel() throws IOException {
        InputStream is = null;
        SentenceModel sm;
        try {
            is = context.getAssets().open("en_sent.bin");
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
            is =  context.getAssets().open("en_token.bin");
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
            is =  context.getAssets().open("en_pos_maxent.bin");
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
            is =  context.getAssets().open("en_lemmatizer.bin");
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
            answer = processInstruction(category, input);

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
            is =  context.getAssets().open("faq_categorizer.txt");
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

    public String processInstruction(String category, String instruction) {
        switch(category) {
            case GREETING:
                return "Hey there! How can I help you?";
            case BOOK_CAB_INSTR:
                return processBookCabCommand(instruction);
            case LOCATION_INQUIRY:
                return processLocationInquiry();
            case DRIVER_INQUIRY :
                return processDriverInquiry();
            case VEHICLE_INQUIRY :
                return processVehicleInquiry();
            case TIME_FOR_DRIVER :
                break;
            case TIME_TO_REACH:
                break;
            case CHANGE_SOURCE :
                return processChangeSource(instruction);
            case CHANGE_DESTINATION :
                return processChangeDestination(instruction);
            case OTP_INQUIRY :
                break;
            case START_RIDE:
                break;
            case ALL_GOOD :
                break;
            case RATING :
                break;
            case AFFIRMATION:
                break;
            case NEGATION:
                break;
            case CANCEL_RIDE:
                break;

        }
        return "Sorry. Could you repeat?";
    }

    private String processChangeDestination(String instruction) {
        askQuestion("Are you sure you want to change the drop location?");
        String ans = listenForAnswer();
        String category = getCategory(ans);
        Location dest = null;

        if(category == AFFIRMATION) {
            while(dest == null) {
                askQuestion("Please specify the drop location");
                ans = listenForAnswer();
                dest = fetchDropLocationFromInstr(ans);
                if (dest == null) {
                    dest = getDroppingPtFromUser();
                    if (dest == null) {
                        return "okay";
                    }
                }
            }
        }
        else {
            return "okay";
        }
        ride.setDestination(dest);
        return "Changed the destination to "+dest.getLocationName();
    }

    private String processChangeSource(String instruction) {
        askQuestion("Are you sure you want to change the pickup?");
        String ans = listenForAnswer();
        String category = getCategory(ans);
        Location source = null;

        if(category == AFFIRMATION) {
            while(source == null) {
                askQuestion("Please specify the pickup");
                ans = listenForAnswer();
                source = fetchSourceLocationFromInstr(ans);
                if (source == null) {
                    source = getPickupFromUser();
                    if (source == null) {
                        return "okay";
                    }
                }
            }
        }
        ride.setSource(source);
        return "Changed the pickup to "+source.getLocationName();
    }

    private String processVehicleInquiry() {
        return "The cab is a white swift desire with number MH12 1234";
    }

    private String processDriverInquiry() {
        return "Driver's name is Ganesh. He has a 4.5 stars rating.";
    }

    private String processLocationInquiry() {
        return "We are currently at "+getCurrentLocation().getLocationName()+". Time to reach destination is 10 minutes.";
    }

    private String processBookCabCommand(String instruction) {
        Location source = fetchSourceLocationFromInstr(instruction);
        Location dest = fetchDropLocationFromInstr(instruction);

        if(source == null) {
            source = getPickupFromUser();
            if(source == null) {
                return "okay";
            }
        }
        if(dest == null) {
            dest = getDroppingPtFromUser();
            if(dest == null) {
                return "okay";
            }
        }
        return confirmSourceAndDest(source, dest);

    }

    private String confirmSourceAndDest(Location source, Location dest) {
        String ans, category;
        if(source!=null && dest!=null) {
            askQuestion("Request to book a cab from "+source+" to "+dest+" received. Say yes to search for nearby rides.");
            ans = listenForAnswer();
            category = getCategory(ans);
            if(category == AFFIRMATION) {
                askQuestion("Looking for nearby drivers");
                // TODO: call a function to start looking for nearby drivers;
                bookRide(source, dest);
                if(ride.getRideStatus() == "BOOKED") {
                    status = ride.getRideStatus();
                    return ride.getDriver().getDriverName()+" has accepted your ride request. They are arriving in "+ride.getTimeInMinutesForDriver()+" minutes";
                }
                else return "Sorry. No rides are available.";
            }
            else if(category == STOP_PROCESS || category == CANCEL_RIDE) {
                return "Okay";
            }
            else if(category == NEGATION) {
               askQuestion("Do you want to update the destination?");
               ans = listenForAnswer();
               category = getCategory(ans);
               if(category == NEGATION) {
                   askQuestion("Do you want to update the pickup?");
                   ans = listenForAnswer();
                   category = getCategory(ans);
                   if(category == NEGATION) {
                       confirmSourceAndDest(source, dest);
                   }
               }
               else {
                   processChangeDestination(ans);
               }
            }
            else {
                return "Sorry, didn't get you.";
            }
        }
        return "";
    }

    private Location getPickupFromUser() {
        Location source = null;
        String ans, category;
        while(source == null) {
            askQuestion("Do you want to use your current location as the pickup?");
            ans = listenForAnswer();
            category = getCategory(ans);
            if(category.equals(STOP_PROCESS) || category == CANCEL_RIDE) {
                return null;
            }
            if(category == AFFIRMATION) {
                source = getCurrentLocation();
            }
            else {
                if(Objects.equals(category, NEGATION)) {
                    askQuestion("Please specify the pickup location to continue.");
                    ans = listenForAnswer();
                    category = getCategory(ans);
                    if(category == STOP_PROCESS || category == CANCEL_RIDE) {
                        return null;
                    }
                }
                source = fetchSourceLocationFromInstr(ans);
            }
        }
        return source;
    }

    private Location getDroppingPtFromUser() {
        Location dest = null;
        String ans, category;
        while(dest == null) {
            askQuestion("Please specify the destination to continue.");
            ans = listenForAnswer();
            category = getCategory(ans);
            if(category == STOP_PROCESS || category == CANCEL_RIDE) {
                return null;
            }
            dest = fetchSourceLocationFromInstr(ans);
        }
        return dest;
    }
    private void bookRide(Location source, Location dest) {
        Vehicle v = new Vehicle("MH12-1234", "Mini", "Celerio");
        Driver d = new Driver("Dilip", v, "12334433", 4);
        ride = new Ride(passenger, d, source, dest);
        ride.setRideStatus("BOOKED");
    }
    private String getCategory(String ans) {
        return AFFIRMATION;
    }

    private Location getCurrentLocation() {
        return new Location("Hadapsar,Pune", "hdp");
    }

    private void askQuestion(String question) {
        //TTS call
    }

    private String listenForAnswer() {
        return "mock";
    }
    private Location fetchSourceLocationFromInstr(String instruction) {
        // TODO: Process the text to fetch starting point
        if(instruction == "nonnull") {
        return new Location("Kharadi,Pune,Maharashtra", "xyz");}
        else return null;
    }

    private Location fetchDropLocationFromInstr(String instruction) {
        // TODO: Process the text to fetch dropping point
        if(instruction == "nonnull") {
            return new Location("Wakad,Pune,Maharashtra", "abc");
        }
        else return null;
    }
}
