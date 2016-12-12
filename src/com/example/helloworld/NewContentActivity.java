package com.example.helloworld;

import java.io.IOException;

import com.example.helloworld.api.Server;
import com.example.helloworld.fragments.inputcells.SimpleTextInputCellFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewContentActivity extends Activity {
	
	SimpleTextInputCellFragment fragTile = new SimpleTextInputCellFragment();
	EditText contentText ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_content);
		
		fragTile = (SimpleTextInputCellFragment) getFragmentManager().findFragmentById(R.id.frag_title);
		contentText = (EditText) findViewById(R.id.text);
		findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String titleText = fragTile.getText();
				String content = contentText.getText().toString();
				
				OkHttpClient okClient = Server.getSharedClient();
				
				MultipartBody requestBody = new MultipartBody.Builder()
						.addFormDataPart("title", titleText)
						.addFormDataPart("text", content)
						.build();
				
				Request request = Server.requestBuilderWithApi("publisharticle")
						.method("post", null)
						.post(requestBody)
						.build();
				
				final ProgressDialog dlg = new ProgressDialog(NewContentActivity.this);
				dlg.setCancelable(false);
				dlg.setCanceledOnTouchOutside(false);
				dlg.setMessage("正在发表");
				dlg.show();

				okClient.newCall(request).enqueue(new Callback() {
					
					@Override
					public void onResponse(Call arg0, Response arg1) throws IOException {
						runOnUiThread(new Runnable() {
							public void run() {
								dlg.dismiss();
								new AlertDialog.Builder(NewContentActivity.this)
								.setMessage("发表成功")
//								.setMessage(responseString)
								.setPositiveButton("OK", new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog, int which) {
										finish();
										overridePendingTransition(R.anim.none, R.anim.slide_out_bottom);
									}	
								})
								.show();
							}
						});
					}
					
					@Override
					public void onFailure(Call arg0, IOException arg1) {
						runOnUiThread(new Runnable() {
							public void run() {
								dlg.dismiss();
								new AlertDialog.Builder(NewContentActivity.this)
								.setMessage("发表失败")
//								.setMessage(responseString)
								.setPositiveButton("OK", new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog, int which) {
										finish();
										overridePendingTransition(R.anim.none, R.anim.slide_out_bottom);
									}	
								})
								.show();
							}
						});
					}
				});
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		fragTile.setLabelText("标题");
		fragTile.setHintText("标题内容");
		fragTile.setIsPassword(false);
	}
}
