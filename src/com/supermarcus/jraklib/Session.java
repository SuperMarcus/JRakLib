package com.supermarcus.jraklib;

import com.supermarcus.jraklib.network.RakLibInterface;
import com.supermarcus.jraklib.protocol.Packet;
import com.supermarcus.jraklib.protocol.raklib.UNCONNECTED_PING;
import com.supermarcus.jraklib.protocol.raklib.UNCONNECTED_PONG;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;

public class Session {
    private InetSocketAddress address;

    private SessionManager manager;

    private boolean isActive = false;

    private WeakReference<RakLibInterface> ownedInterface;

    public Session(SessionManager manager, InetSocketAddress address, RakLibInterface ownedInterface){
        this.address = address;
        this.manager = manager;
        this.ownedInterface = new WeakReference<RakLibInterface>(ownedInterface);
        System.out.println("new session");
    }

    public InetSocketAddress getAddress(){
        return this.address;
    }

    public RakLibInterface getOwnedInterface(){
        return this.ownedInterface.get();
    }

    public void handlePacket(Packet packet){
        switch (packet.getPacketIdentifier()){
            case UNCONNECTED_PING:
                UNCONNECTED_PONG pong = new UNCONNECTED_PONG();
                pong.setServerName(this.manager.getServerName());
                pong.setServerID(this.manager.getServerId());
                pong.setPingID(((UNCONNECTED_PING) packet).getPingID());
                this.sendPacket(pong);
                break;
        }
    }

    public void update(long millis){
        try{
            RakLibInterface rakLibInterface;
            try{
                rakLibInterface = this.getOwnedInterface();
                if(rakLibInterface == null){
                    this.close();
                    return;
                }
            }catch (Exception ignore){}

        }catch (Exception ignore){}//TODO: Add a message or something?
    }

    public void close(){
        this.manager.getSessionMap().removeSession(this.getAddress());
    }

    public void sendPacket(Packet pk){
        pk.encode();
        this.getOwnedInterface().getSocket().writePacket(pk, this.getAddress());
        System.out.println("send pk");
    }
}
