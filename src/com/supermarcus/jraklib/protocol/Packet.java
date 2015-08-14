package com.supermarcus.jraklib.protocol;

import com.supermarcus.jraklib.protocol.raklib.PacketInfo;

import java.nio.ByteBuffer;

abstract public class Packet {
    public static final int MAX_SIZE = 1024 * 1024 * 8;

    private ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_SIZE);

    private BinaryUtils utils = new BinaryUtils(this.buffer);

    private PacketInfo identifier = null;

    public Packet(PacketInfo identifier){
        this.identifier = identifier;
        this.getBuffer().put(this.getNetworkID());
    }

    abstract public void encode();

    abstract public void decode();

    public byte getNetworkID(){
        return this.getPacketIdentifier().getNetworkId();
    }

    public PacketInfo getPacketIdentifier(){
        return this.identifier;
    }

    protected ByteBuffer getBuffer(){
        return buffer;
    }

    public void initBuffer(ByteBuffer buffer){
        this.buffer = buffer;
        this.identifier = PacketInfo.getById(this.getBuffer().get());
    }

    public byte[] toRaw(){
        int length = getBuffer().position() + 1;
        byte[] raw = new byte[length];
        System.arraycopy(getBuffer().array(), 0, raw, 0, length);
        return raw;
    }

    public BinaryUtils getUtils() {
        return utils;
    }
}
