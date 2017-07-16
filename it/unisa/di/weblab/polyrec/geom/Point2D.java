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


package it.unisa.di.weblab.polyrec.geom;

import java.io.Serializable;

public abstract class Point2D implements Cloneable {

	public static class Double extends Point2D implements Serializable {
		private static final long serialVersionUID = 3640335230277743439L;
		public double x, y;

		public Double(double x, double y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public double getX() {
			return x;
		}

		@Override
		public double getY() {
			return y;
		}

		@Override
		public void setLocation(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}

	public abstract double getX();

	public abstract double getY();

	public abstract void setLocation(double x, double y);

	public double distance(double x, double y) {
		x -= getX();
		y -= getY();
		return Math.sqrt(x * x + y * y);
	}

	public double distance(Point2D point) {
		return distance(point.getX(), point.getY());
	}

	@Override
	public String toString() {
		return getClass().getName() + "[x=" + getX() + ",y=" + getY() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = java.lang.Double.doubleToLongBits(getX());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = java.lang.Double.doubleToLongBits(getY());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point2D other = (Point2D) obj;
		if (java.lang.Double.doubleToLongBits(getX()) != java.lang.Double
				.doubleToLongBits(other.getX()))
			return false;
		if (java.lang.Double.doubleToLongBits(getY()) != java.lang.Double
				.doubleToLongBits(other.getY()))
			return false;
		return true;
	}
}
