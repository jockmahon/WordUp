package com.jock.wordup;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class WordUpMain extends Activity implements LoaderCallbacks<Cursor>
{
	private ListView lv_words;
	public static final String APP_TAG = "WORD_UP";
	private String[] columnList;
	private ContentResolver cr;
	private SimpleCursorAdapter mWordAdapter;


	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.layout_main );

		columnList = new String[] { WordUpSQLiteOpenHelper.COLUMN_WORD_ID, WordUpSQLiteOpenHelper.COLUMN_WORD, WordUpSQLiteOpenHelper.COLUMN_WORD_DEF };

		getLoaderManager().initLoader( 0, null, this );

		String[] fromColumns = { WordUpSQLiteOpenHelper.COLUMN_WORD, WordUpSQLiteOpenHelper.COLUMN_WORD_DEF, WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT, WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT };
		int[] toViews = { R.id.tv_list_word, R.id.tv_list_def, R.id.tv_list_word_correct_cnt,R.id.tv_list_word_incorrect_cnt };

		lv_words = (ListView) findViewById( R.id.wordsListView );

		mWordAdapter = new SimpleCursorAdapter( this, R.layout.list_item_card_layout, null, fromColumns, toViews, 0 );
		lv_words.setAdapter( mWordAdapter );

		cr = getContentResolver();

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

	}


	@Override
	public void onLoaderReset( Loader<Cursor> loader )
	{
	}


	@Override
	public boolean onOptionsItemSelected( MenuItem mi )
	{
		switch (mi.getItemId())
		{
		case R.id.mi_delete_all:
			deleteAllWords();
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
}
