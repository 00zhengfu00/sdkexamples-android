package com.easemob.chatuidemo.receiver;

import java.util.List;

import android.content.Context;

import com.easemob.chat.EMChatManager;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

public class XiaomiMessageReceiver extends PushMessageReceiver {
    
    private String mRegId;
    
    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message){
        String command = message.getCommand();
       
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
                EMChatManager.getInstance().setXiaomiRegId(mRegId);
            }
        }
        
    }
}
