package myapps.screenbuddy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Cameron on 6/4/2015.
 */
public class SBDBOpenHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "sbuddy.sqlite";
    public static final String CHARA_TABLE_NAME = "SBChara";
    public static final String IMG_TABLE_NAME = "SBImg";
    public static final String ANIM_TABLE_NAME = "SBAnim";
    public static final String ANIM_IMG_TABLE_NAME = "SBAnimImg";   /* Table connecting animations
                                                                        to their list of images */
    public static final String COL_CHARA_ID = "ID";
    public static final String COL_CHARA_NAME = "NAME";
    public static final String COL_CHARA_PREV_IMG_NAME = "PREVIEW_IMG";
    public static final String COL_IMG_ID = "ID";
    public static final String COL_IMG_CHARA_NAME = "CHARA_NAME";
    public static final String COL_IMG_NAME = "NAME";
    public static final String COL_IMG_DRAWABLE = "DRAWABLE";
    public static final String COL_ANIM_ID = "ID";
    public static final String COL_ANIM_CHARA_NAME = "CHARA_NAME";
    public static final String COL_ANIM_NAME = "NAME";
    public static final String COL_ANIM_FLIP_INTERVAL = "FLIP_INTERVAL";
    public static final String COL_ANIMIMG_ID = "ID";
    public static final String COL_ANIMIMG_ANIM_ID = "ANIM_ID";
    public static final String COL_ANIMIMG_IMG_NAME = "IMG_NAME";
    public static final String COL_ANIMIMG_INDEX = "IDX";

    private static final String CHARA_TABLE_CREATE =
            "CREATE TABLE " + CHARA_TABLE_NAME + "(" +
                    "ID INTEGER PRIMARY KEY, " +
                    "NAME TEXT NOT NULL UNIQUE, " +
                    "PREVIEW_IMG TEXT);";
    private static final String IMG_TABLE_CREATE =
            "CREATE TABLE " + IMG_TABLE_NAME + "(" +
                    "ID INTEGER PRIMARY KEY, " +
                    "CHARA_NAME TEXT NOT NULL, " +
                    "NAME TEXT NOT NULL, " +
                    "DRAWABLE BLOB);";
    private static final String ANIM_TABLE_CREATE =
            "CREATE TABLE " + ANIM_TABLE_NAME + "(" +
                    "ID INTEGER PRIMARY KEY, " +
                    "CHARA_NAME TEXT NOT NULL, " +
                    "NAME TEXT NOT NULL, " +
                    "FLIP_INTERVAL INTEGER NOT NULL DEFAULT 500);";
    private static final String ANIM_IMG_TABLE_CREATE =
            "CREATE TABLE " + ANIM_IMG_TABLE_NAME + "(" +
                    "ID INTEGER PRIMARY KEY, " +
                    "ANIM_ID INTEGER NOT NULL, " +
                    "IMG_NAME TEXT NOT NULL, " +
                    "IDX INTEGER NOT NULL);";

    SBDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CHARA_TABLE_CREATE);
        db.execSQL(IMG_TABLE_CREATE);
        db.execSQL(ANIM_TABLE_CREATE);
        db.execSQL(ANIM_IMG_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
