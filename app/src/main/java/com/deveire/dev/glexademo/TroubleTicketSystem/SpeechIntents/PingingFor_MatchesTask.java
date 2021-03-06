package com.deveire.dev.glexademo.TroubleTicketSystem.SpeechIntents;

import com.deveire.dev.glexademo.SpeechIntent;
import com.deveire.dev.glexademo.TroubleTicketSystem.TroubleTask;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by owenryan on 20/04/2018.
 */

public class PingingFor_MatchesTask extends SpeechIntent
{
    public PingingFor_MatchesTask(TroubleTask taskToMatch)
    {
        super("PingingFor_YesNoTask_" + taskToMatch.getDescription());
        setFillInIntent(false);
        setSpeechPrompt(taskToMatch.getPromptQuestion());
        HashMap<String, ArrayList<String>> responses = new HashMap<>();
        responses.put("Yes", SpeechIntent.compileSynonyms(new String[]{"ok", "yes", "okay", "correct", "yeah", "it does"}));
        responses.put("No", SpeechIntent.compileSynonyms(new String[]{"no", "false", "incorrect", "not okay", "it doesn't", "not"}));
        setResponses(responses);
    }

}
