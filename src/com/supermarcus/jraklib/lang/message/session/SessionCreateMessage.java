package com.supermarcus.jraklib.lang.message.session;

import com.supermarcus.jraklib.Session;
import com.supermarcus.jraklib.lang.message.MessageLevel;

public class SessionCreateMessage extends SessionMessage {
    public SessionCreateMessage(Session session) {
        super(MessageLevel.INFO, session);
    }
}
