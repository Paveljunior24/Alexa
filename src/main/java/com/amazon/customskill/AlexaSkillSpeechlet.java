/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.amazon.customskill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;


/*
 * This class is the actual skill. Here you receive the input and have to produce the speech output. 
 */
public class AlexaSkillSpeechlet implements SpeechletV2{
	
	static Logger logger = LoggerFactory.getLogger(AlexaSkillSpeechlet.class);



	public static String userRequest;

	private Translator translator;
	private static String fromLang = "en";
	private static String toLang = "de";
	private static String text = "";

	private static String confirmEnglish = "Ab jetzt werde ich alle W�rter von englisch   auf deutsch �bersetzen bis du fertig sagst.  viel spass beim lesen!";
	private static String confirmDeutsch = "Ab jetzt werde ich alle W�rter von deutsch   auf englisch �bersetzen bis du fertig sagst.  viel spass beim lesen!";

	private static String correctAnswer = "";
	private static enum RecognitionState {Words, EnglishDeutsch, YesNo};
	private RecognitionState recState;
	private static enum UserIntent {Englisch, Deutsch, Dog,  Cat, Window, Street, Table, Yes, No, Fertig};
	private UserIntent ourUserIntent;

	static String welcomeMsg = "Hallo, in welcher Sprache wollen Sie das Buch lesen? englisch oder deutsch?.";
	static String wrongMsg = "Das ist leider falsch.";
	static String correctMsg = "Das ist richtig.";
	static String continueQuiz = "Ich habe f�r dich ein Vokabel Quiz vorbereitet. M�chtest du das spielen?";
	
	static String goodbyeMsg = "Danke,bis zum n�chsten Mal. Bella Ciao!";
	
	static String errorEnglishDeutschMsg = "Das habe ich nicht verstanden. Sagen Sie bitte Englisch oder Deutsch.";
	static String errorYesNoMsg = "Das habe ich nicht verstanden. Sagen Sie bitte Englisch oder Deutsch.";

	
	private String buildString(String msg, String replacement1, String replacement2) {
		return msg.replace("{replacement}", replacement1).replace("{replacement2}", replacement2);
	}
	
	

	

	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope)
	{
		logger.info("Alexa session begins");
		
		recState = RecognitionState.EnglishDeutsch;
	}

	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope)
	{

		return askUserResponse(welcomeMsg);
	}


	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope)
	{
		IntentRequest request = requestEnvelope.getRequest();
		Intent intent = request.getIntent();
		userRequest = intent.getSlot("anything").getValue();
		logger.info("Received following text: [" + userRequest + "]");
		logger.info("recState is [" + recState + "]");
		SpeechletResponse resp = null;
		switch (recState) {
		case EnglishDeutsch: resp = evaluateEnglishDeutsch(userRequest); recState = RecognitionState.Words; break;
		case Words: try {
				resp = evaluateWords(userRequest);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} break;
		case YesNo: resp = evaluateYesNo(userRequest);
		
		default: resp = response("Erkannter Text: " + userRequest);
		}   
		return resp;
	}

	private SpeechletResponse evaluateEnglishDeutsch(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case Englisch: {

			res = askUserResponse(confirmEnglish); break;
		} case Deutsch: {
			res = askUserResponse(confirmEnglish); break;
		} default: {
			res = askUserResponse(errorEnglishDeutschMsg);
		}
		}
		return res;
	}


	private SpeechletResponse evaluateWords(String userRequest) throws Exception {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		
		
		switch (ourUserIntent) {
		
			
			case Dog: {
				text = "dog";
				res = askUserResponse(Translator.translate(fromLang, toLang, text));
				
			}; break; 
			case Cat: {
				text = "cat";
				res = askUserResponse(Translator.translate(fromLang, toLang, text));
			}; break;
			case Street: {
				text = "street";
				res = askUserResponse(Translator.translate(fromLang, toLang, text));
				
			}; break; 
			case Window: {
				text = "window";
				res = askUserResponse(Translator.translate(fromLang, toLang, text));
			}; break; 
			case Table: {
				text = "table";
				res = askUserResponse(Translator.translate(fromLang, toLang, text));
			}; break; 
			case Fertig: {
				
				res = askUserResponse(continueQuiz);
				recState= RecognitionState.YesNo;
				
			}; break; 
			
				
			
		}
		return res;
	}
	private SpeechletResponse evaluateYesNo(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
			case Yes: {
			
				res = askUserResponse(""); 
			}; break;
			case No: {
				res = askUserResponse(goodbyeMsg);
			}; break;	
			default: {
				res = askUserResponse(errorYesNoMsg);
			}
		}
			return res;
	}
	
	


	//TODO
	private void recognizeUserIntent(String userRequest) {
		switch (userRequest.toLowerCase()) {
		case "englisch": ourUserIntent = UserIntent.Englisch; break;
		case "deutsch": ourUserIntent = UserIntent.Deutsch; break;
		case "dog": ourUserIntent = UserIntent.Dog; break;
		case "cat": ourUserIntent = UserIntent.Cat; break;
		case "window": ourUserIntent = UserIntent.Window; break;
		case "street": ourUserIntent = UserIntent.Street; break;
		case "table": ourUserIntent = UserIntent.Table; break;
		case "fertig": ourUserIntent = UserIntent.Fertig; break;
		case "ja": ourUserIntent = UserIntent.Yes; break;
		case "nein": ourUserIntent = UserIntent.No; break;
		
		
		}
		logger.info("set ourUserIntent to " +ourUserIntent);
	}

	
	

	/**
	 * formats the text in weird ways
	 * @param text
	 * @param i
	 * @return
	 */
	private SpeechletResponse responseWithFlavour(String text, int i) {

		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		switch(i){ 
		case 0: 
			speech.setSsml("<speak><amazon:effect name=\"whispered\">" + text + "</amazon:effect></speak>");
			break; 
		case 1: 
			speech.setSsml("<speak><emphasis level=\"strong\">" + text + "</emphasis></speak>");
			break; 
		case 2: 
			String half1=text.split(" ")[0];
			String[] rest = Arrays.copyOfRange(text.split(" "), 1, text.split(" ").length);
			speech.setSsml("<speak>"+half1+"<break time=\"3s\"/>"+ StringUtils.join(rest," ") + "</speak>");
			break; 
		case 3: 
			String firstNoun="erstes Wort buchstabiert";
			String firstN=text.split(" ")[3];
			speech.setSsml("<speak>"+firstNoun+ "<say-as interpret-as=\"spell-out\">"+firstN+"</say-as>"+"</speak>");
			break; 
		case 4: 
			speech.setSsml("<speak><audio src='soundbank://soundlibrary/transportation/amzn_sfx_airplane_takeoff_whoosh_01'/></speak>");
			break;
		default: 
			speech.setSsml("<speak><amazon:effect name=\"whispered\">" + text + "</amazon:effect></speak>");
		} 

		return SpeechletResponse.newTellResponse(speech);
	}


	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope)
	{
		logger.info("Alexa session ends now");
	}



	/**
	 * Tell the user something - the Alexa session ends after a 'tell'
	 */
	private SpeechletResponse response(String text)
	{
		// Create the plain text output.
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(text);

		return SpeechletResponse.newTellResponse(speech);
	}

	/**
	 * A response to the original input - the session stays alive after an ask request was send.
	 *  have a look on https://developer.amazon.com/de/docs/custom-skills/speech-synthesis-markup-language-ssml-reference.html
	 * @param text
	 * @return
	 */
	private SpeechletResponse askUserResponse(String text)
	{
		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		speech.setSsml("<speak>" + text + "</speak>");

		// reprompt after 8 seconds
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> Bist du noch da?</speak>");

		Reprompt rep = new Reprompt();
		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);
	}


}