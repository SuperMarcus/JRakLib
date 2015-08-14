package com.supermarcus.jraklib.network;

import com.supermarcus.jraklib.protocol.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * UDP Socket for Minecraft: Pocket Edition network protocol
 */
public class ProtocolSocket extends DatagramSocket {
    public static final int DEFAULT_TIMEOUT = 50;

    private PriorityBlockingQueue<QueuePacket> sendBuffer = new PriorityBlockingQueue<QueuePacket>();

    public ProtocolSocket(SocketAddress bindAddress) throws SocketException {
        super(bindAddress);
        this.setSendBufferSize(Packet.MAX_SIZE);
        this.setReceiveBufferSize(Packet.MAX_SIZE);
        this.setSoTimeout(ProtocolSocket.DEFAULT_TIMEOUT);
    }

    /**
     * To queue a packet to send buffer
     *
     * @param packet Packet to send
     */
    public void writePacket(QueuePacket packet){
        this.sendBuffer.offer(packet);
    }

    /**
     * To send a packet
     *
     * @param packet Packet to send
     * @param target Target address
     * @param priority Send priority
     */
    public void writePacket(Packet packet, SocketAddress target, SendPriority priority){
        try{
            byte[] data = packet.toRaw();
            DatagramPacket dPacket = new DatagramPacket(data, data.length, target);
            this.sendBuffer.offer(new QueuePacket(dPacket, priority));
        }catch (Exception ignore){}
    }

    /**
     * To send a packet use normal priority
     *
     * @param packet Packet to send
     * @param target Target address
     */
    public void writePacket(Packet packet, SocketAddress target){
        this.writePacket(packet, target, SendPriority.NORMAL);
    }

    /**
     * To receive a packet
     *
     * @return Packet received
     */
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

    /**
     * Flush queue and send all the packets
     *
     * @throws IOException
     */
    public void flush() throws IOException {
        this.flush(-1);
    }

    /**
     * Flush queue and send packets
     *
     * @param packets Max number of packets to send
     * @throws IOException
     */
    public void flush(int packets) throws IOException {
        if(packets < 0){
            packets = Integer.MAX_VALUE;
        }
        QueuePacket packet;
        while(((--packets) >= 0) && ((packet = this.sendBuffer.poll()) != null)){
            this.writePacket(packet.getPacket());
        }
    }

    /**
     * Real send
     *
     * @param packet Instance of DatagramPacket
     * @throws IOException
     */
    private void writePacket(DatagramPacket packet) throws IOException {
        this.send(packet);
    }

    public boolean isAlive(){
        return !this.isClosed() && this.isBound();
    }
}
