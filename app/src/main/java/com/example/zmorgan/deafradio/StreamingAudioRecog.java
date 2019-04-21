package com.example.zmorgan.deafradio;
// ref: https://code.tutsplus.com/tutorials/create-an-intelligent-app-with-google-cloud-speech-and-natural-language-apis--cms-28890


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.speech.v1p1beta1.Speech;
import com.google.api.services.speech.v1p1beta1.SpeechRequest;
import com.google.api.services.speech.v1p1beta1.SpeechRequestInitializer;
import com.google.api.services.speech.v1p1beta1.model.RecognitionAudio;
import com.google.api.services.speech.v1p1beta1.model.RecognitionConfig;
import com.google.api.services.speech.v1p1beta1.model.RecognizeRequest;
import com.google.api.services.speech.v1p1beta1.model.RecognizeResponse;
import com.google.api.services.speech.v1p1beta1.model.SpeakerDiarizationConfig;
import com.google.api.services.speech.v1p1beta1.model.SpeechRecognitionAlternative;
import com.google.api.services.speech.v1p1beta1.model.SpeechRecognitionResult;
import com.google.api.services.speech.v1p1beta1.model.WordInfo;

import java.util.Iterator;
import java.util.Objects;

public class StreamingAudioRecog {


    public static String streamingMicRecognize(byte[] data, boolean detect_speakers,Context context) throws Exception {
        //Creates a request for our audio data and colours the text accordingly if we choose to differentiate speakers

        //Setup code
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Speech speechService = ExoPlayerController.getInstance().getSpeech();
        RecognitionConfig recognitionConfig = ExoPlayerController.getInstance().getSpeechConfig();
        RecognitionAudio recognitionAudio = new RecognitionAudio();
        recognitionAudio.encodeContent(data);
        // Create request
        RecognizeRequest request = new RecognizeRequest().setConfig(recognitionConfig).setAudio(recognitionAudio);
        // Generate response
        RecognizeResponse response = speechService.speech().recognize(request).execute();
        // Extract transcript
        SpeechRecognitionResult result = response.getResults().get(0);
        SpeechRecognitionAlternative alternative = result.getAlternatives().get(0);
        Iterator<WordInfo> i = alternative.getWords().iterator();
        String htmlTranscript = "";
        Integer lastSpeaker = 0;
        //Go through transcript and colour depending on the speaker
        //Insert new lines if we switch colours and at the end of the transcript
        while(i.hasNext()) {
            WordInfo wordInfo = i.next();
            if(wordInfo.getSpeakerTag() == null || !detect_speakers){
                htmlTranscript += wordInfo.getWord();
            }
            else if (wordInfo.getSpeakerTag() == 1) {
                if(lastSpeaker == 2){
                    htmlTranscript += "<br>";
                }
                htmlTranscript += "<font color=\'" +
                        preferences.getString("FirstColour","red").toLowerCase() +
                        "\'>";
                htmlTranscript += wordInfo.getWord();
                htmlTranscript += "</font>";
                lastSpeaker = 1;
            }
            else{
                if(lastSpeaker == 1){
                    htmlTranscript += "<br>";
                }
                htmlTranscript += "<font color=\'" +
                        preferences.getString("SecondColour","blue").toLowerCase() +
                        "\' >";
                htmlTranscript += wordInfo.getWord();
                htmlTranscript += "</font>";
                lastSpeaker = 2;
            }
            htmlTranscript += " ";

        }
        htmlTranscript += "<br>";
        return htmlTranscript;
    }
}

