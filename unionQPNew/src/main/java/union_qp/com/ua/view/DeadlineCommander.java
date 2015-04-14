package union_qp.com.ua.view;

import java.util.Calendar;

import union_qp.com.ua.R;
import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

public class DeadlineCommander {

	TextView view;
	Context context;
	String deadline;
	
	public DeadlineCommander(Context context, TextView view){
		this.view = view;
		this.context = context;
	}
	
	public void setDeadline() {
		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog dpd = new DatePickerDialog(context,
				new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						if (checkData(year, monthOfYear, dayOfMonth)){
							setData(year, monthOfYear, dayOfMonth);
						}else {
							Toast.makeText(context, context.getResources().getString(R.string.past_date), Toast.LENGTH_SHORT).show();
						}
					}
				}, mYear, mMonth, mDay);
		
		dpd.show();
	}
	
	private void setData(int year, int monthOfYear, int dayOfMonth){
		deadline = norm(dayOfMonth)+"."+norm(monthOfYear)+"."+year;
		view.setText(context.getResources().getString(R.string.deadline_word)+" : "+deadline);
	}
	
	private boolean checkData(int year, int monthOfYear, int dayOfMonth){
		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH);
		
		if (mYear > year) return false;
		if (mYear < year) return true;
		
		if (mMonth > monthOfYear) return false;
		if (mMonth < monthOfYear) return true;
		
		if (mDay > dayOfMonth) return false;
		if (mDay < dayOfMonth) return true;
		
		return true;
	}
	
	private String norm(int inp){
		if (inp < 10)
			return "0"+inp;
		
		return ""+inp;
	}
	
	public String getDeadline(){
		return deadline;
	}
}
