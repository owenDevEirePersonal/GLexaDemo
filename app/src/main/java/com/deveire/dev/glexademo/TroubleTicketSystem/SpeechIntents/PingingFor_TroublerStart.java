package com.deveire.dev.glexademo.TroubleTicketSystem.SpeechIntents;

import com.deveire.dev.glexademo.SpeechIntent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by owenryan on 20/04/2018.
 */

public class PingingFor_TroublerStart extends SpeechIntent
{
    public PingingFor_TroublerStart()
    {
        super("PingingFor_TroublerStart");
        setFillInIntent(false);
        setSpeechPrompt("Instructacon Troubler System Online, how can I help?");
        HashMap<String, ArrayList<String>> responses = new HashMap<>();
        responses.put("Raise Trouble Ticket", SpeechIntent.compileSynonyms(new String[]{"Raise Trouble Ticket", "there is a problem", "there's a problem", "raise a ticket", "trouble ticket", "raise a trouble ticket"}));
        responses.put("Nevermind", SpeechIntent.compileSynonyms(new String[]{"no", "false", "incorrect", "not okay", "nevermind", "forget it"}));
        setResponses(responses);
    }

}
