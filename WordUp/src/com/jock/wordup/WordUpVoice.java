package com.jock.wordup;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class  WordUpVoice
{
	private static int TTS_DATA_CHECK = 1;
	private TextToSpeech tts = null;
	private boolean ttsIsInit = false;
	private Context appContext;


	public WordUpVoice(Context c)
	{
		appContext = c;
	}


	public void setUpVoice( )
	{
		tts = new TextToSpeech( appContext, new OnInitListener()
		{

			@Override
			public void onInit( int status )
			{
				if( status == TextToSpeech.SUCCESS )
				{
					ttsIsInit = true;

					if( tts.isLanguageAvailable( Locale.UK ) >= 0 )
					{
						tts.setLanguage( Locale.UK );
					}

					tts.setPitch( 0.8f );
					tts.setSpeechRate( 1.1f );

				}

			}
		} );

	}


	public void speak( String textToSpeak )
	{
		if( tts != null && ttsIsInit )
		{
			tts.speak( textToSpeak, TextToSpeech.QUEUE_ADD, null );

		}
	}


	public void destroy()
	{
		if( tts != null )
		{
			tts.stop();
			tts.shutdown();
		}

	}
}
