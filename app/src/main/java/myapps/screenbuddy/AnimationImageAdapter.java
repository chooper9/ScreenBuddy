package myapps.screenbuddy;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Cameron on 6/28/2015.
 */
public class AnimationImageAdapter extends ArrayAdapter<Drawable> {
    Context context;

    public AnimationImageAdapter(Context c, int resourceId, List<Drawable> items) {
        super(c, resourceId, items);
        this.context = c;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        Drawable d = (Drawable) getItem(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if(v == null)
            v = inflater.inflate(R.layout.anim_images_select_list, null);

        ((TextView)v.findViewById(R.id.aisl_index)).setText("" + (position+1));
        ((ImageView)v.findViewById(R.id.aisl_image)).setImageDrawable(d);

        return v;
    }
}
