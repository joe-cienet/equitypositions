package com.cienet.equityposition.core;

import java.util.concurrent.Callable;

/**
 * An simple callable task that returns a SimpleTransactionResponse and may throw an exception.</br>
 *  @param <SimpleTransactionResponse> the result type of method {@code call}
 */
//TODO:use an Wrapped Exception but not the Exception
public class SimpleTransactionCallable implements Callable<SimpleTransactionResponse> {
    private final SimpleTransactionRequest request;
    private final ResponseListener listener;
    private final TransactionEngine engine;
    
    public SimpleTransactionCallable(TransactionEngine engine,SimpleTransactionRequest request, ResponseListener listener) {
        this.request = request;
        this.listener = listener;
        this.engine = engine;
    }

    public SimpleTransactionResponse call() {
        SimpleTransactionResponse response = null;
        try {
            response = this.handleRequest(request);
        } catch (Exception ex) {
        	//TODO: return an default empty response
        }
        return response;
    }

    /**
     * Executes the operation of the current request.
     * @param request
     * @throws Exception
     */
    private SimpleTransactionResponse handleRequest(SimpleTransactionRequest request) throws Exception {
        SimpleTransactionResponse response = null;
        switch(request.getAction()){//should handle separately, here is the simple logic for all action
        case INSERT:
        case UPDATE:
        case CANCEL:
            this.engine.updateTradeData(request.getTrade());
            this.engine.updatePositionData(request.getTrade());
            response = new SimpleTransactionResponse(request.getId(),this.engine.listPositions());
            break;
        default:
            break;
        }
        if (listener != null) {
            listener.notifyResponse(response);
        }
        return response;
    }
    
    
}

