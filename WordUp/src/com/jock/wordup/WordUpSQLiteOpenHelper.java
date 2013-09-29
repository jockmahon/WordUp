package com.jock.wordup;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
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

	private static final String SQL_CREATE_DATA_BASE = "create table " + TABLE_WORDS + " ( " + COLUMN_WORD_ID + " integer primary key autoincrement,"
			+ COLUMN_WORD + " text not null, " + COLUMN_WORD_DEF + " text, " + COLUMN_WORD_CORRECTLY_SPLET_CNT + " integer, "
			+ COLUMN_WORD_INCORRECTLY_SPLET_CNT + " integer); ";

	private static final String SQL_DESTROY_TABLE = "DROP TABLE '" + TABLE_WORDS + "'";
	public static final String SQL_ROW_COUNT = "SELECT count(*) from " + TABLE_WORDS + ";";


	public WordUpSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version)
	{
		super( context, name, factory, version );
	}


	@Override
	public void onCreate( SQLiteDatabase db )
	{
		db.execSQL( SQL_CREATE_DATA_BASE );
		// loadWords();
	}


	@Override
	public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
	{
		Log.d( WordUpMain.APP_TAG, "Updating the database." );
		Log.d( WordUpMain.APP_TAG, "Old verson :" + oldVersion );
		Log.d( WordUpMain.APP_TAG, "New verson :" + newVersion );

		db.execSQL( SQL_DESTROY_TABLE );

		onCreate( db );
	}
}
