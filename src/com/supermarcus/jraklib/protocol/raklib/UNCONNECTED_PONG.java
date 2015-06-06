package com.supermarcus.jraklib.protocol.raklib;

import com.supermarcus.jraklib.RakLib;
import com.supermarcus.jraklib.protocol.Packet;

import java.io.ByteArrayOutputStream;

public class UNCONNECTED_PONG extends Packet {
    private long pingID = 0L;

    private long serverID = 0L;

    private String serverName = "";

    public UNCONNECTED_PONG() {
        super(0x1c);
    }

    public long getPingID(){
        return this.pingID;
    }

    public void setPingID(long pingID){
        this.pingID = pingID;
    }

    public long getServerID(){
        return this.serverID;
    }

    public void setServerID(long serverID){
        this.serverID = serverID;
    }

    public String getServerName(){
        return this.serverName;
    }

    public void setServerName(String serverName){
        this.serverName = serverName;
    }

    @Override
    public void encode() {
        this.getBuffer().putLong(this.getPingID());
        this.getBuffer().putLong(this.getServerID());
        this.getBuffer().put(RakLib.MAGIC);

        byte[] nameBytes = this.getServerName().getBytes(RakLib.STRING_DEFAULT_CHARSET);
        this.getBuffer().putShort((short) nameBytes.length);
        this.getBuffer().put(nameBytes);
    }

    @Override
    public void decode() {
        this.setPingID(this.getBuffer().getLong());
        this.setServerID(this.getBuffer().getLong());

        this.getBuffer().position(this.getBuffer().position() + 16);//Skip magic

        int length = this.getBuffer().get();

        ByteArrayOutputStream bufStream = new ByteArrayOutputStream(length);
        while (length > 0){
            --length;
            bufStream.write(this.getBuffer().get());
        }

        this.setServerName(new String(bufStream.toByteArray(), RakLib.STRING_DEFAULT_CHARSET));
    }
}
