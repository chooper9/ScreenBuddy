package myapps.screenbuddy;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Cameron on 5/25/2015.
 */
public class AnimationFlipper {
    private static final String TAG = "ScreenBuddy";

    public String name;

    private RelativeLayout layout;
    private ViewFlipper flipper;
    private RemoteViews rv;
    private ArrayList<String> imageResourceNames;
    // followers
    private Context context;
    private int flipInterval;


    public AnimationFlipper(Context c, String n) {
        context = c;
        name = n;
        flipInterval = 500;
        // Setup layout
        layout = new RelativeLayout(context);
        layout.setPadding(0,0,0,0);
        layout.setBackgroundColor(0);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        ));

        // Setup ViewFlipper
        flipper = new ViewFlipper(context);
        flipper.setAutoStart(true);
        flipper.setFlipInterval(500);

        flipper.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        ));


        // For testing
        /*
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        flipper.setLayoutParams(lp);
        */

        imageResourceNames = new ArrayList<String>();
    }

    public RelativeLayout getLayout() { return layout; }

    public ViewFlipper getFlipper() { return flipper; }

    public ArrayList<String> getImageResourceNames() {
        ArrayList<String> r = new ArrayList<>();
        for(String s : imageResourceNames)
          r.add(s);
        return r;
    }

    public String getImageName(int position) { return imageResourceNames.get(position); }

    public int getImageCount() {
        return imageResourceNames.size();
    }

    public void setInterval(int ms) {
        flipInterval = ms;
        flipper.setFlipInterval(ms);
    }

    public void nextAnimation() {
        int rand = new Random().nextInt(100);
        // requires followers
    }

    /* "Compiles" the ViewFlipper and all its sub-layouts.
     * Call from SBChara or when previewing an animation
     */
    public void generateLayout(HashMap<String, Drawable> imageResources) {
        clearLayout();

        for(String s : imageResourceNames) {
            ImageView iv = createNewImageView();
            iv.setBackground(imageResources.get(s));
            flipper.addView(iv);
        }
        layout.addView(flipper);
    }

    /* Removes all views from the layout and flipper. */
    public void clearLayout() {
        flipper.removeAllViews();
        layout.removeAllViews();
    }

    public void addImage(String n) { imageResourceNames.add(n); }

    public void setImage(int index, String n) {
        if (index >= 0 && index < imageResourceNames.size()) {
            imageResourceNames.set(index, n);
        } else if (index == imageResourceNames.size()) {
            imageResourceNames.add(n);
        }
    }

    public void removeImage(int index) {
        if (index >= 0 && index < imageResourceNames.size())
            imageResourceNames.remove(index);
    }

    public void clearImageResources() { imageResourceNames.clear(); }

    private ImageView createNewImageView() {
        ImageView iv = new ImageView(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER;
        lp.setMargins(2, 2, 2, 2);
        iv.setLayoutParams(lp);
        return iv;
    }

    public long insertIntoDB(String charaName, SQLiteDatabase db) {
        long rawId;
        long animId = -1;

        /* Find the id of this animation if it exists */
        String[] select = {SBDBOpenHelper.COL_ANIM_ID};
        String where = SBDBOpenHelper.COL_ANIM_CHARA_NAME + " = ? AND "
                + SBDBOpenHelper.COL_ANIM_NAME + " = ?";
        String[] whereArgs = {charaName, name};
        Cursor cursor = db.query(SBDBOpenHelper.ANIM_TABLE_NAME, select, where, whereArgs, null, null, null);

        ContentValues cv = new ContentValues();
        cv.put(SBDBOpenHelper.COL_ANIM_CHARA_NAME, charaName);
        cv.put(SBDBOpenHelper.COL_ANIM_NAME, name);
        cv.put(SBDBOpenHelper.COL_ANIM_FLIP_INTERVAL, flipInterval);

        if(cursor.moveToFirst()) {
            if(cursor.getCount() == 1) {
                /* Update existing animation */
                animId = cursor.getInt(0);
                db.update(SBDBOpenHelper.ANIM_TABLE_NAME, cv, where, whereArgs);
            }
        } else {
            /* Insert into SBAnim table */
            animId = db.insert(SBDBOpenHelper.ANIM_TABLE_NAME, null, cv);
        }

        if (animId == -1)
            return -1;

        /* Works for now, but is totes lazy. */
        //TODO: only add/update new/changed entries

        /* Delete this animation's previous img list */
        String imgWhere = SBDBOpenHelper.COL_ANIMIMG_ANIM_ID + " = " + animId;
        db.delete(SBDBOpenHelper.ANIM_IMG_TABLE_NAME, imgWhere, null);

        /* Insert into SBAnimImg table */
        for(int i = 0; i < imageResourceNames.size(); i++) {
            String s = imageResourceNames.get(i);
            Log.v(TAG, "----Inserting into table SBAnimImg: " + s);
            cv.clear();
            cv.put(SBDBOpenHelper.COL_ANIMIMG_ANIM_ID, animId);
            cv.put(SBDBOpenHelper.COL_ANIMIMG_IMG_NAME, s);
            cv.put(SBDBOpenHelper.COL_ANIMIMG_INDEX, i);
            rawId = db.insert(SBDBOpenHelper.ANIM_IMG_TABLE_NAME, null, cv);
            if(rawId == -1)
                return -1;
        }

        return animId;
    }
}
