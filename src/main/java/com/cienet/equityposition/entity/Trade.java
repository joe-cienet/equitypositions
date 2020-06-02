package com.cienet.equityposition.entity;

/**
 * POJO class represents one trade in an Transaction<br/>
 */
public class Trade {
    final Integer id;
    final Integer version;
    final String secCode;
    final Integer quantity;
    final TradeAction action;
    final Boolean buy; 
   
	public Trade(Integer id, Integer version, String secCode, Integer quantity, TradeAction action, Boolean buy) {
		super();
		this.id = id;
		this.version = version;
		this.secCode = secCode;
		this.quantity = quantity;
		this.action = action;
		this.buy = buy;
	}

	public Integer getId() {
		return id;
	}

	public Integer getVersion() {
		return version;
	}

	public String getSecCode() {
		return secCode;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public TradeAction getAction() {
		return action;
	}

	public Boolean isBuy() {
		return buy;
	}

	@Override
    public String toString() {
        return "Trade [id=" + id + ", version=" + version + ", secCode=" + secCode + ", quantity=" + quantity
            + ", action=" + action + ", buy=" + buy + "]";
    }
}
