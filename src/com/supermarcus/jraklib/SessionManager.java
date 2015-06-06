package com.supermarcus.jraklib;

import com.supermarcus.jraklib.network.ReceivedPacket;
import com.supermarcus.jraklib.protocol.Packet;
import com.supermarcus.jraklib.protocol.PacketWrapper;
import com.supermarcus.jraklib.protocol.RawPacket;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager extends Thread {
    public static int MAX_PACKETS_PROCESS_PER_TICK = 500;

    private RakLib rakLib;

    private PacketWrapper wrapper;

    private ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<String, Session>();

    private boolean isShutdown = false;

    public SessionManager(RakLib rakLib){
        this.rakLib = rakLib;
        this.wrapper = new PacketWrapper();
        this.setName("RakLib Server Thread");
        this.start();
    }

    public void run(){
        while (this.isRunning()){
            this.tick();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void tick(){
        int max = SessionManager.MAX_PACKETS_PROCESS_PER_TICK;
        while (((--max) > 0) && this.processPacket());

    }

    private boolean processPacket(){
        ReceivedPacket packet = this.getRakLib().getSocket().readPacket();
        if(packet != null) {
            Packet wrappedPacket = this.getWrapper().wrapPacket(packet);
            if(wrappedPacket != null){
                this.getSession(packet.getSendAddress()).handlePacket(wrappedPacket);
            }else {
                this.getRakLib().getServer().offerPacket(new RawPacket(packet));
            }
            return true;
        }
        return false;
    }

    private RakLib getRakLib(){
        return this.rakLib;
    }

    public Session getSession(InetSocketAddress address){
        String id = address.getHostString() + ":" + address.getPort();
        if(!this.sessionMap.containsKey(id)){
            this.sessionMap.put(id, new Session(this, address));
        }
        return this.sessionMap.get(id);
    }

    public PacketWrapper getWrapper(){
        return this.wrapper;
    }

    public boolean isRunning(){
        return !this.isShutdown;
    }

    public void shutdown(){
        this.isShutdown = true;
        try {
            this.notifyAll();
            this.join();
        } catch (InterruptedException ignore) {}
    }
}
