package com.jock.wordup;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class WordUpContentProvider extends ContentProvider 
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

	private WordUpSQLiteOpenHelper mWordUpSQLiteOpenHelper;


	
	@Override
	public boolean onCreate()
	{
		mWordUpSQLiteOpenHelper = new WordUpSQLiteOpenHelper( getContext(), WordUpSQLiteOpenHelper.DATA_BASE_NAME, null,
				WordUpSQLiteOpenHelper.DATA_BASE_VERSION );

		
		Log.d( Main.APP_TAG, "onCreate WordUpContentProvider"  );

		return true;
	}


	@Override
	public int delete( Uri uri, String selection, String[] selectionArgs )
	{
		SQLiteDatabase db = mWordUpSQLiteOpenHelper.getWritableDatabase();

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

		Log.i( Main.APP_TAG, deleteCount + " deleted" );

		getContext().getContentResolver().notifyChange( uri, null );

		return deleteCount;
	}


	@Override
	public Uri insert( Uri uri, ContentValues cv )
	{
		SQLiteDatabase db = mWordUpSQLiteOpenHelper.getWritableDatabase();

		Log.i( Main.APP_TAG, "on row insert" );

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
		SQLiteDatabase db = mWordUpSQLiteOpenHelper.getWritableDatabase();

		Log.d( Main.APP_TAG, "on Create update"  );
		
		int updateCount = 0;
		updateCount = db.update( WordUpSQLiteOpenHelper.TABLE_WORDS, cv, selection, selectionArgs );

		Log.i( Main.APP_TAG, "on row update, " + String.valueOf( updateCount ) + " row changed" );

		getContext().getContentResolver().notifyChange( uri, null );

		return updateCount;
	}


	@Override
	public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder )
	{
		SQLiteDatabase db = mWordUpSQLiteOpenHelper.getWritableDatabase();

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

}