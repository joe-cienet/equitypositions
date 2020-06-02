package com.cienet.equityposition.core;

/**
 *An simple class of an Task finished callback
 */
public interface ResponseListener {

    /**
     * Notifies the listener of a response.
     * 
     * @param response
     */
    void notifyResponse(SimpleTransactionResponse response);

}
