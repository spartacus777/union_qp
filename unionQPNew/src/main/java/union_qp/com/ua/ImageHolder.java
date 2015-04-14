package union_qp.com.ua;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import at.markushi.ui.CircleButton;

public class ImageHolder {
	private ImageView image;
	private CircleButton btnClose, ibShow;
	private RelativeLayout parent;
    private String path;
	private OnBtnCloseClickListener listener;
	
	public interface OnBtnCloseClickListener{
		public void onBtnClose(RelativeLayout rel);
	}
	
	public ImageHolder(final Context context, Bitmap thumbnail, final String path){
		LayoutInflater inflater = (LayoutInflater)context.getSystemService
			      (Context.LAYOUT_INFLATER_SERVICE);

        this.path = path;

		parent = (RelativeLayout) inflater.inflate(R.layout.customimageview, null);
		parent.setTag(path);
		image = (ImageView) parent.findViewById(R.id.ivImage);
		image.setImageBitmap(thumbnail);
		image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(context, path, Toast.LENGTH_SHORT).show();
			}
		});
		
		btnClose = (CircleButton) parent.findViewById(R.id.ibClose);
		btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getOnBtnCloseClickListener().onBtnClose(getParent());
			}
		});

        ibShow = (CircleButton) parent.findViewById(R.id.ibShow);
        ibShow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(parent.getContext(), ImageViewActivity.class);
                intent.putExtra(ImageViewActivity.PATH, path);

                parent.getContext().startActivity(intent);
            }
        });
	}
	
	public RelativeLayout getParent(){
		return parent;
	}
	
	public void setOnBtnCloseClickListener(OnBtnCloseClickListener listener){
		this.listener = listener;
	}
	
	public OnBtnCloseClickListener getOnBtnCloseClickListener(){
		return listener;
	}
}
