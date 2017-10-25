package com.example.leesanghoon.nsdservice;

import android.app.Activity;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NsdChatActivity extends Activity {

    NsdHelper mNsdHelper;

    private TextView mStatusView;
    private Handler mUpdateHandler;

    public static final String TAG = "NsdChat";

    ChatConnection mConnection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mStatusView = (TextView) findViewById(R.id.status);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
            }
        };

        mConnection = new ChatConnection(mUpdateHandler);

        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();

    }

    public void clickAdvertise(View v) {
        // Register service
        if (mConnection.getLocalPort() > -1) {
            try {
                mNsdHelper.registerService(mConnection.getLocalPort());
                Toast.makeText(NsdChatActivity.this,"REGISTER SUCCESS",Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(NsdChatActivity.this, "이미 리스너가 등록되어 있습니다.", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }
    }

    public void clickDiscover(View v) {
        try {
            mNsdHelper.discoverServices();
            Toast.makeText(NsdChatActivity.this,"DISCOVER SUCCESS",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(NsdChatActivity.this, "이미 리스너가 등록되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void clickConnect(View v) {
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting.");
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }
    }

    public void clickSend(View v) {
        EditText messageView = (EditText) this.findViewById(R.id.chatInput);
        if (messageView != null) {
            String messageString = messageView.getText().toString();
            if (!messageString.isEmpty()) {
                mConnection.sendMessage(messageString);
            }
            messageView.setText("");
        }
    }

    public void addChatLine(String line) {
        mStatusView.append("\n" + line);
    }

    @Override
    protected void onPause() {
        if (mNsdHelper != null) {
            try {
                mNsdHelper.stopDiscovery();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        try {
            mNsdHelper.tearDown();
            mConnection.tearDown();
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
