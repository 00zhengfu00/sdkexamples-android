package com.easemob.chatuidemo.activity;

import java.util.ArrayList;
import java.util.List;

import com.easemob.chat.EMKeywordSearchService;
import com.easemob.chat.EMKeywordSearchService.KeywordSearchInfo;
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
	private List<KeywordSearchInfo> list;
	private ProgressBar pb;
	private TextView tvNull;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_keyword_searching);
		searchContent = (EditText) findViewById(R.id.et_search_content);
		listView = (ListView) findViewById(R.id.listview);
		pb = (ProgressBar) findViewById(R.id.progress);
		tvNull = (TextView) findViewById(R.id.tv_null);

		list = new ArrayList<EMKeywordSearchService.KeywordSearchInfo>();
		searchContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				list.clear();
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
				EMMessage message = list.get(position).getMessage();
				Intent intent = new Intent();
				if (list.get(position).getCount() == 1) {
					intent.setClass(KeywordSearching1Activity.this,
							SearchingConversationActivity.class);
					intent.putExtra("message", message);
				} else {
					intent.setClass(KeywordSearching1Activity.this,
							KeywordSearching2Activity.class);
					if (message.getChatType() == EMMessage.ChatType.Chat) {
						intent.putExtra("name", list.get(position).getUsername());
						intent.putExtra("chattype", EMMessage.ChatType.Chat);
					} else {
						intent.putExtra("name",list.get(position).getUsername());
						intent.putExtra("chattype",
								EMMessage.ChatType.GroupChat);

					}
					intent.putExtra("keyword", keyword);
					intent.putExtra("count",list.get(position).getCount());
				}

				startActivity(intent);

			}
		});
	}

	public void asyncFindConversationByKeyword(final String keyword) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				list.addAll(EMKeywordSearchService.getInstance().findConversationByKeyword(keyword));
				runOnUiThread(new Runnable() {
					public void run() {
						if (list.size() == 0) {
							tvNull.setVisibility(View.VISIBLE);
						} else {
							tvNull.setVisibility(View.GONE);
						}
						pb.setVisibility(View.GONE);
						adapter = new KeywordSearchingAdapter(KeywordSearching1Activity.this,
								list, keyword);
						listView.setAdapter(adapter);

					}
				});
			}
		}).start();
	}

}
