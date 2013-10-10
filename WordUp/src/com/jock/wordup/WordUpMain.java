package com.jock.wordup;

import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class WordUpMain extends Activity implements LoaderCallbacks<Cursor>, OnItemClickListener
{
	public static final String APP_TAG = "WORD_UP";

	private static final int CURSOR_ID = 0;
	private static final int TTS_DATA_CHECK = 1;

	private TextView tv_wordOfTheDay;
	private TextView tv_wordOfTheDayDef;
	private ListView lv_words;
	private String[] columnList;
	private ContentResolver cr;
	private SimpleCursorAdapter mWordAdapter;
	private WordUpVoice voice;


	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
		setContentView( R.layout.layout_main );

		Log.d( WordUpMain.APP_TAG, "onCreate" );

		cr = getContentResolver();

		Intent i = new Intent( Engine.ACTION_CHECK_TTS_DATA );
		startActivityForResult( i, TTS_DATA_CHECK );

		voice = new WordUpVoice( this );

		columnList = new String[] { WordUpSQLiteOpenHelper.COLUMN_WORD_ID, WordUpSQLiteOpenHelper.COLUMN_WORD, WordUpSQLiteOpenHelper.COLUMN_WORD_DEF };

		String[] fromColumns = { WordUpSQLiteOpenHelper.COLUMN_WORD, WordUpSQLiteOpenHelper.COLUMN_WORD_DEF,
				WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT, WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT };
		int[] toViews = { R.id.tv_list_word, R.id.tv_list_def, R.id.tv_list_word_correct_cnt, R.id.tv_list_word_incorrect_cnt };

		tv_wordOfTheDay = (TextView) findViewById( R.id.tv_wordOfTheDay );
		tv_wordOfTheDayDef = (TextView) findViewById( R.id.tv_wordOfTheDayDef );
		lv_words = (ListView) findViewById( R.id.lv_wordsListView );

		lv_words.setOnItemClickListener( this );

		mWordAdapter = new SimpleCursorAdapter( this, R.layout.layout_list_item_card, null, fromColumns, toViews, 0 );
		lv_words.setAdapter( mWordAdapter );

	}


	protected void onActivityResult( int reqCode, int resCode, Intent data )
	{
		if( reqCode == TTS_DATA_CHECK )
		{
			if( reqCode == Engine.CHECK_VOICE_DATA_PASS )
			{
				voice.setUpVoice();
			}
			else
			{
				Intent instalVoice = new Intent( Engine.ACTION_INSTALL_TTS_DATA );
				startActivity( instalVoice );
			}
		}
	}


	@Override
	public void onDestroy()
	{
		voice.destroy();
		super.onDestroy();
	}


	@Override
	protected void onResume()
	{
		super.onResume();
		Log.d( WordUpMain.APP_TAG, "onResume" );

		setProgressBarIndeterminateVisibility( true );
		
		if( getLoaderManager().getLoader( CURSOR_ID ) == null )
		{
			getLoaderManager().initLoader( CURSOR_ID, null, this );
		}
		else
		{
			getLoaderManager().restartLoader( CURSOR_ID, null, this );
		}
	}


	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.word_up_main, menu );
		return true;
	}


	@Override
	public Loader<Cursor> onCreateLoader( int id, Bundle args )
	{
		CursorLoader c = new CursorLoader( this, WordUpContentProvider.CONTENT_URI, null, null, null, null );
		return c;
	}


	@Override
	public void onLoadFinished( Loader<Cursor> loader, Cursor cursor )
	{
		Log.i( APP_TAG, "onLoadFinished, word cnt : " + String.valueOf( cursor.getCount() ) );
		mWordAdapter.swapCursor( cursor );

		int wordCount = cursor.getCount();
		Random rand = new Random();
		int nextWordIndex = rand.nextInt( wordCount );

		cursor.moveToPosition( nextWordIndex );

		tv_wordOfTheDay.setText( cursor.getString( 1 ) );
		tv_wordOfTheDayDef.setText( cursor.getString( 2 ) );
		
		setProgressBarIndeterminateVisibility( false );
	}


	@Override
	public void onLoaderReset( Loader<Cursor> loader )
	{
	}


	private void startTest()
	{
		Intent i = new Intent( this, WordUpTest.class );
		startActivity( i );
	}


	@Override
	public boolean onOptionsItemSelected( MenuItem mi )
	{
		switch (mi.getItemId())
		{
			case R.id.mi_delete_all:
				// deleteAllWords();
				return true;

			case R.id.mi_start_test:
				startTest();
				return true;
			default:
				return false;
		}
	}


	private void deleteAllWords()
	{
		cr.delete( WordUpContentProvider.CONTENT_URI, null, null );
		// mWordAdapter.swapCursor( cr.query( WordUpContentProvider.CONTENT_URI,
		// columnList, null, null, null ) );
	}


	@Override
	public void onItemClick( AdapterView<?> parent, View v, int position, long id )
	{
		Cursor selectedRow = (Cursor) parent.getAdapter().getItem( position );
		String selectedWord = selectedRow.getString( selectedRow.getColumnIndex( WordUpSQLiteOpenHelper.COLUMN_WORD ) );
		speakWord( selectedWord );
	}


	public void speakWord( String word )
	{
		voice.speak( word );
	}
}
