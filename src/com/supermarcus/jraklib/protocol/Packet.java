package com.supermarcus.jraklib.protocol;

import java.nio.ByteBuffer;

abstract public class Packet {
    public static final int MAX_SIZE = 1024 * 1024 * 8;

    private ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_SIZE);

    private int networkID = 0;

    public Packet(int id){
        this.networkID = id;
        this.getBuffer().put((byte) this.getNetworkID());
    }

    abstract public void encode();

    abstract public void decode();

    public int getNetworkID(){
        return networkID;
    }

    protected ByteBuffer getBuffer(){
        return buffer;
    }

    public void initBuffer(ByteBuffer buffer){
        this.buffer = buffer;
        this.networkID = this.getBuffer().get();
    }

    public byte[] toRaw(){
        return getBuffer().array();
    }
}
