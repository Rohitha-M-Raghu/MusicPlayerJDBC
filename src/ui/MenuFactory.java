//$Id$
package ui;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MenuFactory {
	
	private class MethodData {
		private String className;
		private String methodName;
		
		public String getClassName() {
			return className;
		}

		public String getMethodName() {
			return methodName;
		}

		public MethodData(String className, String methodName) {
			super();
			this.className = className;
			this.methodName = methodName;
		}
		
	}
	private final  Map<Integer, MethodData> methodToInvoke;
	
	MenuFactory() {
		methodToInvoke = new HashMap<>();
		methodToInvoke.put(25, new MethodData("queue.SongQueueAPIImpl", "clearQueue"));
		methodToInvoke.put(27, new MethodData("user_preference.UserPreferenceAPIImpl", "changeUserTheme"));
	}
	
	public void invokeMethod(int choice) {
        try {
        	if(methodToInvoke.get(choice) == null) {
        		System.out.println("No methd found to invoke");
        		return;
        	}
            Class<?> cls = Class.forName(methodToInvoke.get(choice).getClassName());
            Method method = cls.getDeclaredMethod(methodToInvoke.get(choice).getMethodName());
            method.invoke(cls.getMethod("getInstance").invoke(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
