package com.supermarcus.jraklib.protocol;

import com.supermarcus.jraklib.network.ReceivedPacket;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class PacketWrapper extends HashMap<Byte, Class<? extends Packet>> {

    public void registerPacket(Packet raw){
        this.registerPacket(raw.getNetworkID(), raw.getClass());
    }

    public void registerPacket(int id, Class<? extends Packet> registerClass){
        this.registerPacket((byte) id, registerClass);
    }

    public void registerPacket(byte id, Class<? extends Packet> registerClass){
        this.put(id, registerClass);
    }

    public Packet matchPacket(byte id){
        try{
            Class<? extends Packet> packet = this.get(id);
            if(packet != null){
                return packet.newInstance();
            }
        }catch (Exception ignore){}
        return null;
    }

    public Packet wrapPacket(ReceivedPacket packet){
        return this.wrapPacket(packet.getRawData());
    }

    public Packet wrapPacket(byte[] buffer){
        Packet raw = this.matchPacket(buffer[0]);
        if(raw != null){
            raw.initBuffer(ByteBuffer.wrap(buffer));
        }
        return raw;
    }
}
