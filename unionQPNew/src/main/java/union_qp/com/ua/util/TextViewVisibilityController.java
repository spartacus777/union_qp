package union_qp.com.ua.util;

import android.view.View;
import android.widget.TextView;

public class TextViewVisibilityController {
	private int counter = 0;
	private TextView view;
	
	public TextViewVisibilityController(TextView view){
		this.view = view;
		view.setVisibility(View.VISIBLE);
	}
	
	public void increase(){
		if (++counter == 1){
			view.setVisibility(View.INVISIBLE);
		}
	}
	
	public void decrease() {
		if (--counter == 0){
			view.setVisibility(View.VISIBLE);
		}
	}

}
