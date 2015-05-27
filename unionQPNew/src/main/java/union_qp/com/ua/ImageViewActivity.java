package union_qp.com.ua;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import anton.kizema.libs.image.ImageConverter;
import union_qp.com.ua.view.ColorSpinnerAdapter;
import union_qp.com.ua.view.MySurfaceView;


public class ImageViewActivity extends Activity {

    private static final int ACTION_BTN_SIZE = 80;
    public static final String PATH = "PATH";

    private final String[] values = {"1", "2", "3", "4", "5", "6", "7", "8"};

    private ViewGroup parent;
    private MenuItem okMenuBtn, colorSelector, sizeSelector;
    private SubActionButton subBtnEdit, subBtnSelect, subBtnSelectSquare;
    private FloatingActionMenu actionMenu;
    private FloatingActionButton actionButton;

    MySurfaceView svImage;

    private String path;

    int color;
    int size;

    boolean isSelectionMode = false;

    private EditText editableTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        initMySurfaceView();
        parent = (ViewGroup) findViewById(R.id.parent);

        initFloatingBtn();
    }

    private void initMySurfaceView(){
        svImage = (MySurfaceView) findViewById(R.id.svImage);
        svImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                int w = svImage.getWidth();
                int h = svImage.getHeight();

                Log.d("ANT", "w:"+w+", h:"+h);

                path = getIntent().getStringExtra(PATH);
                Bitmap b = getBitmap(path, w, h);
                if (b == null){
                    Log.d("ANT", "bimap == null");
                }
                svImage.setImage(b);

                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    svImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    svImage.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    private Bitmap getBitmap(String path, int w, int h){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        return Bitmap.createScaledBitmap(bitmap, w, h, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        svImage.onPauseMySurfaceView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        svImage.onResumeMySurfaceView();
    }

    private void initFloatingBtn(){
        ImageView icon = new ImageView(this); // Create an icon
        icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_pencil));

        actionButton = new FloatingActionButton.Builder(this)
                .setContentView(icon)
                .build();

        subBtnEdit = getSubActionButton(R.drawable.ic_edittext, "Text", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTextPressed();
            }
        });

        subBtnSelect = getSubActionButton(R.drawable.ic_pencil, "Draw", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDrawPressed();
            }
        });

        subBtnSelectSquare = getSubActionButton(R.drawable.ic_select, "Select", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectPressed();
            }
        });

        actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(subBtnEdit)
                .addSubActionView(subBtnSelect)
                .addSubActionView(subBtnSelectSquare)
                .attachTo(actionButton)
                .build();
    }

    private SubActionButton getSubActionButton(int imageId, String text, View.OnClickListener listener){
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        View v = getMenuItem(imageId, text);
        SubActionButton sub =  itemBuilder.setContentView(v).build();
        sub.setOnClickListener(listener);
        sub.setLayoutParams(new ViewGroup.LayoutParams(App.getPixel(ACTION_BTN_SIZE), App.getPixel(ACTION_BTN_SIZE)));
        return sub;
    }

    private View getMenuItem(int imageId, String text){
        LayoutInflater inflater = getLayoutInflater();
        View parent = inflater.inflate(R.layout.menu_item, null);
        ImageView iv = (ImageView) parent.findViewById(R.id.ivImage);
        iv.setImageDrawable(getResources().getDrawable(imageId));

        TextView tv = (TextView) parent.findViewById(R.id.tvText);
        tv.setText(text);

        return parent;
    }

    private void onSelectPressed(){
        actionMenu.close(true);
        actionButton.setEnabled(false);
        showHideActionBarMenu(true);

        svImage.setMode(MySurfaceView.Mode.SELECT);
    }

    private void onTextPressed(){
        actionMenu.close(true);
        actionButton.setEnabled(false);
        showHideActionBarMenu(true);

        cretaeEditableTextView();
        editableTv.setTextColor(color);
        editableTv.setTextSize(size*2 - 10 + getResources().getDimension(R.dimen.text_size));
    }

    private void onDrawPressed(){
        actionMenu.close(true);
        actionButton.setEnabled(false);
        showHideActionBarMenu(true);

        svImage.setMode(MySurfaceView.Mode.DRAW);
    }

    @Override
    public void onBackPressed() {
        if (!actionButton.isEnabled()){
            if (isSelectionMode){

            } else {
                parent.removeView(editableTv);
            }
            showHideActionBarMenu(false);
            actionButton.setEnabled(true);
            return;
        }

        save();
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    private void showHideActionBarMenu(boolean show){
        okMenuBtn.setVisible(show);
        colorSelector.setVisible(show);
        sizeSelector.setVisible(show);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_view, menu);
        okMenuBtn = menu.findItem(R.id.ok);
        colorSelector = menu.findItem(R.id.colorSelector);
        sizeSelector = menu.findItem(R.id.sizeSelector);

        showHideActionBarMenu(false);

        initColorSlider();
        initSizeSlider();


        return true;
    }

    private void initSizeSlider(){
        Spinner spinner = (Spinner) sizeSelector.getActionView();
        spinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1, android.R.id.text1, values);
        spinner.setAdapter(adapter);
        spinner.setSelection(4);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                size = Integer.parseInt(values[arg2]);
                svImage.setSize(size);
                if (editableTv != null) {
                    editableTv.setTextSize(size*2 - 10 + getResources().getDimension(R.dimen.text_size));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
    }

    private void initColorSlider(){
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
                    if (editableTv != null) {
                        editableTv.setTextColor(color);
                    }
                    svImage.setColor(color);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
            return true;
        }

        if (id == R.id.ok) {
            showHideActionBarMenu(false);
            actionMenu.open(true);
            actionButton.setEnabled(true);

            if (svImage.getMode() == MySurfaceView.Mode.DRAW) {
                svImage.setMode(MySurfaceView.Mode.NONE);
            } else if(svImage.getMode() == MySurfaceView.Mode.SELECT) {
                svImage.setMode(MySurfaceView.Mode.NONE);
            } else{
                //text printed
                svImage.drawText(editableTv);
                parent.removeView(editableTv);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void save(){
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(new File(path));
            fo.write(ImageConverter.bitmapToBite(svImage.getDrawingBitmap()));
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void cretaeEditableTextView() {
        ViewGroup group = (ViewGroup) getLayoutInflater().inflate(R.layout.c_edittext, parent);
        EditText editText = (EditText) group.findViewById(R.id.etEditTExt);
        group.removeView(editText);
        group = null;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        editText.setLayoutParams(params);
        editableTv = editText;
        editableTv.setTextSize(getResources().getDimension(R.dimen.text_size));
        editText.setOnTouchListener(new MyTouchListener());

        parent.addView(editText);
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
