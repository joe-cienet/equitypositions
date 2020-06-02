package com.cienet.equityposition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cienet.equityposition.core.TransactionEngine;

public class TransactionEngineTest {

    TransactionEngine engine;

    @Before
    public void before() {
        engine = new TransactionEngine();
    }

    @After
    public void after() {

    }

    @Test
    public void testStart() {
        boolean status = engine.start();
        assertTrue("Should start the engine ok", status);
    }


    @Test
    public void testStartAgain() {
        engine.start();
        boolean status = engine.start();
        assertFalse("Should not start the engine again", status);
    }

    @Test
    public void testClose() {
        engine.close();
        assertTrue("Should the engine is not alive", !engine.isActive());
        engine.start();
        engine.close();
        assertTrue("Should the engine is not alive", !engine.isActive());
        
    }

}
