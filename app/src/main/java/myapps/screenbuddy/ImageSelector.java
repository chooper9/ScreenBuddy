package myapps.screenbuddy;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;


public class ImageSelector extends ActionBarActivity {
    private static final String TAG = "SB.ImageSelector";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selector);

        Intent intent = getIntent();
        String mCharaName = intent.getStringExtra(MainActivity.EXTRA_CHARA_NAME);
        final boolean isAnimImg = intent.getBooleanExtra(MainActivity.EXTRA_ANIM_IMG_REQUEST, false);

        SQLiteDatabase db = new SBDBOpenHelper(this).getReadableDatabase();
        String query = "SELECT ID _id, NAME, DRAWABLE FROM SBImg WHERE CHARA_NAME = ?";
        String[] queryArgs = {mCharaName};
        Cursor cursor1 = db.rawQuery(query, queryArgs);

        Cursor cursor;
        if(isAnimImg) {
            // Add extra element for removing an animation image
            MatrixCursor extra = new MatrixCursor(new String[]{"_id", "NAME", "DRAWABLE"});
            extra.addRow(new String[]{"-1", "REMOVE", "NULL"});
            Cursor[] cursors = {cursor1, extra};
            cursor = new MergeCursor(cursors);
        } else {
            cursor = cursor1;
        }

        if(cursor.moveToFirst()) {
                    /* Create list view */
            String[] fromColumns = {SBDBOpenHelper.COL_IMG_DRAWABLE};
            int[] toViews = {R.id.image_grid_img};
            SimpleCursorAdapter gridAdapter = new SimpleCursorAdapter(this, R.layout.image_resource_grid, cursor, fromColumns, toViews, 0);
            gridAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View v, Cursor cursor, int columnIndex) {
                    if (v.getId() == R.id.image_grid_img) {
                        if(isAnimImg && cursor.isLast()) {
                            ((ImageView) v).setImageResource(R.drawable.remove);
                            String name = cursor.getString(cursor.getColumnIndex(SBDBOpenHelper.COL_IMG_NAME));
                            v.setTag(name);
                            return true;
                        }

                        byte[] imgAsByteArray = cursor.getBlob(columnIndex);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imgAsByteArray, 0, imgAsByteArray.length);
                        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
                        ((ImageView) v).setImageDrawable(drawable);

                        // set name/id as tag
                        String imgName = cursor.getString(cursor.getColumnIndex(SBDBOpenHelper.COL_IMG_NAME));
                        v.setTag(imgName);

                        return true;
                    }
                    return false;
                }
            });
            GridView grid = (GridView) findViewById(R.id.image_selector_grid);
            grid.setAdapter(gridAdapter);
            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String imgName = (String) view.findViewById(R.id.image_grid_img).getTag();
                    Log.v(TAG, "grid.onItemClick:tag: " + imgName);
                    Intent intent = new Intent();
                    intent.putExtra(MainActivity.EXTRA_IMG_NAME, imgName);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        } else {
            Log.v(TAG, "No images found for this character!");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_selector, menu);
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
}
