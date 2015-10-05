package myapps.screenbuddy;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashSet;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {myapps.screenbuddy.CharacterFragment.OnCharacterSelectedListener} interface
 * to handle interaction events.
 * Use the {@link CharacterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CharacterFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "SBCharacterFragment";
    private static final String ARG_NAME = "name";

    private String mCharaName;
    private SBChara mChara;
    private int spinnerPosition;
    HashSet<String> characterNames;

    private OnCharacterSelectedListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment CharacterFragment.
     */
    public static CharacterFragment newInstance(String n) {
        Log.v(TAG, "newInstance");
        CharacterFragment fragment = new CharacterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, n);
        fragment.setArguments(args);
        return fragment;
    }

    public CharacterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spinnerPosition = 0;
        characterNames = new HashSet<>();
        if (getArguments() != null) {
            mCharaName = getArguments().getString(ARG_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_character, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        populateSpinner();
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnCharacterSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setCharacter(SBChara c) {
        mChara = c;
    }

    public void onNewItemClicked(View v) {
        Log.v(TAG, "onNewCharaClicked");
        // show new_item_name.xml
        FrameLayout layout = (FrameLayout) getView();
        View new_item_layout = getActivity().getLayoutInflater().inflate(R.layout.new_item_name, null);
        if(new_item_layout != null)
            layout.addView(new_item_layout);
    }

    public void onNewItemCancel(View v) {
        Log.v(TAG, "Cancel new chara");
        // hide new_item_name.xml
        FrameLayout layout = (FrameLayout) getView();
        try {
            View new_item_layout = layout.findViewById(R.id.nin_root);
            EditText editText = (EditText)layout.findViewById(R.id.new_name_edit_text);
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            layout.removeView(new_item_layout);
        } catch (NullPointerException e) {
            Log.v(TAG, "NullPointerException! Could not remove new item view!");
        }
    }

    public void onNewItemOK(View v) {
        Log.v(TAG, "New chara OK");

        EditText editText = (EditText)getView().findViewById(R.id.new_name_edit_text);
        String name = editText.getText().toString();
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        editText.clearFocus();
        if(name == null || name.equals("")) {
            Toast.makeText(getActivity().getApplicationContext(), "Please enter a name.", Toast.LENGTH_SHORT).show();
            return;
        } else if(characterNames.contains(name)) {
            Toast.makeText(getActivity().getApplicationContext(), "Name already in use!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            mChara = new SBChara(getActivity().getApplicationContext(), name);
            SQLiteDatabase db = new SBDBOpenHelper(getActivity()).getWritableDatabase();
            mChara.saveNewCharacter(db);
            db.close();
            populateSpinner();
        }

        // hide new_item_name.xml
        FrameLayout layout = (FrameLayout) getView();
        try {
            View new_item_layout = layout.findViewById(R.id.nin_root);
            layout.removeView(new_item_layout);
        } catch (NullPointerException e) {
            Log.v(TAG, "NullPointerException! Could not remove new item view!");
        }
    }

    public void populateSpinner() {
        Log.v(TAG, "populateSpinner");
        /* Populate the spinner */
        View v = getView();
        SQLiteDatabase db = new SBDBOpenHelper(getActivity()).getWritableDatabase();
        String query = "SELECT chara.ID _id, chara.NAME, img.DRAWABLE FROM SBChara chara " +
                       "LEFT JOIN SBImg img ON chara.PREVIEW_IMG = img.NAME " +
                       "AND img.CHARA_NAME = chara.NAME";
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            /* Populate spinner */
            String[] fromColumns = {SBDBOpenHelper.COL_CHARA_NAME, SBDBOpenHelper.COL_IMG_DRAWABLE};
            int[] toViews = {R.id.csl_chara_name, R.id.csl_preview};
            SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(v.getContext(), R.layout.character_select_list, cursor, fromColumns, toViews, 0);
            spinnerAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View v, Cursor cursor, int columnIndex) {
                    if(v.getId() == R.id.csl_preview) {
                        byte[] imgAsByteArray = cursor.getBlob(columnIndex);
                        if(imgAsByteArray != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imgAsByteArray, 0, imgAsByteArray.length);
                            BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
                            ((ImageView) v).setImageDrawable(drawable);
                        }
                        return true;
                    }
                    return false;
                }
            });
            Spinner spinner = (Spinner) v.findViewById(R.id.character_spinner);
            spinner.setAdapter(spinnerAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    spinnerPosition = position;
                    Cursor c = (Cursor) parent.getItemAtPosition(position);
                    String name = c.getString(c.getColumnIndex(SBDBOpenHelper.COL_CHARA_NAME));
                    mCharaName = name;
                    mListener.onCharacterSelected(name);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    return;
                }
            });
            spinner.setSelection(spinnerPosition);

            // add each name to the array
            characterNames.clear();
            cursor.moveToFirst();
            do {
                characterNames.add(cursor.getString(cursor.getColumnIndex(SBDBOpenHelper.COL_CHARA_NAME)));
            } while (cursor.moveToNext());
        } else {
            Log.v(TAG, "No characters in database.");
        }

        db.close();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnCharacterSelectedListener {
        public void onCharacterSelected(String name);
        public void onPreviewImageClicked(View v);
        public void onSaveClicked(View v);
        public void onNewItemClicked(View v);
        public void onNewItemCancel(View v);
        public void onNewItemOK(View v);
    }

}
