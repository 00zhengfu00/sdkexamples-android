package com.easemob.chatuidemo.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.adapter.KeywordSearchingAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

public class KeywordSearching1Activity extends BaseActivity {

	EditText searchContent;
	ListView listView;
	private KeywordSearchingAdapter adapter;
	private String keyword;
	private List<Map.Entry<Pair<String, Long>, EMMessage>> entriesList;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_keyword_searching);
		searchContent = (EditText) findViewById(R.id.et_search_content);
		listView = (ListView) findViewById(R.id.listview);

		entriesList = new ArrayList<Map.Entry<Pair<String,Long>,EMMessage>>();
		searchContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				entriesList.clear();
				if(TextUtils.isEmpty(s.toString())){
					adapter.notifyDataSetChanged();
					return;
				}
				keyword = s.toString();
				Iterator<Map.Entry<Pair<String, Long>,EMMessage>> iterator = EMChatManager.getInstance().findConversationByKeyword(keyword.toString()).entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<Pair<String, Long>, EMMessage> entry = iterator.next();
					entriesList.add(entry);
				}
				adapter = new KeywordSearchingAdapter(KeywordSearching1Activity.this,entriesList,s.toString());
				listView.setAdapter(adapter);
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
				EMMessage message = entriesList.get(position).getValue();
				Intent intent = new Intent();
				if(entriesList.get(position).getKey().second == 1){
					intent.setClass(KeywordSearching1Activity.this, SearchingConversationActivity.class);
					if (message.getChatType() == EMMessage.ChatType.Chat) {
						intent.putExtra("userId",entriesList.get(position).getKey().first);
						intent.putExtra("msgId",message.getMsgId());
						intent.putExtra("chatType", 1);
					} else {
						intent.putExtra("groupId",entriesList.get(position).getKey().first);
						intent.putExtra("msgId",message.getMsgId());
						intent.putExtra("chatType", 2);
					}
				}else {
					intent.setClass(KeywordSearching1Activity.this, KeywordSearching2Activity.class);
					if(message.getChatType() == EMMessage.ChatType.Chat){
						intent.putExtra("name",entriesList.get(position).getKey().first);
						intent.putExtra("chattype", EMMessage.ChatType.Chat);
					}else {
						intent.putExtra("name",entriesList.get(position).getKey().first);
						intent.putExtra("chattype", EMMessage.ChatType.GroupChat);
						
					}
					intent.putExtra("keyword", keyword);
					intent.putExtra("count", entriesList.get(position).getKey().second);
				}
				
				
				startActivity(intent);
				
			}
		});
	}

}
