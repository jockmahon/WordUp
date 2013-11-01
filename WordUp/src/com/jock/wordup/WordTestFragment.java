package com.jock.wordup;

import java.util.ArrayList;
import java.util.List;
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
import android.widget.Toast;

public class WordTestFragment extends Fragment implements OnClickListener
{
	private static Random rand;
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
	private int testSize = 0;

	private String testWord = "";


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

		Bundle bundle = getArguments();

		// the # of words from the dialog
		testSize = bundle.getInt( Main.TEST_SIZE );
		testWord = bundle.getString( Main.TEST_WORDS );

		rand = new Random();

		cr = ( (Main) getActivity() ).getActivityResolver();
		columnList = new String[] { WordUpSQLiteOpenHelper.COLUMN_WORD_ID, WordUpSQLiteOpenHelper.COLUMN_WORD,
				WordUpSQLiteOpenHelper.COLUMN_WORD_DEF, WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT,
				WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT, WordUpSQLiteOpenHelper.COLUMN_WORD_TOTAL_SPLET_CNT };

		setUpWordTest();
	}


	private void setUpWordTest()
	{

		Cursor rowCount = cr.query( WordUpContentProvider.CONTENT_URI, columnList, testWord, null, WordUpSQLiteOpenHelper.COLUMN_WORD + " desc" );

		String ids = "";
		int nextNum = 0;
		int wordCount = rowCount.getCount();

		if( wordCount < testSize )
		{
			wordCount = testSize;
		}

		List<Integer> al = new ArrayList<Integer>();
		al.contains( nextNum );

		// build up the ids to be used in the IN condition in the sql
		for(int i = 0; i < testSize - 1; i++)
		{
			do
			{
				nextNum = rand.nextInt( wordCount );

			} while (al.contains( nextNum ) == true);

			al.add( nextNum );
			
			ids += "'" + String.valueOf( nextNum ) + "',";
		}

		
		Integer[] d = new Integer[al.size()];
		d = (Integer[])al.toArray();
		
		
		Log.d( Main.APP_TAG, ids );

		results = cr.query( WordUpContentProvider.CONTENT_URI, columnList, WordUpSQLiteOpenHelper.COLUMN_WORD_ID + " IN (" + ids + ")", null,
				WordUpSQLiteOpenHelper.COLUMN_WORD + " desc" );

		results.moveToPosition( 0 );

	}


	private Boolean isLastword()
	{
		return results.isLast();
	}


	private void getRandomWord()
	{
		Log.d( Main.APP_TAG, String.valueOf( results.getCount() ) + " words" );
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

	int testTotalCorrect = 0;
	int testTotalIncorrect = 0;


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

				testTotalCorrect++;

				int correctCnt = results.getInt( 3 );
				correctCnt = correctCnt + 1;
				cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT, correctCnt );
				cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_TOTAL_SPLET_CNT, totalCnt );

				cr.update( WordUpContentProvider.CONTENT_URI, cv, " " + WordUpSQLiteOpenHelper.COLUMN_WORD_ID + " = " + results.getInt( 0 ), null );

			}
			else
			{
				attemptResult.setText( R.string.msg_word_incorrect );

				testTotalIncorrect++;

				int incorrectCnt = results.getInt( 4 );
				incorrectCnt = incorrectCnt + 1;
				cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT, incorrectCnt );
				cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_TOTAL_SPLET_CNT, totalCnt );

				Log.i( Main.APP_TAG, " " + WordUpSQLiteOpenHelper.COLUMN_WORD_ID + " = " + results.getInt( 0 ) );

				cr.update( WordUpContentProvider.CONTENT_URI, cv, " " + WordUpSQLiteOpenHelper.COLUMN_WORD_ID + " = " + results.getInt( 0 ), null );
			}

			wordAttempt.setText( "" );
			isFinished();
		}
	}


	private void isFinished()
	{
		if( isLastword() )
		{
			Toast.makeText( getActivity(), "Finished, " + testTotalCorrect + " right. " + testTotalIncorrect + " wrong", Toast.LENGTH_SHORT ).show();
		}
		else
		{
			results.moveToNext();
			getRandomWord();
		}

	}


	private void moveToNextWord()
	{
		resetActivity();
		isFinished();
	}


	private void resetActivity()
	{
		attemptResult.setText( "" );
		wordAttempt.setText( "" );
	}
}
