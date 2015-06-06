package com.supermarcus.jraklib;

import com.supermarcus.jraklib.protocol.RawPacket;

import java.util.concurrent.LinkedBlockingQueue;

abstract public class RakLibServerInstance {
    private LinkedBlockingQueue<RawPacket> packetQueue = new LinkedBlockingQueue<RawPacket>();

    public final void offerPacket(RawPacket raw){
        this.getPacketQueue().offer(raw);
    }

    protected LinkedBlockingQueue<RawPacket> getPacketQueue(){
        return this.packetQueue;
    }
}
