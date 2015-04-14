package union_qp.com.ua.view;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import at.markushi.ui.CircleButton;

public class SendWidget {
	
	private ProgressBar pbDownload;
	private TextView info;
	private CircleButton button;
	
	private boolean isInProgress = false;
	private Context context;
	
	private OnFinishedTask listener;
	
	public interface OnFinishedTask{
		public void onFinishedTask();
	}

	public SendWidget(Context context, ProgressBar pbDownload, TextView info, CircleButton button){
		this.context = context;
		this.pbDownload = pbDownload;
		this.info = info;
		this.button = button;
	}
	
	
	public void setProgress(int progress){
		if (progress == 0){
			button.setEnabled(false);
			isInProgress = true;
			info.setVisibility(View.INVISIBLE);
			pbDownload.setVisibility(View.VISIBLE);
			
			if (pbDownload.getMax() == 0){
				finishTask();
			}
			return;
		}
		
		if (progress == pbDownload.getMax()){
			button.setEnabled(true);
			info.setVisibility(View.VISIBLE);
			pbDownload.setVisibility(View.INVISIBLE);
			
			finishTask();
			return;
		}
		
		pbDownload.setProgress(progress);
	}
	
	private void finishTask(){
		isInProgress = false;
		if (getOnFinished() != null)
			getOnFinished().onFinishedTask();
	}
	
	public boolean isInProgress(){
		return isInProgress;
	}
	
	public void setMax(int max){
		pbDownload.setMax(max);
	}
	
	public void setOnFinished(OnFinishedTask listener){
		this.listener = listener;
	}
	
	public OnFinishedTask getOnFinished(){
		return listener;
	}
	
}
