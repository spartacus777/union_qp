package union_qp.com.ua;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class App extends Application{

	private static int height;
	private static int width;
	private static DisplayMetrics metrics;
	
	@Override
	public void onCreate(){
		init();
	}
	
	private void init(){
		height = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
		width = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		
		metrics = getResources().getDisplayMetrics();
	}
	
	public static int getW() {
		return width;
	}

	public static int getH() {
		return height;
	}
	
	public static int getPixel(int dpi){
		return (int)(metrics.density * dpi);
	}

}
