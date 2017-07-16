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

import it.unisa.di.weblab.polyrec.geom.Point;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class PolylineAligner {
	//returns the best alignment
	private Polyline pfThiss;
	private Polyline pfOther;
	private ArrayList<Integer> thisVert;
	private ArrayList<Integer> otherVert;
	private Gesture gThiss;
	private Gesture gOther;
	private int addedT = 0;
	private int addedO = 0;
	private double score;
	private int insertions, deletions, matches;
	private ArrayList<Point> alignment;
	private NeedlemanWunsch nw;
	
	public static TPoint pointOnLine(TPoint first, TPoint last, double length){
		double x = first.x+(last.x-first.x)*length;
		double y = first.y+(last.y-first.y)*length;
		return new TPoint(x,y,0);
	}
	



	public PolylineAligner(Polyline pfThiss, Polyline pfOther) {
		super();
		this.pfThiss = pfThiss;
		this.pfOther = pfOther;
		thisVert = new ArrayList<Integer>();
//		thisVert.add(0);
		otherVert = new ArrayList<Integer>();
//		otherVert.add(0);
//		System.out.println("aligning "+thiss+" "+other);
		gThiss = pfThiss.getGesture();
		gOther = pfOther.getGesture();
	}



	public int getAddedAngles(){
		return addedT+addedO;
	}

	public AbstractMap.SimpleEntry<Polyline,Polyline> align(){
		
		//align
		nw = new NeedlemanWunsch(pfThiss, pfOther);
//		System.out.println("Before alignment "+pfThiss.getNumLines()+" "+pfOther.getNumLines());
		List<Point> matched = nw.getMatchedPoints();
//		System.out.println("num angles: "+pfThiss.getNumVertexes()+" "+pfOther.getNumVertexes());
//		System.out.println(matched);
		score = nw.getScore();
		deletions = nw.getDeletions();
		insertions = nw.getInsertions();
		matches = nw.getMatches()+2;
		alignment = nw.getAlignment();
		
		int previousX=0;
		int previousY=0;
		
	
//		System.out.println("Comparing "+thiss.points.size()+" to "+other.points.size());
		for(int i=0; i<matched.size(); i++){
			Point p = matched.get(i);
//			System.out.println("Extracting match "+p);
			
			// INSERT in X
			int toInsertX = p.y-previousY-1;
			for(int j=0; j<toInsertX; j++){
				double dist = pfOther.getLengthProportion(previousY, p.y, previousY+j+1);
				insert(gThiss, new ArrayList<Integer>(pfThiss.getIndexes()), thisVert, previousX, p.x, dist);
				addedT++;
			}
			
			// INSERT in Y
			int toInsertY = p.x-previousX-1;
			for(int j=0; j<toInsertY; j++){
//				System.out.println(previousX+" "+p.x+" "+(previousX+j+1));
				double dist = pfThiss.getLengthProportion(previousX, p.x, previousX+j+1);
				insert(gOther, new ArrayList<Integer>(pfOther.getIndexes()), otherVert, previousY, p.y, dist);
				addedO++;
			}
		
			previousX = p.x;
			previousY = p.y;
		
		}
		thisVert.addAll(pfThiss.getIndexes());
		Collections.sort(thisVert);
//		System.out.println(thisVert);
		
		otherVert.addAll(pfOther.getIndexes());
		Collections.sort(otherVert);
//		System.out.println(otherVert);
		
		Polyline t = gThiss.getPoly(thisVert);
		Polyline o = gOther.getPoly(otherVert);
//		System.out.println("After alignment "+t.getNumLines()+" "+o.getNumLines());
		return new AbstractMap.SimpleEntry<Polyline,Polyline>(t, o);
	}
	
	
	private void insert(Gesture g, ArrayList<Integer> vertexesFrom, ArrayList<Integer> vertexesTo, int prev, int next, double dist){
//		System.out.println("inserting between "+prev+" and "+next+" at "+dist);
		int toAdd = vertexesFrom.get(prev) + g.partOf(vertexesFrom.get(prev), vertexesFrom.get(next)).pointOnCurve(dist);
		vertexesTo.add(toAdd);
	}
	
	public double getScore(){
		return score;
	}



	public int getInsertions() {
		return insertions;
	}



	public int getDeletions() {
		return deletions;
	}



	public int getMatches() {
		return matches;
	}
	
	public ArrayList<Point> getAlignment() {
		return alignment;
	}
	public void printMatrix(){
		nw.printMatrix();
	}
	
}
