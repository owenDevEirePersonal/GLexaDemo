package com.deveire.dev.glexademo.TroubleTicketSystem;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.deveire.dev.glexademo.R;
import com.deveire.dev.glexademo.SpeechIntent;
import com.deveire.dev.glexademo.SpeechIntents.PingingFor_Clarification;
import com.deveire.dev.glexademo.SpeechIntents.PingingFor_YesNo;
import com.deveire.dev.glexademo.TroubleTicketSystem.SpeechIntents.PingingFor_MatchesKeyword;
import com.deveire.dev.glexademo.TroubleTicketSystem.SpeechIntents.PingingFor_MatchesTask;
import com.deveire.dev.glexademo.TroubleTicketSystem.SpeechIntents.PingingFor_TroublerStart;
import com.deveire.dev.glexademo.TroubleTicketSystem.SpeechIntents.PingingFor_YourOwnTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TroubleTicketActivity extends Activity implements RecognitionListener
{

    private NfcAdapter nfcAdapt;

    private TextView debugText;
    private TextView outputText;
    private Button startButton;

    private SpeechRecognizer recog;
    private Intent recogIntent;
    private SpeechIntent pingingRecogFor;
    private SpeechIntent previousPingingRecogFor;

    //private final PingingForTest pingingForTest = new PingingForTest();

    //[Experimental Recog instantly stopping BugFix Variables]
    private boolean recogIsRunning;
    private Timer recogDefibulatorTimer;
    private TimerTask recogDefibulatorTask; //will check to see if recogIsRunning and if not will destroy and instanciate recog, as recog sometimes kills itself silently
    //requiring a restart. This loop will continually kill and restart recog, preventing it from killing itself off.
    private RecognitionListener recogListener;
    //[/Experimental Recog instantly stopping BugFix Variables]

    //[TextToSpeech Variables]
    private TextToSpeech toSpeech;
    //[/End of TextToSpeech Variables]

    private PingingFor_YesNo pingingFor_isAFishYesNo;

    //[Troubler Variables]
    private ArrayList<TroubleTask> allTroubleTasks;
    private ArrayList<TroubleTask> potentialTroubleTasks;
    private ArrayList<TroubleTask> eliminatedTroubleTasks;

    private ArrayList<TroubleKeyword> allKnownKeywords;
    private ArrayList<TroubleKeyword> usedKeywords;
    private TroubleKeyword currentKeyword;

    private int currentFinalistIndex;
    //[End of Troubler Variables]


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trouble_ticket);

        debugText = (TextView) findViewById(R.id.debugText);
        outputText = (TextView) findViewById(R.id.outputText);
        startButton = (Button) findViewById(R.id.startButton);

        startButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                debugText.setText("Card Swiped, begining");
                Log.i("TTDemo", debugText.getText().toString());
                //startDialog(pingingFor_isAFishYesNo);
                startDialog(new PingingFor_TroublerStart());
            }
        });

        setupSpeechRecognition();
        setupTextToSpeech();

        pingingFor_isAFishYesNo = new PingingFor_YesNo();
        pingingFor_isAFishYesNo.setSpeechPrompt("Is that a fish");

        setupTroubler();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        toSpeech.stop();
        toSpeech.shutdown();
        super.onStop();
    }


//+++++++++++++++++++++++++++++++Troubler Code++++++++++++++++++++++++++++++++++++++
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    private void setupTroubler()
    {
        allTroubleTasks = new ArrayList<TroubleTask>();
        potentialTroubleTasks = new ArrayList<TroubleTask>();
        eliminatedTroubleTasks = new ArrayList<TroubleTask>();

        allKnownKeywords = new ArrayList<TroubleKeyword>();
        usedKeywords = new ArrayList<TroubleKeyword>();

        currentFinalistIndex = 0;

        ArrayList<TroubleKeyword> newTaskTags = new ArrayList<TroubleKeyword>();
        newTaskTags.add(new TroubleKeyword("Milk", new String[]{"Milk", "Cow Liquid", "Cream"}, "Does your meal use any milk?"));
        newTaskTags.add(new TroubleKeyword("Eggs", new String[]{"Eggs", "egg", "eggwhites"}, "Does your meal use eggs?"));
        newTaskTags.add(new TroubleKeyword("Bacon", new String[]{"Bacon", "rashers", "pork"}, "Does your meal use bacon or other pork products?"));
        allTroubleTasks.add(new TroubleTask("Cook 1 serving of Meal type A", "Do you wish to order Meal Type A?", newTaskTags, "1 Frying Pan, 3 eggs, 4 strips of bacon and a half litre of milk"));
        addToKnownKeywords(new TroubleTask("Cook 1 serving of Meal type A", "Do you wish to order Meal Type A?", newTaskTags, "1 Frying Pan, 3 eggs, 4 strips of bacon and a half litre of milk"));

        newTaskTags = new ArrayList<TroubleKeyword>();
        newTaskTags.add(new TroubleKeyword("Milk", new String[]{"Milk", "Cow Liquid", "Cream"}, "Does you meal use any milk?"));
        newTaskTags.add(new TroubleKeyword("Eggs", new String[]{"Eggs", "egg", "eggwhites"}, "Does your meal use eggs?"));
        newTaskTags.add(new TroubleKeyword("Pancakes", new String[]{"Pancakes", "flapjacks", "waffles"}, "Does your meal include pancakes, panacakes or whatever you call them?"));
        allTroubleTasks.add(new TroubleTask("Cook 1 serving of Meal type B", "Do you wish to order Meal Type B?", newTaskTags, "1 Frying Pan, 3 eggs, 1 jug of pancake mix and a half litre of milk"));
        addToKnownKeywords(new TroubleTask("Cook 1 serving of Meal type B", "Do you wish to order Meal Type B?", newTaskTags, "1 Frying Pan, 3 eggs, 1 jug of pancake mix and a half litre of milk"));

        newTaskTags = new ArrayList<TroubleKeyword>();
        newTaskTags.add(new TroubleKeyword("fish", new String[]{"fish", "cod", "sea meat"}, "Does your meal use some kind of fish?"));
        newTaskTags.add(new TroubleKeyword("Eggs", new String[]{"Eggs", "egg", "eggwhites"}, "Does your meal use eggs?"));
        newTaskTags.add(new TroubleKeyword("Beans", new String[]{"beans"}, "Does your meal include beans, baked or otherwise?"));
        allTroubleTasks.add(new TroubleTask("Cook 1 serving of Meal type C", "Do you wish to order Meal Type C?", newTaskTags, "1 Frying Pan, 3 eggs, 1 codfish and half a tin of beans"));
        addToKnownKeywords(new TroubleTask("Cook 1 serving of Meal type C", "Do you wish to order Meal Type C?", newTaskTags, "1 Frying Pan, 3 eggs, 1 codfish and half a tin of beans"));

        newTaskTags = new ArrayList<TroubleKeyword>();
        newTaskTags.add(new TroubleKeyword("fish", new String[]{"fish", "cod", "sea meat"}, "Does your meal use some kind of fish?"));
        newTaskTags.add(new TroubleKeyword("Bacon", new String[]{"Bacon", "rashers", "pork"}, "Does your meal use bacon or other pork products?"));
        allTroubleTasks.add(new TroubleTask("Cook 1 serving of Meal type D", "Do you wish to order Meal Type D?", newTaskTags, "1 Frying Pan, 1 codfish and 5 strips of bacon"));
        addToKnownKeywords(new TroubleTask("Cook 1 serving of Meal type D", "Do you wish to order Meal Type D?", newTaskTags, "1 Frying Pan, 1 codfish and 5 strips of bacon"));

        newTaskTags = new ArrayList<TroubleKeyword>();
        newTaskTags.add(new TroubleKeyword("Beans", new String[]{"beans"}, "Does your meal include beans, baked or otherwise?"));
        newTaskTags.add(new TroubleKeyword("Cheese", new String[]{"Cheese"}, "Does your meal include cheese or other fermented non-meat, non-manure bovine product?"));
        allTroubleTasks.add(new TroubleTask("Cook 1 serving of Meal type E", "Do you wish to order Meal Type E?", newTaskTags, "1 Pot, 47 Cheese wheels, 1 tin of beans, 5 salt pile and Alchemy Skill of 25 or greater"));
        addToKnownKeywords(new TroubleTask("Cook 1 serving of Meal type E", "Do you wish to order Meal Type E?", newTaskTags, "1 Pot, 47 Cheese wheels, 1 tin of beans, 5 salt pile and Alchemy Skill of 25 or greater"));

        newTaskTags = new ArrayList<TroubleKeyword>();
        newTaskTags.add(new TroubleKeyword("Milk", new String[]{"Milk", "Cow Liquid", "Cream"}, "Does you meal use any milk?"));
        newTaskTags.add(new TroubleKeyword("Eggs", new String[]{"Eggs", "egg", "eggwhites"}, "Does your meal use eggs?"));
        newTaskTags.add(new TroubleKeyword("Cheese", new String[]{"Cheese"}, "Does your meal include cheese or other fermented non-meat, non-manure bovine product?"));
        allTroubleTasks.add(new TroubleTask("Cook 1 serving of Meal type F", "Do you wish to order Meal Type F?", newTaskTags, "1 Pot, 49 and a half Cheese wheels, 2 eggs, 5 buckets of milk and knowledge of the Whirlwind Sprint Shout"));
        addToKnownKeywords(new TroubleTask("Cook 1 serving of Meal type F", "Do you wish to order Meal Type F?", newTaskTags, "1 Pot, 49 and a half Cheese wheels, 2 eggs, 5 buckets of milk and knowledge of the Whirlwind Sprint Shout"));

        newTaskTags = new ArrayList<TroubleKeyword>();
        newTaskTags.add(new TroubleKeyword("Beans", new String[]{"beans"}, "Does your meal include beans, baked or otherwise?"));
        newTaskTags.add(new TroubleKeyword("Eggs", new String[]{"Eggs", "egg", "eggwhites"}, "Does your meal use eggs?"));
        newTaskTags.add(new TroubleKeyword("fish", new String[]{"fish", "cod", "sea meat"}, "Does your meal use some kind of fish?"));
        allTroubleTasks.add(new TroubleTask("Cook 1 serving of Meal type G", "Do you wish to order Meal Type G?", newTaskTags, "1 Pot, 1 Herring, red for preferance, 8 eggs and a large tin of beans"));
        addToKnownKeywords(new TroubleTask("Cook 1 serving of Meal type G", "Do you wish to order Meal Type G?", newTaskTags, "1 Pot, 1 Herring, red for preferance, 8 eggs and a large tin of beans"));
    }

    private void addToKnownKeywords(TroubleTask aTask)
    {
        for (TroubleKeyword aKey: aTask.getTags())
        {
            if (!isInArray(allKnownKeywords, aKey))
            {
                allKnownKeywords.add(aKey);
            }
        }
    }

    private TroubleKeyword getMostUsefulKeyword()
    {
        Log.i("TTDemo", "getting Most Useful Keyword");
        Boolean noUsefulKeywordsFound = true;
        TroubleKeyword currentMostUseful = allKnownKeywords.get(0);
        int currentMostUsefulCount = Math.abs(potentialTroubleTasks.size());


        for (TroubleKeyword aKeyword: allKnownKeywords)
        {
            Log.i("TTDemo", "  Testing for past use of: " + aKeyword.getKeyword());
            if(!isInArray(usedKeywords, aKeyword))
            {
                Log.i("TTDemo", "  running heuristic on: " + aKeyword.getKeyword());
                int currentCount = 0;
                for (TroubleTask aTask: potentialTroubleTasks)
                {
                    if(isInArray(aTask.getTags(), aKeyword))
                    {
                        currentCount++;
                    }
                }

                if(Math.abs(currentCount - ((int)(potentialTroubleTasks.size() / 2))) < Math.abs(currentMostUsefulCount - ((int)(potentialTroubleTasks.size() / 2))))
                {
                    currentMostUseful = aKeyword;
                    currentMostUsefulCount = currentCount;

                    //At least 1 keyword was found to not be a tag of at least 1 of the potential tasks, meaning that asking about tags can still eliminate potential tasks.
                    //Log.i("TTDemo", "Unique keyword found, setting to false");
                    noUsefulKeywordsFound = false;
                }
            }
        }

        if(noUsefulKeywordsFound)
        {
            //will trigger an if statement for handling the situtation in which all remaining potential tasks have identical tags and thus asking for tags would be irrelevant
            Log.i("TTDemo", "allKeywords are present, returning null");
            return null;
        }
        else
        {
            Log.i("TTDemo", "returning most useful keyword");
            return currentMostUseful;
        }
    }

    private boolean isInArray(ArrayList<TroubleKeyword> list, TroubleKeyword item)
    {
        for (TroubleKeyword a:list)
        {
            if(a.matches(item))
            {
                return true;
            }
        }
        return false;
    }

    private void updatePotentialTasks(TroubleKeyword tag, boolean isTagCorrect)
    {
        if(isTagCorrect)
        {
            outputText.setText("Tag " + tag.getKeyword() + " is correct.");
        }
        else
        {
            outputText.setText("Tag " + tag.getKeyword() + " is incorrect.");
        }


        ArrayList<TroubleTask> newPotentialTroubleTasks = new ArrayList<TroubleTask>();
        for (TroubleTask aTask: potentialTroubleTasks)
        {
            if((isTagCorrect && isInArray(aTask.getTags(), tag)) || (!isTagCorrect && !isInArray(aTask.getTags(), tag)))
            {
                newPotentialTroubleTasks.add(aTask);
            }
            else
            {
                eliminatedTroubleTasks.add(aTask);
            }
        }
        potentialTroubleTasks = newPotentialTroubleTasks;
    }

//+++++++++++++++++++++++++++++++End of Troubler Code+++++++++++++++++++++++++++++++
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++





//+++++++++++++++++++++++++++++++Voice Interface Code+++++++++++++++++++++++++++++++
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //++++++[Text To Speech Code]
    public void startDialog(SpeechIntent intent)
    {
        pingingRecogFor = intent;
        Log.i("Speech", "Starting Dialog with textToSpeech for intent: " + intent.getName());
        toSpeech.speak(pingingRecogFor.getSpeechPrompt(), TextToSpeech.QUEUE_FLUSH, null, pingingRecogFor.getName());
    }

    private void setupTextToSpeech()
    {
        toSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                Log.i("Text To Speech Update", "onInit Complete");
                toSpeech.setLanguage(Locale.ENGLISH);
                HashMap<String, String> endOfSpeakIndentifier = new HashMap();
                endOfSpeakIndentifier.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "endOfSpeech");
                toSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener()
                {
                    @Override
                    public void onStart(String utteranceId)
                    {
                        Log.i("Speech", "onStart called");
                    }

                    @Override
                    public void onDone(String utteranceId)
                    {
                        Log.i("Speech", utteranceId + " DONE!");
                        if (utteranceId.matches(new PingingFor_Clarification().getName()))
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    startRecogListening(pingingRecogFor);
                                }
                            });
                        }
                        else if(utteranceId.matches(pingingFor_isAFishYesNo.getName()))
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    startRecogListening(pingingRecogFor);
                                }
                            });
                        }
                        else if(utteranceId.matches(new PingingFor_TroublerStart().getName()))
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    startRecogListening(pingingRecogFor);
                                }
                            });
                        }
                        else if(utteranceId.matches(new PingingFor_YourOwnTask().getName()))
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    startRecogListening(pingingRecogFor);
                                }
                            });
                        }
                        else if(utteranceId.matches(new PingingFor_MatchesTask(potentialTroubleTasks.get(currentFinalistIndex)).getName()))
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    startRecogListening(pingingRecogFor);
                                }
                            });
                        }
                        else if(utteranceId.matches(new PingingFor_MatchesKeyword(currentKeyword).getName())) //caution: currentKeyword may be null if task picking has reached finalists stage
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    startRecogListening(pingingRecogFor);
                                }
                            });
                        }
                        else
                        {
                            Log.e("Speech", "Unrecognised utteranceID");
                        }
                        //TODO: Add calls to startRecogListening for each SpeechIntent
                        //toSpeech.shutdown();
                    }

                    @Override
                    public void onError(String utteranceId)
                    {
                        Log.i("Speech", "ERROR DETECTED");
                    }
                });
            }
        });
    }
    //++++++[/End of Text To Speech Code]

    //++++++[Recognistion Setup Code]
    private void setupSpeechRecognition()
    {
        recogIsRunning = false;
        recog = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        recogListener = this;
        recog.setRecognitionListener(recogListener);
        recogIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recogIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
        recogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recogIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        /*recogDefibulatorTimer = new Timer();
        recogDefibulatorTask = new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(!recogIsRunning)
                        {
                            recog.destroy();
                            recog = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                            recog.setRecognitionListener(recogListener);
                        }
                    }
                });
            }
        };
        recogDefibulatorTimer.schedule(recogDefibulatorTask, 0, 4000);*/
    }
//++++++[end of Recognition Setup Code]

    //++++++++[Recognition Listener Code]
    @Override
    public void onReadyForSpeech(Bundle bundle)
    {
        Log.e("Recog", "ReadyForSpeech");
        //recogIsRunning = false;
    }

    @Override
    public void onBeginningOfSpeech()
    {
        Log.e("Recog", "BeginningOfSpeech");
        //recogIsRunning = true;
    }

    @Override
    public void onRmsChanged(float v)
    {
        Log.e("Recog", "onRmsChanged");
    }

    @Override
    public void onBufferReceived(byte[] bytes)
    {
        Log.e("Recog", "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech()
    {
        Log.e("Recog", "End ofSpeech");
        recog.stopListening();
    }

    @Override
    public void onError(int i)
    {
        switch (i)
        {
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                Log.e("Recog", "SPEECH TIMEOUT ERROR");
                break;
            case SpeechRecognizer.ERROR_SERVER:
                Log.e("Recog", "SERVER ERROR");
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                Log.e("Recog", "BUSY ERROR");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                Log.e("Recog", "NETWORK TIMEOUT ERROR");
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                Log.e("Recog", "TIMEOUT ERROR");
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                Log.e("Recog", "INSUFFICENT PERMISSIONS ERROR");
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                Log.e("Recog", "CLIENT ERROR");
                break;
            case SpeechRecognizer.ERROR_AUDIO:
                Log.e("Recog", "AUDIO ERROR");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                Log.e("Recog", "NO MATCH ERROR");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    debugText.setText("No Response Detected aborting.");
                    toSpeech.speak("No Response Detected, aborting.", TextToSpeech.QUEUE_FLUSH, null, "EndError");
                }
                break;
            default:
                Log.e("Recog", "UNKNOWN ERROR: " + i);
                break;
        }
    }


    @Override
    public void onResults(Bundle bundle)
    {
        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        debugText.setText("" + matches.toString());
        handleResults(matches);
    }

    @Override
    public void onPartialResults(Bundle bundle)
    {
        Log.e("Recog", "Partial Result");
    }

    @Override
    public void onEvent(int i, Bundle bundle)
    {
        Log.e("Recog", "onEvent");
    }
//++++++++[end of Recognition Listener Code]

//++++++++[Recognition Other Code]

    //Start listening for a user response to intent
    private void startRecogListening(SpeechIntent intent)
    {
        Log.i("Output", "starting Recog for: " + intent.getName());
        debugText.setText("starting Recog for: " + intent.getName());

        pingingRecogFor = intent;
        recog = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        recogListener = this;
        recog.setRecognitionListener(recogListener);
        recogIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recogIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
        recogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recogIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        recog.startListening(recogIntent);
    }

    //Start of Recog result handling, if the recog intent was for clarification get the 1st keyword and prepare a response,
    // otherwise send the user response to be clarified.
    private void handleResults(ArrayList<String> matches)
    {
        Log.i("Output", "handleResults with: " + matches.toString());
        debugText.setText("handleResults");

        if (pingingRecogFor.getName().matches(new PingingFor_Clarification().getName())) //this is messy, FIND A BETTER WAY OF GETTING THE NAME FOR PingingFor_Clarification
        {
            if (sortThroughForFirstMatch(matches, pingingRecogFor).matches("-NoMatchFound-"))
            {
                //repeat clarification
                startRecogListening(pingingRecogFor);
            }
            else
            {
                prepareResponseFor(sortThroughForFirstMatch(matches, pingingRecogFor), previousPingingRecogFor);
            }
        }
        else
        {
            if (pingingRecogFor.isFillInIntent())
            {
                fillResponse(matches, pingingRecogFor);
                //prepareResponseFor(matches.get(0), pingingRecogFor);
            }
            else
            {
                filterThroughClarification(matches, pingingRecogFor);
            }
        }
    }

    private void fillResponse(ArrayList<String> results, SpeechIntent pingingFor)
    {
        if (results.size() > 0)
        {
            prepareResponseFor(results.get(0), pingingFor);
        }
        else
        {
            Log.e("Output", "Error, fillResponse got no results");
        }
    }

    //Returns first matching keyword found
    private String sortThroughForFirstMatch(ArrayList<String> results, SpeechIntent pingingFor)
    {
        Log.i("Output", "sortThroughForFirstMatch");
        debugText.setText("sortThroughForFirstMatch");

        for (String aResult : results)
        {
            for (String keyword : pingingFor.getResponseKeywords())
            {
                if (aResult.toLowerCase().contains(keyword.toLowerCase()))
                {
                    return keyword;
                }
                else
                {
                    for (String synonym : pingingFor.getResponseSynonyms(keyword))
                    {
                        if (aResult.toLowerCase().contains(synonym.toLowerCase()))
                        {
                            return keyword;
                        }
                    }
                }
            }
        }
        return "-NoMatchFound-";
    }

    //Check all results for keywords regardless of accuracy, then present all found keys words to clarification methods.
    //More thorough but more likely to ask the user for clarification
    private ArrayList<String> sortAllPossibleResultsForAllMatches(ArrayList<String> results, SpeechIntent pingingFor)
    {
        Log.i("Output", "sortAllPossibleResultsForAllMatches");
        debugText.setText("sortAllPossibleResultsForAllMatches");

        ArrayList<String> foundMatches = new ArrayList<String>();
        for (String aResult : results)
        {
            for (String keyword : pingingFor.getResponseKeywords())
            {
                for (String synonym : pingingFor.getResponseSynonyms(keyword))
                {
                    if (aResult.toLowerCase().contains(synonym.toLowerCase()))
                    {
                        foundMatches.add(keyword);
                        break;
                    }
                }
            }
        }

        ArrayList<String> foundUniqueMatches = new ArrayList<>();
        for (String aMatch : foundMatches)
        {
            boolean isDupe = false;
            for (String aUniqueMatch : foundUniqueMatches)
            {
                if (aMatch.matches(aUniqueMatch))
                {
                    isDupe = true;
                    break;
                }
            }
            if (!isDupe)
            {
                foundUniqueMatches.add(aMatch);
            }
        }
        foundMatches = foundUniqueMatches;

        return foundMatches;
    }

    //Check for matches from most accurate to least accurate, but stop searching as soon as any result produces a match.
    //Less through but less likely to ask the user for unnessary clarification
    //if the most accurate result(1st in the array) contains no key words, swap to the next most accurate result.
    private ArrayList<String> sortForAllMatchesInMostAccurateResult(ArrayList<String> results, SpeechIntent pingingFor)
    {
        Log.i("Output", "sortAllPossibleResultsForAllMatches");
        debugText.setText("sortAllPossibleResultsForAllMatches");

        ArrayList<String> foundMatches = new ArrayList<String>();
        for (String aResult : results)
        {
            for (String keyword : pingingFor.getResponseKeywords())
            {
                for (String synonym : pingingFor.getResponseSynonyms(keyword))
                {
                    if (aResult.toLowerCase().contains(synonym.toLowerCase()))
                    {
                        foundMatches.add(keyword);
                        break;
                    }
                }
            }


            if (foundMatches.size() > 0)
            {
                break;
            }
        }

        return foundMatches;
    }

    //Gets possible keywords said by the user from sortAllPossibleResultsForAllMatches then launches recog for Clarification if more than 1 keyword found,
    // else, passes intent to output handling.
    private void filterThroughClarification(ArrayList<String> results, SpeechIntent pingingFor)
    {
        Log.i("Output", "filterThroughClarification");
        debugText.setText("filterThroughClarification");

        ArrayList<String> possibleKeywords = sortAllPossibleResultsForAllMatches(results, pingingFor);

        if (possibleKeywords.size() > 1)
        {
            Log.i("Output", "about to start pinging for Clarification with: " + possibleKeywords.toString());
            Log.i("Output", "about to start pinging for Clarification with: " + possibleKeywords.toString());
            debugText.setText("filterThroughClarification for clarification: Did you mean? " + possibleKeywords.toString());
            previousPingingRecogFor = pingingFor;
            startDialog(new PingingFor_Clarification(possibleKeywords));
        }
        else if (possibleKeywords.size() == 1)
        {
            prepareResponseFor(possibleKeywords.get(0), pingingFor);
        }
        else
        {
            Log.i("Output", "Error prepareingResponses from filterThroughClarification, as possibleKeywords is empty: ");
            debugText.setText("Error prepareingResponses from filterThroughClarification, as possibleKeywords is empty: ");
            previousPingingRecogFor = pingingFor;
            startDialog(new PingingFor_Clarification(pingingFor.getResponseKeywords()));
        }
    }

    //Call the Output method for the given intent
    private void prepareResponseFor(String result, SpeechIntent pingingFor)
    {
        Log.i("Output", "prepareResponseFor" + pingingFor.getName() + " with result: " + result);
        debugText.setText("prepareResponseFor" + pingingFor.getName() + " with result: " + result);

        if (pingingFor.getName().matches(pingingFor_isAFishYesNo.getName()))
        {
            if(result.matches("Yes"))
            {
                toSpeech.speak("Thats a fish", TextToSpeech.QUEUE_FLUSH, null, "ThatsAFish");
                outputText.setText("Thats a fish");
            }
            else if (result.matches("No"))
            {
                toSpeech.speak("That's no fish, its a crusteacan", TextToSpeech.QUEUE_FLUSH, null, "ThatsNoFish");
                outputText.setText("That's no fish, its a crusteacan");
            }

        }
        else if (pingingFor.getName().matches(new PingingFor_TroublerStart().getName()))
        {
            if(result.matches("Raise Trouble Ticket"))
            {
                outputText.setText("Raising Trouble Ticket");
                potentialTroubleTasks = allTroubleTasks;
                eliminatedTroubleTasks = new ArrayList<TroubleTask>();
                usedKeywords = new ArrayList<TroubleKeyword>();
                currentKeyword = getMostUsefulKeyword();
                if(currentKeyword != null)
                {
                    usedKeywords.add(currentKeyword);
                    startDialog(new PingingFor_MatchesKeyword(currentKeyword));
                }
                else
                {
                    currentFinalistIndex = 0;
                    startDialog(new PingingFor_MatchesTask(potentialTroubleTasks.get(currentFinalistIndex)));
                }
            }
        }
        else if (pingingFor.getName().matches(new PingingFor_YourOwnTask().getName()))
        {
            outputText.setText("Creating a new alert with the description: " + result);
            toSpeech.speak("Creating a new alert with the description: " + result, TextToSpeech.QUEUE_FLUSH, null, "Own Task Final Alert");
        }
        else if(pingingFor.getName().matches(new PingingFor_MatchesTask(potentialTroubleTasks.get(currentFinalistIndex)).getName()))
        {
            if(result.matches("Yes"))
            {
                toSpeech.speak("Creating New Alert: " + potentialTroubleTasks.get(currentFinalistIndex).getDescription() + ". And the requirements are as follows: " + potentialTroubleTasks.get(currentFinalistIndex).getRequirements(), TextToSpeech.QUEUE_FLUSH, null, "Final Alert Creation");
                outputText.setText("Creating New Alert: " + potentialTroubleTasks.get(currentFinalistIndex).getDescription() + " \n\n And the requirements are as follows: " + potentialTroubleTasks.get(currentFinalistIndex).getRequirements());
            }
            else if(result.matches("No"))
            {
                currentFinalistIndex++;
                if(currentFinalistIndex < potentialTroubleTasks.size())
                {
                    startDialog(new PingingFor_MatchesTask(potentialTroubleTasks.get(currentFinalistIndex)));
                }
                else
                {
                    startDialog(new PingingFor_YourOwnTask());
                }
            }

        }
        else if(pingingFor.getName().matches(new PingingFor_MatchesKeyword(currentKeyword).getName())) //caution: currentKeyword may be null if task picking has reached finalists stage
        {
            if(result.matches("Yes"))
            {
                updatePotentialTasks(currentKeyword, true);
            }
            else if(result.matches("No"))
            {
                updatePotentialTasks(currentKeyword, false);
            }

            if(potentialTroubleTasks.size() == 1)
            {
                toSpeech.speak("Creating New Alert: " + potentialTroubleTasks.get(0).getDescription() + ". And the requirements are as follows: " + potentialTroubleTasks.get(0).getRequirements(), TextToSpeech.QUEUE_FLUSH, null, "Final Alert Creation");
                outputText.setText("Creating New Alert: " + potentialTroubleTasks.get(0).getDescription() + " \n\n And the requirements are as follows: " + potentialTroubleTasks.get(0).getRequirements());
            }
            else if (potentialTroubleTasks.size() == 0)
            {
                startDialog(new PingingFor_YourOwnTask());
            }
            else
            {
                currentKeyword = getMostUsefulKeyword();
                if(currentKeyword != null)
                {
                    Log.i("TTDemo", "currentKeyword is not null, moving to Task tag elimination");
                    usedKeywords.add(currentKeyword);
                    startDialog(new PingingFor_MatchesKeyword(currentKeyword));
                }
                else
                {
                    Log.i("TTDemo", "currentKeyword is null, moving to Task finalist elimination");
                    currentFinalistIndex = 0;
                    Log.i("TTDemo", "currentTask Finalist: " + potentialTroubleTasks.get(currentFinalistIndex).getPromptQuestion());
                    startDialog(new PingingFor_MatchesTask(potentialTroubleTasks.get(currentFinalistIndex)));
                }
            }

        }
        else
        {
            Log.e("Response:", "No response setup for this intent: " + pingingFor.getName());
            debugText.setText("No response setup for this intent: " + pingingFor.getName());
        }
    }

//++++++++[/Recognition Other Code]


//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//++++++++++++++++++++++++++End of Voice Interface Code+++++++++++++++++++++++++++++
}
