package myapps.screenbuddy;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Cameron on 5/29/2015.
 */
public class SBChara {
    private static final String TAG = "ScreenBuddy";

    private HashMap<String, AnimationFlipper> animations;
    private HashMap<String, Drawable> imageResources;
    private Context context;
    private String name;
    private String previewImageName;
    private String currentAnim;

    public SBChara(Context c, String n) {
        Log.v(TAG, "Creating character: " + n);
        context = c;
        name = n;
        previewImageName = null;
        animations = new HashMap<String, AnimationFlipper>();
        imageResources = new HashMap<String, Drawable>();
    }

    public String getName() { return name; }

    public String getPreviewImageName() { return previewImageName; }

    public void setPreviewImageName(String n) { previewImageName = n; }

    public Drawable getPreviewImage() {
        if(previewImageName != null)
          return imageResources.get(previewImageName);

        return null;
    }

    public ArrayList<String> getAnimationNames() {
        ArrayList<String> animNames = new ArrayList<>();
        for(Map.Entry<String, AnimationFlipper> e : animations.entrySet()) {
            animNames.add(e.getKey());
        }
        return animNames;
    }

    public ArrayList<String> getImageNames() {
        ArrayList<String> imgs = new ArrayList<>();
        for(Map.Entry<String, Drawable> e : imageResources.entrySet()) {
            imgs.add(e.getKey());
        }
        return imgs;
    }

    public ArrayList<Drawable> getImages() {
        ArrayList<Drawable> imgs = new ArrayList<>();
        for(Map.Entry<String, Drawable> e : imageResources.entrySet()) {
            imgs.add(e.getValue());
        }
        return imgs;
    }

    public AnimationFlipper getAnimation(String n) { return animations.get(n); }

    public Drawable getImage(String n) { return imageResources.get(n); }

    public String nextAnimation() {
        // requires followers
        //animations.get(currentAnim).nextAnimation();

        Object[] animNames = animations.keySet().toArray();
        int rand = new Random().nextInt(animNames.length);
        return (String) animNames[rand];
    }

    /* "Compiles" all animations
     *  Call when done editing a character or after
     *  loading from the database.
     */
    public void generateAnimations() {
        for(Map.Entry<String, AnimationFlipper> e : animations.entrySet()) {
            e.getValue().generateLayout(imageResources);
        }
    }

    /* Removes imageViews from the AnimationFlipper
     */
    public void clearAnimationLayouts() {
        for(Map.Entry<String, AnimationFlipper> e : animations.entrySet()) {
            e.getValue().clearLayout();
        }
    }

    public void removeAnimation(String n) {
        // TODO
    }

    public boolean addResource(String n, Drawable d) {
        if(imageResources.get(n) != null)
            return false;
        imageResources.put(n, d);
        return true;
    }

    public void removeResources() {
    }

    public boolean isValidAnimName(String n) {
        return (animations.get(n) == null);
    }

    public void newAnimation(String n) {
        AnimationFlipper af = new AnimationFlipper(context, n);
        animations.put(n, af);
    }

    public long saveNewCharacter(SQLiteDatabase db) {
        Log.v(TAG, "Inserting into table SBChara: " + name);
        ContentValues cv = new ContentValues();
        cv.put(SBDBOpenHelper.COL_CHARA_NAME, name);
        return db.insert(SBDBOpenHelper.CHARA_TABLE_NAME, null, cv);
    }

    public long savePreviewImage(SQLiteDatabase db) {
        Log.v(TAG, "Updating preview img in table SBChara: " + name);

        /* Insert into SBChara table */
        ContentValues cv = new ContentValues();
        cv.put(SBDBOpenHelper.COL_CHARA_PREV_IMG_NAME, previewImageName);
        String where = SBDBOpenHelper.COL_CHARA_NAME + " = ?";
        String[] whereArgs = {name};

        return db.update(SBDBOpenHelper.CHARA_TABLE_NAME, cv, where, whereArgs);
    }

    public long saveAnimations(SQLiteDatabase db) {
        long rawId = -1;
        /* Insert into SBAnim table */
        for(Map.Entry<String, AnimationFlipper> e : animations.entrySet()) {
            Log.v(TAG, "--Inserting into table SBAnim: " + e.getKey());
            rawId = e.getValue().insertIntoDB(name, db);
            if(rawId == -1)
                return -1;
        }
        return rawId;
    }

    public long saveNewImage(SQLiteDatabase db, String n, Drawable d) {
        Log.v(TAG, "--Inserting into table SBImg: " + n);
        ContentValues cv = new ContentValues();
        cv.put(SBDBOpenHelper.COL_IMG_NAME, n);
        cv.put(SBDBOpenHelper.COL_IMG_CHARA_NAME, name);
        // drawable blob
        BitmapDrawable bitmapDrawable = (BitmapDrawable) d;
        Bitmap b = bitmapDrawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imgAsByteArray = baos.toByteArray();
        cv.put(SBDBOpenHelper.COL_IMG_DRAWABLE, imgAsByteArray);
        return db.insert(SBDBOpenHelper.IMG_TABLE_NAME, null, cv);
    }

    public long insertIntoDB(SQLiteDatabase db) {
        long rawId;
        Log.v(TAG, "Inserting into table SBChara: " + name);

        /* Insert into SBChara table */
        ContentValues cv = new ContentValues();
        cv.put(SBDBOpenHelper.COL_CHARA_NAME, name);
        if(previewImageName != null)
            cv.put(SBDBOpenHelper.COL_CHARA_PREV_IMG_NAME, previewImageName);
        long retId = db.insertWithOnConflict(SBDBOpenHelper.CHARA_TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        if(retId == -1)
            return -1;

        /* Insert into SBImg table */
        for(Map.Entry<String, Drawable> e : imageResources.entrySet()) {
            Log.v(TAG, "--Inserting into table SBImg: " + e.getKey());
            cv.clear();
            cv.put(SBDBOpenHelper.COL_IMG_NAME, e.getKey());
            cv.put(SBDBOpenHelper.COL_IMG_CHARA_NAME, name);
            // drawable blob
            BitmapDrawable bitmapDrawable = (BitmapDrawable) e.getValue();
            Bitmap b = bitmapDrawable.getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imgAsByteArray = baos.toByteArray();
            cv.put(SBDBOpenHelper.COL_IMG_DRAWABLE, imgAsByteArray);
            rawId = db.insertWithOnConflict(SBDBOpenHelper.IMG_TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            if(rawId == -1)
                return -1;
        }

        /* Insert into SBAnim table */
        for(Map.Entry<String, AnimationFlipper> e : animations.entrySet()) {
            Log.v(TAG, "--Inserting into table SBAnim: " + e.getKey());
            rawId = e.getValue().insertIntoDB(name, db);
            if(rawId == -1)
                return -1;
        }

        Log.v(TAG, "Insert Complete!");
        return retId;
    }
}
