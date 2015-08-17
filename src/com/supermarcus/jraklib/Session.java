package com.supermarcus.jraklib;

import com.supermarcus.jraklib.lang.BinaryConvertible;
import com.supermarcus.jraklib.lang.message.session.SessionCreateMessage;
import com.supermarcus.jraklib.network.RakLibInterface;
import com.supermarcus.jraklib.network.SendPriority;
import com.supermarcus.jraklib.protocol.Packet;

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
        manager.queueMessage(new SessionCreateMessage(this));
    }

    public InetSocketAddress getAddress(){
        return this.address;
    }

    public RakLibInterface getOwnedInterface(){
        return this.ownedInterface.get();
    }

    public void handlePacket(Packet packet){

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
        this.sendPacket(pk, SendPriority.NORMAL);
    }

    public void sendPacket(Packet pk, SendPriority priority){
        pk.encode();
        this.sendPacket((BinaryConvertible) pk, priority);
    }

    public void sendPacket(BinaryConvertible pk, SendPriority priority){
        this.getOwnedInterface().getSocket().writePacket(pk, this.getAddress(), priority);
    }
}
