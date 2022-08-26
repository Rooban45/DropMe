package com.poc.dropme.Gadgets.SmsBroadcast;

public interface MessageListenerPassenger {

    /**
     * To call this method when new message received and send back
     * @param message Message
     */
    void PassengerMessageReceived(String message);
}
