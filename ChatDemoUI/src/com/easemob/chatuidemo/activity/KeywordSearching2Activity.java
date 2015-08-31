package com.easemob.chatuidemo.activity;

import java.util.ArrayList;
import java.util.List;

import com.easemob.chat.EMKeywordSearchService;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.adapter.KeywordSearchingAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class KeywordSearching2Activity extends Activity{
	
	EditText searchContent;
	ListView listView;
	private KeywordSearchingAdapter adapter;
	List<EMMessage> msgsList;
	private String name;
	private EMMessage.ChatType chatType;
	private String keyword;
	private LinearLayout showNumberLayout;
	private TextView showNumberContent;
	private long count;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_keyword_searching);

		name = getIntent().getExtras().getString("name");
		chatType = (ChatType) getIntent().getExtras().getSerializable("chattype");
		keyword = getIntent().getExtras().getString("keyword");
		count = getIntent().getExtras().getLong("count");
		
		showNumberLayout = (LinearLayout)findViewById(R.id.linear_show_number);
		showNumberLayout.setVisibility(View.VISIBLE);
		showNumberContent = (TextView)findViewById(R.id.tv_show_number);
		
		showNumberContent.setText("共"+count+"条与"+keyword +"相关的聊天记录");
		
		searchContent = (EditText) findViewById(R.id.et_search_content);
		searchContent.setVisibility(View.GONE);
		listView = (ListView) findViewById(R.id.listview);

		msgsList = new ArrayList<EMMessage>();
		msgsList.addAll(EMKeywordSearchService.getInstance().loadMessages(chatType,keyword,null,20,name));
		
		adapter = new KeywordSearchingAdapter(KeywordSearching2Activity.this,msgsList,keyword,2);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				EMMessage message = msgsList.get(position);
				startActivity(new Intent(KeywordSearching2Activity.this,
							SearchingConversationActivity.class).putExtra("message",message));
			}
		});
	}


	
}
