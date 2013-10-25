package com.jock.wordup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class NewTestDialog extends android.app.DialogFragment implements OnItemSelectedListener, OnClickListener
{
	private static EditText et_numWords;
	private static Spinner sp_includedWords;


	static NewTestDialog newInstance()
	{
		NewTestDialog instance = new NewTestDialog();
		return instance;
	}

	public interface StartTestListener
	{
		void onStartTest( int testSize );
	}


	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

	}


	@Override
	public View onCreateView( LayoutInflater li, ViewGroup container, Bundle state )
	{
		View view = li.inflate( R.layout.layout_new_test_dialog, container );

		getDialog().setTitle( "New test Options" );

		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource( getActivity().getBaseContext(), R.array.filter_list,
				android.R.layout.simple_spinner_dropdown_item );

		sp_includedWords = (Spinner) view.findViewById( R.id.spinn_includedWords );
		sp_includedWords.setOnItemSelectedListener( this );
		sp_includedWords.setAdapter( mSpinnerAdapter );

		et_numWords = (EditText) view.findViewById( R.id.et_numWords );

		Button btn_startText = (Button) view.findViewById( R.id.btn_startTest );
		btn_startText.setOnClickListener( this );
		
		btn_startText.requestFocus();

		return view;

	}


	@Override
	public void onClick( View v )
	{
		if( v.getId() == R.id.btn_startTest)
		{
			StartTestListener parentActivity = (StartTestListener) getActivity();
			parentActivity.onStartTest( Integer.valueOf( et_numWords.getText().toString() ) );
		}
	}


	@Override
	public void onItemSelected( AdapterView<?> arg0, View arg1, int itemId, long arg3 )
	{
		String mSelection = "";
		if( itemId == 1 )
		{
			mSelection = WordUpSQLiteOpenHelper.COLUMN_WORD_TOTAL_SPLET_CNT + " = 0 ";
		}
		else if( itemId == 2 )
		{
			mSelection = WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT + " > " + WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT;
		}
		else if( itemId == 3 )
		{
			mSelection = WordUpSQLiteOpenHelper.COLUMN_WORD_INCORRECTLY_SPLET_CNT + " > " + WordUpSQLiteOpenHelper.COLUMN_WORD_CORRECTLY_SPLET_CNT;
		}

		Log.d( Main.APP_TAG, "" + mSelection );
	}


	@Override
	public void onNothingSelected( AdapterView<?> arg0 )
	{
	}
}
