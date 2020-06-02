package com.cienet.equityposition.core;

import com.cienet.equityposition.entity.Trade;
import com.cienet.equityposition.entity.TradeAction;
/**
 * An simple class represents one Transaction request<br/>
 * The RequestContext are not used <br/>
 */
public class SimpleTransactionRequest {

    final int id;
    final Trade trade;

    public SimpleTransactionRequest(int newId, Trade newTrade) {
        id = newId;
        trade = newTrade;
    }

    public int getId(){
        return this.id;
    }
    
    public Trade getTrade() {
        return this.trade;
    }
    
    public TradeAction getAction(){
        return this.trade.getAction();
    }

    @Override
    public String toString() {
        return "Transaction [id=" + id +"] ,"+ trade.toString();
    }

}
