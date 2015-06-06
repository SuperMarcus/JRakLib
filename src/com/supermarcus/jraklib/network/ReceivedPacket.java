package com.supermarcus.jraklib.network;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class ReceivedPacket {
    private byte[] rawPacket;

    private InetSocketAddress fromAddress;

    public ReceivedPacket(DatagramPacket dPacket){
        this(dPacket.getData(), new InetSocketAddress(dPacket.getAddress(), dPacket.getPort()));
    }
    
    public ReceivedPacket(byte[] buffer, InetSocketAddress address){
        this.fromAddress = address;
        this.rawPacket = buffer;
    }

    public InetSocketAddress getSendAddress(){
        return this.fromAddress;
    }

    public byte[] getRawData(){
        return this.rawPacket;
    }
}
