package com.supermarcus.test;

import com.supermarcus.jraklib.MessageHandler;
import com.supermarcus.jraklib.PacketHandler;
import com.supermarcus.jraklib.RakLibServerInstance;
import com.supermarcus.jraklib.Session;
import com.supermarcus.jraklib.lang.ACKNotification;
import com.supermarcus.jraklib.lang.RawPacket;
import com.supermarcus.jraklib.lang.message.RakLibMessage;
import com.supermarcus.jraklib.lang.message.session.SessionOpenMessage;
import com.supermarcus.jraklib.network.SendPriority;
import com.supermarcus.jraklib.protocol.raklib.EncapsulatedPacket;
import com.supermarcus.test.protocol.BatchPacket;
import com.supermarcus.test.protocol.StrangePacket;

import java.net.InetSocketAddress;

public class RakLibServerTest extends RakLibServerInstance {
    public static long TEST_TIMEOUT = 2 * 60 * 1000;

    public static void main(String args[]){
        new RakLibServerTest();
    }

    public RakLibServerTest(){
        try {
            this.addMessageHandler(new MessageHandler() {
                @Override
                public void onMessage(RakLibMessage message) {
                    System.out.println(message);
                    if (message instanceof SessionOpenMessage) {
                        StrangePacket packet = new StrangePacket();
                        packet.setDestination(new InetSocketAddress("104.128.50.172", 19132));
                        System.out.println("\n\n\n----Transfering----\n\n");
                        ((SessionOpenMessage) message).getSession().sendPacket(packet, SendPriority.NORMAL);
                    }
                }
            });
            this.getSessionManager().setPacketHandler(new PacketHandler() {
                @Override
                public void onRawPacket(RawPacket packet) {
                    System.out.println("New Raw Packet #" + (packet.getData()[0] & 0xff));
                }

                @Override
                public void onACKNotification(ACKNotification notification) {
                    System.out.println("New ACK Notification");
                }

                @Override
                public void onEncapsulated(Session session, EncapsulatedPacket packet, int flags) {
                    System.out.println("New Encapsulated packet #" + (packet.getBuffer()[0] & 0xff) + " from " + session.getAddress());
                    if (packet.getBuffer()[0] == BatchPacket.NETWORK_ID) {
                        BatchPacket dataPacket = BatchPacket.fromBinary(packet.getBuffer());

                    }
                }
            });
            this.getSessionManager().addInterface(new InetSocketAddress("0.0.0.0", 19132));

            System.out.println("Test is running...");
            long startMillis = System.currentTimeMillis();
            while((System.currentTimeMillis() - startMillis) < TEST_TIMEOUT){
                System.out.println("Test is running for " + ((System.currentTimeMillis() - startMillis) / 1000) + "sec");
                Thread.sleep(10 * 1000);
            }
            System.out.println("Finish test");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
