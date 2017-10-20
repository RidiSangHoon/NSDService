package com.example.leesanghoon.nsdservice;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientActivity extends Activity {

    private String SERVICE_NAME = "Client Device";
    private String SERVICE_TYPE = "_http._tcp.";

    private InetAddress hostAddress;
    private int hostPort;
    private NsdManager mNsdManager;

    LinearLayout loginPanel, chatPanel;

    EditText editTextUserName, editTextAddress;
    Button buttonConnect;
    TextView chatMsg, textPort;

    EditText editTextSay;
    Button buttonSend;
    Button buttonDisconnect;

    String msgLog = "";

    ChatClientThread chatClientThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        hostPort=0;

        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        mNsdManager.discoverServices(SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

        loginPanel = (LinearLayout)findViewById(R.id.loginpanel);
        chatPanel = (LinearLayout)findViewById(R.id.chatpanel);

        editTextUserName = (EditText) findViewById(R.id.username);
        editTextAddress = (EditText) findViewById(R.id.address);
        textPort = (TextView) findViewById(R.id.port);
        buttonConnect = (Button) findViewById(R.id.connect);
        textPort.setText("port: " + hostPort);
        editTextAddress.setText("Still Searching...");
        buttonDisconnect = (Button) findViewById(R.id.disconnect);
        chatMsg = (TextView) findViewById(R.id.chatmsg);

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
        buttonDisconnect.setOnClickListener(buttonDisconnectOnClickListener);

        editTextSay = (EditText)findViewById(R.id.say);
        buttonSend = (Button)findViewById(R.id.send);

        buttonSend.setOnClickListener(buttonSendOnClickListener);
    }
    @Override
    protected void onPause() {
        if (mNsdManager != null) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdManager != null) {
            //mNsdManager.discoverServices(
            //       SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        }
    }

    @Override
    protected void onDestroy() {
        if (mNsdManager != null) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
        super.onDestroy();
    }

    View.OnClickListener buttonDisconnectOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if(chatClientThread==null){
                return;
            }
            chatClientThread.disconnect();
        }

    };

    View.OnClickListener buttonSendOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (editTextSay.getText().toString().equals("")) {
                return;
            }

            if(chatClientThread==null){
                return;
            }

            chatClientThread.sendMsg(editTextSay.getText().toString() + "\n");
        }

    };

    View.OnClickListener buttonConnectOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String textUserName = editTextUserName.getText().toString();
            if (textUserName.equals("")) {
                Toast.makeText(ClientActivity.this, "Enter User Name",
                        Toast.LENGTH_LONG).show();
                return;
            }

            String textAddress = editTextAddress.getText().toString();
            if (textAddress.equals("")) {
                //Toast.makeText(ClientActivity.this, "Enter Addresse",
                //        Toast.LENGTH_LONG).show();
                editTextAddress.setText(hostAddress.toString());
                return;
            }

            msgLog = "";
            chatMsg.setText(msgLog);
            loginPanel.setVisibility(View.GONE);
            chatPanel.setVisibility(View.VISIBLE);

            chatClientThread = new ChatClientThread(
                    textUserName, hostAddress.getHostAddress(), hostPort);
            chatClientThread.start();
        }

    };

    NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {

        // Called as soon as service discovery begins.
        @Override
        public void onDiscoveryStarted(String regType) {
            Toast.makeText(ClientActivity.this, "Discovering devices", Toast.LENGTH_SHORT).show();
            Log.d("nsdservice", "Service discovery started");
        }

        @Override
        public void onServiceFound(NsdServiceInfo service) {
            // A service was found! Do something with it.
            Log.d("nsdservice", "Service discovery success : " + service);
            Log.d("nsdservice", "Host = "+ service.getServiceName());
            Log.d("nsdservice", "port = " + String.valueOf(service.getPort()));

            Toast.makeText(ClientActivity.this, "service found"+ service.getPort() , Toast.LENGTH_SHORT).show();

            if (!service.getServiceType().equals(SERVICE_TYPE)) {
                // Service type is the string containing the protocol and
                // transport layer for this service.
                Log.d("nsdservice", "Unknown Service Type: " + service.getServiceType());
            } else if (service.getServiceName().equals(SERVICE_NAME)) {
                //Name of the service
                Log.d("nsdservice", "Same machine: " + SERVICE_NAME);
            } else {
                Log.d("nsdservice", "Diff Machine : " + service.getServiceName());
                // connect to the service and obtain serviceInfo
                mNsdManager.resolveService(service, mResolveListener);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo service) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Toast.makeText(ClientActivity.this, "Service lost", Toast.LENGTH_SHORT).show();
            Log.e("nsdserviceLost", "service lost" + service);
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Toast.makeText(ClientActivity.this, "Discovering devices stopped", Toast.LENGTH_SHORT).show();
            Log.i("nsdserviceDstopped", "Discovery stopped: " + serviceType);
        }

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e("nsdServiceSrartDfailed", "Discovery failed: Error code:" + errorCode);
            Toast.makeText(ClientActivity.this, "Discover start failed", Toast.LENGTH_SHORT).show();
            mNsdManager.stopServiceDiscovery(this);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e("nsdserviceStopDFailed", "Discovery failed: Error code:" + errorCode);
            Toast.makeText(ClientActivity.this, "Discover stop failed", Toast.LENGTH_SHORT).show();
            mNsdManager.stopServiceDiscovery(this);
        }
    };

    NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Toast.makeText(ClientActivity.this, "Resolve failed", Toast.LENGTH_SHORT).show();
            // Called when the resolve fails. Use the error code to debug.
            Log.e("nsdservicetag", "Resolve failed " + errorCode);
            Log.e("nsdservicetag", "serivce = " + serviceInfo);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {

            Log.d("nsdservicetag", "Resolve Succeeded. " + serviceInfo);

            if (serviceInfo.getServiceName().equals(SERVICE_NAME)) {
                Log.d("nsdservicetag", "Same IP.");
                return;
            }

            // Obtain port and IP
            hostPort = serviceInfo.getPort();
            hostAddress = serviceInfo.getHost();
            ClientActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    editTextAddress.setText(hostAddress.getHostAddress());
                }
            });
            textPort.setText("port: " + hostPort);
            Toast.makeText(ClientActivity.this, "host address = " + hostAddress.getHostAddress(), Toast.LENGTH_SHORT).show();
        }
    };

    private class ChatClientThread extends Thread {

        String name;
        String dstAddress;
        int dstPort;

        String msgToSend = "";
        boolean goOut = false;

        ChatClientThread(String name, String address, int port) {
            this.name = name;
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket = null;

            //Getting Local address
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

            JSONObject jsonData = new JSONObject();
            try {
                jsonData.put("request", "");
                //                jsonData.put("request", REQUEST_CONNECT_CLIENT);
                jsonData.put("ipAddress", ip);
                jsonData.put("clientName", name);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("nsdServicejsontag", "can't put request");
                return;
            }
            DataOutputStream dataOutputStream = null;
            DataInputStream dataInputStream = null;

            try {
                socket = new Socket(dstAddress, dstPort);
                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream.writeUTF(jsonData.toString());
                dataOutputStream.flush();

                while (!goOut) {
                    if (dataInputStream.available() > 0) {
                        msgLog += dataInputStream.readUTF();

                        ClientActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                chatMsg.setText(msgLog);
                            }
                        });
                    }

                    if(!msgToSend.equals("")){
                        dataOutputStream.writeUTF(msgToSend);
                        dataOutputStream.flush();
                        msgToSend = "";
                    }
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
                final String eString = e.toString();
                ClientActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ClientActivity.this, eString, Toast.LENGTH_LONG).show();
                    }

                });
            } catch (IOException e) {
                e.printStackTrace();
                final String eString = e.toString();
                ClientActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ClientActivity.this, eString, Toast.LENGTH_LONG).show();
                    }

                });
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                ClientActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        loginPanel.setVisibility(View.VISIBLE);
                        chatPanel.setVisibility(View.GONE);
                    }

                });
            }

        }

        private void sendMsg(String msg){
            msgToSend = msg;
        }

        private void disconnect(){
            goOut = true;
        }
    }

}