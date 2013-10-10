package com.jock.wordup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

public class WordUpSQLiteOpenHelper extends SQLiteOpenHelper
{
	public static final int DATA_BASE_VERSION = 1;
	public static final String DATA_BASE_NAME = "words.db";
	public static final String TABLE_WORDS = "words";

	public static final String COLUMN_WORD_ID = "_id";
	public static final String COLUMN_WORD = "word";
	public static final String COLUMN_WORD_DEF = "word_def";
	public static final String COLUMN_WORD_CORRECTLY_SPLET_CNT = "correct_word_cnt";
	public static final String COLUMN_WORD_INCORRECTLY_SPLET_CNT = "incorrect_word_cnt";
	public static final String COLUMN_WORD_TOTAL_SPLET_CNT = "total_word_cnt";

	private static final String SQL_CREATE_DATA_BASE = "create table " + TABLE_WORDS + " ( " + COLUMN_WORD_ID + " integer primary key autoincrement,"
			+ COLUMN_WORD + " text not null, " + COLUMN_WORD_DEF + " text, " + COLUMN_WORD_CORRECTLY_SPLET_CNT + " integer, "
			+ COLUMN_WORD_INCORRECTLY_SPLET_CNT + " integer," + COLUMN_WORD_TOTAL_SPLET_CNT + " integer ); ";

	private static final String SQL_DESTROY_TABLE = "DROP TABLE '" + TABLE_WORDS + "'";
	public static final String SQL_ROW_COUNT = "SELECT count(*) from " + TABLE_WORDS + ";";

	private final Resources resources;

	public WordUpSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version)
	{
		super( context, name, factory, version );
		Log.d( WordUpMain.APP_TAG, "Calling WordUpSQLiteOpenHelper constructor." );
		
		resources =  context.getResources();
	}


	@Override
	public void onCreate( SQLiteDatabase db )
	{
		Log.d( WordUpMain.APP_TAG, "Creating " + TABLE_WORDS );
		db.execSQL( SQL_CREATE_DATA_BASE );
		// loadWords();

		InputStream inputStream = resources.openRawResource( R.raw.words );
		BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );

		try
		{
			String line;
			while (( line = reader.readLine() ) != null)
			{
				String[] strings = TextUtils.split( line, "\t" );
				if( strings.length < 2 ) continue;

				Log.d( WordUpMain.APP_TAG, "Adding " + strings[0].trim() );
				
				ContentValues cv = new ContentValues();
				cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD,strings[0].trim() );
				cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_DEF, strings[1].trim() );
				cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT, 0 );
				cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT, 0 );
				cv.put( WordUpSQLiteOpenHelper.COLUMN_WORD_TOTAL_SPLET_CNT, 0 );

				db.insert( TABLE_WORDS, null, cv );
			}
		}
		catch (IOException e)
		{
			Log.d( WordUpMain.APP_TAG, "Moose, error occured" );
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch (IOException e)
			{
				Log.d( WordUpMain.APP_TAG, "Moose 2, error occured" );
			}
		}		
		
	}


	@Override
	public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
	{
		Log.d( WordUpMain.APP_TAG, "Updating " + TABLE_WORDS );
		Log.d( WordUpMain.APP_TAG, TABLE_WORDS+ " current verson :" + oldVersion );
		Log.d( WordUpMain.APP_TAG, TABLE_WORDS+ " new verson :" + newVersion );

		db.execSQL( SQL_DESTROY_TABLE );

		onCreate( db );
	}
}
