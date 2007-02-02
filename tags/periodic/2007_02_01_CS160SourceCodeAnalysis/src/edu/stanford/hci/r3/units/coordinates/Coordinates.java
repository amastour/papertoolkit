package edu.stanford.hci.r3.units.coordinates;

import edu.stanford.hci.r3.units.Units;

/**
 * <p>
 * A point in 2D space.
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class Coordinates {

	/**
	 * X Value of this coordinate.
	 */
	protected Units x;

	/**
	 * Y Value of this coordinate.
	 */
	protected Units y;

	/**
	 * @param x
	 * @param y
	 */
	public Coordinates(Units xCoord, Units yCoord) {
		x = xCoord;
		y = yCoord;
	}

	/**
	 * @return
	 */
	public Units getX() {
		return x;
	}

	/**
	 * @return
	 */
	public Units getY() {
		return y;
	}

	/**
	 * Subclasses of Coordinates should verify that the passed-in unit is in fact of the right type!
	 * 
	 * @param xCoord
	 */
	public void setX(Units xCoord) {
		x = xCoord;
	}

	/**
	 * Subclasses of Coordinates should verify that the passed-in unit is in fact of the right type!
	 * 
	 * @param yCoord
	 */
	public void setY(Units yCoord) {
		y = yCoord;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Coordinates: x=[" + x.toString() + "]  y=[" + y.toString() + "]";
	}
}
