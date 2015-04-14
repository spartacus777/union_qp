package union_qp.com.ua.view;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import union_qp.com.ua.R;


public class ColorSpinnerAdapter extends ArrayAdapter<String> {

    public int[] colors={Color.RED,Color.BLACK,Color.WHITE,Color.BLUE,Color.GREEN};
    private LayoutInflater inf;

    public ColorSpinnerAdapter(Context context) {
        super(context, R.layout.spinner, new ArrayList<String>());

        inf = LayoutInflater.from(context);
    }

    @Override
    public String getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return colors.length;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inf.inflate(R.layout.spinner, parent, false);
        }

        ImageView view = (ImageView) convertView.findViewById(android.R.id.text1);

        view.setBackgroundColor(colors[position]);

        return convertView;
    }
}