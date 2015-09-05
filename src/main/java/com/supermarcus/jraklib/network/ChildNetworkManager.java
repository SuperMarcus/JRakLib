package com.supermarcus.jraklib.network;

public class ChildNetworkManager extends NetworkManager {
    private NetworkManager parent;

    private RakLibInterface owner;

    public ChildNetworkManager(NetworkManager parent, RakLibInterface owner){
        this.parent = parent;
        this.owner = owner;
    }

    public void onSocketSend(long sendBytes){
        this.addSendBytes(sendBytes);
    }

    public void onSocketRead(long readBytes){
        this.addReceivedBytes(readBytes);
    }

    protected void addSendBytes(long bytes){
        super.addSendBytes(bytes);
        this.getParent().addSendBytes(bytes);
    }

    protected void addReceivedBytes(long bytes){
        super.addReceivedBytes(bytes);
        this.getParent().addReceivedBytes(bytes);
    }

    public NetworkManager getParent() {
        return parent;
    }

    public RakLibInterface getOwner() {
        return owner;
    }
}
