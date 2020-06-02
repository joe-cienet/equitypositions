package com.cienet.equityposition.entity;

/**
 * Represents a trade action in a transaction:<br/>
 * INSERT always be 1st version of a Trade, <br/>
 * CANCEL always be last version of Trade. <br/>
 */
public enum TradeAction {
	INSERT("INSERT"),
	UPDATE("UPDATE"),
	CANCEL("CANCEL");

	public final String value;

	private TradeAction(String value) {
		this.value = value;
	}

	public boolean isInsertAction() {
		return this == INSERT;
	}
	
	public boolean isUpdateAction() {
		return this == UPDATE;
	}

	public boolean isCancelAction() {
		return this == CANCEL;
	}

}
