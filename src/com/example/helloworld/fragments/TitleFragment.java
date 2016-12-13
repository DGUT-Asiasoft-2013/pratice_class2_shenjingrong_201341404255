package com.example.helloworld.fragments;

import com.example.helloworld.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class TitleFragment extends Fragment{
	
	View view;
	Button btnEdit;
	TextView tvTitletext;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(view ==null){
			view = inflater.inflate(R.layout.fragment_title, null);
			btnEdit = (Button) view.findViewById(R.id.btn_edit);
			tvTitletext = (TextView) view.findViewById(R.id.frag_title_text);
			btnEdit.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					back();
				}
			});
		}
		
		return view;
	}

	public void setTitleText(String text){
		tvTitletext.setText(text);
	}
	
	
	public static interface OnGoBackListener{
		void goBack();
	}
	
	OnGoBackListener onGoBackListener;
	
	void setOnGoBackListener(OnGoBackListener onGoBackListener){
		this.onGoBackListener = onGoBackListener;
	}
	
	void back(){
		if(onGoBackListener != null){
			onGoBackListener.goBack();
		}
	}
}
