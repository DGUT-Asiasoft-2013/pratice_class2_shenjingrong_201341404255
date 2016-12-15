package com.example.helloworld;

import java.io.IOException;

import java.util.List;


import com.example.helloworld.api.Server;
import com.example.helloworld.fragments.TitleFragment;
import com.example.helloworld.fragments.TitleFragment.OnGoBackListener;
import com.example.helloworld.fragments.widgets.AvatarView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.helloworld.api.entity.Article;
import com.example.helloworld.api.entity.Comment;
import com.example.helloworld.api.entity.Page;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommentsActivity extends Activity {
	
	TitleFragment fragTitle = new TitleFragment();
	ListView listView;
	List<Comment> commentList;
	int commentPage = 0;
	Button btnLoadMore;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_article_comments);
		LayoutInflater inflater = LayoutInflater.from(CommentsActivity.this);
		View view2 = inflater.inflate(R.layout.activity_load_more, null);
		
		btnLoadMore = (Button) view2.findViewById(R.id.load_more);
		fragTitle = (TitleFragment) getFragmentManager().findFragmentById(R.id.frag_article_comments);
		listView = (ListView) findViewById(R.id.lv_article_comments_list);
		
		OnGoBackListener goBackListener = new OnGoBackListener() {
			public void goBack() {
				finish();
			}
		};
		btnLoadMore.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				loadMore();
			}
		});
		fragTitle.setOnGoBackListener(goBackListener);
		listView.addFooterView(view2);
		listView.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		fragTitle.setTitleText("评论区");
		loadComments();
	}
	
	BaseAdapter adapter = new BaseAdapter() {
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			if(convertView == null){
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				view = inflater.inflate(R.layout.activity_feed_article_comments_list, null);
			}else{
				view = convertView;
			}
			AvatarView av = (AvatarView) view.findViewById(R.id.av_comment_person_face);
			TextView tvPersonName = (TextView) view.findViewById(R.id.tv_article_comment_person_name);
			TextView tvCommentTime = (TextView) view.findViewById(R.id.tv_article_comment_time);
			TextView tvContent = (TextView) view.findViewById(R.id.tv_article_comment_content);
			
			Comment comment = commentList.get(position);
			av.load(comment.getAuthor());
			tvPersonName.setText(comment.getAuthor().getName());
			tvContent.setText(comment.getContent());
			
			String dateStr = DateFormat.format("yyyy-MM-dd hh:mm", comment.getCreateDate()).toString();
			tvCommentTime.setText(dateStr);
			
			return view;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public Object getItem(int position) {
			return commentList ==null? null: commentList.get(position);
		}
		
		@Override
		public int getCount() {
			return commentList == null? 0 : commentList.size();
		}
	};
	
	/**
	 * 加载评论内容
	 */
	public void loadComments(){
		OkHttpClient client = Server.getSharedClient();
		
		Request request = Server.requestBuilderWithApi("article/"+getIntent().getIntExtra("articleId",0)+"/comments")
				.get()
				.build();
		final ProgressDialog dlg = new ProgressDialog(CommentsActivity.this);
		dlg.setCancelable(false);
		dlg.setCanceledOnTouchOutside(false);
		dlg.setMessage("正在获取数据");
		dlg.show();
		client.newCall(request).enqueue(new Callback() {
			public void onResponse(Call arg0, Response arg1) throws IOException {
				String result = arg1.body().string();
				ObjectMapper mapper = new ObjectMapper();
				Page<Comment> page = mapper.readValue(result, new TypeReference<Page<Comment>>(){});
				commentList = page.getContent();
				commentPage = page.getNumber();
				runOnUiThread(new Runnable() {
					public void run() {
						dlg.dismiss();
						adapter.notifyDataSetInvalidated();
					}
				});
				
			}
			public void onFailure(Call arg0, IOException arg1) {
				
				runOnUiThread(new Runnable() {
					public void run() {
						dlg.dismiss();
						Toast.makeText(CommentsActivity.this, "获取评论失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	/**
	 * 获取更多用户评论
	 */
	void loadMore(){
		OkHttpClient client = Server.getSharedClient();
		Request request = Server.requestBuilderWithApi("article/"+getIntent().getIntExtra("articleId",0)+"/comments/"+(commentPage+1)).build();
		final ProgressDialog dlg = new ProgressDialog(this);
		dlg.setCancelable(false);
		dlg.setCanceledOnTouchOutside(false);
		dlg.setMessage("正在获取数据");
		dlg.show();
		client.newCall(request).enqueue(new Callback() {
			public void onResponse(Call arg0, Response arg1) throws IOException {
				dlg.dismiss();
				String responseString = arg1.body().string();
				Page<Comment> page = new ObjectMapper().readValue(responseString,new TypeReference<Page<Comment>>(){});
				if(commentList == null){
					commentList = page.getContent();
				}else{
					commentList.addAll(page.getContent());
				}
				commentPage = page.getNumber();
				runOnUiThread(new Runnable() {
					public void run() {
						adapter.notifyDataSetInvalidated();
					}
				});
			}

			public void onFailure(Call arg0, final IOException arg1) {
				dlg.dismiss();
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(CommentsActivity.this, "获取信息失败", Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}
}
