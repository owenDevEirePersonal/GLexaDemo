package com.deveire.dev.glexademo;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.deveire.dev.glexademo.SpeechIntents.PingingForOtherTest;
import com.deveire.dev.glexademo.SpeechIntents.PingingForTest;
import com.deveire.dev.glexademo.SpeechIntents.PingingFor_Clarification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements RecognitionListener
{

    private Button listenButton;
    private TextView debugText;
    private TextView outputText;

    private SpeechRecognizer recog;
    private Intent recogIntent;
    /*private int pingingRecogFor;
    private int previousPingingRecogFor;
    private final int pingingRecogFor_FoodName = 1;
    private final int pingingRecogFor_TroubleTicketConfirmation = 2;
    private final int pingingRecogFor_Clarification = 3;
    private final int pingingRecogFor_ScriptedExchange = 4;
    private final int pingingRecogFor_CleanerCommands = 5;
    private final int pingingRecogFor_CleanerTroubleTicket1 = 6;
    private final int pingingRecogFor_CleanerTroubleTicket2 = 7;
    private final int pingingRecogFor_CleanerTroubleTicket1a = 8;
    private final int pingingRecogFor_Nothing = -1;*/

    private SpeechIntent pingingRecogFor;
    private SpeechIntent previousPingingRecogFor;
    private final PingingForTest pingingForTest = new PingingForTest();
    private final PingingForOtherTest pingingForOtherTest = new PingingForOtherTest();

    private String[] currentPossiblePhrasesNeedingClarification;
    private boolean clarificationAskToRepeatOnNoMatch;
    private String lastResponseToClarification;

    //[Experimental Recog instantly stopping BugFix Variables]
    private boolean recogIsRunning;
    private Timer recogDefibulatorTimer;
    private TimerTask recogDefibulatorTask; //will check to see if recogIsRunning and if not will destroy and instanciate recog, as recog sometimes kills itself silently
    //requiring a restart. This loop will continually kill and restart recog, preventing it from killing itself off.
    private RecognitionListener recogListener;
    //[/Experimental Recog instantly stopping BugFix Variables]

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listenButton = (Button) findViewById(R.id.listenButton);
        listenButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startRecogListening(pingingForTest);
            }
        });

        debugText = (TextView) findViewById(R.id.debugText);
        outputText = (TextView) findViewById(R.id.outputText);

        recogIsRunning = false;
        recog = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        recogListener = this;
        recog.setRecognitionListener(recogListener);
        recogIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en");
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




        currentPossiblePhrasesNeedingClarification = new String[]{};
    }


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
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: Log.e("Recog", "SPEECH TIMEOUT ERROR"); break;
            case SpeechRecognizer.ERROR_SERVER: Log.e("Recog", "SERVER ERROR"); break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: Log.e("Recog", "BUSY ERROR"); break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: Log.e("Recog", "NETWORK TIMEOUT ERROR"); break;
            case SpeechRecognizer.ERROR_NETWORK: Log.e("Recog", "TIMEOUT ERROR"); break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: Log.e("Recog", "INSUFFICENT PERMISSIONS ERROR"); break;
            case SpeechRecognizer.ERROR_CLIENT: Log.e("Recog", "CLIENT ERROR"); break;
            case SpeechRecognizer.ERROR_AUDIO: Log.e("Recog", "AUDIO ERROR"); break;
            case SpeechRecognizer.ERROR_NO_MATCH: Log.e("Recog", "NO MATCH ERROR");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    debugText.setText("No Response Detected aborting.");
                    //toSpeech.speak("No Response Detected, aborting.", TextToSpeech.QUEUE_FLUSH, null, "EndError");
                }
                break;
            default: Log.e("Recog", "UNKNOWN ERROR: " + i); break;
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
//++++++++[/Recognition Listener Code]

//++++++++[Recognition Other Code]
    private String sortThroughRecognizerResults(ArrayList<String> results, String[] matchablePhrases)
    {
        for (String aResult: results)
        {
            Log.i("Recog", "Sorting results for result: " + aResult);
            for (String aPhrase: matchablePhrases)
            {
                Log.i("Recog", "Sorting results, matching Result: " + aResult.toLowerCase().replace("-", " ") + " to Phrase: " + aPhrase.toLowerCase());
                if((aResult.toLowerCase().replace("-"," ")).contains(aPhrase.toLowerCase()))
                {
                    Log.i("Recog", "Match Found");
                    return aPhrase;
                }
            }
        }
        Log.i("Recog", "No matches found, returning empty string \"\" .");
        return "";
    }


    private void handleResults(ArrayList<String> matches)
    {
        Log.i("Output", "handleResults with: " + matches.toString());
        debugText.setText("handleResults");

        if(pingingRecogFor.getName().matches(new PingingFor_Clarification().getName())) //this is messy, FIND A BETTER WAY OF GETTING THE NAME FOR PingingFor_Clarification
        {
            if(sortThroughForFirstMatch(matches, pingingRecogFor).matches("-NoMatchFound-"))
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
            sortResultsMore(matches, pingingRecogFor);
        }
    }

    private String sortThroughForFirstMatch(ArrayList<String> results, SpeechIntent pingingFor)
    {
        Log.i("Output", "sortThroughForFirstMatch");
        debugText.setText("sortThroughForFirstMatch");

        for (String aResult: results)
        {
            for (String keyword: pingingFor.getResponseKeywords())
            {
                if(aResult.toLowerCase().contains(keyword.toLowerCase()))
                {
                    return keyword;
                }
                else
                {
                    for (String synonym: pingingFor.getResponseSynonyms(keyword))
                    {
                        if(aResult.toLowerCase().contains(synonym.toLowerCase()))
                        {
                            return keyword;
                        }
                    }
                }
            }
        }
        return "-NoMatchFound-";
    }

    private ArrayList<String> sortResultsThoroughly(ArrayList<String> results, SpeechIntent pingingFor)
    {
        Log.i("Output", "sortResultsThoroughly");
        debugText.setText("sortResultsThoroughly");

        ArrayList<String> foundMatches = new ArrayList<String>();
        for (String aResult: results)
        {
            for (String keyword: pingingFor.getResponseKeywords())
            {
                for (String synonym: pingingFor.getResponseSynonyms(keyword))
                {
                    if(aResult.toLowerCase().contains(synonym.toLowerCase()))
                    {
                        foundMatches.add(keyword);
                        break;
                    }
                }
            }

            //TODO: Decide on clarification culling method.
            //Method 1: Check for matches from most accurate to least accurate, but stop searching as soon as any result produces a match.
            //Less through but less likely to ask the user for unnessary clarification
            //if the most accurate result(1st in the array) contains no key words, swap to the next most accurate result.
            /*if(foundMatches.size() > 0)
            {
                break;
            }*/
            //End of method 1
        }
        //TODO: Decide on clarification culling method.
        //Method 2: Check all results for keywords regardless of accuracy, then present all found keys words to clarification methods.
        //More through but more likely to ask the user for clarification
        ArrayList<String> foundUniqueMatches = new ArrayList<>();
        for (String aMatch: foundMatches)
        {
            boolean isDupe = false;
            for (String aUniqueMatch: foundUniqueMatches)
            {
                if(aMatch.matches(aUniqueMatch))
                {
                    isDupe = true;
                    break;
                }
            }
            if(!isDupe)
            {
                foundUniqueMatches.add(aMatch);
            }
        }
        foundMatches = foundUniqueMatches;
        //End of Method 2


        return foundMatches;
    }

    private void sortResultsMore(ArrayList<String> results, SpeechIntent pingingFor)
    {
        Log.i("Output", "sortResultsMore");
        debugText.setText("sortResultsMore");

        ArrayList<String> possibleKeywords = sortResultsThoroughly(results, pingingFor);

        if(possibleKeywords.size() > 1)
        {
            Log.i("Output", "about to start pinging for Clarification with: " + possibleKeywords.toString());
            Log.i("Output", "about to start pinging for Clarification with: " + possibleKeywords.toString());
            debugText.setText("sortResultsMore for clarification: Did you mean? " + possibleKeywords.toString() );
            previousPingingRecogFor = pingingFor;
            startRecogListening(new PingingFor_Clarification(possibleKeywords));
        }
        else if(possibleKeywords.size() == 1)
        {
            prepareResponseFor(possibleKeywords.get(0), pingingFor);
        }
        else
        {
            Log.i("Output", "Error prepareingResponses from sortResultsMore, as possibleKeywords is empty: ");
            debugText.setText("Error prepareingResponses from sortResultsMore, as possibleKeywords is empty: ");
        }
        //TODO: Clarify which possible keyword is ment.
    }

    private void prepareResponseFor(String result, SpeechIntent pingingFor)
    {
        Log.i("Output", "prepareResponseFor" + pingingFor.getName() + " with result: " + result);
        debugText.setText("prepareResponseFor" + pingingFor.getName() + " with result: " + result);

        if(pingingFor.getName().matches(pingingForTest.getName()))
        {
            pingingForTestResponse(result);
        }
        else if (pingingFor.getName().matches(pingingForOtherTest.getName()))
        {
            pingingForOtherTestResponse(result);
        }
        else
        {
            Log.e("Response:", "No response setup for this intent: " + pingingFor.getName());
            debugText.setText("No response setup for this intent: " + pingingFor.getName());
        }
    }

//+++++ [pingingFor Response/Output Methods] +++++

    private void pingingForTestResponse(String result)
    {
        switch (result)
        {
            case "Yes": outputText.setText("Yes a potato"); break;
            case "No": outputText.setText("That would be an ecumenical matter"); break;
            default: break;
        }
    }

    private void pingingForOtherTestResponse(String result)
    {
        switch (result)
        {
            case "Down with this sort of thing": outputText.setText("Down with this sort of thing");
            case "Careful now": outputText.setText("Careful now!");
            default: break;
        }
    }

//+++++ [/pingingFor Response/Output Methods] +++++





/*
    private void sortThroughRecognizerResultsForAllPossiblities(ArrayList<String> results, String[] matchablePhrases, boolean askForClarification)
    {
        if(pingingRecogFor != pingingRecogFor_Clarification)
        {
            previousPingingRecogFor = pingingRecogFor;
        }

        ArrayList<String> possibleResults = new ArrayList<String>();
        for (String aResult: results)
        {
            Log.i("Recog", "All Possiblities, Sorting results for result: " + aResult);
            for (String aPhrase: matchablePhrases)
            {
                Boolean isDuplicate = false;
                Log.i("Recog", "All Possiblities, Sorting results for result: " + aResult.toLowerCase().replace("-", " ") + " and Phrase: " + aPhrase.toLowerCase());
                //if a previous possiblity contained a key phrase, do not search other possiblities for that key phrase.
                for (String b: possibleResults)
                {
                    if(b.matches(aPhrase)){isDuplicate = true; break;}
                }

                if((aResult.toLowerCase().replace("-"," ")).contains(aPhrase.toLowerCase()) && !isDuplicate)
                {
                    Log.i("Recog", "All Possiblities, Match Found");
                    possibleResults.add(aPhrase);
                }
            }
        }

        currentPossiblePhrasesNeedingClarification = possibleResults.toArray(new String[possibleResults.size()]);
        //if there is more than 1 keyword in the passed phrase, the method will list those keywords back to the user and ask them to repeat  the correct 1.
        //This in turn will call recogResult from the utterance listener and trigger the pinging for Clarification case where the repeated word will then be used
        //to resolve the logic of the previous call to recogResult.
        if(askForClarification)
        {
            if (possibleResults.size() > 1)
            {
                String clarificationString = "I'm sorry but did you mean.";

                for (String a : possibleResults)
                {
                    clarificationString += (". " + a);
                    if (!possibleResults.get(possibleResults.size() - 1).matches(a))
                    {
                        clarificationString += ". or";
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    pingingRecogFor = pingingRecogFor_Clarification;
                    //toSpeech.speak(clarificationString, TextToSpeech.QUEUE_FLUSH, null, textToSpeechID_Clarification);
                }
            }
            //if there is only 1 keyword in the passed phrase, the method skips speech confirmation and immediately calls it's own listener in recogResults,
            // which(given that there is only 1 possible match, will skip to resolving the previous call to recogResult's logic)
            else if (possibleResults.size() == 1)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    pingingRecogFor = previousPingingRecogFor;
                    recogResultLogicPostClarification(possibleResults);
                    //toSpeech.speak("h", TextToSpeech.QUEUE_FLUSH, null, textToSpeechID_Clarification);
                }
            }
            else if (clarificationAskToRepeatOnNoMatch)//Boolean is set in RecogLogic before calling this method. ask the user to repeat thier answer if there is no match found based on what they answered to the clarification question
            {
                Log.i("Recog", "No matches found, Requesting Repetition .");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    currentPossiblePhrasesNeedingClarification = matchablePhrases;
                    //toSpeech.speak("Can you please repeat that?", TextToSpeech.QUEUE_FLUSH, null, textToSpeechID_Clarification);
                }
            }
            else //alternatively, the function simply returns no match(this is to be used by dialog that will record the answer if it does not match any preset keywords, e.g. the trouble ticket dialog)
            {
                lastResponseToClarification = results.get(0);
                Log.i("Recog", "No matches found, Returning empty/previous statement: " + lastResponseToClarification);
                pingingRecogFor = previousPingingRecogFor;
                possibleResults = new ArrayList<String>();
                possibleResults.add(lastResponseToClarification);
                recogResultLogicPostClarification(possibleResults);
            }
        }
        else
        {
            if(possibleResults.size() > 0)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    pingingRecogFor = previousPingingRecogFor;
                    recogResultLogicPostClarification((ArrayList<String>) possibleResults.subList(0, 0));
                    //toSpeech.speak("h", TextToSpeech.QUEUE_FLUSH, null, textToSpeechID_Clarification);
                }
            }
            else
            {
                lastResponseToClarification = results.get(0);
                Log.i("Recog", "No matches found, Returning empty/previous statement: " + lastResponseToClarification);
                pingingRecogFor = previousPingingRecogFor;
                possibleResults = new ArrayList<String>();
                possibleResults.add(lastResponseToClarification);
                recogResultLogicPostClarification(possibleResults);
            }
        }
    }

    private String sortThroughRecognizerResults(ArrayList<String> results, String matchablePhrase)
    {
        for (String aResult: results)
        {
            Log.i("Recog", "Sorting results for result: " + aResult.replace("-", " ") + " and Phrase: " + matchablePhrase.toLowerCase());
            if((aResult.replace("-", " ")).contains(matchablePhrase.toLowerCase()))
            {
                Log.i("Recog", "Match Found");
                return matchablePhrase;
            }
        }
        Log.i("Recog", "No matches found, returning empty string \"\" .");
        return "";
    }


    //CALLED FROM: RecogListener onResults()
    private void recogResultLogic(ArrayList<String> matches)
    {
        String[] phrases;
        Log.i("Recog", "Results recieved: " + matches);
        String response = "-Null-";
        String matchedKeyword = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Log.i("Recog", "Pinging For: " + pingingRecogFor);
            switch (pingingRecogFor)
            {
                case pingingRecogFor_Clarification:

                    Log.i("Recog", "onResult for Clarification");
                    phrases = currentPossiblePhrasesNeedingClarification;
                    response = sortThroughRecognizerResults(matches, phrases);
                    Log.i("Recog", "onClarification: Response= " + response);
                    if(response.matches("") && clarificationAskToRepeatOnNoMatch)
                    {
                        Log.i("Recog", "Unrecongised response: " + response);
                        pingingRecogFor = pingingRecogFor_Clarification;
                        ArrayList<String> copyOfCurrentPossiblePhrases = new ArrayList<String>(Arrays.asList(currentPossiblePhrasesNeedingClarification));
                        //pinging for Clarification does not set clarificationAskToRepeatOnNoMatch, as it will carry on using whichever the previous value was.
                        sortThroughRecognizerResultsForAllPossiblities(copyOfCurrentPossiblePhrases, phrases, true);
                    }
                    else if(response.matches("") && !clarificationAskToRepeatOnNoMatch)
                    {
                        Log.i("Recog", "Unrecognised Response (do not repeat): " + response);
                        ArrayList<String> clarifiedResponse = new ArrayList<String>();
                        clarifiedResponse.add(lastResponseToClarification);
                        pingingRecogFor = previousPingingRecogFor;
                        recogResultLogic(clarifiedResponse);
                    }
                    else
                    {
                        Log.i("Recog", "Clarification Returned: " + response);
                        ArrayList<String> clarifiedResponse = new ArrayList<String>();
                        clarifiedResponse.add(response);
                        pingingRecogFor = previousPingingRecogFor;
                        recogResultLogic(clarifiedResponse);
                    }
                    break;


                case pingingRecogFor_TroubleTicketConfirmation:
                    clarificationAskToRepeatOnNoMatch = false;
                    sortThroughRecognizerResultsForAllPossiblities(matches, new String[]{"yes", "ok", "no"}, true);
                    break;
            }
        }
    }

    //Identical to recogResultLogic BUT all calls to sortThroughRecognizerResultsForAllPossibilies replaced with sortThroughRecognizerResults
    //as we have now clarified which phrase the user meant.
    private void recogResultLogicPostClarification(ArrayList<String> matches)
    {
        String[] phrases;
        Log.i("Recog", "Results Post Logic recieved: " + matches);
        String response = "-Null-";
        String matchedKeyword = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Log.i("Recog", "Pinging For: " + pingingRecogFor);
            switch (pingingRecogFor)
            {
                case pingingRecogFor_Clarification:

                    Log.i("Recog", "onResultPostClarification for Clarification");
                    phrases = currentPossiblePhrasesNeedingClarification;
                    response = sortThroughRecognizerResults(matches, phrases);
                    Log.i("Recog", "onClarification: Response= " + response);
                    if(response.matches("") && clarificationAskToRepeatOnNoMatch)
                    {
                        Log.i("Recog", "Unrecongised response: " + response);
                        pingingRecogFor = pingingRecogFor_Clarification;
                        ArrayList<String> copyOfCurrentPossiblePhrases = new ArrayList<String>(Arrays.asList(currentPossiblePhrasesNeedingClarification));
                        //pinging for Clarification does not set clarificationAskToRepeatOnNoMatch, as it will carry on using whichever the previous value was.
                        sortThroughRecognizerResultsForAllPossiblities(copyOfCurrentPossiblePhrases, phrases, true);
                    }
                    else if(response.matches("") && !clarificationAskToRepeatOnNoMatch)
                    {
                        Log.i("Recog", "Unrecognised Response (do not repeat): " + response);
                        ArrayList<String> clarifiedResponse = new ArrayList<String>();
                        clarifiedResponse.add(lastResponseToClarification);
                        pingingRecogFor = previousPingingRecogFor;
                        recogResultLogic(clarifiedResponse);
                    }
                    else
                    {
                        Log.i("Recog", "Clarification Returned: " + response);
                        ArrayList<String> clarifiedResponse = new ArrayList<String>();
                        clarifiedResponse.add(response);
                        pingingRecogFor = previousPingingRecogFor;
                        recogResultLogic(clarifiedResponse);
                    }
                    break;


                case pingingRecogFor_TroubleTicketConfirmation:
                    matchedKeyword = sortThroughRecognizerResults(matches, new String[]{"yes", "ok", "no"});
                    if(!matchedKeyword.matches(""))
                    {
                        if(matchedKeyword.matches("yes") || matchedKeyword.matches("ok"))
                        {
                            //toSpeech.speak("Ok, registering trouble ticket with maintenance. Is there anything else I can help you with?", TextToSpeech.QUEUE_FLUSH, null, "AnythingElse");
                        }
                        else if (matchedKeyword.matches("no"))
                        {
                            //toSpeech.speak("Understood, I will not create a trouble ticket. Is there anything else Wida can help you with?", TextToSpeech.QUEUE_FLUSH, null, "AnythingElse");
                        }
                    }
                    else
                    {
                        //toSpeech.speak("I'll take that as a no. Is there anything else I can help you with?", TextToSpeech.QUEUE_FLUSH, null, "AnythingElse");
                    }
                    break;
            }
        }
    }*/


    private void startRecogListening(SpeechIntent intent)
    {
        Log.i("Output", "starting Recog for: " + intent.getName());
        debugText.setText("starting Recog for: " + intent.getName());

        pingingRecogFor = intent;
        recog = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        recogListener = this;
        recog.setRecognitionListener(recogListener);
        recogIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en");
        recogIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
        recogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recogIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        recog.startListening(recogIntent);
    }
//++++++++[/Recognition Other Code]

}
