package com.jock.wordup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class WordUpContentProvider extends ContentProvider implements Runnable
{
	public static final Uri CONTENT_URI = Uri.parse( "content://com.jock.wordupcontentprovider/words" );

	private static final int ALL_ROWS = 1;
	private static final int SINGLE_ROW = 2;

	private static final UriMatcher uriMatcher;

	static
	{
		uriMatcher = new UriMatcher( UriMatcher.NO_MATCH );
		uriMatcher.addURI( "com.jock.wordupcontentprovider", "words", ALL_ROWS );
		uriMatcher.addURI( "com.jock.wordupcontentprovider", "words/#", SINGLE_ROW );
	}

	private WordUpSQLiteOpenHelper WordUpSQLiteOpenHelper;


	@Override
	public boolean onCreate()
	{
		WordUpSQLiteOpenHelper = new WordUpSQLiteOpenHelper( getContext(), WordUpSQLiteOpenHelper.DATA_BASE_NAME, null,
				WordUpSQLiteOpenHelper.DATA_BASE_VERSION );

		// put back in to load words
		// loadWords();

		return true;
	}


	public void run()
	{
		try
		{
			loadWordsFromFile();

		}
		catch (IOException e)
		{
			throw new RuntimeException();
		}
	}


	public void loadWords()
	{
		new Thread( this ).start();
	}


	private void loadWordsFromFile() throws IOException
	{
		final Resources resources = getContext().getResources();
		InputStream inputStream = resources.openRawResource( R.raw.words );
		BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );

		try
		{
			String line;
			while (( line = reader.readLine() ) != null)
			{
				String[] strings = TextUtils.split( line, "\t" );
				if( strings.length < 2 ) continue;
				long id = addWord( strings[0].trim(), strings[1].trim() );
				if( id < 0 )
				{

				}
			}
		}
		finally
		{
			reader.close();
		}

	}


	public long addWord( String word, String definition )
	{
		Log.d( WordUpMain.APP_TAG, word );

		SQLiteDatabase db = WordUpSQLiteOpenHelper.getWritableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put( WordUpSQLiteOpenHelper.COLUMN_WORD, word );
		initialValues.put( WordUpSQLiteOpenHelper.COLUMN_WORD_DEF, definition );

		return db.insert( WordUpSQLiteOpenHelper.TABLE_WORDS, null, initialValues );
	}


	@Override
	public int delete( Uri uri, String selection, String[] selectionArgs )
	{
		SQLiteDatabase db = WordUpSQLiteOpenHelper.getWritableDatabase();

		switch (uriMatcher.match( uri ))
		{
			case SINGLE_ROW:
				String rowID = uri.getPathSegments().get( 1 );
				selection = WordUpSQLiteOpenHelper.COLUMN_WORD_ID + " = " + rowID
						+ ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" );
			default:
				break;
		}

		if( selection == null )
		{
			selection = "1";
		}

		int deleteCount = db.delete( WordUpSQLiteOpenHelper.TABLE_WORDS, selection, selectionArgs );

		getContext().getContentResolver().notifyChange( uri, null );

		return deleteCount;
	}


	@Override
	public Uri insert( Uri uri, ContentValues cv )
	{
		SQLiteDatabase db = WordUpSQLiteOpenHelper.getWritableDatabase();

		long id = db.insert( WordUpSQLiteOpenHelper.TABLE_WORDS, null, cv );

		if( id > -1 )
		{
			Uri insertedID = ContentUris.withAppendedId( CONTENT_URI, id );

			getContext().getContentResolver().notifyChange( insertedID, null );
			return insertedID;
		}
		else
		{
			return null;
		}
	}


	@Override
	public int update( Uri uri, ContentValues cv, String selection, String[] selectionArgs )
	{
		SQLiteDatabase db = WordUpSQLiteOpenHelper.getWritableDatabase();

		int updateCount = 0;
		updateCount = db.update( WordUpSQLiteOpenHelper.TABLE_WORDS, cv, selection, selectionArgs );

		Log.i( WordUpMain.APP_TAG, String.valueOf( updateCount ) );

		getContext().getContentResolver().notifyChange( uri, null );

		return updateCount;
	}


	@Override
	public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder )
	{
		SQLiteDatabase db = WordUpSQLiteOpenHelper.getWritableDatabase();

		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables( WordUpSQLiteOpenHelper.TABLE_WORDS );

		switch (uriMatcher.match( uri ))
		{
			case SINGLE_ROW:
				String rowID = uri.getPathSegments().get( 1 );
				builder.appendWhere( WordUpSQLiteOpenHelper.COLUMN_WORD_ID + " = " + rowID );
			default:
				break;
		}

		Cursor cursor = builder.query( db, projection, selection, selectionArgs, null, null, sortOrder );
		return cursor;
	}


	@Override
	public String getType( Uri uri )
	{
		switch (uriMatcher.match( uri ))
		{
			case ALL_ROWS:
				return "vnd.android.cursor.dir/vnd.example.words";
			case SINGLE_ROW:
				return "vnd.android.cursor.item/vnd.example.words";
			default:
				throw new IllegalArgumentException( "unsupported URI : " + uri );

		}
	}


	@Override
	public Bundle call( String method, String arg, Bundle extras )
	{
		if( method.equals( "addWord" ) )
		{
			addWord( extras.getString( "word" ), extras.getString( "def" ) );
		}

		return new Bundle();
	}
}