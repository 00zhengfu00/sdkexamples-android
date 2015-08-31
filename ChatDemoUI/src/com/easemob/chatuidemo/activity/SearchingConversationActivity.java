/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easemob.chatuidemo.activity;

import java.util.List;

import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMKeywordSearchService;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.adapter.SearchingConversationAdapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 聊天页面
 * 
 */
public class SearchingConversationActivity extends BaseActivity {
	private static final String TAG = "ChatActivity";
	public static final int REQUEST_CODE_CONTEXT_MENU = 3;
	public static final int REQUEST_CODE_TEXT = 5;
	public static final int REQUEST_CODE_PICTURE = 7;

	public static final int RESULT_CODE_COPY = 1;
	public static final int RESULT_CODE_DELETE = 2;
	public static final int RESULT_CODE_FORWARD = 3;

	public static final int CHATTYPE_SINGLE = 1;
	public static final int CHATTYPE_GROUP = 2;

	private ListView listView;
	private EMConversation conversation;
	// 给谁发送消息
	private String toChatUsername;
	private SearchingConversationAdapter adapter;

	private ClipboardManager clipboard;
	private boolean isUpLoading;
	private boolean isDownLoading;
	private final int pagesize = 20;
	private boolean haveMoreUpData = true;
	private boolean haveMoreDownData = true;
	public EMGroup group;

	private SwipeRefreshLayout swipeRefreshLayout;
	public String playMsgId;
	private EMMessage message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searching_conversation);
		initView();
		setUpView();
	}

	/**
	 * initView
	 */
	protected void initView() {
		listView = (ListView) findViewById(R.id.list);

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.chat_swipe_layout);

		swipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						if (listView.getFirstVisiblePosition() == 0
								&& !isUpLoading && haveMoreUpData) {
							List<EMMessage> messages;
							try {
								messages = conversation.loadMoreMessages(true, adapter.getItem(0).getMsgId(),pagesize);
							} catch (Exception e1) {
								swipeRefreshLayout.setRefreshing(false);
								return;
							}

							if (messages.size() > 0) {
								adapter.notifyDataSetChanged();
								adapter.refreshSeekTo(messages.size() - 1);
								if (messages.size() != pagesize) {
									haveMoreUpData = false;
								}
							} else {
								haveMoreUpData = false;
							}

							isUpLoading = false;

						} else {
							Toast.makeText(
									SearchingConversationActivity.this,
									getResources().getString(
											R.string.no_more_messages),
									Toast.LENGTH_SHORT).show();
						}
						swipeRefreshLayout.setRefreshing(false);
					}
				}, 1000);
			}
		});
	}

	private void setUpView() {
		clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		
		message = getIntent().getParcelableExtra("message");

		if (message.getChatType() == EMMessage.ChatType.Chat) { // 单聊
			toChatUsername = message.getFrom();
			((TextView) findViewById(R.id.name)).setText(toChatUsername);
		} else {
			// 群聊
			toChatUsername = message.getTo();

			group = EMGroupManager.getInstance().getGroup(toChatUsername);

			if (group != null) {
				((TextView) findViewById(R.id.name)).setText(group
						.getGroupName());
			} else {
				((TextView) findViewById(R.id.name)).setText(toChatUsername);
			}
		}

		conversation = EMKeywordSearchService.getInstance().getConversation(toChatUsername);
		conversation.addMessage(message);

		conversation.loadMoreMessages(false,message.getMsgId(), pagesize);

		adapter = new SearchingConversationAdapter(
				SearchingConversationActivity.this, toChatUsername, conversation);
		// 显示消息
		listView.setAdapter(adapter);

		listView.setOnScrollListener(new ListScrollListener());
		adapter.refresh();

	}

	/**
	 * onActivityResult
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
			switch (resultCode) {
			case RESULT_CODE_COPY: // 复制消息
				EMMessage copyMsg = ((EMMessage) adapter.getItem(data.getIntExtra("position", -1)));
				clipboard.setText(((TextMessageBody) copyMsg.getBody()).getMessage());
				break;
			case RESULT_CODE_DELETE: // 删除消息
				EMMessage deleteMsg = (EMMessage) adapter.getItem(data
						.getIntExtra("position", -1));
				conversation.removeMessage(deleteMsg.getMsgId());
				adapter.refreshSeekTo(data.getIntExtra("position",
						adapter.getCount()) - 1);
				break;

			case RESULT_CODE_FORWARD: // 转发消息
				EMMessage forwardMsg = (EMMessage) adapter.getItem(data
						.getIntExtra("position", 0));
				Intent intent = new Intent(this, ForwardMessageActivity.class);
				intent.putExtra("forward_msg", forwardMsg);
				intent.putExtra("iskeywordsearch", true);
				
				startActivity(intent);

				break;

			default:
				break;
			}
		}
		if (resultCode == RESULT_OK) {
			if (conversation.getMsgCount() > 0) {
				adapter.refresh();
			}
		}
	}

	/**
	 * 返回
	 * 
	 * @param view
	 */
	public void back(View view) {
		finish();
	}

	/**
	 * listview滑动监听listener
	 * 
	 */
	private class ListScrollListener implements OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE:

				if (view.getLastVisiblePosition() == conversation.getAllMessages().size()-1 && !isDownLoading && haveMoreDownData && conversation.getAllMessages().size() != 0) {
					isDownLoading = true;
					List<EMMessage> messages;
					String msgid = conversation.getAllMessages().get(conversation.getAllMessages().size()-1).getMsgId();
					try {
						messages = conversation.loadMoreMessages(false,msgid, pagesize);
					} catch (Exception e1) {
						return;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					if (messages.size() != 0) { // 刷新ui
						if (messages.size() > 0) {
							adapter.refreshSeekTo(messages.size() - 1);
						}

						if (messages.size() != pagesize)
							haveMoreDownData = false;
					} else {
						haveMoreDownData = false;
					}
					isDownLoading = false;

				}
				break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

		}
	}

	public String getToChatUsername() {
		return toChatUsername;
	}

	public ListView getListView() {
		return listView;
	}

}
