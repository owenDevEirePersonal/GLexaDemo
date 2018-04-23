package com.deveire.dev.glexademo.SpeechIntents;

import com.deveire.dev.glexademo.SpeechIntent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by owenryan on 20/04/2018.
 */

public class PingingForOtherTest extends SpeechIntent
{
    public PingingForOtherTest()
    {
        super("PingingForOtherTest");
        setFillInIntent(false);
        HashMap<String, ArrayList<String>> responses = new HashMap<>();
        responses.put("Down with this sort of thing", SpeechIntent.compileSynonyms(new String[]{"enough", "down", "no more"}));
        responses.put("Careful now", SpeechIntent.compileSynonyms(new String[]{"careful", "easy", "be careful"}));
        setResponses(responses);
    }

    public String getOutput(String response)
    {
        switch (response)
        {
            case "Down with this sort of thing": return "Down with this sort of thing";
            case "Careful now": return "Careful now!";
            default: return "What?";
        }
    }
}
