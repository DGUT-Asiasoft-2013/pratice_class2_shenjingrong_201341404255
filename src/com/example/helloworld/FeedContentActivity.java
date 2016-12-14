package com.example.helloworld;

import java.io.IOException;

import com.example.helloworld.api.Server;
import com.example.helloworld.fragments.TitleFragment;
import com.example.helloworld.fragments.TitleFragment.OnGoBackListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FeedContentActivity extends Activity {

	TitleFragment fragTitle = new TitleFragment();
	OnGoBackListener onGoBackListener;

	EditText etContent;
	Button btnComment;
	Intent intent;
	TextView tvTitle;
	TextView tvText;
	TextView tvLookComment;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_content_msg);
		fragTitle = (TitleFragment) getFragmentManager().findFragmentById(R.id.frag_title);
		etContent = (EditText) findViewById(R.id.et_content);
		btnComment = (Button) findViewById(R.id.btn_comment_publish);
		tvTitle = (TextView) findViewById(R.id.tv_article_title);
		tvText = (TextView) findViewById(R.id.tv_article_text);
		tvLookComment = (TextView) findViewById(R.id.tv_lookcomments);

		intent = getIntent();
		onGoBackListener = new OnGoBackListener() {
			public void goBack() {
				finish();
			}
		};
		fragTitle.setOnGoBackListener(onGoBackListener);

		btnComment.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				postComment();
			}
		});

		tvLookComment.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				lookComments();
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		fragTitle.setTitleText("文章详情");
		tvTitle.setText(intent.getStringExtra("title"));
		tvText.setText(intent.getStringExtra("text"));
	}

	/**
	 * 发表评论
	 */
	public void postComment() {
		OkHttpClient client = Server.getSharedClient();

		MultipartBody requestBody = new MultipartBody.Builder()
				.addFormDataPart("content", etContent.getText().toString()).build();

		Request request = Server.requestBuilderWithApi("/article/" + intent.getIntExtra("articleId", 0) + "/comments")
				.method("post", null).post(requestBody).build();

		final ProgressDialog dlg = new ProgressDialog(this);
		dlg.setCancelable(false);
		dlg.setCanceledOnTouchOutside(false);
		dlg.setMessage("正在发表");
		dlg.show();

		client.newCall(request).enqueue(new Callback() {
			public void onResponse(Call arg0, Response arg1) throws IOException {
				dlg.dismiss();
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(FeedContentActivity.this, "发表成功", Toast.LENGTH_LONG).show();
					}
				});

			}

			@Override
			public void onFailure(Call arg0, IOException arg1) {
				dlg.dismiss();
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(FeedContentActivity.this, "发表失败", Toast.LENGTH_LONG).show();
					}
				});

			}
		});
	}

	/**
	 * 查看评论
	 */
	public void lookComments() {
		Intent itnt = new Intent(FeedContentActivity.this,CommentsActivity.class);
		itnt.putExtra("articleId", intent.getIntExtra("articleId", 0));
		startActivity(itnt);
	}
}
