package com.supermarcus.jraklib;

import com.supermarcus.jraklib.lang.BinaryConvertible;
import com.supermarcus.jraklib.lang.RecoveryDataPacket;
import com.supermarcus.jraklib.lang.message.session.SessionCloseMessage;
import com.supermarcus.jraklib.lang.message.session.SessionCreateMessage;
import com.supermarcus.jraklib.network.RakLibInterface;
import com.supermarcus.jraklib.network.SendPriority;
import com.supermarcus.jraklib.protocol.Packet;
import com.supermarcus.jraklib.protocol.raklib.*;
import com.supermarcus.jraklib.protocol.raklib.acknowledge.ACK;
import com.supermarcus.jraklib.protocol.raklib.acknowledge.AcknowledgePacket;
import com.supermarcus.jraklib.protocol.raklib.acknowledge.NACK;
import com.supermarcus.jraklib.protocol.raklib.data.DATA_PACKET_4;
import com.supermarcus.jraklib.protocol.raklib.data.DataPacket;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Session {
    public static final long UPDATE_TIMEOUT = 10 * 1000;

    public static final int MAX_MTU_SIZE = 1464;

    public static int WINDOW_SIZE = 1024 * 2;

    private InetSocketAddress address;

    private SessionManager manager;

    private boolean isActive = false;

    private WeakReference<RakLibInterface> ownedInterface;

    private State state = State.UNCONNECTED;

    private DATA_PACKET_4 sendQueue = new DATA_PACKET_4();

    private long lastUpdate = System.currentTimeMillis();

    private long clientID = 0L;

    private HashSet<Integer> ACKQueue = new HashSet<>();

    private HashSet<Integer> NACKQueue = new HashSet<>();

    private ConcurrentLinkedQueue<DataPacket> packetToSend = new ConcurrentLinkedQueue<>();

    private TreeMap<Integer, RecoveryDataPacket> recoveryQueue = new TreeMap<>();

    private HashMap<Integer, ArrayList<Integer>> needACK = new HashMap<>();

    private ArrayList<Integer> receivedWindow = new ArrayList<>();

    private int windowStart = 0;

    private int windowEnd = Session.WINDOW_SIZE;

    private int sendSeqNumber = 0;

    private int lastSeqNumber = -1;

    private int mtuSize = 548; //Min size

    public Session(SessionManager manager, InetSocketAddress address, RakLibInterface ownedInterface){
        this.address = address;
        this.manager = manager;
        this.ownedInterface = new WeakReference<>(ownedInterface);
        manager.queueMessage(new SessionCreateMessage(this));
    }

    public InetSocketAddress getAddress(){
        return this.address;
    }

    public RakLibInterface getOwnedInterface(){
        return this.ownedInterface.get();
    }

    public void handleEncapsulatedPacket(EncapsulatedPacket packet){

    }

    public void handlePacket(Packet packet){
        this.isActive = true;
        this.lastUpdate = System.currentTimeMillis();
        if((this.state == State.CONNECTED) || (this.state == State.CONNECTING_2)){
            if(packet instanceof DataPacket){
                System.out.println("DataPacket: " + packet.getClass().getSimpleName());//TODO
                if((((DataPacket) packet).getSeqNumber() < this.windowStart) || (((DataPacket) packet).getSeqNumber() > this.windowEnd) || this.receivedWindow.contains(((DataPacket) packet).getSeqNumber())){
                    return;
                }

                System.out.println("VDataPacket: " + packet.getClass().getSimpleName());//TODO

                int diff = ((DataPacket) packet).getSeqNumber() - this.lastSeqNumber;

                this.NACKQueue.remove(((DataPacket) packet).getSeqNumber());
                this.ACKQueue.add(((DataPacket) packet).getSeqNumber());
                this.receivedWindow.add(((DataPacket) packet).getSeqNumber());

                if(diff != 1){
                    for(int i = this.lastSeqNumber; i < ((DataPacket) packet).getSeqNumber(); ++i){
                        this.NACKQueue.add(i);
                    }
                }

                if(diff >= 1){
                    this.lastSeqNumber = ((DataPacket) packet).getSeqNumber();
                    this.windowStart += diff;
                    this.windowEnd += diff;
                }

                for (BinaryConvertible encapsulatedPacket : ((DataPacket) packet).getPackets()){
                    if(encapsulatedPacket instanceof EncapsulatedPacket){
                        this.handleEncapsulatedPacket((EncapsulatedPacket) encapsulatedPacket);
                    }
                }
            }else if(packet instanceof AcknowledgePacket){
                if(packet instanceof ACK){
                    for(Integer seq : ((ACK) packet).getPackets()){
                        if(this.recoveryQueue.containsKey(seq)){
                            for(BinaryConvertible binPk : this.recoveryQueue.get(seq).getPacket().getPackets()){
                                if((binPk instanceof EncapsulatedPacket) && (((EncapsulatedPacket) binPk).needACK()) && (null != ((EncapsulatedPacket) binPk).getMessageIndex())){
                                    this.needACK.get(((EncapsulatedPacket) binPk).getIdentifierACK()).remove(((EncapsulatedPacket) binPk).getMessageIndex());
                                }
                            }
                            this.recoveryQueue.remove(seq);
                        }
                    }
                }else if(packet instanceof NACK){
                    for(Integer seq : ((NACK) packet).getPackets()){
                        if(this.recoveryQueue.containsKey(seq)){
                            DataPacket pk = this.recoveryQueue.get(seq).getPacket();
                            pk.setSeqNumber(this.sendSeqNumber++);
                            this.packetToSend.add(pk);
                            this.recoveryQueue.remove(seq);
                        }
                    }
                }
            }
        }

        if(packet.getNetworkID() > 0x00){
            if(packet instanceof OPEN_CONNECTION_REQUEST_1){
                OPEN_CONNECTION_REPLY_1 reply = new OPEN_CONNECTION_REPLY_1();
                reply.setMtuSize(((OPEN_CONNECTION_REQUEST_1) packet).getMtuSize());
                System.out.println("Mtu size: " + ((OPEN_CONNECTION_REQUEST_1) packet).getMtuSize());//TODO
                reply.setServerID(this.manager.getServerId());
                this.sendPacket(reply);
                this.state = State.CONNECTING_1;
            }else if(this.state == State.CONNECTING_1 && packet instanceof OPEN_CONNECTION_REQUEST_2){
                this.clientID = ((OPEN_CONNECTION_REQUEST_2) packet).getClientID();
                if((((OPEN_CONNECTION_REQUEST_2) packet).getServerAddress().getPort() == this.getOwnedInterface().getSocket().getPort()) || !this.manager.isPortChecking()){
                    this.setMtuSize(Math.min(Math.abs(((OPEN_CONNECTION_REQUEST_2) packet).getMtuSize()), Session.MAX_MTU_SIZE));
                    System.out.println("Address: " + ((OPEN_CONNECTION_REQUEST_2) packet).getServerAddress() + " Mtu size: " + this.getMtuSize());//TODO
                    OPEN_CONNECTION_REPLY_2 reply = new OPEN_CONNECTION_REPLY_2();
                    reply.setMtuSize(this.getMtuSize());
                    reply.setServerID(this.manager.getServerId());
                    reply.setClientAddress(this.getAddress());
                    this.sendPacket(reply);
                    this.state = State.CONNECTING_2;
                }
            }
        }
    }

    public void sendQueue(){
        if(this.sendQueue.countPackets() > 0){
            this.sendQueue.setSeqNumber(this.sendSeqNumber++);
            this.sendPacket(this.sendQueue);
            this.recoveryQueue.put(this.sendQueue.getSeqNumber(), new RecoveryDataPacket(this.sendQueue, System.currentTimeMillis()));
            this.sendQueue = new DATA_PACKET_4();
        }
    }

    public void update(final long millis){
        try{
            RakLibInterface rakLibInterface;

            try{
                rakLibInterface = this.getOwnedInterface();

                if(rakLibInterface == null){
                    this.close();
                    return;
                }
            }catch (Exception ignore){}

            if(!this.isActive && ((this.lastUpdate + Session.UPDATE_TIMEOUT) < millis)){
                this.close(SessionCloseMessage.Reason.TIMEOUT);
            }

            this.isActive = false;

            if(!this.ACKQueue.isEmpty()){
                ACK pk = new ACK();
                pk.addPackets(this.ACKQueue);
                this.sendPacket(pk);
                this.ACKQueue.clear();
            }

            if(!this.NACKQueue.isEmpty()){
                NACK pk = new NACK();
                pk.addPackets(this.NACKQueue);
                this.sendPacket(pk);
                this.NACKQueue.clear();
            }

            if(!this.packetToSend.isEmpty()){
                int limit = 16;
                while(((--limit) >= 0) && !this.packetToSend.isEmpty()){
                    DataPacket pk = this.packetToSend.poll();
                    RecoveryDataPacket rpk = new RecoveryDataPacket(pk, millis);
                    this.recoveryQueue.put(rpk.getSeqNumber(), rpk);
                    this.sendPacket(pk);
                }
                if(this.packetToSend.size() > Session.WINDOW_SIZE){
                    this.packetToSend.clear();
                }
            }

            if(!this.needACK.isEmpty()){
                final HashSet<Integer> needToRemove = new HashSet<>();
                this.needACK.forEach(new BiConsumer<Integer, ArrayList<Integer>>() {
                    @Override
                    public void accept(Integer identifier, ArrayList<Integer> indexes) {
                        if(indexes.isEmpty()){
                            needToRemove.add(identifier);
                        }
                    }
                });
                for(Integer identifier : needToRemove){
                    this.needACK.remove(identifier);
                    this.manager.notifyACK(this, identifier);
                }
            }

            if(!this.recoveryQueue.isEmpty()){
                final HashSet<RecoveryDataPacket> needToRecovery = new HashSet<>();
                this.recoveryQueue.forEach(new BiConsumer<Integer, RecoveryDataPacket>() {
                    @Override
                    public void accept(Integer seq, RecoveryDataPacket pk) {
                        if (pk.getSendTime() < (millis - 8)) {
                            needToRecovery.add(pk);
                        }
                    }
                });
                for(RecoveryDataPacket pk : needToRecovery){
                    this.recoveryQueue.remove(pk.getSeqNumber());
                    this.packetToSend.add(pk.getPacket());
                }
            }

            this.receivedWindow.removeIf(new Predicate<Integer>() {
                @Override
                public boolean test(Integer seq) {
                    return (seq < Session.this.windowStart);
                }
            });

            this.sendQueue();
        }catch (Exception ignore){}//TODO: Add a message or something?
    }

    public void close(){
        this.manager.getSessionMap().removeSession(this.getAddress());
    }

    public void close(SessionCloseMessage.Reason reason){
        this.manager.queueMessage(new SessionCloseMessage(reason, this));
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

    public long getClientID() {
        return clientID;
    }

    public int getMtuSize() {
        return mtuSize;
    }

    public void setMtuSize(int mtuSize) {
        this.mtuSize = mtuSize;
    }

    public enum State {
        UNCONNECTED(0),
        CONNECTING_1(1),
        CONNECTING_2(2),
        CONNECTED(3);

        private int value;

        State(int value){
            this.value = value;
        }

        public int getState(){
            return this.value;
        }
    }
}
