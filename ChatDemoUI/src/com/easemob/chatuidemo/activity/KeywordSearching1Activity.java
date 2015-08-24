package com.easemob.chatuidemo.activity;

import java.util.ArrayList;
import java.util.List;

import com.easemob.chat.EMChatManager;
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

public class KeywordSearching1Activity extends BaseActivity {

	EditText searchContent;
	ListView listView;
	private KeywordSearchingAdapter adapter;
	List<EMMessage> msgsList;
	private String keyword;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_keyword_searching);
		searchContent = (EditText) findViewById(R.id.et_search_content);
		listView = (ListView) findViewById(R.id.listview);

		msgsList = new ArrayList<EMMessage>();
		searchContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				msgsList.clear();
				if(TextUtils.isEmpty(s.toString())){
					adapter.notifyDataSetChanged();
					return;
				}
				keyword = s.toString();
				msgsList.addAll(EMChatManager.getInstance().findConversationByKeyword(s.toString()));
				adapter = new KeywordSearchingAdapter(KeywordSearching1Activity.this,msgsList,s.toString());
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
				EMMessage message = msgsList.get(position);
				Intent intent = new Intent();
				if(adapter.counts.get(position) == 1){
					intent.setClass(KeywordSearching1Activity.this, SearchingConversationActivity.class);
					if (message.getChatType() == EMMessage.ChatType.Chat) {
						intent.putExtra("userId",message.getFrom());
						intent.putExtra("msgId",message.getMsgId());
						intent.putExtra("chatType", 1);
					} else {
						intent.putExtra("groupId",message.getTo());
						intent.putExtra("msgId",message.getMsgId());
						intent.putExtra("chatType", 2);
					}
				}else {
					intent.setClass(KeywordSearching1Activity.this, KeywordSearching2Activity.class);
					if(message.getChatType() == EMMessage.ChatType.Chat){
						if(message.direct == EMMessage.Direct.RECEIVE){
							intent.putExtra("name",message.getFrom());
							intent.putExtra("chattype", EMMessage.ChatType.Chat);
						}else {
							intent.putExtra("name",message.getTo());
							intent.putExtra("chattype", EMMessage.ChatType.Chat);
						}
					}else {
						intent.putExtra("name",message.getTo());
						intent.putExtra("chattype", EMMessage.ChatType.GroupChat);
						
					}
					intent.putExtra("keyword", keyword);
					intent.putExtra("count", adapter.counts.get(position));
				}
				
				
				startActivity(intent);
				
			}
		});
	}

}
