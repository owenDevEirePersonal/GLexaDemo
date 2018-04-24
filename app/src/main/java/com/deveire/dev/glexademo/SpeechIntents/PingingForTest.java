package com.deveire.dev.glexademo.SpeechIntents;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.deveire.dev.glexademo.R;
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
        setSpeechPrompt("Would that be an non-ecumenical matter?");
        HashMap<String, ArrayList<String>> responses = new HashMap<>();
        responses.put("Yes", SpeechIntent.compileSynonyms(new String[]{"yes", "ok", "Jim", "Joe"}));
        responses.put("No", SpeechIntent.compileSynonyms(new String[]{"ecumenical matter", "No", "That", "Drink"}));
        setResponses(responses);
    }

    @Override
    public void getOutput(Context context, String keyword)
    {
        TextView outputText = (TextView) ((Activity)context).findViewById(R.id.outputText);
        switch (keyword)
        {
            case "Yes": outputText.setText("Yes a potato"); break;
            case "No": outputText.setText("That would be an ecumenical matter"); break;
            default: outputText.setText("What? :" + keyword + " is not a keyword with an implimentation"); break;
        }
    }
}
