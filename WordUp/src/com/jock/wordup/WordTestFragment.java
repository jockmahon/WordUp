package com.jock.wordup;

import java.util.Locale;
import java.util.Random;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;


public class WordTestFragment extends Fragment implements OnClickListener
{
	private ImageButton repeatWord;
	private Button nextWord;
	private Button submitWord;

	private EditText wordAttempt;

	private TextView wordDef;
	private TextView attemptResult;

	private ContentResolver cr;
	private String[] columnList;
	private Cursor results;

	private String currentWord;

	private int wordCount;
	private Boolean[] tested;
	private int nextWordIndex = 0;


	@Override
	public View onCreateView( LayoutInflater li, ViewGroup container, Bundle state )
	{
		View view = li.inflate( R.layout.layout_test, container, false );

		repeatWord = (ImageButton) view.findViewById( R.id.btn_repeatWord );
		nextWord = (Button) view.findViewById( R.id.btn_nextWord );
		submitWord = (Button) view.findViewById( R.id.btn_submitWord );
		wordAttempt = (EditText) view.findViewById( R.id.et_wordAttempt );
		attemptResult = (TextView) view.findViewById( R.id.tv_result );
		wordDef = (TextView) view.findViewById( R.id.tv_word_def );

		repeatWord.setOnClickListener( this );
		nextWord.setOnClickListener( this );
		submitWord.setOnClickListener( this );

		getRandomWord();

		return view;

	}


	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		cr = ( (Main) getActivity() ).getActivityResolver();
		columnList = new String[] { WordUpSQLiteOpenHelper.COLUMN_WORD_ID, WordUpSQLiteOpenHelper.COLUMN_WORD,
				WordUpSQLiteOpenHelper.COLUMN_WORD_DEF, WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT,
				WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT, WordUpSQLiteOpenHelper.COLUMN_WORD_TOTAL_SPLET_CNT };

		setUpWordTest();
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


	private void getRandomWord()
	{
		int wordCount = results.getCount();

		Log.d( Main.APP_TAG, String.valueOf( wordCount ) + " words" );

		Random rand = new Random();

		do
		{
			nextWordIndex = rand.nextInt( wordCount );

		} while (tested[nextWordIndex] == true);

		results.moveToPosition( nextWordIndex );

		Log.d( Main.APP_TAG, "------------------------------" );
		Log.d( Main.APP_TAG, String.valueOf( results.getString( 1 ) ) );
		Log.d( Main.APP_TAG, String.valueOf( results.getInt( 3 ) ) + " correct attempts" );
		Log.d( Main.APP_TAG, String.valueOf( results.getInt( 4 ) ) + " incorrect attempts" );
		Log.d( Main.APP_TAG, String.valueOf( results.getInt( 5 ) ) + " total attempts" );
		Log.d( Main.APP_TAG, "------------------------------" );

		displayWord();
	}


	private void displayWord()
	{
		currentWord = results.getString( 1 );
		wordDef.setText( results.getString( 2 ) );

		( (Main) getActivity() ).speakWord( currentWord );
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

			ContentValues cv = new ContentValues();
			int totalCnt = results.getInt( 5 );
			totalCnt = totalCnt + 1;

			if( wordAttempt.getText().toString().toLowerCase( Locale.getDefault() ).equals( currentWord.toLowerCase( Locale.getDefault() ) ) )
			{
				attemptResult.setText( R.string.msg_word_correct );

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

				Log.i( Main.APP_TAG, " " + WordUpSQLiteOpenHelper.COLUMN_WORD_ID + " = " + results.getInt( 0 ) );

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
}
