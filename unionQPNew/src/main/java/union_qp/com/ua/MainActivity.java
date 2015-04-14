package union_qp.com.ua;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.TreeSet;

import anton.kizema.libs.HorizontalSliderWidget;
import anton.kizema.libs.HorizontalSliderWidget.OnSliderSelectListener;
import at.markushi.ui.CircleButton;
import union_qp.com.ua.ImageHolder.OnBtnCloseClickListener;
import union_qp.com.ua.util.SimpleBitmapFactory;
import union_qp.com.ua.util.TextViewVisibilityController;
import union_qp.com.ua.util.ZipManager;
import union_qp.com.ua.util.ZipManager.OnSkippedFile;
import union_qp.com.ua.view.CommentWidget;
import union_qp.com.ua.view.DeadlineCommander;
import union_qp.com.ua.view.SendWidget;
import union_qp.com.ua.view.SendWidget.OnFinishedTask;

public class MainActivity extends Activity implements OnFinishedTask, OnSliderSelectListener, OnSkippedFile{

	private static final int SELECT_PHOTO_FROM_GALLERY = 100;
	private static final int REQUEST_PHOTO_FROM_CAMERA = 1;
	private static final int EMAIL_START = 4391;

	private static final int ELEMENTS = 2;
	private static final int SLIDER_HEIGHT = 40;
	
	public static final int NUM_ORDER = 0;
	public static final int NUM_ONLINE = 1;
	
	private File directory;
	private String outputZip = "Apply.zip";

	private GalleryPhotoPicker mGalleryPhotoPicker;
	private CameraPhotoPicker mPhotoPicker;
	private DeadlineCommander mDeadlineCommander;
	private HorizontalSliderWidget slider;
	private String sliderSelectedText;

	private TreeSet<String> pathContainer;
	private CommentWidget mCommentWidget;
	private ZipManager zipManager;
	private Handler handler;
	
	private SendWidget mSendWidget;
	private String phone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		init();
		getPhoneNumber();
	}
	
	private void getPhoneNumber(){
		TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		phone = telemamanger.getLine1Number();
		
		if ( !(phone != null && phone.length() > 0)){
			mCommentWidget.showPhone();
		}
	}

	private void init() {
		mPhotoPicker = new CameraPhotoPicker();
		mGalleryPhotoPicker = new GalleryPhotoPicker();
		pathContainer = new TreeSet<String>();
		mDeadlineCommander = new DeadlineCommander(this, (TextView) findViewById(R.id.tvDeadline));
		
		initSlider();
		initCommentWidget();
		initSendWidget();
		
		zipManager = new ZipManager();
		zipManager.setOnSkippedFile(this);
		handler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				mSendWidget.setProgress(msg.what);
			}
		};
	}
	
	private void initSlider(){
		slider = new HorizontalSliderWidget(this, ELEMENTS, App.getW() - ((int)
                ( 2*getResources().getDimension(R.dimen.activity_horizontal_margin))), App.getPixel(SLIDER_HEIGHT));

		slider.setTextLabels(getResources().getString(R.string.zakaz), getResources().getString(R.string.online));
		slider.setTagsLabels(NUM_ORDER, NUM_ONLINE);
		slider.setElementBackground(R.drawable.back_textview);
		slider.setOnSliderSelectListener(this);
		slider.setSliderBackground(R.drawable.frame);
		
		sliderSelectedText = getResources().getString(R.string.zakaz);
		
		RelativeLayout sliderLayout = (RelativeLayout) findViewById(R.id.slider);
		sliderLayout.addView(slider.getView());
	}
	
	private void initCommentWidget(){
		mCommentWidget = new CommentWidget(this, (RelativeLayout) findViewById(R.id.rlComment),
				(EditText) findViewById(R.id.etComment), (EditText) findViewById(R.id.etName),
				(EditText) findViewById(R.id.etPhone) );
		mCommentWidget.setDuration(slider.getDuration());
	}
	
	private void initSendWidget(){
		mSendWidget = new SendWidget(this, (ProgressBar) findViewById(R.id.pb),
				(TextView) findViewById(R.id.tvSendInfo), (CircleButton) findViewById(R.id.ibSend));
		mSendWidget.setOnFinished(this);
	}

	public void onGalleryButtonClick(View v) {
		mGalleryPhotoPicker.onGalleryPhotoButtonClick();
	}

	public void onPhotoButtonClick(View v) {
		mPhotoPicker.onPhotoButtonClick();
	}
	
	public void onDeadlinePick(View v){
		mDeadlineCommander.setDeadline();
	}
	
	public void onSend(View v){
		send();
	}

    private void enableDisableView(boolean enabled) {
        ScrollView layout = (ScrollView) findViewById(R.id.scParent);
        enableDisableView(layout, enabled);
    }

    private void enableDisableView(View view, boolean enabled) {
        view.setEnabled(enabled);

        if ( view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;

            for ( int idx = 0 ; idx < group.getChildCount() ; idx++ ) {
                enableDisableView(group.getChildAt(idx), enabled);
            }
        }
    }

	private void send(){
		if ( !mCommentWidget.hasName() ){
			Toast.makeText(this, getResources().getString(R.string.enter_name), Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (mCommentWidget.isShowPhone()){
			phone = mCommentWidget.getPhone();
			
			if (phone.length() <= 7){
				Toast.makeText(this, getResources().getString(R.string.proper_phone_needed), Toast.LENGTH_SHORT).show();
				return;
			}
		}
		
		mSendWidget.setMax(pathContainer.size());
		mSendWidget.setProgress(0);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Exception res = zipManager.zip(handler, pathContainer, "" + Environment.getExternalStorageDirectory().getPath() + "/" + outputZip);
			}
		});
		thread.start();
        enableDisableView(false);
	}
	
	@Override
	public void onFinishedTask() {
        enableDisableView(true);
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "union-qp_zakaz@mail.ru" });
		intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.mail_title));
		intent.putExtra(Intent.EXTRA_TEXT, parserOfMessage());
		intent.setType("application/zip");

		if (pathContainer.size() != 0)
			intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Environment
				.getExternalStorageDirectory().getPath() + "/" + outputZip));

		startActivityForResult( Intent.createChooser(intent, "Send mail..."), EMAIL_START);
	}

	public String parserOfMessage()
	{
		StringBuilder ret = new StringBuilder();
		
		ret.append(getResources().getString(R.string.work_type) + " : " + sliderSelectedText+"\n");
		ret.append(getResources().getString(R.string.your_name) + " : " + mCommentWidget.getName()+"\n");
		ret.append(getResources().getString(R.string.your_comment) + " : " + mCommentWidget.getComment()+"\n");
		
		ret.append(getResources().getString(R.string.your_phone) + " : " + phone +"\n");
		ret.append(getResources().getString(R.string.your_deadline) + " : " + mDeadlineCommander.getDeadline()+"\n");
		return ret.toString();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case SELECT_PHOTO_FROM_GALLERY:
			if (resultCode == RESULT_OK) {
				mGalleryPhotoPicker.onActivitySelectedGalleryPhoto(imageReturnedIntent.getData());
			}
			break;
		case REQUEST_PHOTO_FROM_CAMERA:
			if (resultCode == RESULT_OK) {
				mPhotoPicker.onActivitySelectedPhoto();
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, getResources().getString(R.string.no_photo), Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id){
		case R.id.info:
			infoBtnPressed();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class CameraPhotoPicker implements OnBtnCloseClickListener {
		private String localPhotoPath;

		private LinearLayout LayoutGallery;
		private TextViewVisibilityController controller;

		public CameraPhotoPicker() {
			createDirectory();

			LayoutGallery = (LinearLayout) findViewById(R.id.LayoutGalleryPhoto);
			controller = new TextViewVisibilityController(
					(TextView) findViewById(R.id.tvGalleryTextPhoto));
		}

		public void onPhotoButtonClick() {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			Uri uri2 = generateFileUri(REQUEST_PHOTO_FROM_CAMERA);
			localPhotoPath = uri2.getEncodedPath().toString();

			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri2);
			startActivityForResult(intent, REQUEST_PHOTO_FROM_CAMERA);
		}

		public void onActivitySelectedPhoto() {
			Bitmap thumbnail = SimpleBitmapFactory.decodeSampledBitmapFromPath(
					localPhotoPath, getThumbW(), getThumbH());

			if (thumbnail == null) {
				Log.d("TT", "thumbnail == null");
			} else {
				Log.d("TT", "thumbnail != null");
				inflateImage(thumbnail, localPhotoPath);
			}
			// TODO add thumb
		}

		private void inflateImage(Bitmap thumbnail, String path) {
			controller.increase();

			ImageHolder image = new ImageHolder(MainActivity.this, thumbnail,
					path);
			image.setOnBtnCloseClickListener(this);
			LayoutGallery.addView(image.getParent());
			pathContainer.add(path);
		}

		@Override
		public void onBtnClose(RelativeLayout rel) {
			controller.decrease();

			LayoutGallery.removeView(rel);
			pathContainer.remove((String) rel.getTag());
		}

		private Uri generateFileUri(int type) {
			File file = null;
			switch (type) {
			case REQUEST_PHOTO_FROM_CAMERA:
				file = new File(getDirectory().getPath() + "/" + "photo_"
						+ System.currentTimeMillis() + ".jpg");
				break;
			}
			return Uri.fromFile(file);
		}

		private void createDirectory() {
			directory = new File(
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					"Union-QP");
			if (!directory.exists())
				directory.mkdirs();
		}

		private File getDirectory() {
			return directory;
		}
	}

	private class GalleryPhotoPicker implements OnBtnCloseClickListener {
		private LinearLayout LayoutGallery;
		private TextViewVisibilityController controller;

		public GalleryPhotoPicker() {

			LayoutGallery = (LinearLayout) findViewById(R.id.LayoutGallery);
			controller = new TextViewVisibilityController(
					(TextView) findViewById(R.id.tvGalleryText));
		}

		public void onGalleryPhotoButtonClick() {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			Uri uri = Uri.parse(Environment.getExternalStorageDirectory()
					.getPath());

			intent.setDataAndType(uri, "image/*");
			startActivityForResult(
					Intent.createChooser(intent, getResources().getString(R.string.choose_file)),
					SELECT_PHOTO_FROM_GALLERY);
		}

		public void onActivitySelectedGalleryPhoto(Uri selectedImage) {
			String path = getRealPathFromURI(selectedImage);
			Bitmap thumbnail = SimpleBitmapFactory.decodeSampledBitmapFromPath(
					path, getThumbW(), getThumbH());

			Log.d("TT", "path = " + path);

			if (thumbnail == null) {
				Log.d("TT", "thumbnail == null");
			} else {
				Log.d("TT", "thumbnail != null");
			}
			inflateImage(thumbnail, path);
		}

		public String getRealPathFromURI(Uri contentUri) {
			String[] proj = { MediaStore.Images.Media.DATA };

            Log.d("ANT", "contentUri:"+contentUri);

			Cursor cursor = getContentResolver().query(contentUri, proj, null,
					null, null);
            if (cursor == null){
                return "";
            }

			int column_index = cursor
					.getColumnIndex(MediaStore.Images.Media.DATA);
            if (column_index == -1){
                return "null";
            }

			cursor.moveToFirst();
			return cursor.getString(column_index);
		}

		private void inflateImage(Bitmap thumbnail, String path) {
			controller.increase();

			if (thumbnail == null) {
				thumbnail = SimpleBitmapFactory
						.decodeSampledBitmapFromResource(getResources(),
								R.drawable.doc, getThumbW(), getThumbH());
			}

			ImageHolder image = new ImageHolder(MainActivity.this, thumbnail,
					path);
			image.setOnBtnCloseClickListener(this);
			LayoutGallery.addView(image.getParent());
			pathContainer.add(path);
		}

		@Override
		public void onBtnClose(RelativeLayout rel) {
			controller.decrease();

			LayoutGallery.removeView(rel);
			pathContainer.remove((String) rel.getTag());
		}
	}

	private int getThumbW() {
		return App.getPixel(60);
	}

	private int getThumbH() {
		return App.getPixel(60);
	}

	private void shareBtnPressed(){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("image/*"); 
		
		Uri imageUri = Uri.parse("android.resource://+"+getApplicationContext().getPackageName()+"/"+R.drawable.ic_launcher);

		intent.putExtra(Intent.EXTRA_TEXT, "eample");
		intent.putExtra(Intent.EXTRA_TITLE, "example");
		intent.putExtra(Intent.EXTRA_SUBJECT, "example");
		intent.putExtra(Intent.EXTRA_STREAM, imageUri);

		startActivity(intent);
	}
	
	private void infoBtnPressed(){
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setTitle(getResources().getString(R.string.info_title));
		ad.setMessage(getResources().getString(R.string.info_message));
		ad.setPositiveButton(getResources().getString(R.string.info_button), new OnClickListener() {
			public void onClick(DialogInterface dialog, int arg1) {
			}
		});
		ad.show();
	}

	@Override
	public void onSelected(TextView textView) { }

	@Override
	public void onSelectStarted(TextView textView) {
		Log.d("TAG", "textView: "+textView.getTag());
		sliderSelectedText = textView.getText().toString();
		mCommentWidget.selected((Integer) textView.getTag());		
	}

	@Override
	public void onSkip(String fileName) {
		Toast.makeText(this, getResources().getString(R.string.no_file)+": "+fileName, Toast.LENGTH_SHORT).show();
	}
}
