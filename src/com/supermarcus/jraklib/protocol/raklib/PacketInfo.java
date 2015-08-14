package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.protocol.Packet;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * This class contains all the known packets id
 */
public enum PacketInfo {
    UNCONNECTED_PING((byte) 0x01, UNCONNECTED_PING.class),

    UNCONNECTED_PONG((byte) 0x1c, UNCONNECTED_PONG.class);

    private byte id;

    private Class<? extends Packet> packet;

    PacketInfo(byte id, Class<? extends Packet> packet){
        this.id = id;
        this.packet = packet;
    }

    public byte getNetworkId(){
        return this.id;
    }

    public Packet wrap(byte[] buffer){
        Packet instance = null;
        if(buffer[0] == this.getNetworkId()){
            try {
                instance = this.packet.newInstance();
                instance.initBuffer(ByteBuffer.wrap(buffer));
            } catch (Exception e) {
                instance = null;
            }
        }
        return instance;
    }

    public static PacketInfo getById(byte id){
        for(PacketInfo p : PacketInfo.values()){
            if(p.getNetworkId() == id){
                return p;
            }
        }
        return null;
    }
}
