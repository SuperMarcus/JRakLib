package com.supermarcus.jraklib.network;

import com.supermarcus.jraklib.protocol.Packet;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

public class ProtocolSocket extends DatagramSocket {

    public ProtocolSocket(SocketAddress bindAddress) throws SocketException {
        super(bindAddress);
        this.setSendBufferSize(Packet.MAX_SIZE);
        this.setReceiveBufferSize(Packet.MAX_SIZE);
    }

    public boolean writePacket(Packet packet, SocketAddress target){
        try{
            byte[] data = packet.toRaw();
            DatagramPacket dPacket = new DatagramPacket(data, data.length, target);
            this.send(dPacket);
            return true;
        }catch (Exception ignore){}
        return false;
    }

    public ReceivedPacket readPacket(){
        try{
            DatagramPacket dPacket = new DatagramPacket(new byte[Packet.MAX_SIZE], Packet.MAX_SIZE);
            this.receive(dPacket);
            byte[] buf = dPacket.getData();
            if(buf[0] > 0){
                return new ReceivedPacket(dPacket);
            }
        }catch (Exception ignore){}
        return null;
    }

    public boolean isAlive(){
        return !this.isClosed() && this.isBound();
    }
}
