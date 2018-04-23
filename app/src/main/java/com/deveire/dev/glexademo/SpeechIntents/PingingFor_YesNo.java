package com.deveire.dev.glexademo.SpeechIntents;

import com.deveire.dev.glexademo.SpeechIntent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by owenryan on 20/04/2018.
 */

public class PingingFor_YesNo extends SpeechIntent
{
    public PingingFor_YesNo()
    {
        super("PingingFor_YesNo");
        setFillInIntent(false);
        HashMap<String, ArrayList<String>> responses = new HashMap<>();
        responses.put("Yes", SpeechIntent.compileSynonyms(new String[]{"ok", "yes", "okay", "correct"}));
        responses.put("No", SpeechIntent.compileSynonyms(new String[]{"no", "false", "incorrect", "not okay"}));
        setResponses(responses);
    }

    public String getOutput(String response)
    {
        switch (response)
        {
            case "Yes": return "Yes";
            case "No": return  "No";
            default: return "WHAT! ERROR! THIS SHOULD'T HAPPEN!";
        }
    }
}
