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

public class PingingForTestFill extends SpeechIntent
{
    public PingingForTestFill()
    {
        super("PingingForTestFill");
        setFillInIntent(true);
        setSpeechPrompt("Where should I deliver the tea?");
    }

    @Override
    public void getOutput(Context context, String keyword)
    {
        TextView outputText = (TextView) ((Activity)context).findViewById(R.id.outputText);
        switch (keyword)
        {
            default: outputText.setText("What? :" + keyword + " is not a keyword with an implimentation"); break;
        }
    }
}
