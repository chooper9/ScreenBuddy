package myapps.screenbuddy;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImageFragment.OnImageClickedListener} interface
 * to handle interaction events.
 * Use the {@link ImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "SBImageFragment";

    SBChara mChara;

    private OnImageClickedListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment ImageFragment.
     */
    public static ImageFragment newInstance() {
        Log.v(TAG, "newInstance");
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        if (mChara != null)
            populateGrid();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnImageClickedListener) activity;
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
        populateGrid();
    }

    public void onImagesAdded() {
        populateGrid();
    }

    private void populateGrid() {
        Log.v(TAG, "populateGrid: " + mChara.getName());
        View v = getView();
        if (v == null)
            return;

        ArrayList<String> imgNames = mChara.getImageNames();
        ArrayList<Drawable> imgs = mChara.getImages();
        ArrayList<ImageData> imgsData = new ArrayList<>();
        for(int i = 0; i < imgNames.size(); i++) {
            ImageData imgData = new ImageData();
            imgData.name = imgNames.get(i);
            imgData.img = imgs.get(i);
            imgsData.add(imgData);
        }

        ImageArrayAdapter gridAdapter = new ImageArrayAdapter(v.getContext(), R.layout.image_resource_grid, imgsData);
        GridView grid = (GridView) v.findViewById(R.id.image_grid);
        grid.setAdapter(gridAdapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageData imgData = (ImageData) parent.getItemAtPosition(position);
                mListener.onImageClicked(imgData.name);
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnImageClickedListener {
        public void onImageClicked(String name);
        public void onLoadImagesClicked(View v);
    }

}

class ImageData {
    public int id;
    public String name;
    public Drawable img;
}
