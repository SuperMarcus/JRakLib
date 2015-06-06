package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.RakLib;
import com.supermarcus.jraklib.protocol.Packet;

public class UNCONNECTED_PING extends Packet {

    private long pingID = 0L;

    public UNCONNECTED_PING() {
        super(0x01);
    }

    public long getPingID(){
        return this.pingID;
    }

    public void setPingID(long pingID){
        this.pingID = pingID;
    }

    @Override
    public void encode() {
        this.getBuffer().putLong(this.getPingID());
        this.getBuffer().put(RakLib.MAGIC);
    }

    @Override
    public void decode() {
        this.setPingID(this.getBuffer().getLong());
        //Magic
    }
}
