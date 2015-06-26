package com.supermarcus.jraklib;

import com.supermarcus.jraklib.protocol.RawPacket;

import java.util.concurrent.LinkedBlockingQueue;

abstract public class RakLibServerInstance {
    private LinkedBlockingQueue<RawPacket> packetQueue = new LinkedBlockingQueue<RawPacket>();

    private SessionManager manager = new SessionManager();

    public final void offerPacket(RawPacket raw){
        this.getPacketQueue().offer(raw);
    }

    protected SessionManager getSessionManager(){
        return manager;
    }

    protected void addMessageHandler(MessageHandler handler){
        this.getSessionManager().addMessageHandler(handler);
    }

    protected LinkedBlockingQueue<RawPacket> getPacketQueue(){
        return this.packetQueue;
    }
}
