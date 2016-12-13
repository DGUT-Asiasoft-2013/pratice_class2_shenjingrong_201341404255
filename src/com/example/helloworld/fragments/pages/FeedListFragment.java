package com.example.helloworld.fragments.pages;

import java.io.IOException;
import java.sql.ParameterMetaData;
import java.sql.SQLException;
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
import com.example.helloworld.api.entity.User;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
	
	ArrayList<Article> articleList;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (view==null){
			view = inflater.inflate(R.layout.fragment_page_feed_list, null);
			
			listView = (ListView) view.findViewById(R.id.list);
			
			
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					onItemClicked(position);
				}
			});
			listView.setAdapter(listAdapter);
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
			
			if(convertView==null){
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				view = inflater.inflate(android.R.layout.simple_list_item_1, null);	
			}else{
				view = convertView;
			}
			Article article = articleList.get(position);
			TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			text1.setText(article.getTitle());
			
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
			return articleList==null ? 0 : articleList.size();
		}
	};
	/**
	 * 文章列表的点击事件
	 * @param position
	 */
	void onItemClicked(int position){
		String text = articleList.get(position).getTitle();
		
		Intent itnt = new Intent(getActivity(), FeedContentActivity.class);
		itnt.putExtra("text", text);
		
		startActivity(itnt);
	}
	/**
	 * 获取文章数据
	 */
	public void getArticleList(){
		OkHttpClient client = Server.getSharedClient();

//		MultipartBody requestBody = new MultipartBody.Builder()
//				.addFormDataPart("userId", 1+"")
//				.build();

		Request request = Server.requestBuilderWithApi("feeds")
				.build();

		final ProgressDialog dlg = new ProgressDialog(getActivity());
		dlg.setCancelable(false);
		dlg.setCanceledOnTouchOutside(false);
		dlg.setMessage("正在获取数据");
		dlg.show();

		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onResponse(Call arg0, Response arg1) throws IOException {
				dlg.dismiss();
				final String responseString = arg1.body().string();
				try {
					JSONObject jsonObject = new JSONObject(responseString);
					final String contentObject = jsonObject.getString("content");
					
					ObjectMapper mapper = new ObjectMapper();
					articleList = mapper.readValue(contentObject, new TypeReference<ArrayList<Article>>(){});
					
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getActivity(), articleList.size()+"", Toast.LENGTH_LONG).show();
							listAdapter.notifyDataSetChanged();
						}
					});
		
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				
			}

			@Override
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
