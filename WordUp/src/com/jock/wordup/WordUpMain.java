package com.jock.wordup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class WordUpMain extends Activity implements LoaderCallbacks<Cursor>
{

	public static final String APP_TAG = "WORD_UP";
	private String[] columnList;
	private ContentResolver cr;


	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.layout_main );

		columnList = new String[] { WordUpSQLiteOpenHelper.COLUMN_WORD_ID, WordUpSQLiteOpenHelper.COLUMN_WORD, WordUpSQLiteOpenHelper.COLUMN_WORD_DEF };

		getLoaderManager().initLoader( 0, null, this );

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
		Log.i( APP_TAG, String.valueOf( cursor.getCount() ) );
		// cursorAdapter.swapCursor( cursor );

		if( cursor.getCount() == 0 )
		{
			final Resources resources = getResources();
			InputStream inputStream = resources.openRawResource( R.raw.words );
			BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );

			try
			{
				String line;
				while (( line = reader.readLine() ) != null)
				{
					String[] strings = TextUtils.split( line, "\t" );
					if( strings.length < 2 ) continue;

					Bundle bundle = new Bundle();
					bundle.putSerializable( "word", strings[0].trim() );
					bundle.putSerializable( "def", strings[1].trim() );
					cr.call( WordUpContentProvider.CONTENT_URI, "addWord", null, bundle );

					// ContentValues initialValues = new ContentValues();
					// initialValues.put( WordUpSQLiteOpenHelper.COLUMN_WORD, strings[0].trim() );
					// initialValues.put( WordUpSQLiteOpenHelper.COLUMN_WORD_DEF, strings[1].trim() );
					// cr.insert( WordUpContentProvider.CONTENT_URI, initialValues );
				}
			}
			catch (IOException e)
			{
				Log.d( APP_TAG, "Moose, error occured" );
			}
			finally
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					Log.d( APP_TAG, "Moose 2, error occured" );
				}
			}
		}
		else
		{
			Log.d( APP_TAG, "Words Exist already" );
		}
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
	}
}
