package edu.stanford.hci.r3.pen;

import edu.stanford.hci.r3.pen.streaming.PenListener;
import edu.stanford.hci.r3.pen.streaming.PenSample;

/**
 * <p>
 * Use this to display Pen Coordinates to the console. Useful for debugging. If you get a message
 * like: Port COM5 not found, You may want to make sure that you have JavaCOMM installed.
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>.</span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class PenCoordinateDebugger {

	/**
	 * @return
	 */
	private static PenListener getDebugPenListener() {
		return new PenListener() {
			public void penDown(PenSample sample) {
				System.out.println("Pen Down: " + sample);
			}

			public void penUp(PenSample sample) {
				System.out.println("Pen Up: " + sample);
			}

			public void sample(PenSample sample) {
				System.out.println(sample);
			}
		};
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Pen pen = new Pen();
		pen.startLiveMode();
		pen.addLivePenListener(getDebugPenListener());
	}
}