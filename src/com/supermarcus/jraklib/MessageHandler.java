package com.supermarcus.jraklib;

import com.supermarcus.jraklib.lang.message.RakLibMessage;

public interface MessageHandler {
    public void onMessage(RakLibMessage message);
}