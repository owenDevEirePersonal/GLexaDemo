package com.deveire.dev.glexademo.SpeechIntents;

import android.util.Log;

import com.deveire.dev.glexademo.SpeechIntent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by owenryan on 20/04/2018.
 */

public class PingingFor_Clarification extends SpeechIntent
{
    public PingingFor_Clarification(ArrayList<String> keywordsToDecideOn)
    {
        super("PingingFor_Clarification");
        setFillInIntent(false);
        HashMap<String, ArrayList<String>> responses = new HashMap<>();
        for (String aKeyword: keywordsToDecideOn)
        {
            responses.put(aKeyword, compileSynonyms(new String[]{aKeyword}));
        }
        setResponses(responses);
    }

    public PingingFor_Clarification()
    {
        super("PingingFor_Clarification");
        Log.i("WARNING:", "PingingFor_Clarification() should only be used to get the name of PingingFor-Clarification");
    }

    public String getOutput(String response)
    {
        switch (response)
        {
            default: return "WHAT! ERROR! You shouldn't call this, this is Clarification, you should define output outside of this Intent!";
        }
    }

}
