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

    @Override
    public void getOutput(Context context, String keyword)
    {
        TextView outputText = (TextView) ((Activity)context).findViewById(R.id.outputText);
        switch (keyword)
        {
            case "Down with this sort of thing": outputText.setText("Down with this sort of thing"); break;
            case "Careful now": outputText.setText("Careful now"); break;
            default: outputText.setText("What? :" + keyword + " is not a keyword with an implimentation"); break;
        }
    }
}
