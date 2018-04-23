package com.deveire.dev.glexademo.SpeechIntents;

import com.deveire.dev.glexademo.SpeechIntent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by owenryan on 20/04/2018.
 */

public class PingingForTest extends SpeechIntent
{
    public PingingForTest()
    {
        super("PingingForTest");
        setFillInIntent(false);
        HashMap<String, ArrayList<String>> responses = new HashMap<>();
        responses.put("Yes", SpeechIntent.compileSynonyms(new String[]{"yes", "ok", "Jim", "Joe"}));
        responses.put("No", SpeechIntent.compileSynonyms(new String[]{"ecumenical matter", "No", "That", "Drink"}));
        setResponses(responses);
    }

    public String getOutput(String keyword)
    {
        switch (keyword)
        {
            case "Yes": return "Yes a potato";
            case "That would be an ecumenical matter": return "That would be an ecumenical matter";
            default: return "What?";
        }
    }
}
