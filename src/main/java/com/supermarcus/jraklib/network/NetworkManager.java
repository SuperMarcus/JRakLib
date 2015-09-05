package com.supermarcus.jraklib.network;

import com.supermarcus.jraklib.protocol.Packet;

import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkManager {
    public static final int CALCULATE_MAX_QUEUE = 64;

    private long sendBytes = 0L;

    private long receivedBytes = 0L;

    private long lastSendBytes = 0L;

    private long lastReceivedBytes = 0L;

    private long lastCalculated = 0L;

    private double receiveSpeed = 0;

    private double sendSpeed = 0;

    private ConcurrentLinkedQueue<Double> sendCalculateQueue = new ConcurrentLinkedQueue<>();

    private ConcurrentLinkedQueue<Double> receiveCalculateQueue = new ConcurrentLinkedQueue<>();

    public void doUpdate(long millis){
        synchronized (this){
            long timeSpend = millis - this.lastCalculated;

            if(timeSpend > 0){
                double lastReceivedBytesPerSec = ((double) (this.lastReceivedBytes * 1000)) / ((double) timeSpend);
                double lastSendBytesPerSec = ((double) (this.lastSendBytes * 1000)) / ((double) timeSpend);

                this.sendCalculateQueue.offer(lastSendBytesPerSec);
                this.receiveCalculateQueue.offer(lastReceivedBytesPerSec);

                if(this.sendCalculateQueue.size() >= NetworkManager.CALCULATE_MAX_QUEUE || this.receiveCalculateQueue.size() >= NetworkManager.CALCULATE_MAX_QUEUE){
                    while(this.sendCalculateQueue.size() >= NetworkManager.CALCULATE_MAX_QUEUE){
                        this.sendCalculateQueue.poll();
                    }
                    while(this.receiveCalculateQueue.size() >= NetworkManager.CALCULATE_MAX_QUEUE){
                        this.receiveCalculateQueue.poll();
                    }
                }

                for(Double cSendBytes : this.sendCalculateQueue){
                    this.sendSpeed = (this.sendSpeed + cSendBytes) / 2;
                }

                for(Double cReceiveBytes : this.receiveCalculateQueue){
                    this.receiveSpeed = (this.receiveSpeed + cReceiveBytes) / 2;
                }

                this.lastReceivedBytes = 0;
                this.lastSendBytes = 0;

                if((this.sendBytes > (Long.MAX_VALUE - (Packet.MAX_SIZE * 16))) || (this.receivedBytes > (Long.MAX_VALUE - (Packet.MAX_SIZE * 16)))){
                    this.clearCounter0();
                }
            }

            this.lastCalculated = millis;
        }
    }

    protected void addSendBytes(long bytes){
        synchronized (this){
            this.sendBytes += bytes;
            this.lastSendBytes += bytes;
        }
    }

    protected void addReceivedBytes(long bytes){
        synchronized (this){
            this.receivedBytes += bytes;
            this.lastReceivedBytes += bytes;
        }
    }

    public void clearCounter(){
        synchronized (this){
            this.clearCounter0();
        }
    }

    public long getSendedBytes() {
        return sendBytes;
    }

    public long getReceivedBytes() {
        return receivedBytes;
    }

    public double getSendSpeed(){
        return sendSpeed;
    }

    public double getReceiveSpeed(){
        return receiveSpeed;
    }

    /**
     * While holding lock
     */
    private void clearCounter0(){
        this.receiveCalculateQueue.clear();
        this.sendCalculateQueue.clear();
        this.sendBytes = 0;
        this.receivedBytes = 0;
        this.lastSendBytes = 0;
        this.lastReceivedBytes = 0;
        this.sendSpeed = 0;
        this.receiveSpeed = 0;
    }
}
