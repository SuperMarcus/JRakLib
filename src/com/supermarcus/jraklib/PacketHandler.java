package com.supermarcus.jraklib;

import com.supermarcus.jraklib.protocol.RawPacket;

public interface PacketHandler {
    public void onRawPacket(RawPacket packet);
}
