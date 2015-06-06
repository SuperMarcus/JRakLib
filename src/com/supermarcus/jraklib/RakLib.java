package com.supermarcus.jraklib;

import com.supermarcus.jraklib.network.ProtocolSocket;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;

public class RakLib {
    public static final byte[] MAGIC = new byte[]{(byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0xfe, (byte) 0xfe, (byte) 0xfe, (byte) 0xfe, (byte) 0xfd, (byte) 0xfd, (byte) 0xfd, (byte) 0xfd, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78};

    public static final Charset STRING_DEFAULT_CHARSET = Charset.forName("UTF-8");

    private ProtocolSocket socket;

    private RakLibServerInstance server;

    private SessionManager sessionManager;

    public RakLib(RakLibServerInstance serverInstance) throws SocketException {
        this(serverInstance, new InetSocketAddress("0.0.0.0", 19132));
    }

    public RakLib(RakLibServerInstance serverInstance, SocketAddress serverAddress) throws SocketException {
        this.socket = new ProtocolSocket(serverAddress);
        this.server = serverInstance;
        this.sessionManager = new SessionManager(this);
    }

    public RakLibServerInstance getServer(){
        return this.server;
    }

    public SessionManager getSessionManager(){
        return this.sessionManager;
    }

    public ProtocolSocket getSocket(){
        return this.socket;
    }
}
