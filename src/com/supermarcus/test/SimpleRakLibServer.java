package com.supermarcus.test;

import com.supermarcus.jraklib.MessageHandler;
import com.supermarcus.jraklib.RakLibServerInstance;
import com.supermarcus.jraklib.lang.exceptions.InterfaceOutOfPoolSizeException;
import com.supermarcus.jraklib.lang.message.RakLibMessage;
import com.supermarcus.jraklib.protocol.raklib.OPEN_CONNECTION_REPLY_2;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class SimpleRakLibServer extends RakLibServerInstance {
    public static void main(String args[]){
        new SimpleRakLibServer();
    }

    public SimpleRakLibServer(){
        try {
            this.addMessageHandler(new MessageHandler() {
                @Override
                public void onMessage(RakLibMessage message) {
                    System.out.println(message);
                }
            });
            this.getSessionManager().addInterface(new InetSocketAddress("0.0.0.0", 19132));
        } catch (SocketException | InterfaceOutOfPoolSizeException e) {
            e.printStackTrace();
        }
    }
}
