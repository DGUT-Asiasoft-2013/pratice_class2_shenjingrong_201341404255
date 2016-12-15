package com.example.helloworld;

import java.io.IOException;
import java.util.List;

import com.example.helloworld.api.Server;
import com.example.helloworld.api.entity.Comment;
import com.example.helloworld.api.entity.Page;
import com.example.helloworld.api.entity.User;
import com.example.helloworld.fragments.TitleFragment;
import com.example.helloworld.fragments.TitleFragment.OnGoBackListener;
import com.example.helloworld.fragments.widgets.AvatarView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

public class CommentNoticeActivity extends Activity{

	TitleFragment fragTitle = new TitleFragment();
	ListView lvCommentNotice ;
	Button btnLoadMore;
	
	List<Comment> commentList ;
	User user;
	int pageNum=0;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_comment_notice);
		
		user = (User) getIntent().getSerializableExtra("user");
		LayoutInflater inflater = LayoutInflater.from(CommentNoticeActivity.this);
		View view2 = inflater.inflate(R.layout.activity_load_more, null);
		btnLoadMore = (Button) view2.findViewById(R.id.load_more);
		
		fragTitle = (TitleFragment) getFragmentManager().findFragmentById(R.id.frag_comment_notice);
		lvCommentNotice = (ListView) findViewById(R.id.lv_comment_list);
		lvCommentNotice.addFooterView(view2);
		lvCommentNotice.setAdapter(listAdapter);
		
		OnGoBackListener backListener = new OnGoBackListener() {
			public void goBack() {
				finish();
			}
		};
		fragTitle.setOnGoBackListener(backListener);
		btnLoadMore.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				loadMore();
			}
		});
		getCommentNotice();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		fragTitle.setTitleText("评论通知");
	}
	
	/**
	 * 获取评论消息
	 */
	public void getCommentNotice(){
		OkHttpClient client = Server.getSharedClient();
		Request request = Server.requestBuilderWithApi("/comment/"+user.getId()+"/"+pageNum)
				.build();
		final ProgressDialog dialog = new ProgressDialog(CommentNoticeActivity.this);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setMessage("获取数据中");
		dialog.show();
		client.newCall(request).enqueue(new Callback() {
			public void onResponse(Call arg0, Response arg1) throws IOException {
				dialog.dismiss();
				String res = arg1.body().string();
				Page<Comment> page = new ObjectMapper().readValue(res, new TypeReference<Page<Comment>>(){});
				if(commentList==null){
					commentList = page.getContent();
				}else{
					commentList.addAll(page.getContent());
				}
				pageNum =1 + page.getNumber();
				
				runOnUiThread(new Runnable() {
					public void run() {
						
						Toast.makeText(CommentNoticeActivity.this, "成功", Toast.LENGTH_SHORT).show();
						listAdapter.notifyDataSetInvalidated();
					}
				});
			}
			
			public void onFailure(Call arg0, IOException arg1) {
				dialog.dismiss();
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(CommentNoticeActivity.this, "联网失败，请检查网络", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	public void loadMore(){
		getCommentNotice();
	}
	
	BaseAdapter listAdapter = new BaseAdapter() {
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			if(convertView == null){
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				view = inflater.inflate(R.layout.fragment_comment_notice, null);
			}else{
				view = convertView;
			}
			AvatarView av = (AvatarView) view.findViewById(R.id.av_comment_person_face);
			TextView tvPersonName = (TextView) view.findViewById(R.id.tv_article_comment_person_name);
			TextView tvCommentTime = (TextView) view.findViewById(R.id.tv_article_comment_time);
			TextView tvContent = (TextView) view.findViewById(R.id.tv_article_comment_content);
			TextView tvArticleName = (TextView) view.findViewById(R.id.tv_comment_article_name);
			
			Comment comment = commentList.get(position);
			av.load(comment.getAuthor());
			tvPersonName.setText(comment.getAuthor().getName());
			tvContent.setText(comment.getContent());
			tvArticleName.setText(comment.getArticle().getTitle());
			
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
			return commentList.get(position);
		}
		
		@Override
		public int getCount() {
			return commentList == null? 0 : commentList.size();
		}
	};
}
