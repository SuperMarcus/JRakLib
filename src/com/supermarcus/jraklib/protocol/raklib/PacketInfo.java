package com.supermarcus.jraklib.protocol.raklib;

/**
 * This class contains all the known packets id
 */
public enum PacketInfo {
    UNCONNECTED_PING((byte) 0x01),

    UNCONNECTED_PONG((byte) 0x1c);

    private byte id;

    PacketInfo(byte id){
        this.id = id;
    }

    public byte getNetworkId(){
        return this.id;
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
