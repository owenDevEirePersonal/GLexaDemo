package com.deveire.dev.glexademo.TroubleTicketSystem.SpeechIntents;

import android.content.Context;

import com.deveire.dev.glexademo.SpeechIntent;
import com.deveire.dev.glexademo.TroubleTicketSystem.TroubleKeyword;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by owenryan on 20/04/2018.
 */

public class PingingFor_MatchesKeyword extends SpeechIntent
{
    public PingingFor_MatchesKeyword(TroubleKeyword keywordToMatch)
    {
        super("PingingFor_YesNo_" + keywordToMatch);
        setFillInIntent(false);
        setSpeechPrompt(keywordToMatch.getPromptQuestion());
        HashMap<String, ArrayList<String>> responses = new HashMap<>();
        responses.put("Yes", SpeechIntent.compileSynonyms(new String[]{"ok", "yes", "okay", "correct", "yeah", "it does"}));
        responses.put("No", SpeechIntent.compileSynonyms(new String[]{"no", "false", "incorrect", "not okay", "it doesn't", "not"}));
        setResponses(responses);
    }

    @Override
    public void getOutput(Context context, String keyword)
    {
        /*TextView outputText = (TextView) ((Activity)context).findViewById(R.id.outputText);
        switch (keyword)
        {
            case "Yes": outputText.setText("Yes"); break;
            case "No": outputText.setText("No"); break;
            default: outputText.setText("What? :" + keyword + " is not a keyword with an implimentation"); break;
        }*/
    }
}
