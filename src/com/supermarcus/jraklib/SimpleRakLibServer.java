package com.supermarcus.jraklib;

import com.supermarcus.jraklib.lang.exceptions.InterfaceOutOfPoolSizeException;
import com.supermarcus.jraklib.lang.message.RakLibMessage;

import java.net.InetSocketAddress;
import java.net.SocketException;

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
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (InterfaceOutOfPoolSizeException e) {
            e.printStackTrace();
        }
    }
}
