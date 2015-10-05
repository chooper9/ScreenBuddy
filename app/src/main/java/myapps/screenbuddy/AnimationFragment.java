package myapps.screenbuddy;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AnimationFragment.OnAnimationSelectedListener} interface
 * to handle interaction events.
 * Use the {@link AnimationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnimationFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "SBAnimationFragment";
    private static final String ARG_CHARA_NAME = "character_name";

    private SBChara mChara;
    private AnimationFlipper mAnim;
    private String mCharaName;
    private int animImgPosition;

    private OnAnimationSelectedListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment AnimationFragment.
     */
    public static AnimationFragment newInstance(String charaName) {
        Log.v(TAG, "newInstance");
        AnimationFragment fragment = new AnimationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHARA_NAME, charaName);
        fragment.setArguments(args);
        return fragment;
    }

    public AnimationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        animImgPosition = -1;
        if (getArguments() != null) {
            mCharaName = getArguments().getString(ARG_CHARA_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_animation, container, false);
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
            mListener = (OnAnimationSelectedListener) activity;
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

    public void onNewItemClicked(View v) {
        Log.v(TAG, "onNewAnimClicked");
        if(mChara == null)
            return;
        // show new_item_name.xml
        FrameLayout layout = (FrameLayout) getView();
        View new_item_layout = getActivity().getLayoutInflater().inflate(R.layout.new_item_name, null);
        if(new_item_layout != null)
            layout.addView(new_item_layout);
    }

    public void onNewItemCancel(View v) {
        Log.v(TAG, "Cancel new anim");
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
        Log.v(TAG, "New anim OK");

        EditText editText = (EditText)getView().findViewById(R.id.new_name_edit_text);
        String name = editText.getText().toString();
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        editText.clearFocus();
        if(name == null || name.equals("")) {
            Toast.makeText(getActivity().getApplicationContext(), "Please enter a name.", Toast.LENGTH_SHORT).show();
            return;
        } else if(mChara.isValidAnimName(name)) {
            mChara.newAnimation(name);
            populateSpinner();
            Spinner spinner = (Spinner) getView().findViewById(R.id.animation_spinner);
            ArrayAdapter<String> sAdapter = (ArrayAdapter<String>) spinner.getAdapter();
            spinner.setSelection(sAdapter.getPosition(name));
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Name already in use!", Toast.LENGTH_SHORT).show();
            return;
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

    public void setCharacter(SBChara c) {
        mChara = c;
        populateSpinner();
    }

    public void setAnimImg(String name) {
        Log.v(TAG, "setAnimImg: " + mAnim.name + " " + animImgPosition + " " + name);
        mAnim.setImage(animImgPosition, name);
        populateAnimImages();
    }

    public void removeAnimImg() {
        Log.v(TAG, "removeAnimImg: " + mAnim.name + " " + animImgPosition);
        mAnim.removeImage(animImgPosition);
        populateAnimImages();
    }

    private void populateSpinner() {
        Log.v(TAG, "populateSpinner: " + mChara.getName());
        View v = getView();
        if (v == null)
            return;
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(v.getContext(), R.layout.animation_select_list, R.id.asl_anim_name, mChara.getAnimationNames());
        Spinner spinner = (Spinner) v.findViewById(R.id.animation_spinner);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String s = (String) parent.getItemAtPosition(position);
                Log.v(TAG, "animation selected: " + s);
                mAnim = mChara.getAnimation(s);
                populateAnimImages();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.v(TAG, "Animation spinner onNothingSelected");
            }
        });
        ListView listView = (ListView) v.findViewById(R.id.animation_images_list);
        if(spinner.getCount() == 0)
            listView.setVisibility(View.INVISIBLE);
        else
            listView.setVisibility(View.VISIBLE);
    }

    private void populateAnimImages() {
        Log.v(TAG, "populateAnimImages: " + mChara.getName() + "->" + mAnim.name);
        View v = getView();
        ArrayList<String> animImgs = mAnim.getImageResourceNames();
        ArrayList<Drawable> animImgDrwbls = new ArrayList<>();
        for(String s : animImgs)
            animImgDrwbls.add(mChara.getImage(s));
        AnimationImageAdapter listAdapter = new AnimationImageAdapter(v.getContext(), R.layout.anim_images_select_list, animImgDrwbls);
        listAdapter.add(getResources().getDrawable(R.drawable.plus));
        ListView listView = (ListView) v.findViewById(R.id.animation_images_list);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "Animation image clicked!");
                animImgPosition = position;
                mListener.onAnimImageClicked();
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnAnimationSelectedListener {
        public void onAnimationSelected(int id);
        public void onNewItemClicked(View v);
        public void onNewItemCancel(View v);
        public void onNewItemOK(View v);
        public void onAnimImageClicked();
    }

}
