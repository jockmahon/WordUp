package com.jock.wordup;

import java.util.Locale;
import java.util.Random;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class WordUpTest extends Activity implements OnClickListener, OnCheckedChangeListener
{
	private static final int TTS_DATA_CHECK = 1;

	private ImageButton repeatWord;
	private Button nextWord;
	private Button submitWord;

	private EditText wordAttempt;

	private TextView wordDef;
	private TextView attemptResult;

	// private CheckBox showWordDef;

	private ContentResolver cr;
	private String[] columnList;
	private Cursor results;

	private String currentWord;

	private TextToSpeech tts = null;
	private boolean ttsIsInit = false;

	private int wordCount;
	private Boolean[] tested;
	private int nextWordIndex = 0;


	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		setContentView( R.layout.layout_test );

		repeatWord = (ImageButton) findViewById( R.id.btn_repeatWord );
		nextWord = (Button) findViewById( R.id.btn_nextWord );
		submitWord = (Button) findViewById( R.id.btn_submitWord );
		wordAttempt = (EditText) findViewById( R.id.et_wordAttempt );
		attemptResult = (TextView) findViewById( R.id.tv_result );
		wordDef = (TextView) findViewById( R.id.tv_word_def );
		// showWordDef = (CheckBox) findViewById( R.id.cb_showWordDef );

		repeatWord.setOnClickListener( this );
		nextWord.setOnClickListener( this );
		submitWord.setOnClickListener( this );

		// showWordDef.setOnCheckedChangeListener( this );

		cr = getContentResolver();
		columnList = new String[] { WordUpSQLiteOpenHelper.COLUMN_WORD_ID, WordUpSQLiteOpenHelper.COLUMN_WORD,
				WordUpSQLiteOpenHelper.COLUMN_WORD_DEF, WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT,
				WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT, WordUpSQLiteOpenHelper.COLUMN_WORD_TOTAL_SPLET_CNT };

		// lowestCount = cr.query(WordUpContentProvider.CONTENT_URI, ["min("+
		// WordUpSQLiteOpenHelper., null, null,
		// WordUpSQLiteOpenHelper.COLUMN_WORD
		// + " desc");

		setUpWordTest();

		Intent i = new Intent( Engine.ACTION_CHECK_TTS_DATA );
		startActivityForResult( i, TTS_DATA_CHECK );

		getRandomWord();
	}


	private void setUpWordTest()
	{
		results = cr.query( WordUpContentProvider.CONTENT_URI, columnList, null, null, WordUpSQLiteOpenHelper.COLUMN_WORD + " desc" );

		wordCount = results.getCount();
		nextWordIndex = 0;

		tested = new Boolean[wordCount];

		for(int i = 0; i < tested.length; i++)
		{
			tested[i] = false;
		}
	}


	private Boolean areUntestedWords()
	{
		for(int i = 0; i < tested.length; i++)
		{
			if( tested[i] == false )
			{
				return true;
			}
		}

		return false;
	}


	@Override
	public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
	{
		Log.d( WordUpMain.APP_TAG, String.valueOf( isChecked ) );
		// if( buttonView.getId() == R.id.cb_showWordDef )
		// {
		// wordDef.setVisibility( ( isChecked ) ? View.VISIBLE : View.INVISIBLE
		// );
		// }
	}


	private void getRandomWord()
	{
		int wordCount = results.getCount();

		Log.d( WordUpMain.APP_TAG, String.valueOf( wordCount ) + " words" );

		Random rand = new Random();

		do
		{
			nextWordIndex = rand.nextInt( wordCount );

		} while (tested[nextWordIndex] == true);

		results.moveToPosition( nextWordIndex );

		Log.d( WordUpMain.APP_TAG, "------------------------------" );
		Log.d( WordUpMain.APP_TAG, String.valueOf( results.getString( 1 ) ) );
		Log.d( WordUpMain.APP_TAG, String.valueOf( results.getInt( 3 ) ) + " correct attempts" );
		Log.d( WordUpMain.APP_TAG, String.valueOf( results.getInt( 4 ) ) + " incorrect attempts" );
		Log.d( WordUpMain.APP_TAG, String.valueOf( results.getInt( 5 ) ) + " total attempts" );
		Log.d( WordUpMain.APP_TAG, "------------------------------" );

		displayWord();
	}


	private void displayWord()
	{
		currentWord = results.getString( 1 );
		wordDef.setText( results.getString( 2 ) );

		speak( currentWord );
	}


	@Override
	public void onClick( View v )
	{
		if( v.getId() == R.id.btn_repeatWord )
		{
			displayWord();
		}
		else if( v.getId() == R.id.btn_nextWord )
		{
			moveToNextWord();
		}
		else if( v.getId() == R.id.btn_submitWord )
		{
			String[] args = new String[] { results.getString( 0 ) };
			ContentValues cv = new ContentValues();
			int totalCnt = results.getInt( 5 );
			totalCnt = totalCnt + 1;

			if( wordAttempt.getText().toString().toLowerCase( Locale.getDefault() ).equals( currentWord.toLowerCase( Locale.getDefault() ) ) )
			{
				attemptResult.setText( R.string.msg_word_correct );

				// showWordDef.setChecked( false );

				int correctCnt = results.getInt( 3 );
				correctCnt = correctCnt + 1;
				cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT, correctCnt );
				cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_TOTAL_SPLET_CNT, totalCnt );

				cr.update( WordUpContentProvider.CONTENT_URI, cv, " " + WordUpSQLiteOpenHelper.COLUMN_WORD_ID + " = " + results.getInt( 0 ), null );

				tested[nextWordIndex] = true;

			}
			else
			{
				attemptResult.setText( R.string.msg_word_incorrect );

				int incorrectCnt = results.getInt( 4 );
				incorrectCnt = incorrectCnt + 1;
				cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT, incorrectCnt );
				cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_TOTAL_SPLET_CNT, totalCnt );

				Log.i( WordUpMain.APP_TAG, " " + WordUpSQLiteOpenHelper.COLUMN_WORD_ID + " = " + results.getInt( 0 ) );

				cr.update( WordUpContentProvider.CONTENT_URI, cv, " " + WordUpSQLiteOpenHelper.COLUMN_WORD_ID + " = " + results.getInt( 0 ), null );

				tested[nextWordIndex] = true;
			}

			wordAttempt.setText( "" );
			isFinished();
		}
	}


	private void isFinished()
	{
		if( !areUntestedWords() )
		{
			setUpWordTest();
		}
		getRandomWord();

	}


	private void moveToNextWord()
	{
		isFinished();
		resetActivity();
	}


	private void resetActivity()
	{
		attemptResult.setText( "" );
		wordAttempt.setText( "" );
	}


	@Override
	protected void onActivityResult( int reqCode, int resCode, Intent data )
	{
		if( reqCode == TTS_DATA_CHECK )
		{
			if( reqCode == Engine.CHECK_VOICE_DATA_PASS )
			{
				tts = new TextToSpeech( this, new OnInitListener()
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
			else
			{
				Intent instalVoice = new Intent( Engine.ACTION_INSTALL_TTS_DATA );
				startActivity( instalVoice );
			}
		}
	}


	private void speak( String textToSpeak )
	{
		if( tts != null && ttsIsInit )
		{
			tts.speak( textToSpeak, TextToSpeech.QUEUE_ADD, null );

		}
	}


	@Override
	public void onDestroy()
	{
		if( tts != null )
		{
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

}
