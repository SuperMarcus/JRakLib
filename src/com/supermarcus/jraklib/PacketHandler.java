package com.supermarcus.jraklib;

import com.supermarcus.jraklib.lang.ACKNotification;
import com.supermarcus.jraklib.protocol.RawPacket;

public interface PacketHandler {
    void onRawPacket(RawPacket packet);
    void onACKNotification(ACKNotification notification);
}
