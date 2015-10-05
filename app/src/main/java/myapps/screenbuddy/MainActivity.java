package myapps.screenbuddy;

import java.io.InputStream;
import java.util.Locale;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements android.support.v7.app.ActionBar.TabListener,
                                                               CharacterFragment.OnCharacterSelectedListener,
                                                               AnimationFragment.OnAnimationSelectedListener,
                                                               ImageFragment.OnImageClickedListener {
    public static final String EXTRA_CHARA_NAME = "ScreenBuddy.CharacterName";
    public static final String EXTRA_IMG_NAME = "ScreenBuddy.ImageName";
    public static final String EXTRA_ANIM_IMG_REQUEST = "ScreenBuddy.AnimImg";

    private static final String TAG = "ScreenBuddyMain";
    private static final String STATE_CHARA = "Character";
    private static final int REQUEST_PREVIEW_IMAGE_SELECT = 1;
    private static final int REQUEST_ANIMATION_IMAGE_SELECT = 2;
    private static final int REQUEST_PICTURES_FROM_GALLERY = 3;
    private static final int FRAGMENT_CHARACTER = 0;
    private static final int FRAGMENT_ANIMATION = 1;
    private static final int FRAGMENT_IMAGE = 2;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    DisableableViewPager mViewPager;

    private CharacterFragment mCharacterFragment;
    private AnimationFragment mAnimationFragment;
    private ImageFragment mImageFragment;

    private SQLiteDatabase db;
    private SBChara mChara;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TAG, "onCreate");

        db = new SBDBOpenHelper(this).getWritableDatabase();
        mChara = null;

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(android.app.ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (DisableableViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        String name = "";
        if (mChara != null)
            name = mChara.getName();
        savedInstanceState.putString(STATE_CHARA, name);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        if(mViewPager.isEnabled())
            mViewPager.setCurrentItem(tab.getPosition());
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // if the pager is enabled and the tab position is offset from the pager position
        if (mViewPager.isEnabled() && (tab.getPosition() != mViewPager.getCurrentItem()))
            mViewPager.setCurrentItem(tab.getPosition());
    }

    public void onCharacterSelected(String n) {
        Log.v(TAG, "onCharacterSelected: " + n);
        if (mChara == null || !n.equals(mChara.getName())) {
            loadSelectedCharacter(n);
            mCharacterFragment.setCharacter(mChara);
            if (mAnimationFragment != null)
                mAnimationFragment.setCharacter(mChara);
            if (mImageFragment != null)
                mImageFragment.setCharacter(mChara);
        }
    }

    public void onPreviewImageClicked(View v) {
        Log.v(TAG, "onPreviewImageClicked");
        if(mChara == null)
            return;
        Intent intent = new Intent(this, ImageSelector.class);
        intent.putExtra(EXTRA_CHARA_NAME, mChara.getName());
        startActivityForResult(intent, REQUEST_PREVIEW_IMAGE_SELECT);
    }

    /* Saves the animations. Preview image and
       image resources are saved when added */
    public void onSaveClicked(View v) {
        Log.v(TAG, "onSaveClicked");
        if(mChara == null)
            return;
        long result = mChara.saveAnimations(db);
        if(result == -1)
            Log.v(TAG, "Error during save");
        Toast.makeText(this, "Character Saved", Toast.LENGTH_SHORT).show();
    }

    public void onNewItemClicked(View v) {
        Log.v(TAG, "onNewItemClicked");
        Log.v(TAG, "currentItem: " + mViewPager.getCurrentItem());
        mViewPager.setEnabled(false);
        switch(mViewPager.getCurrentItem()) {
            case FRAGMENT_CHARACTER:
                mCharacterFragment.onNewItemClicked(v);
                break;
            case FRAGMENT_ANIMATION:
                if(mChara == null) {
                    mViewPager.setEnabled(true);
                    return;
                }
                mAnimationFragment.onNewItemClicked(v);
                break;
            default: break;
        }
    }

    public void onNewItemCancel(View v) {
        Log.v(TAG, "Cancel new item");
        mViewPager.setEnabled(true);
        switch(mViewPager.getCurrentItem()) {
            case FRAGMENT_CHARACTER:
                mCharacterFragment.onNewItemCancel(v);
                break;
            case FRAGMENT_ANIMATION:
                mAnimationFragment.onNewItemCancel(v);
                break;
            default: break;
        }
    }

    public void onNewItemOK(View v) {
        Log.v(TAG, "New item OK");
        mViewPager.setEnabled(true);
        switch(mViewPager.getCurrentItem()) {
            case FRAGMENT_CHARACTER:
                mCharacterFragment.onNewItemOK(v);
                break;
            case FRAGMENT_ANIMATION:
                mAnimationFragment.onNewItemOK(v);
                break;
            default: break;
        }
    }

    public void onAnimationSelected(int id) {
        return;
    }

    public void onAnimImageClicked() {
        Intent intent = new Intent(this, ImageSelector.class);
        intent.putExtra(EXTRA_CHARA_NAME, mChara.getName());
        intent.putExtra(EXTRA_ANIM_IMG_REQUEST, true);
        startActivityForResult(intent, REQUEST_ANIMATION_IMAGE_SELECT);
    }

    public void onImageClicked(String name) {
        Log.v(TAG, "image clicked: " + name);
    }

    public void onLoadImagesClicked(View v) {
        Log.v(TAG, "onLoadImagesClicked");
        if(mChara == null)
            return;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICTURES_FROM_GALLERY);
    }

    public void loadSelectedCharacter(String charaName) {
        String[] select = {SBDBOpenHelper.COL_CHARA_NAME, SBDBOpenHelper.COL_CHARA_PREV_IMG_NAME};
        String where = SBDBOpenHelper.COL_CHARA_NAME + " = \"" + charaName +"\"";
        Cursor cursor = db.query(SBDBOpenHelper.CHARA_TABLE_NAME, select, where, null, null, null, null);

        if(cursor.moveToFirst()) {
            /* Create the character with this context */
            mChara = new SBChara(getApplicationContext(), charaName);
            mChara.setPreviewImageName(cursor.getString(cursor.getColumnIndex(SBDBOpenHelper.COL_CHARA_PREV_IMG_NAME)));

            /* Get the image resources and add them to the character */
            Log.v(TAG, "Select images from database");
            select = new String[]{SBDBOpenHelper.COL_IMG_NAME, SBDBOpenHelper.COL_IMG_DRAWABLE};
            where = SBDBOpenHelper.COL_IMG_CHARA_NAME + " = \"" + charaName + "\"";
            cursor = db.query(SBDBOpenHelper.IMG_TABLE_NAME, select, where, null, null, null, null);
            if(cursor.moveToFirst()) {
                do {
                    String imgName = cursor.getString(cursor.getColumnIndex(SBDBOpenHelper.COL_IMG_NAME));
                    byte[] imgAsByteArray = cursor.getBlob(cursor.getColumnIndex(SBDBOpenHelper.COL_IMG_DRAWABLE));
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imgAsByteArray, 0, imgAsByteArray.length);
                    BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
                    mChara.addResource(imgName, drawable);
                } while (cursor.moveToNext());

                /* Create animations */
                Log.v(TAG, "Select animations from database");
                select = new String[]{SBDBOpenHelper.COL_ANIM_ID, SBDBOpenHelper.COL_ANIM_NAME, SBDBOpenHelper.COL_ANIM_FLIP_INTERVAL};
                where = SBDBOpenHelper.COL_ANIM_CHARA_NAME + " = \"" + charaName + "\"";
                Cursor animationsCursor = db.query(SBDBOpenHelper.ANIM_TABLE_NAME, select, where, null, null, null, null);
                if(animationsCursor.moveToFirst()) {
                    do {
                        int animId = animationsCursor.getInt(animationsCursor.getColumnIndex(SBDBOpenHelper.COL_ANIM_ID));
                        String animName = animationsCursor.getString(animationsCursor.getColumnIndex(SBDBOpenHelper.COL_ANIM_NAME));
                        mChara.newAnimation(animName);
                        AnimationFlipper anim = mChara.getAnimation(animName);

                        /* Add this animation's images */
                        Log.v(TAG, "--Add images to animation \"" + animName + "\"");
                        select = new String[]{SBDBOpenHelper.COL_ANIMIMG_INDEX, SBDBOpenHelper.COL_ANIMIMG_IMG_NAME};
                        where = SBDBOpenHelper.COL_ANIMIMG_ANIM_ID + " = " + animId;
                        String orderBy = SBDBOpenHelper.COL_ANIMIMG_INDEX + " ASC";
                        Cursor animImgCursor = db.query(SBDBOpenHelper.ANIM_IMG_TABLE_NAME, select, where, null, null, null, orderBy);
                        if(animImgCursor.moveToFirst()) {
                            do {
                                String imgResName = animImgCursor.getString(animImgCursor.getColumnIndex(SBDBOpenHelper.COL_ANIMIMG_IMG_NAME));
                                anim.addImage(imgResName);
                                Log.v(TAG, "----Added \"" + imgResName + "\"");
                            } while (animImgCursor.moveToNext());
                        } else {
                            Log.v(TAG, "--No images found in this animation!");
                        }
                    } while (animationsCursor.moveToNext());
                } else {
                    Log.v(TAG, "No animations found for character in database!");
                }
            } else {
                Log.v(TAG, "No images found for character in database!");
            }
        } else {
            Log.v(TAG, "Character not found in database!");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        if(requestCode == REQUEST_PREVIEW_IMAGE_SELECT) {
            if(resultCode == RESULT_OK) {
                String prevImgName = data.getStringExtra(EXTRA_IMG_NAME);
                Log.v(TAG, "-- image name: " + prevImgName);
                if(prevImgName != null) {
                    mChara.setPreviewImageName(prevImgName);
                    long id = mChara.savePreviewImage(db);
                    Log.v(TAG, "Updated preview image. DB call returned: " + id);
                    mCharacterFragment.populateSpinner();
                }
            }
        } else if(requestCode == REQUEST_ANIMATION_IMAGE_SELECT) {
            if(resultCode == RESULT_OK) {
                String imgName = data.getStringExtra(EXTRA_IMG_NAME);
                Log.v(TAG, "-- image name: " + imgName);
                if(imgName != null) {
                    if(imgName.equals("REMOVE")) {
                        mAnimationFragment.removeAnimImg();
                    } else {
                        mAnimationFragment.setAnimImg(imgName);
                    }
                }
            }
        } else if(requestCode == REQUEST_PICTURES_FROM_GALLERY) {
            if(resultCode == RESULT_OK) {
                Log.v(TAG, "data: " + data);
                ClipData clip;
                if((clip = data.getClipData()) != null) {
                    for(int i = 0; i < clip.getItemCount(); i++) {
                        Uri uri = clip.getItemAt(i).getUri();
                        if(uri != null) {
                            try {
                                String uriName = uri.toString();
                                InputStream inputStream = getContentResolver().openInputStream(uri);
                                Drawable d = Drawable.createFromStream(inputStream, uriName);
                                if(mChara.addResource(uriName, d))
                                    mChara.saveNewImage(db, uriName, d);
                            } catch (Exception e) {
                                Log.v(TAG, "Failed to get image from uri!");
                            }
                        }
                    }
                } else {
                    Uri photoUri = data.getData();
                    try {
                        String uriName = photoUri.toString();
                        InputStream inputStream = getContentResolver().openInputStream(photoUri);
                        Drawable d = Drawable.createFromStream(inputStream, photoUri.toString());
                        if(mChara.addResource(photoUri.toString(), d))
                            mChara.saveNewImage(db, uriName, d);
                    } catch (Exception e) {
                        Log.v(TAG, "Failed to get image from uri!");
                    }
                }
                mImageFragment.onImagesAdded();
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    mCharacterFragment = CharacterFragment.newInstance("");
                    return mCharacterFragment;
                case 1:
                    mAnimationFragment = AnimationFragment.newInstance("");
                    if (mChara != null)
                        mAnimationFragment.setCharacter(mChara);
                    return mAnimationFragment;
                case 2:
                    mImageFragment = ImageFragment.newInstance();
                    if (mChara != null)
                        mImageFragment.setCharacter(mChara);
                    return mImageFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }
}
