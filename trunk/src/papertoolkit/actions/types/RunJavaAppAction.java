package papertoolkit.actions.types;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import papertoolkit.actions.R3Action;


/**
 * <p>
 * Runs the main method of a java class.
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class RunJavaAppAction implements R3Action {

	private Class<?> classToRun;

	/**
	 * @param classWithMainFunction
	 */
	public RunJavaAppAction(Class classWithMainFunction) {
		classToRun = classWithMainFunction;
	}

	/**
	 * Invokes the main method of the given class.
	 * 
	 * @see papertoolkit.actions.R3Action#invoke()
	 */
	public void invoke() {
		try {
			Method method = classToRun.getMethod("main", new Class[] { String[].class });
			method.invoke(null, new Object[] { new String[] {} });
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}