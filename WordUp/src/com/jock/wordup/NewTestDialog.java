package com.jock.wordup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class NewTestDialog extends android.app.DialogFragment implements OnItemSelectedListener
{
	static NewTestDialog newInstance()
	{
		NewTestDialog instance = new NewTestDialog();

		// Bundle args = new Bundle();
		// args.putInt("num", num);
		// f.setArguments(args);

		return instance;
	}


	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		// mNum = getArguments().getInt( "num" );
	}


	@Override
	public View onCreateView( LayoutInflater li, ViewGroup container, Bundle state )
	{
		View view = li.inflate( R.layout.layout_new_test_dialog, container, false );

		getDialog().setTitle( "New test Options" );

		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource( getActivity().getBaseContext(), R.array.filter_list,
				android.R.layout.simple_spinner_dropdown_item );

		Spinner includedWords = (Spinner) view.findViewById( R.id.spinn_includedWords );
		includedWords.setOnItemSelectedListener( this );
		includedWords.setAdapter( mSpinnerAdapter );

		return view;

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
		// TODO Auto-generated method stub

	}


	@Override
	public Dialog onCreateDialog( Bundle savedInstanceState )
	{
		super.onCreateDialog( savedInstanceState );

		return new AlertDialog.Builder( getActivity() ).setTitle( "moose" ).setPositiveButton( "ok", new DialogInterface.OnClickListener()
		{
			public void onClick( DialogInterface dialog, int whichButton )
			{
				( (Main) getActivity() ).doPositiveClick();
			}
		} ).setNegativeButton( "no", new DialogInterface.OnClickListener()
		{
			public void onClick( DialogInterface dialog, int whichButton )
			{
				( (Main) getActivity() ).doNegativeClick();
			}
		} ).create();
	}

}
