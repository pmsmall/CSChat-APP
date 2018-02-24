package com.qq.task;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Frank on 2017.12.02.
 */

public class CreateWSTask implements Runnable{

    WebSocketClient mClient;
    String url="";
    @Override
    public void run(){
        try {
            mClient=new WebSocketClient(new URI(url)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    System.out.print("1222222222222222222+"+serverHandshake.getHttpStatusMessage());
                }

                @Override
                public void onMessage(String s) {

                }

                @Override
                public void onClose(int i, String s, boolean b) {

                }

                @Override
                public void onError(Exception e) {

                }
            };
            mClient.send("123");

        }catch (URISyntaxException e){
            e.printStackTrace();
        }

    }
}
