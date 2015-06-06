package com.supermarcus.jraklib;

import com.supermarcus.jraklib.protocol.Packet;

import java.net.InetSocketAddress;

public class Session {
    private InetSocketAddress address;

    private SessionManager manager;

    private boolean isActive = false;

    public Session(SessionManager manager, InetSocketAddress address){
        this.address = address;
        this.manager = manager;
    }

    public InetSocketAddress getAddress(){
        return this.address;
    }

    public void handlePacket(Packet packet){

    }

    public void update(){
        
    }
}
