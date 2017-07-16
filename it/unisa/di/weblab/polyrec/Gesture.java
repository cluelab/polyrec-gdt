/*
PolyRec Project
Copyright (c) 2015, Vittorio Fuccella - Weblab - http://weblab.di.unisa.it/
All rights reserved. Includes a reference implementation of the following:

* Vittorio Fuccella, Gennaro Costagliola. "Unistroke Gesture Recognition
  Through Polyline Approximation and Alignment". In Proceedings of the 33rd
  annual ACM conference on Human factors in computing systems (CHI '15).
  April 18-23, 2015, Seoul, Republic of Korea.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the PolyRec Project nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package it.unisa.di.weblab.polyrec;

import it.unisa.di.weblab.polyrec.geom.Rectangle2D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Gesture class
 * 
 * @author Vittorio
 *
 */
public class Gesture implements Serializable {
	public ArrayList<TPoint> points;
	private GestureInfo info;

	private TPoint centroid = null;
	private ArrayList<Double> lengths;
	private Rectangle2D.Double boundingBox = null;

	private boolean rotInv = false;
	private int pointersNum = 1;

	/**
	 * Constructor
	 */
	public Gesture() {
		this.points = new ArrayList<TPoint>();
	}

	/**
	 * @return information attached to this gesture
	 */
	public GestureInfo getInfo() {
		return info;
	}

	/**
	 * @param info
	 *            information attached to this gesture
	 */
	public void setInfo(GestureInfo info) {
		this.info = info;
	}

	private void invalidate() {
		lengths = null;
		centroid = null;
		boundingBox = null;
	}

	/**
	 * @return the duration in milliseconds
	 */
	public long getMilliseconds() {
		TPoint first = points.get(0);
		TPoint last = points.get(points.size() - 1);
		return last.time - first.time;
	}

	/**
	 * @return the points
	 */
	public ArrayList<TPoint> getPoints() {
		return this.points;
	}

	/**
	 * @param indexes
	 *            the indexes of the sampled points
	 * @return the polyline approximating the gesture
	 */
	public Polyline getPoly(List<Integer> indexes) {
		return new Polyline(this, indexes);
	}

	/**
	 * @param points
	 *            The points
	 */
	public void setPoints(ArrayList<TPoint> points) {
		this.points = points;
	}

	/**
	 * Appends a point to the gesture
	 * 
	 * @param point
	 *            The new point
	 */
	public void addPoint(TPoint point) {
		points.add(point);
		invalidate();
	}

	/**
	 * @param sensitive
	 *            Rotation sensitive or invariant context
	 * @return The indicative angle
	 */
	public double getIndicativeAngle(boolean sensitive) {
		double iAngle = Math.atan2(-(getCentroid().y - points.get(0).y), getCentroid().x - points.get(0).x);
		iAngle = (iAngle >= 0 ? iAngle : (2 * Math.PI + iAngle));

		double delta = 0.0;
		if (sensitive) {
			double baseOrientation = (Math.PI / 4.0) * Math.floor((iAngle + Math.PI / 8.0) / (Math.PI / 4.0));
			delta = baseOrientation + iAngle;
		} else
			delta = iAngle;

		return delta;

	}

	private void calculateCentroid() {
		centroid = new TPoint(0.0d, 0.0d, 0l);
		Integer length = points.size();

		ListIterator<TPoint> iterator = points.listIterator();
		while (iterator.hasNext()) {
			TPoint point = iterator.next();
			centroid.x += point.x;
			centroid.y += point.y;
		}
		centroid.x /= length;
		centroid.y /= length;
	}

	/**
	 * @return The centroid of the points
	 */
	public TPoint getCentroid() {
		if (centroid == null)
			calculateCentroid();
		return centroid;
	}

	private void calculateLengths() {
		Double length = 0.0d;
		lengths = new ArrayList<Double>();
		lengths.add(length);
		TPoint temTPoint = null;
		ListIterator<TPoint> iterator = points.listIterator();

		while (iterator.hasNext()) {
			TPoint point = iterator.next();
			if (temTPoint != null) {
				length += temTPoint.distance(point);// TPoint.dist( temTPoint,
													// point );
				lengths.add(length);
			}
			temTPoint = point;
		}
	}

	/**
	 * @return The distance between the two endpoints
	 */
	protected double getEndpointsDistance() {
		TPoint first = points.get(0);
		TPoint last = points.get(points.size() - 1);
		return first.distance(last);
	}

	/**
	 * @return The length
	 */
	public double getLength() {
		if (lengths == null)
			calculateLengths();
		return lengths.get(points.size() - 1);
	}

	/**
	 * @param start
	 *            Start point index
	 * @param end
	 *            End point index
	 * @return The length between the points
	 */
	protected double getLength(int start, int end) {

		if (lengths == null)
			calculateLengths();

		return lengths.get(end) - lengths.get(start);
	}

	/**
	 * @param point
	 *            Point index
	 * @return The length up to the point
	 */
	protected double getLength(int point) {

		if (lengths == null)
			calculateLengths();

		return lengths.get(point);
	}

	/**
	 * @return The array of points rotated to zero
	 */
	public ArrayList<TPoint> getRotateToZero() {
		TPoint centroid = getCentroid();
		double theta = Math.atan2(centroid.y - points.get(0).y, centroid.x - points.get(0).x);
		return getRotateBy(-theta);
	}

	/**
	 * @param _theta
	 *            the rotation angle
	 * @return he array of points rotated to a given angle
	 */
	public ArrayList<TPoint> getRotateBy(Double _theta) {

		TPoint centroid = getCentroid();

		Double sin = Math.sin(_theta);
		Double cos = Math.cos(_theta);
		ArrayList<TPoint> result = new ArrayList<TPoint>();

		ListIterator<TPoint> iterator = points.listIterator();
		while (iterator.hasNext()) {
			TPoint point = iterator.next();
			result.add(new TPoint((point.x - centroid.x) * cos - (point.y - centroid.y) * sin + centroid.x,
					(point.x - centroid.x) * sin + (point.y - centroid.y) * cos + centroid.y, point.time));
		}
		return result;
	}

	/**
	 * @return The bounding box
	 */
	public Rectangle2D.Double getBoundingBox() {
		if (boundingBox == null)
			calculateBoundingBox();
		return boundingBox;
	}

	private void calculateBoundingBox() {

		Double minX, maxX, minY, maxY;
		maxX = maxY = Double.NEGATIVE_INFINITY;
		minX = minY = Double.POSITIVE_INFINITY;

		ListIterator<TPoint> iterator = points.listIterator();
		while (iterator.hasNext()) {
			TPoint point = iterator.next();
			minX = Math.min(point.x, minX);
			maxX = Math.max(point.x, maxX);
			minY = Math.min(point.y, minY);
			maxY = Math.max(point.y, maxY);
		}

		boundingBox = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
	}

	/**
	 * @return The length of the diagonal of the gesture
	 */
	public double getDiagonal() {
		Rectangle2D.Double _box = getBoundingBox();
		return Math.sqrt(_box.height * _box.height + _box.width * _box.width);
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < points.size(); i++)
			buf.append(points.get(i));
		return buf.toString();
	}

	/**
	 * @param fromIndex
	 *            Start point index
	 * @param toIndex
	 *            End point index
	 * @return The subgesture between two points
	 */
	public Gesture partOf(int fromIndex, int toIndex) {
		List<TPoint> sub = points.subList(fromIndex, toIndex);
		Gesture part = new Gesture();
		part.setPoints(new ArrayList<TPoint>(sub));
		return part;
	}

	/**
	 * @param length
	 *            The distance from the start point in [0,1]
	 * @return A point on the gesture
	 */
	public int pointOnCurve(double length) {
		double referenceLength = getLength() * length;
		double tempLength = 0;
		for (int i = 1; i < points.size(); i++) {
			TPoint current = points.get(i);
			TPoint previous = points.get(i - 1);
			tempLength += current.distance(previous);
			if (tempLength >= referenceLength)
				return i;
		}
		return points.size() - 1;
	}

	/**
	 * @param lp
	 *            The list of points
	 * @param reference
	 *            The reference point for translation
	 * @return The translated points
	 */
	public static ArrayList<TPoint> getTranslated(List<TPoint> lp, TPoint reference) {

		ArrayList<TPoint> translated = new ArrayList<TPoint>();

		ListIterator<TPoint> iterator = lp.listIterator();
		while (iterator.hasNext()) {
			TPoint point = iterator.next();
			translated.add(new TPoint(point.x - reference.x, point.y - reference.y, point.time));
		}

		return translated;
	}

	/**
	 * @param reference
	 *            The reference point for translation
	 * @return The translated points
	 */
	public ArrayList<TPoint> getTranslated(TPoint reference) {

		ArrayList<TPoint> translated = new ArrayList<TPoint>();
		ListIterator<TPoint> iterator = points.listIterator();
		while (iterator.hasNext()) {
			TPoint point = iterator.next();
			translated.add(new TPoint(point.x - reference.x, point.y - reference.y, point.time));
		}
		return translated;
	}

	public static void main(String args[]) {
		Gesture s = new Gesture();
		for (int i = 0; i < 100; i++)
			s.addPoint(new TPoint(i, 0, 0));
		System.out.println(s.pointOnCurve(10));
	}

	
	/*metodi aggiunti da Roberto*/
	public boolean isRotInv() {
		return rotInv;
	}

	public void setRotInv(boolean rotInv) {
		this.rotInv = rotInv;
	}


	public Gesture normalizedGesture(double targetWidth, double targetHeight, int padding) {

		double zoom = Math.max(targetHeight-padding, targetWidth-padding) / Math.max(getBoundingBox().height, getBoundingBox().width);
		
		Gesture normalizedGesture = new Gesture();
		normalizedGesture.setInfo(getInfo());
		normalizedGesture.setRotInv(isRotInv());
		normalizedGesture.setPointers(pointersNum);

		for (int i = 0; i < points.size(); i++) {
			TPoint p1 = points.get(i);
			normalizedGesture.addPoint(new TPoint((p1.getX() * zoom), (p1.getY() * zoom), p1.getTime()));

		}

		return normalizedGesture;
	}
	
	public int getPointers() {
		return pointersNum;
	}
	
	public void setPointers(int pointers) {
		this.pointersNum = pointers;
	}
	
	

}
