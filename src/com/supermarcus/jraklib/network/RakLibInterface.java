package com.supermarcus.jraklib.network;

import com.supermarcus.jraklib.SessionManager;
import com.supermarcus.jraklib.lang.message.server.*;
import com.supermarcus.jraklib.protocol.Packet;
import com.supermarcus.jraklib.protocol.RawPacket;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;

public class RakLibInterface extends Thread{
    public static final byte[] MAGIC = new byte[]{0, -1, -1, 0, -2, -2, -2, -2, -3, -3, -3, -3, 18, 52, 86, 120};

    public static final Charset STRING_DEFAULT_CHARSET = Charset.forName("UTF-8");

    public static final long NORMAL_TICK = 50;

    public static final int MAX_PACKET_PER_TICK = 500;

    private ProtocolSocket socket;

    private SessionManager sessionManager;

    private boolean running = false;

    private long startTime = 0L;

    private int serverId;

    private int tickCounterPointer = 0;

    volatile private long[] averageTickMillis = new long[]{0, 0, 0, 0, 0, 0};

    public RakLibInterface(InetSocketAddress serverAddress, SessionManager manager, int serverId) throws SocketException {
        this.socket = new ProtocolSocket(serverAddress);
        this.sessionManager = manager;
        this.serverId = serverId;
        this.start();
    }

    public void run(){
        this.running = true;
        synchronized (this){
            this.startTime = System.currentTimeMillis();
        }
        this.getSessionManager().queueMessage(new InterfaceStartMessage(this.getStartTimeMillis(), this));
        try{
            while(this.isRunning()){
                long tickStart = System.currentTimeMillis();

                this.onTick();

                long current = System.currentTimeMillis();
                long sleepMillis = (tickStart + RakLibInterface.NORMAL_TICK - current);
                long takes = current - tickStart;
                if(sleepMillis > RakLibInterface.NORMAL_TICK){
                    this.getSessionManager().queueMessage(new TimeWarningMessage(this));
                    sleepMillis = RakLibInterface.NORMAL_TICK;
                }else if(sleepMillis <= 0){
                    this.getSessionManager().queueMessage(new ServerOverloadedMessage(takes, this));
                    sleepMillis = 1;
                }

                this.averageTickMillis[(this.tickCounterPointer = ((++this.tickCounterPointer) % this.averageTickMillis.length))] = takes;

                Thread.sleep(sleepMillis);
            }
        }catch (Throwable t){
            this.getSessionManager().queueMessage(new InterfaceInterruptMessage(t, this));
        }
        this.getSessionManager().queueMessage(new InterfaceShutdownMessage(System.currentTimeMillis(), this));
        this.running = false;
    }

    public boolean receivePacket(){
        ReceivedPacket packet = this.getSocket().readPacket();
        if(packet != null){
            Packet wrap = this.getSessionManager().getWrapper().wrapPacket(packet);
            if(wrap != null){
                try{
                    wrap.decode();
                    System.out.println("Received new " + wrap.getClass().getSimpleName());
                    for(byte b : packet.getRawData()){
                        System.out.print(b + " ");
                    }
                    this.getSessionManager().getSessionMap().getSession(packet.getSendAddress(), this).handlePacket(wrap);
                }catch (Exception ignore){}
            }else{
                this.getSessionManager().queueRaw(new RawPacket(packet.getRawData(), packet.getSendAddress()));
            }
            return true;
        }
        return false;
    }

    public double getLoad(){
        long millisSum = 0;
        for(long t : this.averageTickMillis){
            millisSum += t;
        }
        return Math.max(((millisSum / this.averageTickMillis.length) / RakLibInterface.NORMAL_TICK), 1D);
    }

    public void onTick(){
        int max = RakLibInterface.MAX_PACKET_PER_TICK;
        while((max > 0) && this.receivePacket())--max;
    }

    public boolean isRunning(){
        return this.running;
    }

    public void shutdown(){
        this.running = false;
    }

    public int getServerId(){
        return serverId;
    }

    public SessionManager getSessionManager(){
        return this.sessionManager;
    }

    public ProtocolSocket getSocket(){
        return this.socket;
    }

    public boolean isTerminated(){
        return !(this.getSocket().isAlive() && this.isAlive());
    }

    public long getStartTimeMillis(){
        return this.startTime;
    }

    public boolean equals(Object object){
        return (object instanceof RakLibInterface) && (((RakLibInterface) object).getServerId() == this.getServerId());
    }

    public void finalize(){
        try {
            super.finalize();
            this.getSocket().close();
            this.interrupt();
        } catch (Throwable ignore) {}
    }
}
