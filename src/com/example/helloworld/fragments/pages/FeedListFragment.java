package com.example.helloworld.fragments.pages;

import java.io.IOException;
import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.helloworld.FeedContentActivity;
import com.example.helloworld.HelloWorldActivity;
import com.example.helloworld.LoginActivity;
import com.example.helloworld.MD5;
import com.example.helloworld.R;
import com.example.helloworld.api.Server;
import com.example.helloworld.api.entity.Article;
import com.example.helloworld.api.entity.Page;
import com.example.helloworld.api.entity.User;
import com.example.helloworld.fragments.widgets.AvatarView;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FeedListFragment extends Fragment {

	View view;
	ListView listView;
	Button btnLoadMore;
	List<Article> articleList;
	int pageNum = 0;
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.fragment_page_feed_list, null);
			View view2 = inflater.inflate(R.layout.activity_load_more, null);
			
			listView = (ListView) view.findViewById(R.id.list);
			btnLoadMore = (Button) view2.findViewById(R.id.load_more);
			
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					onItemClicked(position);
				}
			});	
			listView.addFooterView(view2);
			listView.setAdapter(listAdapter);
		
			btnLoadMore.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					loadMore();
				}
			});
		}

		return view;
	}

	public void onResume() {
		super.onResume();
		getArticleList();
	}

	BaseAdapter listAdapter = new BaseAdapter() {

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;

			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				view = inflater.inflate(R.layout.activity_feed_article_list, null);
			} else {
				view = convertView;
			}
			Article article = articleList.get(position);
			TextView title = (TextView) view.findViewById(R.id.tv_title);
			TextView text = (TextView) view.findViewById(R.id.tv_text);
			TextView createDate = (TextView) view.findViewById(R.id.tv_create_date);
			AvatarView avatar = (AvatarView) view.findViewById(R.id.iv_avatar);
			title.setText(article.getTitle());
			text.setText(article.getText());
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd"); 
			createDate.setText(sdf.format(article.getCreateDate()));
			User user = new User();
			user.setAvatar(article.getAuthorAvatar());
			avatar.load(user);
			return view;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return articleList.get(position);
		}

		@Override
		public int getCount() {
			return articleList == null ? 0 : articleList.size();
		}
	};

	/**
	 * 文章列表的点击事件
	 * 
	 * @param position
	 */
	void onItemClicked(int position) {
		String text = articleList.get(position).getTitle();
		Intent itnt = new Intent(getActivity(), FeedContentActivity.class);
		itnt.putExtra("text", text);
		itnt.putExtra("title", articleList.get(position).getTitle());
		itnt.putExtra("articleId", articleList.get(position).getId());
		startActivity(itnt);
	}

	/**
	 * 获取文章数据
	 */
	public void getArticleList() {
		OkHttpClient client = Server.getSharedClient();
		Request request = Server.requestBuilderWithApi("feeds").build();
		final ProgressDialog dlg = new ProgressDialog(getActivity());
		dlg.setCancelable(false);
		dlg.setCanceledOnTouchOutside(false);
		dlg.setMessage("正在获取数据");
		dlg.show();
		client.newCall(request).enqueue(new Callback() {
			public void onResponse(Call arg0, Response arg1) throws IOException {
				dlg.dismiss();
				String responseString = arg1.body().string();
				Page<Article> page = new ObjectMapper().readValue(responseString,new TypeReference<Page<Article>>(){});
				articleList = (ArrayList<Article>) page.getContent();
				pageNum = page.getNumber();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getActivity(), "当前页数："+pageNum, Toast.LENGTH_LONG).show();
						listAdapter.notifyDataSetInvalidated();
					}
				});
			}

			public void onFailure(Call arg0, final IOException arg1) {
				dlg.dismiss();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getActivity(), "获取信息失败", Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}

	/**
	 * 加载更多
	 */
	public void loadMore(){
		OkHttpClient client = Server.getSharedClient();
		Request request = Server.requestBuilderWithApi("feeds/"+(pageNum+1)).get().build();
		final ProgressDialog dlg = new ProgressDialog(getActivity());
		dlg.setCancelable(false);
		dlg.setCanceledOnTouchOutside(false);
		dlg.setMessage("正在获取数据");
		dlg.show();
		client.newCall(request).enqueue(new Callback() {
			public void onResponse(Call arg0, Response arg1) throws IOException {
				dlg.dismiss();
				String responseString = arg1.body().string();
				Page<Article> page = new ObjectMapper().readValue(responseString,new TypeReference<Page<Article>>(){});
				if(articleList == null){
					articleList = page.getContent();
				}else{
					articleList.addAll(page.getContent());
				}
				pageNum = page.getNumber();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getActivity(), "当前页数："+pageNum, Toast.LENGTH_LONG).show();
						listAdapter.notifyDataSetInvalidated();
					}
				});
			}

			public void onFailure(Call arg0, final IOException arg1) {
				dlg.dismiss();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getActivity(), "获取信息失败", Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}
}
