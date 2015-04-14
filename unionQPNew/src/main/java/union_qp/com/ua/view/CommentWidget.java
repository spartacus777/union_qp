package union_qp.com.ua.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.widget.EditText;
import android.widget.RelativeLayout;

import union_qp.com.ua.App;
import union_qp.com.ua.MainActivity;
import union_qp.com.ua.R;

public class CommentWidget {

	private Context context;
	private RelativeLayout parent;
	private EditText comment;
	private EditText name;
	private EditText etPhone;
	private boolean isVisible = false;
	
	private ObjectAnimator animIn;
	private ObjectAnimator animOut;
	private int duration = 600;
	
	private int number;
	
	public CommentWidget(Context context, RelativeLayout parent, EditText comment, EditText name, EditText etPhone){
		this.context = context;
		this.parent = parent;
		this.comment = comment;
		this.name = name;
		this.etPhone = etPhone;

		initAnim();
	}
	
	public void showPhone(){
		isVisible = true;
	}
	
	public boolean isShowPhone(){
		return isVisible;
	}
	
	private void initAnim(){
		animIn = animOut = ObjectAnimator.ofFloat(parent, "x", 0);
		animIn.setDuration(duration/2);
		
		animOut = ObjectAnimator.ofFloat(parent, "x", - App.getW());
		animOut.setDuration(duration/2);
        animOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                comment.setText("");
                etPhone.setText("");
                name.setText("");

                switch (number) {
                    case MainActivity.NUM_ORDER:
                        comment.setHint(R.string.comment);
                        break;
                    case MainActivity.NUM_ONLINE:
                        comment.setHint(R.string.comment_to_online);
                        break;
                }
                parent.setX(App.getW());
                animIn.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
	}
	
	public boolean hasName(){
		if (name.getText().toString().length() >= 2)
			return true;
		
		return false;
	}
	
	public String getName(){
		return name.getText().toString();
	}
	
	public String getComment(){
		return comment.getText().toString();
	}
	
	public String getPhone(){
		return etPhone.getText().toString();
	}

	public void selected(int num){
		number = num;
        animOut.start();
	}
	
	
	public void setDuration(int duration){
		this.duration = duration;
	}
}
