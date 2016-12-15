package com.example.helloworld;

import java.io.IOException;

import com.example.helloworld.api.Server;
import com.example.helloworld.fragments.TitleFragment;
import com.example.helloworld.fragments.TitleFragment.OnGoBackListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
	TextView tvLike;

	boolean isLike = false;
	int countlikes = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_content_msg);
		fragTitle = (TitleFragment) getFragmentManager().findFragmentById(R.id.frag_title);
		etContent = (EditText) findViewById(R.id.et_content);
		btnComment = (Button) findViewById(R.id.btn_comment_publish);
		tvTitle = (TextView) findViewById(R.id.tv_article_title);
		tvText = (TextView) findViewById(R.id.tv_article_text);
		tvLookComment = (TextView) findViewById(R.id.tv_lookcomments);
		tvLike = (TextView) findViewById(R.id.tv_likes);

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

		tvLike.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				likes();
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		fragTitle.setTitleText("文章详情");
		tvTitle.setText(intent.getStringExtra("title"));
		tvText.setText(intent.getStringExtra("text"));
		checkLikes();
		countLikes();
	}

	/**
	 * 发表评论
	 */
	public void postComment() {
		OkHttpClient client = Server.getSharedClient();

		MultipartBody requestBody = new MultipartBody.Builder()
				.addFormDataPart("content", etContent.getText().toString()).build();

		Request request = Server.requestBuilderWithApi("article/" + intent.getIntExtra("articleId", 0) + "/comments")
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
		Intent itnt = new Intent(FeedContentActivity.this, CommentsActivity.class);
		itnt.putExtra("articleId", intent.getIntExtra("articleId", 0));
		startActivity(itnt);
	}

	/**
	 * 点赞
	 */
	public void likes() {
		OkHttpClient client = Server.getSharedClient();

		MultipartBody requestBody = new MultipartBody.Builder().addFormDataPart("isLike", String.valueOf(!isLike))
				.build();

		Request request = Server.requestBuilderWithApi("article/likes/" + intent.getIntExtra("articleId", 0))
				.post(requestBody).build();

		client.newCall(request).enqueue(new Callback() {
			public void onResponse(Call arg0, Response arg1) throws IOException {
				String rs = arg1.body().string();
				final int res = Integer.valueOf(rs);
				runOnUiThread(new Runnable() {
					public void run() {
						if (res == 0) {
							isLike = false;
							tvLike.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
							countLikes();
							Toast.makeText(FeedContentActivity.this, "取消点赞", Toast.LENGTH_SHORT).show();
						} else {
							isLike = true;
							tvLike.setTextColor(getResources().getColor(android.R.color.darker_gray));
							countLikes();
							Toast.makeText(FeedContentActivity.this, "点赞", Toast.LENGTH_SHORT).show();
						}

					}
				});
			}

			public void onFailure(Call arg0, IOException arg1) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(FeedContentActivity.this, "点赞失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	/**
	 * 检查是否点赞
	 */
	public void checkLikes() {
		OkHttpClient client = Server.getSharedClient();

		Request request = Server
				.requestBuilderWithApi("/article/" + intent.getIntExtra("articleId", 0) + "/like/checklike").build();
		client.newCall(request).enqueue(new Callback() {
			public void onResponse(Call arg0, Response arg1) throws IOException {
				String rs = arg1.body().string();
				boolean islike = Boolean.valueOf(rs);
				if (islike) {
					runOnUiThread(new Runnable() {
						public void run() {
							isLike = true;
							tvLike.setTextColor(getResources().getColor(android.R.color.darker_gray));
						}
					});
				} else {
					runOnUiThread(new Runnable() {
						public void run() {
							isLike = false;
							tvLike.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
						}
					});
				}
			}

			@Override
			public void onFailure(Call arg0, IOException arg1) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(FeedContentActivity.this, "点赞检查出错", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	/**
	 * 统计点赞人数
	 * @return
	 */
	public void countLikes() {
		OkHttpClient client = Server.getSharedClient();

		Request request = Server.requestBuilderWithApi("article/like/count/" + intent.getIntExtra("articleId", 0))
				.build();
		client.newCall(request).enqueue(new Callback() {
			public void onResponse(Call arg0, Response arg1) throws IOException {
				String rs = arg1.body().string();
				final int res = Integer.valueOf(rs);
				runOnUiThread(new Runnable() {
					public void run() {
						tvLike.setText("点赞"+res);
					}
				});
			}

			@Override
			public void onFailure(Call arg0, IOException arg1) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(FeedContentActivity.this, "点赞数出错", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

	}
}
