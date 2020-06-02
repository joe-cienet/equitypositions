package com.cienet.equityposition.core;

import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import com.cienet.equityposition.entity.Trade;
import com.cienet.equityposition.entity.TradeAction;

/**
 * Core engine to accept incoming transactions and submit the wrap request to an
 * simple callable task </br>
 * The task handle the transaction request according the operation and
 * wirte/update the database</br>
 * Once task finished , the callback listener will be notified.
 * TODO:Use an Wrapped Exception but not the exception
 * TODO:should use the configured param but not the default value
 */
public class TransactionEngine implements ResponseListener, AutoCloseable {

	public static final int DEFAULT_MIN_THREADS = 20;
	public static final int DEFAULT_MAX_THREADS = 200;
	public static final int DEFAULT_MAX_IDLE_TIME = 60 * 1000; // 1 minutes
	public static final int DEFAULT_MAP_SIZE = 100;
	protected volatile boolean started = false;
	private ThreadPoolExecutor executor;

	// simple database
	private final ConcurrentHashMap<Integer, Trade> trades;
	private final ConcurrentHashMap<String, Integer> positions;

	public TransactionEngine() {
		this.trades = new ConcurrentHashMap<Integer, Trade>(DEFAULT_MAP_SIZE);
		this.positions = new ConcurrentHashMap<String, Integer>(DEFAULT_MAP_SIZE);
	}

	public boolean start() {
		if (!started) {
			started = true;
			//TODO:use an customized ThreadPoolExecutor instead of the default
			executor = new ThreadPoolExecutor(DEFAULT_MIN_THREADS, DEFAULT_MAX_THREADS, DEFAULT_MAX_IDLE_TIME,
					TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory("EQUITY-POSITION"));
			return true;
		} else {
			return false;
		}

	}

	public SimpleTransactionResponse execute(SimpleTransactionRequest request) throws Exception {
		return execute(request, DEFAULT_MAX_IDLE_TIME);
	}

	public SimpleTransactionResponse execute(SimpleTransactionRequest request, long executionTimeout) throws Exception {
		Future<SimpleTransactionResponse> future = null;
		try {
			SimpleTransactionCallable callable = new SimpleTransactionCallable(this, request, this);
			future = executor.submit(callable);
			SimpleTransactionResponse response = getResponse(future, executionTimeout);
			return response;
		} catch (RejectedExecutionException ex) {
			// should wrap this exception
			throw ex;
		} catch (Exception e) {
			throw e;
		} finally {
			if (future != null) {
				future.cancel(true);
			}
		}
	}

	private SimpleTransactionResponse getResponse(Future<SimpleTransactionResponse> future, long executionTimeout)
			throws Exception {
		SimpleTransactionResponse response = null;
		try {
			response = future.get(executionTimeout, TimeUnit.MILLISECONDS);
		}
		// TODO:should handle each exception separately and throw an new wrapped
		// exception
		catch (TimeoutException | CancellationException | InterruptedException | ExecutionException ex) {
			throw new Exception(ex);
		}
		return response;
	}

	/**
	 *Simple ThreadFactory to create an specified prefix name
	 */
	class DefaultThreadFactory implements ThreadFactory {
		private final ThreadGroup threadGroup;
		private final AtomicInteger currentThreadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private boolean isDaemon = false;
		private static final String DEFAULT_THREAD_FACTORY_NAME = "default";

		public DefaultThreadFactory() {
			this(DEFAULT_THREAD_FACTORY_NAME);
		}

		public DefaultThreadFactory(String prefix) {
			this(prefix, false);
		}

		public DefaultThreadFactory(String prefix, boolean isDaemon) {
			SecurityManager s = System.getSecurityManager();
			this.threadGroup = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			this.namePrefix = prefix + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(threadGroup, r, namePrefix + currentThreadNumber.getAndIncrement(), 0);
			thread.setDaemon(isDaemon);
			return thread;
		}
	}

	/**
	 * Update the trade database with an new Trade Thread Safe
	 * 
	 * @param trade
	 */
	public void updateTradeData(Trade trade) {
		Trade value = trades.get(trade.getId());
		if (value == null) {
			synchronized (trades) {
				value = trades.get(trade.getId());
				if (value == null) {
					trades.put(trade.getId(), trade);
				}
			}
		} else {
			synchronized (trades) {
				value = trades.get(trade.getId());
				if (value == null) {
					trades.put(trade.getId(), trade);
				} else {
					if (!value.getAction().isCancelAction()) {
						trades.put(trade.getId(), trade);
					} else {
						// CANCEL will always be last version of Trade. Should never go here
						// TODO: log/exception or validate data before going here
					}
				}
			}
		}
	}

	/**
	 * Update the position database with an new trade Thread Safe
	 * 
	 * @param trade
	 */
	public void updatePositionData(Trade trade) {
		Integer value = positions.get(trade.getSecCode());
		if (value == null) {
			synchronized (trades) {
				value = positions.get(trade.getSecCode());
				if (value == null) {
					Integer quant = trade.isBuy() ? trade.getQuantity() : -trade.getQuantity();
					positions.put(trade.getSecCode(), quant);
				}
			}
		} else {
			synchronized (trades) {
				if (trade.getAction().isCancelAction()) {
					positions.put(trade.getSecCode(), 0);
				} else if (trade.getAction().isUpdateAction()) {
					Integer quant = trade.isBuy() ? trade.getQuantity() : -trade.getQuantity();
					positions.put(trade.getSecCode(), quant);
				} else { // INSERT
					Integer quant = positions.get(trade.getSecCode());
					quant += trade.isBuy() ? trade.getQuantity() : -trade.getQuantity();
					positions.put(trade.getSecCode(), quant);
				}
			}
		}
	}

	/**
	 * Simple functions that generate the response data
	 * 
	 * @return
	 */
	public String listPositions() {
		StringBuffer response = new StringBuffer("Position \r\n");
		for (Entry<String, Integer> entry : positions.entrySet()) {
			response.append(entry.getKey()).append(":").append(entry.getValue()).append("\r\n");
		}
		return response.toString();
	}

	@Override
	public synchronized void close() {
		if (!started) {
			return;
		}
		started = false;
		this.executor.shutdown();
	}

	public boolean isActive() {
		return this.started;
	}

	@Override
	public void notifyResponse(SimpleTransactionResponse response) {
		// simple print the response
		System.out.println(response.toString());
	}

	public static void main(String[] args) {

		final TransactionEngine engine = new TransactionEngine();
		engine.start();
		SimpleTransactionRequest[] reqs = {
				new SimpleTransactionRequest(1, new Trade(1, 1, "REL", 50, TradeAction.INSERT, true)),
				new SimpleTransactionRequest(2, new Trade(1, 2, "ITC", 40, TradeAction.INSERT, false)),
				new SimpleTransactionRequest(3, new Trade(1, 2, "INF", 70, TradeAction.INSERT, true)),
				new SimpleTransactionRequest(4, new Trade(1, 2, "REL", 60, TradeAction.UPDATE, true)),
				new SimpleTransactionRequest(5, new Trade(1, 2, "ITC", 30, TradeAction.CANCEL, true)),
				new SimpleTransactionRequest(6, new Trade(1, 2, "INF", 20, TradeAction.INSERT, false)) };
		try {
			for (SimpleTransactionRequest req : reqs) {
				engine.execute(req);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				engine.close();
			}
		}));
		System.exit(0);
	}
}
