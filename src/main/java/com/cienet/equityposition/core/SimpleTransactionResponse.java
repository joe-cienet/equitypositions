
package com.cienet.equityposition.core;

/**
 * The result type of the SimpleTransactionCallable</br>
 */
public class SimpleTransactionResponse {

    Integer id ;
    
    String userData;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSimleResponse() {
        return userData;
    }

    public void setSimleResponse(String simleResponse) {
        this.userData = simleResponse;
    }

    public SimpleTransactionResponse(Integer id, String simleResponse) {
        super();
        this.id = id;
        this.userData = simleResponse;
    }
    
    public String toString(){
        return "TransactionResponse [id=" + id + "]  \r\n " + userData;
        
    }
}
