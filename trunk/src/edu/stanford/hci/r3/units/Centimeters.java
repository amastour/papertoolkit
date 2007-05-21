package edu.stanford.hci.r3.units;

/**
 * <p>
 * Represents the centimeter unit, 1/100 of a meter.
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 *
 */
public class Centimeters extends Units {

	/**
	 * Identity Element, representing one centimeter.
	 */
	public static final Units ONE = new Centimeters(1.0);

	/**
	 * 1 cm == 1/100 m.
	 */
	public Centimeters() {
		super(1);
	}

	/**
	 * @param cm
	 */
	public Centimeters(double cm) {
		super(cm);
	}

	/**
	 * @see edu.stanford.hci.r3.units.Units#getNumberOfUnitsInOneInch()
	 */
	@Override
	protected double getNumberOfUnitsInOneInch() {
		return 2.54;
	}
}
