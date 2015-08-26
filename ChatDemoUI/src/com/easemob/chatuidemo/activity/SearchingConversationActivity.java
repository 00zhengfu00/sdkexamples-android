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

import java.io.File;
import java.util.List;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMConversation.EMConversationType;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chatuidemo.R;
import com.easemob.chatuidemo.adapter.SearchingConversationAdapter;
import com.easemob.chatuidemo.utils.ImageUtils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
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

	public static final int RESULT_CODE_DELETE = 2;
	public static final int RESULT_CODE_FORWARD = 3;

	public static final int CHATTYPE_SINGLE = 1;
	public static final int CHATTYPE_GROUP = 2;

	private ListView listView;
	private int chatType;
	private EMConversation conversation;
	// 给谁发送消息
	private String toChatUsername;
	private SearchingConversationAdapter adapter;

	private boolean isUpLoading;
	private boolean isDownLoading;
	private final int pagesize = 20;
	private boolean haveMoreUpData = true;
	private boolean haveMoreDownData = true;
	public EMGroup group;

	private SwipeRefreshLayout swipeRefreshLayout;
	public String playMsgId;
	private String matchMsgId;

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
								if (chatType == CHATTYPE_SINGLE) {
									messages = conversation.loadMoreMsgFromDB(adapter.getItem(0).getMsgId(),pagesize);
								} else {
									messages = conversation
											.loadMoreGroupMsgFromDB(adapter
													.getItem(0).getMsgId(),
													pagesize);
								}
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

		matchMsgId = getIntent().getStringExtra("msgId");
		// 判断单聊还是群聊
		chatType = getIntent().getIntExtra("chatType", CHATTYPE_SINGLE);

		if (chatType == CHATTYPE_SINGLE) { // 单聊
			toChatUsername = getIntent().getStringExtra("userId");
			((TextView) findViewById(R.id.name)).setText(toChatUsername);
		} else {
			// 群聊
			toChatUsername = getIntent().getStringExtra("groupId");

			group = EMGroupManager.getInstance().getGroup(toChatUsername);

			if (group != null) {
				((TextView) findViewById(R.id.name)).setText(group
						.getGroupName());
			} else {
				((TextView) findViewById(R.id.name)).setText(toChatUsername);
			}

		}

		//生成搜索会话
		EMChatManager.getInstance().createSearchingRecordConversation(toChatUsername,
				matchMsgId);

		if (chatType == CHATTYPE_SINGLE) {
			conversation = EMChatManager.getInstance().getConversationByType(
					toChatUsername, EMConversationType.Chat);
		} else if (chatType == CHATTYPE_GROUP) {
			conversation = EMChatManager.getInstance().getConversationByType(
					toChatUsername, EMConversationType.GroupChat);
		}

		// 初始化db时，每个conversation加载数目是getChatOptions().getNumberOfMessagesLoaded
		// 这个数目如果比用户期望进入会话界面时显示的个数不一样，就多加载一些
		final List<EMMessage> msgs = conversation.getAllMessages();
		int msgCount = msgs != null ? msgs.size() : 0;
		if (msgCount < conversation.getAllMsgCount() && msgCount < pagesize) {
			String msgId = null;
			if (msgs != null && msgs.size() > 0) {
				msgId = msgs.get(0).getMsgId();
			}
			if (chatType == CHATTYPE_SINGLE) {
				conversation.loadMoreMsgFromDB(msgId, pagesize);
			} else {
				conversation.loadMoreGroupMsgFromDB(msgId, pagesize);
			}
		}

		adapter = new SearchingConversationAdapter(
				SearchingConversationActivity.this, toChatUsername, chatType);
		// 显示消息
		listView.setAdapter(adapter);

		listView.setOnScrollListener(new ListScrollListener());
		adapter.refreshSelectLast();

		// show forward message if the message is not null
		String forward_msg_id = getIntent().getStringExtra("forward_msg_id");
		if (forward_msg_id != null) {
			// 显示发送要转发的消息
			forwardMessage(forward_msg_id);
		}
	}

	/**
	 * onActivityResult
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
			switch (resultCode) {
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
				intent.putExtra("forward_msg_id", forwardMsg.getMsgId());
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
	 * 转发消息
	 * 
	 * @param forward_msg_id
	 */
	protected void forwardMessage(String forward_msg_id) {
		final EMMessage forward_msg = EMChatManager.getInstance().getMessage(
				forward_msg_id);
		EMMessage.Type type = forward_msg.getType();
		switch (type) {
		case TXT:
			// 获取消息内容，发送消息
			String content = ((TextMessageBody) forward_msg.getBody())
					.getMessage();
			sendText(content);
			break;
		case IMAGE:
			// 发送图片
			String filePath = ((ImageMessageBody) forward_msg.getBody())
					.getLocalUrl();
			if (filePath != null) {
				File file = new File(filePath);
				if (!file.exists()) {
					// 不存在大图发送缩略图
					filePath = ImageUtils.getThumbnailImagePath(filePath);
				}
				sendPicture(filePath);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 发送文本消息
	 * 
	 * @param content
	 *            message content
	 * @param isResend
	 *            boolean resend
	 */
	public void sendText(String content) {

		if (content.length() > 0) {
			EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
			// 如果是群聊，设置chattype,默认是单聊
			if (chatType == CHATTYPE_GROUP) {
				message.setChatType(ChatType.GroupChat);
			}
			TextMessageBody txtBody = new TextMessageBody(content);
			// 设置消息body
			message.addBody(txtBody);
			// 设置要发给谁,用户username或者群聊groupid
			message.setReceipt(toChatUsername);
			// 把messgage加到conversation中
			conversation.addMessage(message);
			// 通知adapter有消息变动，adapter会根据加入的这条message显示消息和调用sdk的发送方法
			adapter.refreshSelectLast();

		}
	}

	/**
	 * 发送图片
	 * 
	 * @param filePath
	 */
	private void sendPicture(final String filePath) {
		String to = toChatUsername;
		// create and add image message in view
		final EMMessage message = EMMessage
				.createSendMessage(EMMessage.Type.IMAGE);
		// 如果是群聊，设置chattype,默认是单聊
		if (chatType == CHATTYPE_GROUP) {
			message.setChatType(ChatType.GroupChat);
		}

		message.setReceipt(to);
		ImageMessageBody body = new ImageMessageBody(new File(filePath));
		// 默认超过100k的图片会压缩后发给对方，可以设置成发送原图
		// body.setSendOriginalImage(true);
		message.addBody(body);
		conversation.addMessage(message);

		listView.setAdapter(adapter);
		adapter.refreshSelectLast();
	}

	/**
	 * 返回
	 * 
	 * @param view
	 */
	public void back(View view) {
		onBackPressed();
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
					// sdk初始化加载的聊天记录为20条，到顶时去db里获取更多
					List<EMMessage> messages;
					String msgid = conversation.getAllMessages().get(conversation.getAllMessages().size()-1).getMsgId();
					try {
						// 获取更多messges，调用此方法的时候从db获取的messages
						// sdk会自动存入到此conversation中
						if (chatType == CHATTYPE_SINGLE)
							messages = conversation.loadMoreMessages(true,
									msgid, pagesize);
						else
							messages = conversation.loadMoreGroupMessages(true,
									msgid, pagesize);
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

				}else {
					Toast.makeText(
							SearchingConversationActivity.this,
							getResources().getString(
									R.string.no_more_messages),
							Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

		}
	}

	@Override
	public void onBackPressed() {
		//结束搜索会话
		EMChatManager.getInstance().retrieveConversation(toChatUsername);
		super.onBackPressed();
	}

	public String getToChatUsername() {
		return toChatUsername;
	}

	public ListView getListView() {
		return listView;
	}

}
