package union_qp.com.ua.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;

import union_qp.com.ua.App;
import union_qp.com.ua.R;


public class MySurfaceView extends SurfaceView implements Runnable {

    Thread thread = null;
    SurfaceHolder surfaceHolder;
    volatile boolean running = false;

    private Bitmap btm;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Mode mode = Mode.NONE;
    private Canvas bitCanvas;
    int color;

    private EditText et;
    private boolean drawText = false, actionUp = false;

    volatile boolean touched = false;
    volatile float touched_x, touched_y, down_touched_x, down_touched_y;

    public static enum Mode{
        NONE, DRAW, SELECT;
    }

    public MySurfaceView(Context context) {
        super(context);
        init();
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void onResumeMySurfaceView() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    private void init(){
        surfaceHolder = getHolder();

        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(getResources().getDimension(R.dimen.text_size));
        paint.setStrokeWidth(App.getPixel(2));
        paint.setColor(color);
    }

    public void onPauseMySurfaceView() {
        boolean retry = true;
        running = false;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setImage(Bitmap btm){
        Bitmap workingBitmap = Bitmap.createBitmap(btm);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        this.btm = mutableBitmap;
        bitCanvas = new Canvas(mutableBitmap);
    }

    public Bitmap getDrawingBitmap(){
        return btm;
    }

    public void setColor(int c){
        this.color = c;
        paint.setColor(color);
    }

    public void setMode(Mode mode){
        this.mode = mode;
    }

    public void setSize(int size){
        paint.setTextSize(size*2 - 10 + getResources().getDimension(R.dimen.text_size));
        paint.setStrokeWidth(size);
    }

    public Mode getMode(){
        return mode;
    }

    public void drawText(EditText et){
        drawText = true;
        this.et = et;
    }

    @Override
    public void run() {
        while (running) {
            if (btm == null) {
                continue;
            }

            if (surfaceHolder.getSurface().isValid()) {
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawBitmap(btm, 0, 0, null);

                if (touched) {

                    if (mode == Mode.DRAW) {
                        bitCanvas.drawLine(down_touched_x, down_touched_y, touched_x, touched_y, paint);
                        down_touched_x = touched_x;
                        down_touched_y = touched_y;
                    }

                    if (mode == Mode.SELECT){

                        Canvas c = canvas;

                        if (actionUp){
                            c = bitCanvas;
                            actionUp = false;
                        }
                        c.drawPoint(down_touched_x, down_touched_y, paint);

                        c.drawLine(down_touched_x, down_touched_y, down_touched_x, touched_y, paint);
                        c.drawLine(down_touched_x, down_touched_y, touched_x, down_touched_y, paint);
                        c.drawLine(touched_x, down_touched_y, touched_x, touched_y, paint);
                        c.drawLine(down_touched_x, touched_y, touched_x, touched_y, paint);
                    }
                }

                if (drawText){
                    float s = paint.getStrokeWidth();
                    paint.setStrokeWidth(App.getPixel(2));
                    bitCanvas.drawText(et.getText().toString(), et.getX(), et.getY(), paint);
                    drawText = false;
                    paint.setStrokeWidth(s);
                }

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {



        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                actionUp = false;
                down_touched_x = event.getX();
                down_touched_y = event.getY();

                touched = false;
                break;
            case MotionEvent.ACTION_MOVE:
                touched_x = event.getX();
                touched_y = event.getY();

                touched = true;
                break;
            case MotionEvent.ACTION_UP:
                touched_x = event.getX();
                touched_y = event.getY();

                actionUp = true;

                touched = true;
                break;
            case MotionEvent.ACTION_CANCEL:
                touched = false;
                break;
            case MotionEvent.ACTION_OUTSIDE:
                touched = false;
                break;
            default:
        }
        return true;
    }

}