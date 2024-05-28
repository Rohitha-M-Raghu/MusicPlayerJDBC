//$Id$
package cache;

import java.util.HashMap;
import java.util.Map;

public class Cache {
	
	private Map<String, String> cacheStorage = new HashMap<>();
	
	public static Cache getInstance() {
		return CacheDataInstance.INSTANCE;
	}
	
	private static class CacheDataInstance {
		private static final Cache INSTANCE = new Cache();
	}
	
	public String getDataFromCache(String key) {
		return cacheStorage.get(key);
	}
	
	public void cacheData(String key, String value) {
		cacheStorage.put(key, value);
	}
	
	public void clearCache() {
		cacheStorage.clear();
	}
}
