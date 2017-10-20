package com.example.leesanghoon.nsdservice;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerActivity extends Activity {

    static final int SocketServerPORT = 8080;
    private String SERVICE_NAME = "Test Server Device";
    private String SERVICE_TYPE = "_http._tcp.";
    private NsdManager mNsdManager;

    ServerSocket serverSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        registerService(SocketServerPORT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdManager != null ) {
            registerService(SocketServerPORT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null ){
            try{
                serverSocket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

//        mNsdManager.resolveService(serviceInfo,resolveListener);
        mNsdManager.registerService(serviceInfo,NsdManager.PROTOCOL_DNS_SD,mRegistrationListener);
    }
//
//    NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
//        @Override
//        public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
//
//        }
//
//        @Override
//        public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
//
//        }
//    };
//

    NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener(){
        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            Log.e("ServerActivity","Successfully registered");
            SERVICE_NAME = NsdServiceInfo.getServiceName();
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo,int errorCode) {
            Log.e("ServerActivity","onRegistrationFailed");
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            Log.e("ServerActivity","onServiceUnregistered");
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Failed
            Log.e("ServerActivity","onUnregistrationFailed");
        }
    };
}
