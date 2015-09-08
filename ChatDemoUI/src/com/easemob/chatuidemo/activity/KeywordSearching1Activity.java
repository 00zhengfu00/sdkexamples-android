package com.easemob.chatuidemo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMKeywordSearchInfo;
import com.easemob.chat.EMMessage;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.adapter.KeywordSearchingAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class KeywordSearching1Activity extends BaseActivity {

	EditText searchContent;
	ListView listView;
	private KeywordSearchingAdapter adapter;
	private String keyword;
	private Map<String,EMKeywordSearchInfo> map;
	private ProgressBar pb;
	private TextView tvNull;
	private List<String> list;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_keyword_searching);
		searchContent = (EditText) findViewById(R.id.et_search_content);
		listView = (ListView) findViewById(R.id.listview);
		pb = (ProgressBar) findViewById(R.id.progress);
		tvNull = (TextView) findViewById(R.id.tv_null);

		list = new ArrayList<String>();
		map = new HashMap<String,EMKeywordSearchInfo>();
		searchContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				keyword = s.toString();
				if (TextUtils.isEmpty(keyword)) {
					adapter.notifyDataSetChanged();
					tvNull.setVisibility(View.GONE);
					pb.setVisibility(View.GONE);
					return;
				}
				pb.setVisibility(View.VISIBLE);
				asyncFindConversationByKeyword(keyword);

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				EMMessage message = map.get(list.get(position)).getMessage();
				Intent intent = new Intent();
				if (map.get(list.get(position)).getCount() == 1) {
					intent.setClass(KeywordSearching1Activity.this,
							SearchingConversationActivity.class);
					intent.putExtra("message", message);
				} else {
					intent.setClass(KeywordSearching1Activity.this,
							KeywordSearching2Activity.class);
					intent.putExtra("name",map.get(list.get(position)).getUsername());

					intent.putExtra("keyword", keyword);
					intent.putExtra("count",map.get(list.get(position)).getCount());
				}

				startActivity(intent);

			}
		});
	}

	public void asyncFindConversationByKeyword(final String keyword) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				map.clear();
				list.clear();
				map.putAll(EMChatManager.getInstance().getKeywordInfoList(keyword));
				for (String name : map.keySet()) {
					list.add(name);
				}
				runOnUiThread(new Runnable() {
					public void run() {
						if (map.size() == 0) {
							tvNull.setVisibility(View.VISIBLE);
						} else {
							tvNull.setVisibility(View.GONE);
						}
						pb.setVisibility(View.GONE);
						adapter = new KeywordSearchingAdapter(KeywordSearching1Activity.this,
								map, keyword);
						listView.setAdapter(adapter);

					}
				});
			}
		}).start();
	}

}
