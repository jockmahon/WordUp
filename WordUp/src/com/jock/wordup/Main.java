package com.jock.wordup;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech.Engine;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.jock.wordup.NewTestDialog.StartTestListener;

public class Main extends Activity implements LoaderCallbacks<Cursor>, StartTestListener
{
	public static final String APP_TAG = "WORD_UP";
	public static final String TEST_SIZE = "TEST_SIZE";
	public static final String TEST_WORDS = "TEST_WORDS";
	private static final int TTS_DATA_CHECK = 1;
	private static final int CURSOR_ID = 0;

	private WordUpVoice voice;
	private ContentResolver cr;

	private String[] mProjection;
	private String[] mSelectionArgs;
	private String mSelection;
	private String mSortOrder;
	private ActionBar actionBar;
	private int totalWords = 0;

	private FragmentManager mFragmentManager;

	NewTestDialog newFragment;


	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
		setContentView( R.layout.layout_main );

		Log.d( APP_TAG, "onCreate" );

		cr = getContentResolver();

		Intent i = new Intent( Engine.ACTION_CHECK_TTS_DATA );
		startActivityForResult( i, TTS_DATA_CHECK );

		voice = new WordUpVoice( this );

		mFragmentManager = getFragmentManager();
		FragmentTransaction fragTran = mFragmentManager.beginTransaction();

		actionBar = getActionBar();
		actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_LIST );

		OnNavigationListener mOnNavigationListener = new OnNavigationListener()
		{

			@Override
			public boolean onNavigationItemSelected( int position, long itemId )
			{
				Log.d( APP_TAG, "moose click" );
				resetQueryArgs();

				if( itemId == 1 )
				{
					mSelection = WordUpSQLiteOpenHelper.COLUMN_WORD_TOTAL_SPLET_CNT + " = 0 ";
				}
				else if( itemId == 2 )
				{
					mSelection = WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT + " > "
							+ WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT;
				}
				else if( itemId == 3 )
				{
					mSelection = WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT + " > "
							+ WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT;
				}
				startLoader();
				return true;
			}
		};

		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource( this, R.array.filter_list, android.R.layout.simple_spinner_dropdown_item );
		actionBar.setListNavigationCallbacks( mSpinnerAdapter, mOnNavigationListener );

		actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_LIST );

		WordListFragment wordList = new WordListFragment();
		fragTran.add( R.id.holder, wordList );
		fragTran.commit();
	}


	private void resetQueryArgs()
	{
		String[] columns = { WordUpSQLiteOpenHelper.COLUMN_WORD_ID,
				"upper(" + WordUpSQLiteOpenHelper.COLUMN_WORD + ") as " + WordUpSQLiteOpenHelper.COLUMN_WORD, WordUpSQLiteOpenHelper.COLUMN_WORD_DEF,
				WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT, WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT,
				WordUpSQLiteOpenHelper.COLUMN_WORD_TOTAL_SPLET_CNT };

		mProjection = columns;
		mSelectionArgs = null;
		mSelection = null;
		mSortOrder = WordUpSQLiteOpenHelper.COLUMN_WORD + " asc";

	}


	@Override
	protected void onResume()
	{
		super.onResume();
		Log.d( APP_TAG, "onResume Main" );
		resetQueryArgs();

		startLoader();
	}


	public void startLoader()
	{

		if( getLoaderManager().getLoader( CURSOR_ID ) == null )
		{
			setProgressBarIndeterminateVisibility( true );
			getLoaderManager().initLoader( CURSOR_ID, null, this );
		}
		else
		{
			getLoaderManager().restartLoader( CURSOR_ID, null, this );
		}
	}


	@Override
	public Loader<Cursor> onCreateLoader( int id, Bundle args )
	{
		CursorLoader c = new CursorLoader( this, WordUpContentProvider.CONTENT_URI, mProjection, mSelection, mSelectionArgs, mSortOrder );
		return c;
	}


	public int getWordCount()
	{
		return totalWords;
	}


	@Override
	public void onLoadFinished( Loader<Cursor> loader, Cursor cursor )
	{
		Log.i( APP_TAG, "onLoadFinished, word cnt : " + String.valueOf( cursor.getCount() ) );

		totalWords = cursor.getCount();

		WordListFragment wordList = (WordListFragment) getFragmentManager().findFragmentById( R.id.holder );

		wordList.swapCursor( cursor );

		setProgressBarIndeterminateVisibility( false );
	}


	@Override
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
	public void onLoaderReset( Loader<Cursor> loader )
	{
	}


	@Override
	public void onDestroy()
	{
		voice.destroy();
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.word_up_main, menu );
		return true;
	}


	@Override
	public boolean onOptionsItemSelected( MenuItem mi )
	{
		switch (mi.getItemId())
		{
			case R.id.mi_clear_counts:
				clearCounts();
				return true;

			case R.id.mi_start_test:
				startTest();
				return true;
			default:
				return false;
		}
	}


	private void clearCounts()
	{
		ContentValues cv = new ContentValues();
		cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT, 0 );
		cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT, 0 );
		cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_TOTAL_SPLET_CNT, 0 );

		cr.update( WordUpContentProvider.CONTENT_URI, cv, null, null );

		startLoader();

	}


	public ContentResolver getActivityResolver()
	{
		return cr;
	}


	public void speakWord( String word )
	{
		voice.speak( word );
	}


	private void deleteAllWords()
	{
		// cr.delete( WordUpContentProvider.CONTENT_URI, null, null );
		// mWordAdapter.swapCursor( cr.query( WordUpContentProvider.CONTENT_URI,
		// columnList, null, null, null ) );
	}


	private void startTest()
	{
		newFragment = new NewTestDialog();
		newFragment.show( mFragmentManager, "layout_new_test_dialog" );
	}


	public void doPositiveClick()
	{
		Log.d( "FragmentAlertDialog", "Positive click!" );
	}


	public void doNegativeClick()
	{
		Log.d( "FragmentAlertDialog", "Negative click!" );
	}


	@Override
	public void onStartTest( int testSize, String selectFrom )
	{
		Log.d( APP_TAG, String.valueOf( testSize ) );

		newFragment.dismiss();

		Bundle args = new Bundle();
		args.putInt( TEST_SIZE, testSize );
		args.putString( TEST_WORDS, selectFrom );

		FragmentTransaction fragTran = mFragmentManager.beginTransaction();
		WordTestFragment wordList = new WordTestFragment();
		wordList.setArguments( args );

		fragTran.replace( R.id.holder, wordList );
		fragTran.addToBackStack( "main" );

		actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_STANDARD );

		fragTran.commit();
	}

}
