package org.docear.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.docear.metadata.data.MetaData;
import org.docear.metadata.engines.SearchEngine;
import org.docear.metadata.events.MetaDataEvent;
import org.docear.metadata.events.MetaDataListener;
import org.docear.metadata.extractors.ExtractorConfigKey;
import org.docear.metadata.extractors.MalformedConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaDataSearchHub {
	
	protected final static Logger logger = LoggerFactory.getLogger(MetaDataSearchHub.class);
	public final static ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	private Map<Class<?>, SearchEngine> engines = new HashMap<Class<?>, SearchEngine>();
		
	public MetaDataSearchHub registerSearchEngine(SearchEngine engine){
		this.engines.put(engine.getClass(), engine);
		return this;
	}
	
	public MetaDataSearchHub unregisterSearchEngine(SearchEngine engine){
		this.engines.remove(engine.getClass());
		return this;
	}
	
	public Set<Class<?>> getRegisteredEngines(){
		return this.engines.keySet();
	}
	
	public boolean isRegistered(Class<?> engineClass){
		return this.engines.containsKey(engineClass);
	}
	
	public int getActiveTasks(){
		return executor.getActiveCount();
	}
	
	public BlockingQueue<Runnable> getTaskQueue(){
		return executor.getQueue();
	}
	
	public List<Runnable> shutdownNow(){
		return executor.shutdownNow();
	}
	
	public Collection<MetaData> search(String query, Set<Class<?>> useEngines, Map<ExtractorConfigKey, Object> options) throws MalformedConfigException, IOException{
		Collection<MetaData> results = new ArrayList<MetaData>();
		List<Callable<Collection<MetaData>>> extractors = getExtractors(query, useEngines, options, null);
		try {
			List<Future<Collection<MetaData>>> tasks = executor.invokeAll(extractors);
			for(Future<Collection<MetaData>> task : tasks){				
				results.addAll(task.get());
			}
		} catch (InterruptedException e) {
			logger.warn("Exception occured in blockedSearchThread", e);
		} catch (ExecutionException e) {
			Throwable t = e.getCause();
			if(t != null && t instanceof IOException){
				throw (IOException)t;
			}
			logger.warn("Exception occured in blockedSearchThread", e);
		}
		return results;		
	}	
	
	public void asyncSearch(String query, Set<Class<?>> useEngines, Map<ExtractorConfigKey, Object> options, final  MetaDataListener listener) throws MalformedConfigException{
		MetaDataListener syncListenerWrapper = new MetaDataListener() {
			
			public synchronized void onFinishedRequest(MetaDataEvent event) {
				if(listener != null){
					listener.onFinishedRequest(event);
				}
			}
			
			public synchronized void onCaptchaRequested(MetaDataEvent event) {
				if(listener != null){
					listener.onCaptchaRequested(event);
				}
			}
		};
		
		List<Callable<Collection<MetaData>>> extractors = getExtractors(query, useEngines, options, syncListenerWrapper);
		try{
			for(Callable<Collection<MetaData>> extractor : extractors){
				executor.submit(extractor);
			}
		}catch(Exception e){
			logger.warn(e.getMessage());
		}
	}
	
	private List<Callable<Collection<MetaData>>> getExtractors(String query, Set<Class<?>> useEngines, Map<ExtractorConfigKey, Object> options, MetaDataListener listener)	throws MalformedConfigException {
		if(options == null) options = new HashMap<ExtractorConfigKey, Object>();
		List<Callable<Collection<MetaData>>> extractors = new ArrayList<Callable<Collection<MetaData>>>();		
		for(Class<?> engine : useEngines){
			SearchEngine searchEngine = engines.get(engine);
			if(searchEngine != null){
				extractors.add(searchEngine.getExtractor(query, options, listener));
			}
		}
		return extractors;
	}
}
