package myapps.screenbuddy;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by Cameron on 6/28/2015.
 */
public class ImageArrayAdapter extends ArrayAdapter<ImageData> {
    Context context;

    public ImageArrayAdapter(Context c, int resourceId, List<ImageData> items) {
        super(c, resourceId, items);
        this.context = c;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ImageData imgData = getItem(position);
        Drawable d = imgData.img;

        ImageView imageView;
        if (v == null) {
            Resources r = Resources.getSystem();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, r.getDisplayMetrics());

            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams((int)px, (int)px));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //imageView.setPadding(4, 4, 4, 4);
        } else {
            imageView = (ImageView) v;
        }
        imageView.setImageDrawable(d);
        return imageView;
    }
}
