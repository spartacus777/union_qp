package union_qp.com.ua;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import anton.kizema.libs.image.ImageConverter;
import union_qp.com.ua.view.ColorSpinnerAdapter;


public class ImageViewActivity extends Activity implements View.OnClickListener {

    public static final String PATH = "PATH";

    private String path;

    private TextView tvAddText, tvSelect;

    private ViewGroup parent;

    MenuItem okMenuBtn, colorSelector;
    File imgFile;
    ImageView myImage;
    int color;
    boolean isDisabled = false;

    boolean isSelectionMode = false;
    private List<Vector2i> points;

    private EditText editableTv;

    Paint paint;
    int actionBarHeight;

    private class Vector2i{
        float x;
        float y;

        Vector2i(){
            x=0;
            y=0;
        }

        Vector2i(float x, float y){
            this.x = x;
            this.y = y;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)){
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        parent = (ViewGroup) findViewById(R.id.parent);

        tvSelect = (TextView) findViewById(R.id.tvSelect);
        tvSelect.setOnClickListener(this);

        tvAddText = (TextView) findViewById(R.id.tvAddText);
        tvAddText.setOnClickListener(this);

        myImage = (ImageView) findViewById(R.id.ivImage);

        path = getIntent().getStringExtra(PATH);
        imgFile = new File(path);

        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            myImage.setImageBitmap(myBitmap);
        }

        initFloatingBtn();
    }

    private void initFloatingBtn(){
        ImageView icon = new ImageView(this); // Create an icon
        icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));

        FloatingActionButton actionButton = new FloatingActionButton.Builder(this)
                .setContentView(icon)
                .build();

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        ImageView itemIcon = new ImageView(this);
        itemIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_edittext));
        SubActionButton button1 = itemBuilder.setContentView(itemIcon).build();

        SubActionButton.Builder itemBuilder1 = new SubActionButton.Builder(this);
        ImageView itemIcon1 = new ImageView(this);
        itemIcon1.setImageDrawable(getResources().getDrawable(R.drawable.ic_select));
        SubActionButton button2 = itemBuilder1.setContentView(itemIcon1).build();

        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(button1)
                .addSubActionView(button2)
                .attachTo(actionButton)
                .build();

    }

    @Override
    public void onBackPressed() {
        if (isDisabled){
            if (isSelectionMode){

            } else {
                parent.removeView(editableTv);
            }
            showHideMenu(false);
            disable(false);
            return;
        }
        super.onBackPressed();
    }

    private void showHideMenu(boolean show){
        okMenuBtn.setVisible(show);
        colorSelector.setVisible(show);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_view, menu);
        okMenuBtn = menu.findItem(R.id.ok);

        colorSelector = menu.findItem(R.id.colorSelector);
        showHideMenu(false);

        Spinner spinner = (Spinner) colorSelector.getActionView();
        spinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        final ColorSpinnerAdapter adapter = new ColorSpinnerAdapter(this);
        spinner.setAdapter(adapter);

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            spinner.setDropDownWidth(App.getPixel(40));
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                color = adapter.colors[arg2];
                if (!isSelectionMode) {
                    editableTv.setTextColor(color);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
            return true;
        }

        if (id == R.id.ok) {
            showHideMenu(false);
            disable(false);

            if (isSelectionMode){
                paint.setStrokeWidth(App.getPixel(4));
                isSelectionMode = false;

                FileOutputStream fo = null;
                try {
                    fo = new FileOutputStream(imgFile);
                    fo.write(ImageConverter.bitmapToBite(newImage));
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true;
            }

            Bitmap bm = drawableToBitmap(myImage.getDrawable());
            Bitmap.Config config = bm.getConfig();
            int width = bm.getWidth();
            int height = bm.getHeight();

            Bitmap newImage = Bitmap.createBitmap(width, height, config);

            Canvas c = new Canvas(newImage);
            c.drawBitmap(bm, 0, 0, null);

            Log.d("ANT", "editableTv.getText() " + editableTv.getText().toString());

            paint = new Paint();
            paint.setTextSize(App.getPixel(16));
            paint.setColor(color);
//        paint.setStrokeWidth(App.getPixel(4));
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setDither(true);

            editableTv.clearFocus();
            c.drawText(editableTv.getText().toString(), editableTv.getX() * myImage.getDrawable().getIntrinsicWidth() / myImage.getWidth(),
                    editableTv.getY() * myImage.getDrawable().getIntrinsicHeight() / myImage.getHeight(), paint);

            myImage.setImageBitmap(newImage);

            FileOutputStream fo = null;
            try {
                fo = new FileOutputStream(imgFile);
                fo.write(ImageConverter.bitmapToBite(newImage));
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            parent.removeView(editableTv);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Bitmap loadBitmapFromView(View v, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    Vector2i downPoint;
    Bitmap newImage;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isSelectionMode){
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                Log.d("ANT", "writingasdfesfwerf");
                Vector2i p = new Vector2i( ( event.getRawX() ),
                        ( event.getRawY() - actionBarHeight));

                points.add(p);

                downPoint = new Vector2i(p.x, p.y);

                return true;
            } else {
                Vector2i p = new Vector2i( ( event.getRawX() ),
                        ( event.getRawY() - actionBarHeight));

                points.add(p);

                Bitmap bm = drawableToBitmap(myImage.getDrawable());
                Bitmap.Config config = bm.getConfig();
                int width = bm.getWidth();
                int height = bm.getHeight();

                newImage = Bitmap.createBitmap(width, height, config);

                Canvas c = new Canvas(newImage);
                c.drawBitmap(bm, 0, 0, null);

                paint = new Paint();
                paint.setColor(color);
                paint.setStrokeWidth(App.getPixel(4));
                paint.setAntiAlias(true);
                paint.setFilterBitmap(true);
                paint.setDither(true);
                c.drawLine(p.x, p.y, downPoint.x, downPoint.y, paint);
                downPoint = p;

                myImage.setImageBitmap(newImage);

                return true;
            }

        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        if (isDisabled) {
            return;
        }

        switch (v.getId()) {
            case R.id.tvAddText:
                createTextView();
                disable(true);
                break;
            case R.id.tvSelect:
                points = new LinkedList<Vector2i>();
                showHideMenu(true);
                isSelectionMode = true;
                disable(true);
                break;
        }
    }

    private void disable(boolean f) {
        tvAddText.setEnabled(!f);
        tvSelect.setEnabled(!f);
        isDisabled = f;
    }


    private void createTextView() {
        showHideMenu(true);

        ViewGroup group = (ViewGroup) this.getLayoutInflater().inflate(R.layout.c_edittext, parent);
        EditText tv = (EditText) group.findViewById(R.id.etEditTExt);
        group.removeView(tv);
        group = null;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, App.getPixel(30));
        tv.setLayoutParams(params);
        editableTv = tv;
        initET(tv);

        parent.addView(tv);
    }

    private void initET(EditText et) {
        et.setOnTouchListener(new MyTouchListener());
    }

    private final class MyTouchListener implements View.OnTouchListener {

        private long timeDown;
        private boolean startDrag = false;

        @Override
        public boolean onTouch(View v, MotionEvent ev) {

            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    timeDown = System.currentTimeMillis();
                    startDrag = false;
                    return true;
                }

                case MotionEvent.ACTION_MOVE: {
                    if (System.currentTimeMillis() - timeDown > 500) {
                        if (!startDrag) {
                            v.setAlpha(0.5f);
                        }

                        startDrag = true;

                        v.setX(ev.getRawX());
                        v.setY(ev.getRawY());
                    }

                    return true;
                }

                case MotionEvent.ACTION_UP: {
                    if (startDrag) {
                        v.setAlpha(1f);

                        v.setX(ev.getRawX());
                        v.setY(ev.getRawY());
                        return true;
                    } else {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        v.requestFocus();
                    }

                    return false;
                }
            }

            return true;
        }
    }


}
