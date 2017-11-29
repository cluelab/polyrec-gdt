package it.unisa.di.cluelab.polyrec.gdt;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import it.unisa.di.cluelab.polyrec.DouglasPeuckerReducer;
import it.unisa.di.cluelab.polyrec.Gesture;
import it.unisa.di.cluelab.polyrec.GestureInfo;
import it.unisa.di.cluelab.polyrec.PolyRecognizerGSS;
import it.unisa.di.cluelab.polyrec.Polyline;
import it.unisa.di.cluelab.polyrec.PolylineAligner;
import it.unisa.di.cluelab.polyrec.PolylineFinder;
import it.unisa.di.cluelab.polyrec.TPoint;
import it.unisa.di.cluelab.polyrec.Vector;
import it.unisa.di.cluelab.polyrec.geom.Rectangle2D;

public class ExtendedPolyRecognizerGSS extends PolyRecognizerGSS {
	private static final Double[] DPR_PARAMS = new Double[] { 26d, 22d };
	private static final boolean VERBOSE = false;
	private static final boolean GSS = true;
	
	final static int REPLACE = 0; // removing previous and then add classes from
									// file
	final static int ADD = 1; // add new classes without remove previous, class
								// with same name are replaced (not used)

	final static int ADD_TEMPLATES = 2; // add new classes and add templates to
										// class with same name

	public ExtendedPolyRecognizerGSS() {
		super();
		
	}

	public synchronized ExtendedResult recognizeExt(Gesture gesture) {
		// this.rInvariant = _gesture.isRotInv(); if (this.rInvariant)
		// this.angle = ANGLE_INVARIANT; else this.angle = ANGLE_SENSITIVE;

		final PolylineFinder pf = new DouglasPeuckerReducer(gesture, DPR_PARAMS);
		// polyline del gesto da riconoscere
		final Polyline u = pf.find();

		Double a = Double.POSITIVE_INFINITY;
		String templateName = null;
		Polyline t = null;

		final TreeMap<String, double[]> ranking = new TreeMap<String, double[]>();

		for (Entry<String, ArrayList<Polyline>> e : templates.entrySet()) {
			final ArrayList<Polyline> tempTemplates = e.getValue();
			double classdistance = Double.POSITIVE_INFINITY;
			for (int i = 0; i < tempTemplates.size(); i++) {
				Double distance = 2.0;
				t = tempTemplates.get(i);

				if (tempTemplates.get(i).getGesture().getPointers() == gesture.getPointers()) {
					final PolylineAligner aligner = new PolylineAligner(u, t);
					final AbstractMap.SimpleEntry<Polyline, Polyline> polyPair = aligner.align();

					final int addedAngles = aligner.getAddedAngles();
					final double penalty = 1 + (double) addedAngles / (double) (addedAngles + aligner.getMatches());
					// da riconoscere
					final Polyline unknown = polyPair.getKey();
					// confrontato con
					final Polyline template = polyPair.getValue();

					final List<Vector> vectorsU = unknown.getVectors();
					if (VERBOSE) {
						System.out.println(vectorsU);
					}
					final List<Vector> vectorsT = template.getVectors();
					if (VERBOSE) {
						System.out.println(vectorsT);
					}
					Double bestDist = null;
					if (!GSS) {
						final double uAngle = unknown.getGesture().getIndicativeAngle(!unknown.getGesture().isRotInv());
						if (VERBOSE) {
							System.out.println("Indicative angle = " + uAngle);
						}
						final double tAngle = template.getGesture()
								.getIndicativeAngle(!template.getGesture().isRotInv());
						if (VERBOSE) {
							System.out.println("Indicative angleT = " + tAngle);
						}
						bestDist = getDistanceAtAngle(vectorsU, vectorsT, -uAngle, -tAngle);
						if (VERBOSE) {
							System.out.println("Distance at = " + (-uAngle) + "; dist = " + bestDist);
						}
					} else {
						bestDist = getDistanceAtBestAngle(unknown, template, template.getGesture().isRotInv());
					}
					distance = penalty * bestDist;
				}

				if (distance < classdistance) {
					classdistance = distance;
				}
				if (distance < a) {
					a = distance;
					templateName = e.getKey();
				}
			}

			if (classdistance != Double.POSITIVE_INFINITY) {
				final Double classscore = (2.0f - classdistance) / 2;

				ranking.put(e.getKey(), new double[] { classdistance, Math.round(classscore * 10000) / 100. });
			}
		}

		if (templateName != null) {
			final Double score = (2.0f - a) / 2;

			return new ExtendedResult(templateName, score, ranking);
		}

		// System.out.println(" null distance ");
		return null;
	}

	/**
	 * Verifica similarità tra un template di una classe e i template di tutte
	 * le altre classi
	 * 
	 * @param u
	 *            Template da veriticare
	 * @param className
	 *            Classe del template da verificare
	 * @param scorelimit
	 *            Score al di sopra del cui i template sono troppo simili
	 * @return
	 */
	public synchronized ArrayList<ExtendedResult> verifyTemplate(Polyline u, String className, int scorelimit) {
		// this.rInvariant = _gesture.isRotInv(); if (this.rInvariant)
		// this.angle = ANGLE_INVARIANT; else this.angle = ANGLE_SENSITIVE;

		// PolylineFinder pf = new DouglasPeuckerReducer(_gesture, params);
		// Polyline u = pf.find();

		Double a = Double.POSITIVE_INFINITY;
		String templateName = null;
		Polyline t = null;

		ArrayList<ExtendedResult> results = new ArrayList<ExtendedResult>();

		// per tutte le classi
		for (Entry<String, ArrayList<Polyline>> en : templates.entrySet()) {
			// classe diversa da quella del template da controllare
			String key = en.getKey();
			if (!Objects.equals(key, className)) {
				ArrayList<Polyline> tempTemplates = en.getValue();
				for (int i = 0; i < tempTemplates.size(); i++) {
					t = tempTemplates.get(i);

					PolylineAligner aligner = new PolylineAligner(u, t);
					AbstractMap.SimpleEntry<Polyline, Polyline> polyPair = aligner.align();

					int addedAngles = aligner.getAddedAngles();
					double penalty = 1 + (double) addedAngles / (double) (addedAngles + aligner.getMatches());
					Polyline unknown = polyPair.getKey();// da riconoscere
					Polyline template = polyPair.getValue();// confrontato con

					List<Vector> vectorsU = unknown.getVectors();
					if (VERBOSE)
						System.out.println(vectorsU);
					List<Vector> vectorsT = template.getVectors();
					if (VERBOSE)
						System.out.println(vectorsT);
					Double bestDist = null;
					if (!GSS) {
						double uAngle = unknown.getGesture().getIndicativeAngle(!unknown.getGesture().isRotInv());
						if (VERBOSE)
							System.out.println("Indicative angle = " + uAngle);
						double tAngle = template.getGesture().getIndicativeAngle(!template.getGesture().isRotInv());
						if (VERBOSE)
							System.out.println("Indicative angleT = " + tAngle);
						bestDist = getDistanceAtAngle(vectorsU, vectorsT, -uAngle, -tAngle);
						if (VERBOSE)
							System.out.println("Distance at = " + (-uAngle) + "; dist = " + bestDist);
					} else
						bestDist = getDistanceAtBestAngle(unknown, template, template.getGesture().isRotInv());
					Double distance = penalty * bestDist;
					if (VERBOSE)
						System.out.println("Confronto con Gesture " + key + " ROTINV:"
								+ template.getGesture().isRotInv() + " SCORE:" + (2.0f - distance) / 2);

					Double score = (2.0f - distance) / 2;
					ExtendedResult result = new ExtendedResult(key, score, i);

					// template troppo simili
					if (result.getScore() > scorelimit) {
						results.add(result);

					}
					if (VERBOSE)
						System.out.println(
								"Template troppo simile a polyline " + i + " ROTINV:" + template.getGesture().isRotInv()
										+ " della classe " + key + "  (SCORE:" + (2.0f - distance) / 2 + ")");
					if (distance < a) {
						a = distance;
						templateName = key;
					}

				}
			}

		}

		return results;
	}

	/**
	 * Confronto tra due template
	 * 
	 * @param u
	 *            Primo dei template da confrontare
	 * @param t
	 *            Secondo dei template da confrontare
	 * @return Distanza tra i due template
	 */
	public synchronized Double checkTemplate(Polyline u, Polyline t) {
		PolylineAligner aligner = new PolylineAligner(u, t);
		AbstractMap.SimpleEntry<Polyline, Polyline> polyPair = aligner.align();

		int addedAngles = aligner.getAddedAngles();
		double penalty = 1 + (double) addedAngles / (double) (addedAngles + aligner.getMatches());
		Polyline unknown = polyPair.getKey();// da riconoscere
		Polyline template = polyPair.getValue();// confrontato con

		List<Vector> vectorsU = unknown.getVectors();
		if (VERBOSE)
			System.out.println(vectorsU);
		List<Vector> vectorsT = template.getVectors();
		if (VERBOSE)
			System.out.println(vectorsT);
		Double bestDist = null;
		if (!GSS) {
			double uAngle = unknown.getGesture().getIndicativeAngle(!unknown.getGesture().isRotInv());
			if (VERBOSE)
				System.out.println("Indicative angle = " + uAngle);
			double tAngle = template.getGesture().getIndicativeAngle(!template.getGesture().isRotInv());
			if (VERBOSE)
				System.out.println("Indicative angleT = " + tAngle);
			bestDist = getDistanceAtAngle(vectorsU, vectorsT, -uAngle, -tAngle);
			if (VERBOSE)
				System.out.println("Distance at = " + (-uAngle) + "; dist = " + bestDist);
		} else
			bestDist = getDistanceAtBestAngle(unknown, template, template.getGesture().isRotInv());
		Double distance = penalty * bestDist;
		return distance;

	}

	// copiato da PolyRecognizerGSS
	private static Double getDistanceAtAngle(List<Vector> v1, List<Vector> v2, double theta1, double theta2) {
		final boolean VERBOSE = false;
		double cost = 0;
		if (VERBOSE) {
			System.out.println(v1);
		}
		if (VERBOSE) {
			System.out.println(v2);
		}
		if (v1.size() != v2.size()) {
			System.out.println("distance at angle " + v1.size() + " " + v2.size());
		}
		for (int i = 0; i < v1.size(); i++) {
			final double diff = v1.get(i).difference(v2.get(i), theta1, theta2);
			if (VERBOSE) {
				System.out.print(diff + " + ");
			}
			cost += diff;
		}
		if (VERBOSE) {
			System.out.println();
		}
		return cost;
	}

	public static Gesture normalizeGesture(Gesture gesture, double targetWidth, double targetHeight, int padding) {
		final Rectangle2D.Double bbox = gesture.getBoundingBox();
		final double zoom = Math.max(targetHeight - padding, targetWidth - padding) / Math.max(bbox.height, bbox.width);

		final Gesture normalizedGesture = new Gesture();
		normalizedGesture.setInfo(gesture.getInfo());
		normalizedGesture.setRotInv(gesture.isRotInv());
		normalizedGesture.setPointers(gesture.getPointers());

		for (TPoint p : gesture.getPoints()) {
			normalizedGesture.addPoint(new TPoint(p.getX() * zoom, p.getY() * zoom, p.getTime()));
		}

		return normalizedGesture;
	}

	/**
	 * Get template list for class name
	 * @param name
	 * 		Class name
	 * @return
	 * 
	 */
	public ArrayList<Polyline> getTemplate(String name) {
		return templates.get(name);

	}


	/**
	 * remove a class 
	 * 
	 * @param name
	 */
	public void removeClass(String name) {
		templates.remove(name);
		//this.save();
	};


	/**
	 * edit class name 
	 * 
	 * @param oldname
	 * @param newname
	 */
	public void editClassName(String oldname, String newname) {
		ArrayList<Polyline> polylines = templates.get(oldname);
		templates.remove(oldname);
		templates.put(newname.toLowerCase(), polylines);

		//this.save();
	};


	/**
	 * remove all classes
	 */
	public void removeClasses() {
		templates.clear();
	
	};


	/**
	 * remove polyline from set
	 * @param name 
	 * 			Classname
	 * @param index 
	 * 			Index of polyline to remove
	 */
	public void removePolyline(String name, int index) {
		ArrayList<Polyline> polylines = templates.get(name);
		polylines.remove(index);
		
	};

	
	/**
	 * add a class to the set
	 * @param name
	 * 			Class name
	 */
	public void addClass(String name) {
		if (!getClassNames().contains(name.toLowerCase()))
			templates.put(name.replace('_', '-').toLowerCase(), new ArrayList());
		//this.save();
	}


	/**
	 * get index of class in Treemap
	 * @param classname
	 * 				Class name
	 * @return
	 */
	public int getClassIndex(String classname) {

		Set<Entry<String, ArrayList<Polyline>>> set = templates.entrySet();
		Iterator<Entry<String, ArrayList<Polyline>>> it = set.iterator();

		int i = -1;
		while (it.hasNext()) {
			i++;
			Map.Entry entry = (Map.Entry) it.next();

			if (entry.getKey().equals(classname))
				return i;

		}
		return i;
	}


	/**
	 * 
	 * @param name
	 * @param polyline
	 * @return
	 */
	public int addTemplate(String name, Polyline polyline) {

		if (!templates.containsKey(name))
			templates.put(name, new ArrayList<Polyline>());

		ArrayList<Polyline> templateClass = templates.get(name);

		templateClass.add(polyline);
		return templateClass.size();
	}
	
	@Override
	public int addTemplate(String name, Gesture gesture) {
		
		gesture = normalizeGesture(gesture, Canvas.WIDTH, Canvas.HEIGHT, 150);
		return super.addTemplate(name, gesture);

		
	}

	
	/**
	 * get number of templates in set
	 * 
	 * @return templatesNum
	 * 			Number of templates
	 */
	public int getTamplatesNumber() {
		int templatesNum = 0;
		String[] classes = getClassNames().toArray(new String[0]);
		for (int m = 0; m < classes.length; m++)
			templatesNum += ((ArrayList<Polyline>) getTemplate(classes[m])).size();
		return templatesNum;
	}


	/**
	 * @param className
	 * @param templates
	 */
	public void addTemplates(String className, ArrayList<Polyline> templates) {
		for (Polyline p : templates)
			addTemplate(className, p);

	}
	
	
	
	/**
	 * Add $1 gesture set samples to the gesture set
	 */
	/*
	void addSamples() {

		Gesture g = new Gesture();

		g.addPoint(new TPoint(137, 139, 0));
		g.addPoint(new TPoint(135, 141, 0));
		g.addPoint(new TPoint(133, 144, 0));
		g.addPoint(new TPoint(132, 146, 0));
		g.addPoint(new TPoint(130, 149, 0));
		g.addPoint(new TPoint(128, 151, 0));
		g.addPoint(new TPoint(126, 155, 0));
		g.addPoint(new TPoint(123, 160, 0));
		g.addPoint(new TPoint(120, 166, 0));
		g.addPoint(new TPoint(116, 171, 0));
		g.addPoint(new TPoint(112, 177, 0));
		g.addPoint(new TPoint(107, 183, 0));
		g.addPoint(new TPoint(102, 188, 0));
		g.addPoint(new TPoint(100, 191, 0));
		g.addPoint(new TPoint(95, 195, 0));
		g.addPoint(new TPoint(90, 199, 0));
		g.addPoint(new TPoint(86, 203, 0));
		g.addPoint(new TPoint(82, 206, 0));
		g.addPoint(new TPoint(80, 209, 0));
		g.addPoint(new TPoint(75, 213, 0));
		g.addPoint(new TPoint(73, 213, 0));
		g.addPoint(new TPoint(70, 216, 0));
		g.addPoint(new TPoint(67, 219, 0));
		g.addPoint(new TPoint(64, 221, 0));
		g.addPoint(new TPoint(61, 223, 0));
		g.addPoint(new TPoint(60, 225, 0));
		g.addPoint(new TPoint(62, 226, 0));
		g.addPoint(new TPoint(65, 225, 0));
		g.addPoint(new TPoint(67, 226, 0));
		g.addPoint(new TPoint(74, 226, 0));
		g.addPoint(new TPoint(77, 227, 0));
		g.addPoint(new TPoint(85, 229, 0));
		g.addPoint(new TPoint(91, 230, 0));
		g.addPoint(new TPoint(99, 231, 0));
		g.addPoint(new TPoint(108, 232, 0));
		g.addPoint(new TPoint(116, 233, 0));
		g.addPoint(new TPoint(125, 233, 0));
		g.addPoint(new TPoint(134, 234, 0));
		g.addPoint(new TPoint(145, 233, 0));
		g.addPoint(new TPoint(153, 232, 0));
		g.addPoint(new TPoint(160, 233, 0));
		g.addPoint(new TPoint(170, 234, 0));
		g.addPoint(new TPoint(177, 235, 0));
		g.addPoint(new TPoint(179, 236, 0));
		g.addPoint(new TPoint(186, 237, 0));
		g.addPoint(new TPoint(193, 238, 0));
		g.addPoint(new TPoint(198, 239, 0));
		g.addPoint(new TPoint(200, 237, 0));
		g.addPoint(new TPoint(202, 239, 0));
		g.addPoint(new TPoint(204, 238, 0));
		g.addPoint(new TPoint(206, 234, 0));
		g.addPoint(new TPoint(205, 230, 0));
		g.addPoint(new TPoint(202, 222, 0));
		g.addPoint(new TPoint(197, 216, 0));
		g.addPoint(new TPoint(192, 207, 0));
		g.addPoint(new TPoint(186, 198, 0));
		g.addPoint(new TPoint(179, 189, 0));
		g.addPoint(new TPoint(174, 183, 0));
		g.addPoint(new TPoint(170, 178, 0));
		g.addPoint(new TPoint(164, 171, 0));
		g.addPoint(new TPoint(161, 168, 0));
		g.addPoint(new TPoint(154, 160, 0));
		g.addPoint(new TPoint(148, 155, 0));
		g.addPoint(new TPoint(143, 150, 0));
		g.addPoint(new TPoint(138, 148, 0));
		g.addPoint(new TPoint(136, 148, 0));
		g.setInfo(new GestureInfo(0, null, "triangle", 0));
		addTemplate("triangle", g);

		g = new Gesture();
		g.addPoint(new TPoint(87, 142, 0));
		g.addPoint(new TPoint(89, 145, 0));
		g.addPoint(new TPoint(91, 148, 0));
		g.addPoint(new TPoint(93, 151, 0));
		g.addPoint(new TPoint(96, 155, 0));
		g.addPoint(new TPoint(98, 157, 0));
		g.addPoint(new TPoint(100, 160, 0));
		g.addPoint(new TPoint(102, 162, 0));
		g.addPoint(new TPoint(106, 167, 0));
		g.addPoint(new TPoint(108, 169, 0));
		g.addPoint(new TPoint(110, 171, 0));
		g.addPoint(new TPoint(115, 177, 0));
		g.addPoint(new TPoint(119, 183, 0));
		g.addPoint(new TPoint(123, 189, 0));
		g.addPoint(new TPoint(127, 193, 0));
		g.addPoint(new TPoint(129, 196, 0));
		g.addPoint(new TPoint(133, 200, 0));
		g.addPoint(new TPoint(137, 206, 0));
		g.addPoint(new TPoint(140, 209, 0));
		g.addPoint(new TPoint(143, 212, 0));
		g.addPoint(new TPoint(146, 215, 0));
		g.addPoint(new TPoint(151, 220, 0));
		g.addPoint(new TPoint(153, 222, 0));
		g.addPoint(new TPoint(155, 223, 0));
		g.addPoint(new TPoint(157, 225, 0));
		g.addPoint(new TPoint(158, 223, 0));
		g.addPoint(new TPoint(157, 218, 0));
		g.addPoint(new TPoint(155, 211, 0));
		g.addPoint(new TPoint(154, 208, 0));
		g.addPoint(new TPoint(152, 200, 0));
		g.addPoint(new TPoint(150, 189, 0));
		g.addPoint(new TPoint(148, 179, 0));
		g.addPoint(new TPoint(147, 170, 0));
		g.addPoint(new TPoint(147, 158, 0));
		g.addPoint(new TPoint(147, 148, 0));
		g.addPoint(new TPoint(147, 141, 0));
		g.addPoint(new TPoint(147, 136, 0));
		g.addPoint(new TPoint(144, 135, 0));
		g.addPoint(new TPoint(142, 137, 0));
		g.addPoint(new TPoint(140, 139, 0));
		g.addPoint(new TPoint(135, 145, 0));
		g.addPoint(new TPoint(131, 152, 0));
		g.addPoint(new TPoint(124, 163, 0));
		g.addPoint(new TPoint(116, 177, 0));
		g.addPoint(new TPoint(108, 191, 0));
		g.addPoint(new TPoint(100, 206, 0));
		g.addPoint(new TPoint(94, 217, 0));
		g.addPoint(new TPoint(91, 222, 0));
		g.addPoint(new TPoint(89, 225, 0));
		g.addPoint(new TPoint(87, 226, 0));
		g.addPoint(new TPoint(87, 224, 0));
		g.setInfo(new GestureInfo(0, null, "x", 0));
		addTemplate("x", g);

		g = new Gesture();
		g.addPoint(new TPoint(78, 149, 0));
		g.addPoint(new TPoint(78, 153, 0));
		g.addPoint(new TPoint(78, 157, 0));
		g.addPoint(new TPoint(78, 160, 0));
		g.addPoint(new TPoint(79, 162, 0));
		g.addPoint(new TPoint(79, 164, 0));
		g.addPoint(new TPoint(79, 167, 0));
		g.addPoint(new TPoint(79, 169, 0));
		g.addPoint(new TPoint(79, 173, 0));
		g.addPoint(new TPoint(79, 178, 0));
		g.addPoint(new TPoint(79, 183, 0));
		g.addPoint(new TPoint(80, 189, 0));
		g.addPoint(new TPoint(80, 193, 0));
		g.addPoint(new TPoint(80, 198, 0));
		g.addPoint(new TPoint(80, 202, 0));
		g.addPoint(new TPoint(81, 208, 0));
		g.addPoint(new TPoint(81, 210, 0));
		g.addPoint(new TPoint(81, 216, 0));
		g.addPoint(new TPoint(82, 222, 0));
		g.addPoint(new TPoint(82, 224, 0));
		g.addPoint(new TPoint(82, 227, 0));
		g.addPoint(new TPoint(83, 229, 0));
		g.addPoint(new TPoint(83, 231, 0));
		g.addPoint(new TPoint(85, 230, 0));
		g.addPoint(new TPoint(88, 232, 0));
		g.addPoint(new TPoint(90, 233, 0));
		g.addPoint(new TPoint(92, 232, 0));
		g.addPoint(new TPoint(94, 233, 0));
		g.addPoint(new TPoint(99, 232, 0));
		g.addPoint(new TPoint(102, 233, 0));
		g.addPoint(new TPoint(106, 233, 0));
		g.addPoint(new TPoint(109, 234, 0));
		g.addPoint(new TPoint(117, 235, 0));
		g.addPoint(new TPoint(123, 236, 0));
		g.addPoint(new TPoint(126, 236, 0));
		g.addPoint(new TPoint(135, 237, 0));
		g.addPoint(new TPoint(142, 238, 0));
		g.addPoint(new TPoint(145, 238, 0));
		g.addPoint(new TPoint(152, 238, 0));
		g.addPoint(new TPoint(154, 239, 0));
		g.addPoint(new TPoint(165, 238, 0));
		g.addPoint(new TPoint(174, 237, 0));
		g.addPoint(new TPoint(179, 236, 0));
		g.addPoint(new TPoint(186, 235, 0));
		g.addPoint(new TPoint(191, 235, 0));
		g.addPoint(new TPoint(195, 233, 0));
		g.addPoint(new TPoint(197, 233, 0));
		g.addPoint(new TPoint(200, 233, 0));
		g.addPoint(new TPoint(201, 235, 0));
		g.addPoint(new TPoint(201, 233, 0));
		g.addPoint(new TPoint(199, 231, 0));
		g.addPoint(new TPoint(198, 226, 0));
		g.addPoint(new TPoint(198, 220, 0));
		g.addPoint(new TPoint(196, 207, 0));
		g.addPoint(new TPoint(195, 195, 0));
		g.addPoint(new TPoint(195, 181, 0));
		g.addPoint(new TPoint(195, 173, 0));
		g.addPoint(new TPoint(195, 163, 0));
		g.addPoint(new TPoint(194, 155, 0));
		g.addPoint(new TPoint(192, 145, 0));
		g.addPoint(new TPoint(192, 143, 0));
		g.addPoint(new TPoint(192, 138, 0));
		g.addPoint(new TPoint(191, 135, 0));
		g.addPoint(new TPoint(191, 133, 0));
		g.addPoint(new TPoint(191, 130, 0));
		g.addPoint(new TPoint(190, 128, 0));
		g.addPoint(new TPoint(188, 129, 0));
		g.addPoint(new TPoint(186, 129, 0));
		g.addPoint(new TPoint(181, 132, 0));
		g.addPoint(new TPoint(173, 131, 0));
		g.addPoint(new TPoint(162, 131, 0));
		g.addPoint(new TPoint(151, 132, 0));
		g.addPoint(new TPoint(149, 132, 0));
		g.addPoint(new TPoint(138, 132, 0));
		g.addPoint(new TPoint(136, 132, 0));
		g.addPoint(new TPoint(122, 131, 0));
		g.addPoint(new TPoint(120, 131, 0));
		g.addPoint(new TPoint(109, 130, 0));
		g.addPoint(new TPoint(107, 130, 0));
		g.addPoint(new TPoint(90, 132, 0));
		g.addPoint(new TPoint(81, 133, 0));
		g.addPoint(new TPoint(76, 133, 0));
		g.setInfo(new GestureInfo(0, null, "rectangle", 0));
		addTemplate("rectangle", g);

		g = new Gesture();
		g.addPoint(new TPoint(127, 141, 0));
		g.addPoint(new TPoint(124, 140, 0));
		g.addPoint(new TPoint(120, 139, 0));
		g.addPoint(new TPoint(118, 139, 0));
		g.addPoint(new TPoint(116, 139, 0));
		g.addPoint(new TPoint(111, 140, 0));
		g.addPoint(new TPoint(109, 141, 0));
		g.addPoint(new TPoint(104, 144, 0));
		g.addPoint(new TPoint(100, 147, 0));
		g.addPoint(new TPoint(96, 152, 0));
		g.addPoint(new TPoint(93, 157, 0));
		g.addPoint(new TPoint(90, 163, 0));
		g.addPoint(new TPoint(87, 169, 0));
		g.addPoint(new TPoint(85, 175, 0));
		g.addPoint(new TPoint(83, 181, 0));
		g.addPoint(new TPoint(82, 190, 0));
		g.addPoint(new TPoint(82, 195, 0));
		g.addPoint(new TPoint(83, 200, 0));
		g.addPoint(new TPoint(84, 205, 0));
		g.addPoint(new TPoint(88, 213, 0));
		g.addPoint(new TPoint(91, 216, 0));
		g.addPoint(new TPoint(96, 219, 0));
		g.addPoint(new TPoint(103, 222, 0));
		g.addPoint(new TPoint(108, 224, 0));
		g.addPoint(new TPoint(111, 224, 0));
		g.addPoint(new TPoint(120, 224, 0));
		g.addPoint(new TPoint(133, 223, 0));
		g.addPoint(new TPoint(142, 222, 0));
		g.addPoint(new TPoint(152, 218, 0));
		g.addPoint(new TPoint(160, 214, 0));
		g.addPoint(new TPoint(167, 210, 0));
		g.addPoint(new TPoint(173, 204, 0));
		g.addPoint(new TPoint(178, 198, 0));
		g.addPoint(new TPoint(179, 196, 0));
		g.addPoint(new TPoint(182, 188, 0));
		g.addPoint(new TPoint(182, 177, 0));
		g.addPoint(new TPoint(178, 167, 0));
		g.addPoint(new TPoint(170, 150, 0));
		g.addPoint(new TPoint(163, 138, 0));
		g.addPoint(new TPoint(152, 130, 0));
		g.addPoint(new TPoint(143, 129, 0));
		g.addPoint(new TPoint(140, 131, 0));
		g.addPoint(new TPoint(129, 136, 0));
		g.addPoint(new TPoint(126, 139, 0));
		g.setInfo(new GestureInfo(0, null, "circle", 0));
		addTemplate("circle", g);

		g = new Gesture();
		g.addPoint(new TPoint(91, 185, 0));
		g.addPoint(new TPoint(93, 185, 0));
		g.addPoint(new TPoint(95, 185, 0));
		g.addPoint(new TPoint(97, 185, 0));
		g.addPoint(new TPoint(100, 188, 0));
		g.addPoint(new TPoint(102, 189, 0));
		g.addPoint(new TPoint(104, 190, 0));
		g.addPoint(new TPoint(106, 193, 0));
		g.addPoint(new TPoint(108, 195, 0));
		g.addPoint(new TPoint(110, 198, 0));
		g.addPoint(new TPoint(112, 201, 0));
		g.addPoint(new TPoint(114, 204, 0));
		g.addPoint(new TPoint(115, 207, 0));
		g.addPoint(new TPoint(117, 210, 0));
		g.addPoint(new TPoint(118, 212, 0));
		g.addPoint(new TPoint(120, 214, 0));
		g.addPoint(new TPoint(121, 217, 0));
		g.addPoint(new TPoint(122, 219, 0));
		g.addPoint(new TPoint(123, 222, 0));
		g.addPoint(new TPoint(124, 224, 0));
		g.addPoint(new TPoint(126, 226, 0));
		g.addPoint(new TPoint(127, 229, 0));
		g.addPoint(new TPoint(129, 231, 0));
		g.addPoint(new TPoint(130, 233, 0));
		g.addPoint(new TPoint(129, 231, 0));
		g.addPoint(new TPoint(129, 228, 0));
		g.addPoint(new TPoint(129, 226, 0));
		g.addPoint(new TPoint(129, 224, 0));
		g.addPoint(new TPoint(129, 221, 0));
		g.addPoint(new TPoint(129, 218, 0));
		g.addPoint(new TPoint(129, 212, 0));
		g.addPoint(new TPoint(129, 208, 0));
		g.addPoint(new TPoint(130, 198, 0));
		g.addPoint(new TPoint(132, 189, 0));
		g.addPoint(new TPoint(134, 182, 0));
		g.addPoint(new TPoint(137, 173, 0));
		g.addPoint(new TPoint(143, 164, 0));
		g.addPoint(new TPoint(147, 157, 0));
		g.addPoint(new TPoint(151, 151, 0));
		g.addPoint(new TPoint(155, 144, 0));
		g.addPoint(new TPoint(161, 137, 0));
		g.addPoint(new TPoint(165, 131, 0));
		g.addPoint(new TPoint(171, 122, 0));
		g.addPoint(new TPoint(174, 118, 0));
		g.addPoint(new TPoint(176, 114, 0));
		g.addPoint(new TPoint(177, 112, 0));
		g.addPoint(new TPoint(177, 114, 0));
		g.addPoint(new TPoint(175, 116, 0));
		g.addPoint(new TPoint(173, 118, 0));
		g.setInfo(new GestureInfo(0, null, "check", 0));
		addTemplate("check", g);

		g = new Gesture();
		g.addPoint(new TPoint(79, 245, 0));
		g.addPoint(new TPoint(79, 242, 0));
		g.addPoint(new TPoint(79, 239, 0));
		g.addPoint(new TPoint(80, 237, 0));
		g.addPoint(new TPoint(80, 234, 0));
		g.addPoint(new TPoint(81, 232, 0));
		g.addPoint(new TPoint(82, 230, 0));
		g.addPoint(new TPoint(84, 224, 0));
		g.addPoint(new TPoint(86, 220, 0));
		g.addPoint(new TPoint(86, 218, 0));
		g.addPoint(new TPoint(87, 216, 0));
		g.addPoint(new TPoint(88, 213, 0));
		g.addPoint(new TPoint(90, 207, 0));
		g.addPoint(new TPoint(91, 202, 0));
		g.addPoint(new TPoint(92, 200, 0));
		g.addPoint(new TPoint(93, 194, 0));
		g.addPoint(new TPoint(94, 192, 0));
		g.addPoint(new TPoint(96, 189, 0));
		g.addPoint(new TPoint(97, 186, 0));
		g.addPoint(new TPoint(100, 179, 0));
		g.addPoint(new TPoint(102, 173, 0));
		g.addPoint(new TPoint(105, 165, 0));
		g.addPoint(new TPoint(107, 160, 0));
		g.addPoint(new TPoint(109, 158, 0));
		g.addPoint(new TPoint(112, 151, 0));
		g.addPoint(new TPoint(115, 144, 0));
		g.addPoint(new TPoint(117, 139, 0));
		g.addPoint(new TPoint(119, 136, 0));
		g.addPoint(new TPoint(119, 134, 0));
		g.addPoint(new TPoint(120, 132, 0));
		g.addPoint(new TPoint(121, 129, 0));
		g.addPoint(new TPoint(122, 127, 0));
		g.addPoint(new TPoint(124, 125, 0));
		g.addPoint(new TPoint(126, 124, 0));
		g.addPoint(new TPoint(129, 125, 0));
		g.addPoint(new TPoint(131, 127, 0));
		g.addPoint(new TPoint(132, 130, 0));
		g.addPoint(new TPoint(136, 139, 0));
		g.addPoint(new TPoint(141, 154, 0));
		g.addPoint(new TPoint(145, 166, 0));
		g.addPoint(new TPoint(151, 182, 0));
		g.addPoint(new TPoint(156, 193, 0));
		g.addPoint(new TPoint(157, 196, 0));
		g.addPoint(new TPoint(161, 209, 0));
		g.addPoint(new TPoint(162, 211, 0));
		g.addPoint(new TPoint(167, 223, 0));
		g.addPoint(new TPoint(169, 229, 0));
		g.addPoint(new TPoint(170, 231, 0));
		g.addPoint(new TPoint(173, 237, 0));
		g.addPoint(new TPoint(176, 242, 0));
		g.addPoint(new TPoint(177, 244, 0));
		g.addPoint(new TPoint(179, 250, 0));
		g.addPoint(new TPoint(181, 255, 0));
		g.addPoint(new TPoint(182, 257, 0));
		g.setInfo(new GestureInfo(0, null, "caret", 0));
		addTemplate("caret", g);

		g = new Gesture();
		g.addPoint(new TPoint(307, 216, 0));
		g.addPoint(new TPoint(333, 186, 0));
		g.addPoint(new TPoint(356, 215, 0));
		g.addPoint(new TPoint(375, 186, 0));
		g.addPoint(new TPoint(399, 216, 0));
		g.addPoint(new TPoint(418, 186, 0));
		g.setInfo(new GestureInfo(0, null, "zig-zag", 0));
		addTemplate("zig-zag", g);

		g = new Gesture();
		g.addPoint(new TPoint(68, 222, 0));
		g.addPoint(new TPoint(70, 220, 0));
		g.addPoint(new TPoint(73, 218, 0));
		g.addPoint(new TPoint(75, 217, 0));
		g.addPoint(new TPoint(77, 215, 0));
		g.addPoint(new TPoint(80, 213, 0));
		g.addPoint(new TPoint(82, 212, 0));
		g.addPoint(new TPoint(84, 210, 0));
		g.addPoint(new TPoint(87, 209, 0));
		g.addPoint(new TPoint(89, 208, 0));
		g.addPoint(new TPoint(92, 206, 0));
		g.addPoint(new TPoint(95, 204, 0));
		g.addPoint(new TPoint(101, 201, 0));
		g.addPoint(new TPoint(106, 198, 0));
		g.addPoint(new TPoint(112, 194, 0));
		g.addPoint(new TPoint(118, 191, 0));
		g.addPoint(new TPoint(124, 187, 0));
		g.addPoint(new TPoint(127, 186, 0));
		g.addPoint(new TPoint(132, 183, 0));
		g.addPoint(new TPoint(138, 181, 0));
		g.addPoint(new TPoint(141, 180, 0));
		g.addPoint(new TPoint(146, 178, 0));
		g.addPoint(new TPoint(154, 173, 0));
		g.addPoint(new TPoint(159, 171, 0));
		g.addPoint(new TPoint(161, 170, 0));
		g.addPoint(new TPoint(166, 167, 0));
		g.addPoint(new TPoint(168, 167, 0));
		g.addPoint(new TPoint(171, 166, 0));
		g.addPoint(new TPoint(174, 164, 0));
		g.addPoint(new TPoint(177, 162, 0));
		g.addPoint(new TPoint(180, 160, 0));
		g.addPoint(new TPoint(182, 158, 0));
		g.addPoint(new TPoint(183, 156, 0));
		g.addPoint(new TPoint(181, 154, 0));
		g.addPoint(new TPoint(178, 153, 0));
		g.addPoint(new TPoint(171, 153, 0));
		g.addPoint(new TPoint(164, 153, 0));
		g.addPoint(new TPoint(160, 153, 0));
		g.addPoint(new TPoint(150, 154, 0));
		g.addPoint(new TPoint(147, 155, 0));
		g.addPoint(new TPoint(141, 157, 0));
		g.addPoint(new TPoint(137, 158, 0));
		g.addPoint(new TPoint(135, 158, 0));
		g.addPoint(new TPoint(137, 158, 0));
		g.addPoint(new TPoint(140, 157, 0));
		g.addPoint(new TPoint(143, 156, 0));
		g.addPoint(new TPoint(151, 154, 0));
		g.addPoint(new TPoint(160, 152, 0));
		g.addPoint(new TPoint(170, 149, 0));
		g.addPoint(new TPoint(179, 147, 0));
		g.addPoint(new TPoint(185, 145, 0));
		g.addPoint(new TPoint(192, 144, 0));
		g.addPoint(new TPoint(196, 144, 0));
		g.addPoint(new TPoint(198, 144, 0));
		g.addPoint(new TPoint(200, 144, 0));
		g.addPoint(new TPoint(201, 147, 0));
		g.addPoint(new TPoint(199, 149, 0));
		g.addPoint(new TPoint(194, 157, 0));
		g.addPoint(new TPoint(191, 160, 0));
		g.addPoint(new TPoint(186, 167, 0));
		g.addPoint(new TPoint(180, 176, 0));
		g.addPoint(new TPoint(177, 179, 0));
		g.addPoint(new TPoint(171, 187, 0));
		g.addPoint(new TPoint(169, 189, 0));
		g.addPoint(new TPoint(165, 194, 0));
		g.addPoint(new TPoint(164, 196, 0));
		g.setInfo(new GestureInfo(0, null, "arrow", 0));
		addTemplate("arrow", g);

		g = new Gesture();
		g.addPoint(new TPoint(140, 124, 0));
		g.addPoint(new TPoint(138, 123, 0));
		g.addPoint(new TPoint(135, 122, 0));
		g.addPoint(new TPoint(133, 123, 0));
		g.addPoint(new TPoint(130, 123, 0));
		g.addPoint(new TPoint(128, 124, 0));
		g.addPoint(new TPoint(125, 125, 0));
		g.addPoint(new TPoint(122, 124, 0));
		g.addPoint(new TPoint(120, 124, 0));
		g.addPoint(new TPoint(118, 124, 0));
		g.addPoint(new TPoint(116, 125, 0));
		g.addPoint(new TPoint(113, 125, 0));
		g.addPoint(new TPoint(111, 125, 0));
		g.addPoint(new TPoint(108, 124, 0));
		g.addPoint(new TPoint(106, 125, 0));
		g.addPoint(new TPoint(104, 125, 0));
		g.addPoint(new TPoint(102, 124, 0));
		g.addPoint(new TPoint(100, 123, 0));
		g.addPoint(new TPoint(98, 123, 0));
		g.addPoint(new TPoint(95, 124, 0));
		g.addPoint(new TPoint(93, 123, 0));
		g.addPoint(new TPoint(90, 124, 0));
		g.addPoint(new TPoint(88, 124, 0));
		g.addPoint(new TPoint(85, 125, 0));
		g.addPoint(new TPoint(83, 126, 0));
		g.addPoint(new TPoint(81, 127, 0));
		g.addPoint(new TPoint(81, 129, 0));
		g.addPoint(new TPoint(82, 131, 0));
		g.addPoint(new TPoint(82, 134, 0));
		g.addPoint(new TPoint(83, 138, 0));
		g.addPoint(new TPoint(84, 141, 0));
		g.addPoint(new TPoint(84, 144, 0));
		g.addPoint(new TPoint(85, 148, 0));
		g.addPoint(new TPoint(85, 151, 0));
		g.addPoint(new TPoint(86, 156, 0));
		g.addPoint(new TPoint(86, 160, 0));
		g.addPoint(new TPoint(86, 164, 0));
		g.addPoint(new TPoint(86, 168, 0));
		g.addPoint(new TPoint(87, 171, 0));
		g.addPoint(new TPoint(87, 175, 0));
		g.addPoint(new TPoint(87, 179, 0));
		g.addPoint(new TPoint(87, 182, 0));
		g.addPoint(new TPoint(87, 186, 0));
		g.addPoint(new TPoint(88, 188, 0));
		g.addPoint(new TPoint(88, 195, 0));
		g.addPoint(new TPoint(88, 198, 0));
		g.addPoint(new TPoint(88, 201, 0));
		g.addPoint(new TPoint(88, 207, 0));
		g.addPoint(new TPoint(89, 211, 0));
		g.addPoint(new TPoint(89, 213, 0));
		g.addPoint(new TPoint(89, 217, 0));
		g.addPoint(new TPoint(89, 222, 0));
		g.addPoint(new TPoint(88, 225, 0));
		g.addPoint(new TPoint(88, 229, 0));
		g.addPoint(new TPoint(88, 231, 0));
		g.addPoint(new TPoint(88, 233, 0));
		g.addPoint(new TPoint(88, 235, 0));
		g.addPoint(new TPoint(89, 237, 0));
		g.addPoint(new TPoint(89, 240, 0));
		g.addPoint(new TPoint(89, 242, 0));
		g.addPoint(new TPoint(91, 241, 0));
		g.addPoint(new TPoint(94, 241, 0));
		g.addPoint(new TPoint(96, 240, 0));
		g.addPoint(new TPoint(98, 239, 0));
		g.addPoint(new TPoint(105, 240, 0));
		g.addPoint(new TPoint(109, 240, 0));
		g.addPoint(new TPoint(113, 239, 0));
		g.addPoint(new TPoint(116, 240, 0));
		g.addPoint(new TPoint(121, 239, 0));
		g.addPoint(new TPoint(130, 240, 0));
		g.addPoint(new TPoint(136, 237, 0));
		g.addPoint(new TPoint(139, 237, 0));
		g.addPoint(new TPoint(144, 238, 0));
		g.addPoint(new TPoint(151, 237, 0));
		g.addPoint(new TPoint(157, 236, 0));
		g.addPoint(new TPoint(159, 237, 0));
		g.setInfo(new GestureInfo(0, null, "left square bracket", 0));
		addTemplate("left square bracket", g);

		g = new Gesture();
		g.addPoint(new TPoint(112, 138, 0));
		g.addPoint(new TPoint(112, 136, 0));
		g.addPoint(new TPoint(115, 136, 0));
		g.addPoint(new TPoint(118, 137, 0));
		g.addPoint(new TPoint(120, 136, 0));
		g.addPoint(new TPoint(123, 136, 0));
		g.addPoint(new TPoint(125, 136, 0));
		g.addPoint(new TPoint(128, 136, 0));
		g.addPoint(new TPoint(131, 136, 0));
		g.addPoint(new TPoint(134, 135, 0));
		g.addPoint(new TPoint(137, 135, 0));
		g.addPoint(new TPoint(140, 134, 0));
		g.addPoint(new TPoint(143, 133, 0));
		g.addPoint(new TPoint(145, 132, 0));
		g.addPoint(new TPoint(147, 132, 0));
		g.addPoint(new TPoint(149, 132, 0));
		g.addPoint(new TPoint(152, 132, 0));
		g.addPoint(new TPoint(153, 134, 0));
		g.addPoint(new TPoint(154, 137, 0));
		g.addPoint(new TPoint(155, 141, 0));
		g.addPoint(new TPoint(156, 144, 0));
		g.addPoint(new TPoint(157, 152, 0));
		g.addPoint(new TPoint(158, 161, 0));
		g.addPoint(new TPoint(160, 170, 0));
		g.addPoint(new TPoint(162, 182, 0));
		g.addPoint(new TPoint(164, 192, 0));
		g.addPoint(new TPoint(166, 200, 0));
		g.addPoint(new TPoint(167, 209, 0));
		g.addPoint(new TPoint(168, 214, 0));
		g.addPoint(new TPoint(168, 216, 0));
		g.addPoint(new TPoint(169, 221, 0));
		g.addPoint(new TPoint(169, 223, 0));
		g.addPoint(new TPoint(169, 228, 0));
		g.addPoint(new TPoint(169, 231, 0));
		g.addPoint(new TPoint(166, 233, 0));
		g.addPoint(new TPoint(164, 234, 0));
		g.addPoint(new TPoint(161, 235, 0));
		g.addPoint(new TPoint(155, 236, 0));
		g.addPoint(new TPoint(147, 235, 0));
		g.addPoint(new TPoint(140, 233, 0));
		g.addPoint(new TPoint(131, 233, 0));
		g.addPoint(new TPoint(124, 233, 0));
		g.addPoint(new TPoint(117, 235, 0));
		g.addPoint(new TPoint(114, 238, 0));
		g.addPoint(new TPoint(112, 238, 0));
		g.setInfo(new GestureInfo(0, null, "right square bracket", 0));
		addTemplate("right square bracket", g);

		g = new Gesture();
		g.addPoint(new TPoint(89, 164, 0));
		g.addPoint(new TPoint(90, 162, 0));
		g.addPoint(new TPoint(92, 162, 0));
		g.addPoint(new TPoint(94, 164, 0));
		g.addPoint(new TPoint(95, 166, 0));
		g.addPoint(new TPoint(96, 169, 0));
		g.addPoint(new TPoint(97, 171, 0));
		g.addPoint(new TPoint(99, 175, 0));
		g.addPoint(new TPoint(101, 178, 0));
		g.addPoint(new TPoint(103, 182, 0));
		g.addPoint(new TPoint(106, 189, 0));
		g.addPoint(new TPoint(108, 194, 0));
		g.addPoint(new TPoint(111, 199, 0));
		g.addPoint(new TPoint(114, 204, 0));
		g.addPoint(new TPoint(117, 209, 0));
		g.addPoint(new TPoint(119, 214, 0));
		g.addPoint(new TPoint(122, 218, 0));
		g.addPoint(new TPoint(124, 222, 0));
		g.addPoint(new TPoint(126, 225, 0));
		g.addPoint(new TPoint(128, 228, 0));
		g.addPoint(new TPoint(130, 229, 0));
		g.addPoint(new TPoint(133, 233, 0));
		g.addPoint(new TPoint(134, 236, 0));
		g.addPoint(new TPoint(136, 239, 0));
		g.addPoint(new TPoint(138, 240, 0));
		g.addPoint(new TPoint(139, 242, 0));
		g.addPoint(new TPoint(140, 244, 0));
		g.addPoint(new TPoint(142, 242, 0));
		g.addPoint(new TPoint(142, 240, 0));
		g.addPoint(new TPoint(142, 237, 0));
		g.addPoint(new TPoint(143, 235, 0));
		g.addPoint(new TPoint(143, 233, 0));
		g.addPoint(new TPoint(145, 229, 0));
		g.addPoint(new TPoint(146, 226, 0));
		g.addPoint(new TPoint(148, 217, 0));
		g.addPoint(new TPoint(149, 208, 0));
		g.addPoint(new TPoint(149, 205, 0));
		g.addPoint(new TPoint(151, 196, 0));
		g.addPoint(new TPoint(151, 193, 0));
		g.addPoint(new TPoint(153, 182, 0));
		g.addPoint(new TPoint(155, 172, 0));
		g.addPoint(new TPoint(157, 165, 0));
		g.addPoint(new TPoint(159, 160, 0));
		g.addPoint(new TPoint(162, 155, 0));
		g.addPoint(new TPoint(164, 150, 0));
		g.addPoint(new TPoint(165, 148, 0));
		g.addPoint(new TPoint(166, 146, 0));
		g.setInfo(new GestureInfo(0, null, "v", 0));
		addTemplate("v", g);

		g = new Gesture();
		g.addPoint(new TPoint(123, 129, 0));
		g.addPoint(new TPoint(123, 131, 0));
		g.addPoint(new TPoint(124, 133, 0));
		g.addPoint(new TPoint(125, 136, 0));
		g.addPoint(new TPoint(127, 140, 0));
		g.addPoint(new TPoint(129, 142, 0));
		g.addPoint(new TPoint(133, 148, 0));
		g.addPoint(new TPoint(137, 154, 0));
		g.addPoint(new TPoint(143, 158, 0));
		g.addPoint(new TPoint(145, 161, 0));
		g.addPoint(new TPoint(148, 164, 0));
		g.addPoint(new TPoint(153, 170, 0));
		g.addPoint(new TPoint(158, 176, 0));
		g.addPoint(new TPoint(160, 178, 0));
		g.addPoint(new TPoint(164, 183, 0));
		g.addPoint(new TPoint(168, 188, 0));
		g.addPoint(new TPoint(171, 191, 0));
		g.addPoint(new TPoint(175, 196, 0));
		g.addPoint(new TPoint(178, 200, 0));
		g.addPoint(new TPoint(180, 202, 0));
		g.addPoint(new TPoint(181, 205, 0));
		g.addPoint(new TPoint(184, 208, 0));
		g.addPoint(new TPoint(186, 210, 0));
		g.addPoint(new TPoint(187, 213, 0));
		g.addPoint(new TPoint(188, 215, 0));
		g.addPoint(new TPoint(186, 212, 0));
		g.addPoint(new TPoint(183, 211, 0));
		g.addPoint(new TPoint(177, 208, 0));
		g.addPoint(new TPoint(169, 206, 0));
		g.addPoint(new TPoint(162, 205, 0));
		g.addPoint(new TPoint(154, 207, 0));
		g.addPoint(new TPoint(145, 209, 0));
		g.addPoint(new TPoint(137, 210, 0));
		g.addPoint(new TPoint(129, 214, 0));
		g.addPoint(new TPoint(122, 217, 0));
		g.addPoint(new TPoint(118, 218, 0));
		g.addPoint(new TPoint(111, 221, 0));
		g.addPoint(new TPoint(109, 222, 0));
		g.addPoint(new TPoint(110, 219, 0));
		g.addPoint(new TPoint(112, 217, 0));
		g.addPoint(new TPoint(118, 209, 0));
		g.addPoint(new TPoint(120, 207, 0));
		g.addPoint(new TPoint(128, 196, 0));
		g.addPoint(new TPoint(135, 187, 0));
		g.addPoint(new TPoint(138, 183, 0));
		g.addPoint(new TPoint(148, 167, 0));
		g.addPoint(new TPoint(157, 153, 0));
		g.addPoint(new TPoint(163, 145, 0));
		g.addPoint(new TPoint(165, 142, 0));
		g.addPoint(new TPoint(172, 133, 0));
		g.addPoint(new TPoint(177, 127, 0));
		g.addPoint(new TPoint(179, 127, 0));
		g.addPoint(new TPoint(180, 125, 0));
		g.setInfo(new GestureInfo(0, null, "delete", 0));
		addTemplate("delete", g);

		g = new Gesture();
		g.addPoint(new TPoint(150, 116, 0));
		g.addPoint(new TPoint(147, 117, 0));
		g.addPoint(new TPoint(145, 116, 0));
		g.addPoint(new TPoint(142, 116, 0));
		g.addPoint(new TPoint(139, 117, 0));
		g.addPoint(new TPoint(136, 117, 0));
		g.addPoint(new TPoint(133, 118, 0));
		g.addPoint(new TPoint(129, 121, 0));
		g.addPoint(new TPoint(126, 122, 0));
		g.addPoint(new TPoint(123, 123, 0));
		g.addPoint(new TPoint(120, 125, 0));
		g.addPoint(new TPoint(118, 127, 0));
		g.addPoint(new TPoint(115, 128, 0));
		g.addPoint(new TPoint(113, 129, 0));
		g.addPoint(new TPoint(112, 131, 0));
		g.addPoint(new TPoint(113, 134, 0));
		g.addPoint(new TPoint(115, 134, 0));
		g.addPoint(new TPoint(117, 135, 0));
		g.addPoint(new TPoint(120, 135, 0));
		g.addPoint(new TPoint(123, 137, 0));
		g.addPoint(new TPoint(126, 138, 0));
		g.addPoint(new TPoint(129, 140, 0));
		g.addPoint(new TPoint(135, 143, 0));
		g.addPoint(new TPoint(137, 144, 0));
		g.addPoint(new TPoint(139, 147, 0));
		g.addPoint(new TPoint(141, 149, 0));
		g.addPoint(new TPoint(140, 152, 0));
		g.addPoint(new TPoint(139, 155, 0));
		g.addPoint(new TPoint(134, 159, 0));
		g.addPoint(new TPoint(131, 161, 0));
		g.addPoint(new TPoint(124, 166, 0));
		g.addPoint(new TPoint(121, 166, 0));
		g.addPoint(new TPoint(117, 166, 0));
		g.addPoint(new TPoint(114, 167, 0));
		g.addPoint(new TPoint(112, 166, 0));
		g.addPoint(new TPoint(114, 164, 0));
		g.addPoint(new TPoint(116, 163, 0));
		g.addPoint(new TPoint(118, 163, 0));
		g.addPoint(new TPoint(120, 162, 0));
		g.addPoint(new TPoint(122, 163, 0));
		g.addPoint(new TPoint(125, 164, 0));
		g.addPoint(new TPoint(127, 165, 0));
		g.addPoint(new TPoint(129, 166, 0));
		g.addPoint(new TPoint(130, 168, 0));
		g.addPoint(new TPoint(129, 171, 0));
		g.addPoint(new TPoint(127, 175, 0));
		g.addPoint(new TPoint(125, 179, 0));
		g.addPoint(new TPoint(123, 184, 0));
		g.addPoint(new TPoint(121, 190, 0));
		g.addPoint(new TPoint(120, 194, 0));
		g.addPoint(new TPoint(119, 199, 0));
		g.addPoint(new TPoint(120, 202, 0));
		g.addPoint(new TPoint(123, 207, 0));
		g.addPoint(new TPoint(127, 211, 0));
		g.addPoint(new TPoint(133, 215, 0));
		g.addPoint(new TPoint(142, 219, 0));
		g.addPoint(new TPoint(148, 220, 0));
		g.addPoint(new TPoint(151, 221, 0));
		g.setInfo(new GestureInfo(0, null, "left curly brace", 0));
		addTemplate("left curly brace", g);

		g = new Gesture();
		g.addPoint(new TPoint(117, 132, 0));
		g.addPoint(new TPoint(115, 132, 0));
		g.addPoint(new TPoint(115, 129, 0));
		g.addPoint(new TPoint(117, 129, 0));
		g.addPoint(new TPoint(119, 128, 0));
		g.addPoint(new TPoint(122, 127, 0));
		g.addPoint(new TPoint(125, 127, 0));
		g.addPoint(new TPoint(127, 127, 0));
		g.addPoint(new TPoint(130, 127, 0));
		g.addPoint(new TPoint(133, 129, 0));
		g.addPoint(new TPoint(136, 129, 0));
		g.addPoint(new TPoint(138, 130, 0));
		g.addPoint(new TPoint(140, 131, 0));
		g.addPoint(new TPoint(143, 134, 0));
		g.addPoint(new TPoint(144, 136, 0));
		g.addPoint(new TPoint(145, 139, 0));
		g.addPoint(new TPoint(145, 142, 0));
		g.addPoint(new TPoint(145, 145, 0));
		g.addPoint(new TPoint(145, 147, 0));
		g.addPoint(new TPoint(145, 149, 0));
		g.addPoint(new TPoint(144, 152, 0));
		g.addPoint(new TPoint(142, 157, 0));
		g.addPoint(new TPoint(141, 160, 0));
		g.addPoint(new TPoint(139, 163, 0));
		g.addPoint(new TPoint(137, 166, 0));
		g.addPoint(new TPoint(135, 167, 0));
		g.addPoint(new TPoint(133, 169, 0));
		g.addPoint(new TPoint(131, 172, 0));
		g.addPoint(new TPoint(128, 173, 0));
		g.addPoint(new TPoint(126, 176, 0));
		g.addPoint(new TPoint(125, 178, 0));
		g.addPoint(new TPoint(125, 180, 0));
		g.addPoint(new TPoint(125, 182, 0));
		g.addPoint(new TPoint(126, 184, 0));
		g.addPoint(new TPoint(128, 187, 0));
		g.addPoint(new TPoint(130, 187, 0));
		g.addPoint(new TPoint(132, 188, 0));
		g.addPoint(new TPoint(135, 189, 0));
		g.addPoint(new TPoint(140, 189, 0));
		g.addPoint(new TPoint(145, 189, 0));
		g.addPoint(new TPoint(150, 187, 0));
		g.addPoint(new TPoint(155, 186, 0));
		g.addPoint(new TPoint(157, 185, 0));
		g.addPoint(new TPoint(159, 184, 0));
		g.addPoint(new TPoint(156, 185, 0));
		g.addPoint(new TPoint(154, 185, 0));
		g.addPoint(new TPoint(149, 185, 0));
		g.addPoint(new TPoint(145, 187, 0));
		g.addPoint(new TPoint(141, 188, 0));
		g.addPoint(new TPoint(136, 191, 0));
		g.addPoint(new TPoint(134, 191, 0));
		g.addPoint(new TPoint(131, 192, 0));
		g.addPoint(new TPoint(129, 193, 0));
		g.addPoint(new TPoint(129, 195, 0));
		g.addPoint(new TPoint(129, 197, 0));
		g.addPoint(new TPoint(131, 200, 0));
		g.addPoint(new TPoint(133, 202, 0));
		g.addPoint(new TPoint(136, 206, 0));
		g.addPoint(new TPoint(139, 211, 0));
		g.addPoint(new TPoint(142, 215, 0));
		g.addPoint(new TPoint(145, 220, 0));
		g.addPoint(new TPoint(147, 225, 0));
		g.addPoint(new TPoint(148, 231, 0));
		g.addPoint(new TPoint(147, 239, 0));
		g.addPoint(new TPoint(144, 244, 0));
		g.addPoint(new TPoint(139, 248, 0));
		g.addPoint(new TPoint(134, 250, 0));
		g.addPoint(new TPoint(126, 253, 0));
		g.addPoint(new TPoint(119, 253, 0));
		g.addPoint(new TPoint(115, 253, 0));
		g.setInfo(new GestureInfo(0, null, "right curly brace", 0));
		addTemplate("right curly brace", g);

		g = new Gesture();
		g.addPoint(new TPoint(75, 250, 0));
		g.addPoint(new TPoint(75, 247, 0));
		g.addPoint(new TPoint(77, 244, 0));
		g.addPoint(new TPoint(78, 242, 0));
		g.addPoint(new TPoint(79, 239, 0));
		g.addPoint(new TPoint(80, 237, 0));
		g.addPoint(new TPoint(82, 234, 0));
		g.addPoint(new TPoint(82, 232, 0));
		g.addPoint(new TPoint(84, 229, 0));
		g.addPoint(new TPoint(85, 225, 0));
		g.addPoint(new TPoint(87, 222, 0));
		g.addPoint(new TPoint(88, 219, 0));
		g.addPoint(new TPoint(89, 216, 0));
		g.addPoint(new TPoint(91, 212, 0));
		g.addPoint(new TPoint(92, 208, 0));
		g.addPoint(new TPoint(94, 204, 0));
		g.addPoint(new TPoint(95, 201, 0));
		g.addPoint(new TPoint(96, 196, 0));
		g.addPoint(new TPoint(97, 194, 0));
		g.addPoint(new TPoint(98, 191, 0));
		g.addPoint(new TPoint(100, 185, 0));
		g.addPoint(new TPoint(102, 178, 0));
		g.addPoint(new TPoint(104, 173, 0));
		g.addPoint(new TPoint(104, 171, 0));
		g.addPoint(new TPoint(105, 164, 0));
		g.addPoint(new TPoint(106, 158, 0));
		g.addPoint(new TPoint(107, 156, 0));
		g.addPoint(new TPoint(107, 152, 0));
		g.addPoint(new TPoint(108, 145, 0));
		g.addPoint(new TPoint(109, 141, 0));
		g.addPoint(new TPoint(110, 139, 0));
		g.addPoint(new TPoint(112, 133, 0));
		g.addPoint(new TPoint(113, 131, 0));
		g.addPoint(new TPoint(116, 127, 0));
		g.addPoint(new TPoint(117, 125, 0));
		g.addPoint(new TPoint(119, 122, 0));
		g.addPoint(new TPoint(121, 121, 0));
		g.addPoint(new TPoint(123, 120, 0));
		g.addPoint(new TPoint(125, 122, 0));
		g.addPoint(new TPoint(125, 125, 0));
		g.addPoint(new TPoint(127, 130, 0));
		g.addPoint(new TPoint(128, 133, 0));
		g.addPoint(new TPoint(131, 143, 0));
		g.addPoint(new TPoint(136, 153, 0));
		g.addPoint(new TPoint(140, 163, 0));
		g.addPoint(new TPoint(144, 172, 0));
		g.addPoint(new TPoint(145, 175, 0));
		g.addPoint(new TPoint(151, 189, 0));
		g.addPoint(new TPoint(156, 201, 0));
		g.addPoint(new TPoint(161, 213, 0));
		g.addPoint(new TPoint(166, 225, 0));
		g.addPoint(new TPoint(169, 233, 0));
		g.addPoint(new TPoint(171, 236, 0));
		g.addPoint(new TPoint(174, 243, 0));
		g.addPoint(new TPoint(177, 247, 0));
		g.addPoint(new TPoint(178, 249, 0));
		g.addPoint(new TPoint(179, 251, 0));
		g.addPoint(new TPoint(180, 253, 0));
		g.addPoint(new TPoint(180, 255, 0));
		g.addPoint(new TPoint(179, 257, 0));
		g.addPoint(new TPoint(177, 257, 0));
		g.addPoint(new TPoint(174, 255, 0));
		g.addPoint(new TPoint(169, 250, 0));
		g.addPoint(new TPoint(164, 247, 0));
		g.addPoint(new TPoint(160, 245, 0));
		g.addPoint(new TPoint(149, 238, 0));
		g.addPoint(new TPoint(138, 230, 0));
		g.addPoint(new TPoint(127, 221, 0));
		g.addPoint(new TPoint(124, 220, 0));
		g.addPoint(new TPoint(112, 212, 0));
		g.addPoint(new TPoint(110, 210, 0));
		g.addPoint(new TPoint(96, 201, 0));
		g.addPoint(new TPoint(84, 195, 0));
		g.addPoint(new TPoint(74, 190, 0));
		g.addPoint(new TPoint(64, 182, 0));
		g.addPoint(new TPoint(55, 175, 0));
		g.addPoint(new TPoint(51, 172, 0));
		g.addPoint(new TPoint(49, 170, 0));
		g.addPoint(new TPoint(51, 169, 0));
		g.addPoint(new TPoint(56, 169, 0));
		g.addPoint(new TPoint(66, 169, 0));
		g.addPoint(new TPoint(78, 168, 0));
		g.addPoint(new TPoint(92, 166, 0));
		g.addPoint(new TPoint(107, 164, 0));
		g.addPoint(new TPoint(123, 161, 0));
		g.addPoint(new TPoint(140, 162, 0));
		g.addPoint(new TPoint(156, 162, 0));
		g.addPoint(new TPoint(171, 160, 0));
		g.addPoint(new TPoint(173, 160, 0));
		g.addPoint(new TPoint(186, 160, 0));
		g.addPoint(new TPoint(195, 160, 0));
		g.addPoint(new TPoint(198, 161, 0));
		g.addPoint(new TPoint(203, 163, 0));
		g.addPoint(new TPoint(208, 163, 0));
		g.addPoint(new TPoint(206, 164, 0));
		g.addPoint(new TPoint(200, 167, 0));
		g.addPoint(new TPoint(187, 172, 0));
		g.addPoint(new TPoint(174, 179, 0));
		g.addPoint(new TPoint(172, 181, 0));
		g.addPoint(new TPoint(153, 192, 0));
		g.addPoint(new TPoint(137, 201, 0));
		g.addPoint(new TPoint(123, 211, 0));
		g.addPoint(new TPoint(112, 220, 0));
		g.addPoint(new TPoint(99, 229, 0));
		g.addPoint(new TPoint(90, 237, 0));
		g.addPoint(new TPoint(80, 244, 0));
		g.addPoint(new TPoint(73, 250, 0));
		g.addPoint(new TPoint(69, 254, 0));
		g.addPoint(new TPoint(69, 252, 0));
		g.setInfo(new GestureInfo(0, null, "star", 0));
		addTemplate("star", g);

		g = new Gesture();
		g.addPoint(new TPoint(81, 219, 0));
		g.addPoint(new TPoint(84, 218, 0));
		g.addPoint(new TPoint(86, 220, 0));
		g.addPoint(new TPoint(88, 220, 0));
		g.addPoint(new TPoint(90, 220, 0));
		g.addPoint(new TPoint(92, 219, 0));
		g.addPoint(new TPoint(95, 220, 0));
		g.addPoint(new TPoint(97, 219, 0));
		g.addPoint(new TPoint(99, 220, 0));
		g.addPoint(new TPoint(102, 218, 0));
		g.addPoint(new TPoint(105, 217, 0));
		g.addPoint(new TPoint(107, 216, 0));
		g.addPoint(new TPoint(110, 216, 0));
		g.addPoint(new TPoint(113, 214, 0));
		g.addPoint(new TPoint(116, 212, 0));
		g.addPoint(new TPoint(118, 210, 0));
		g.addPoint(new TPoint(121, 208, 0));
		g.addPoint(new TPoint(124, 205, 0));
		g.addPoint(new TPoint(126, 202, 0));
		g.addPoint(new TPoint(129, 199, 0));
		g.addPoint(new TPoint(132, 196, 0));
		g.addPoint(new TPoint(136, 191, 0));
		g.addPoint(new TPoint(139, 187, 0));
		g.addPoint(new TPoint(142, 182, 0));
		g.addPoint(new TPoint(144, 179, 0));
		g.addPoint(new TPoint(146, 174, 0));
		g.addPoint(new TPoint(148, 170, 0));
		g.addPoint(new TPoint(149, 168, 0));
		g.addPoint(new TPoint(151, 162, 0));
		g.addPoint(new TPoint(152, 160, 0));
		g.addPoint(new TPoint(152, 157, 0));
		g.addPoint(new TPoint(152, 155, 0));
		g.addPoint(new TPoint(152, 151, 0));
		g.addPoint(new TPoint(152, 149, 0));
		g.addPoint(new TPoint(152, 146, 0));
		g.addPoint(new TPoint(149, 142, 0));
		g.addPoint(new TPoint(148, 139, 0));
		g.addPoint(new TPoint(145, 137, 0));
		g.addPoint(new TPoint(141, 135, 0));
		g.addPoint(new TPoint(139, 135, 0));
		g.addPoint(new TPoint(134, 136, 0));
		g.addPoint(new TPoint(130, 140, 0));
		g.addPoint(new TPoint(128, 142, 0));
		g.addPoint(new TPoint(126, 145, 0));
		g.addPoint(new TPoint(122, 150, 0));
		g.addPoint(new TPoint(119, 158, 0));
		g.addPoint(new TPoint(117, 163, 0));
		g.addPoint(new TPoint(115, 170, 0));
		g.addPoint(new TPoint(114, 175, 0));
		g.addPoint(new TPoint(117, 184, 0));
		g.addPoint(new TPoint(120, 190, 0));
		g.addPoint(new TPoint(125, 199, 0));
		g.addPoint(new TPoint(129, 203, 0));
		g.addPoint(new TPoint(133, 208, 0));
		g.addPoint(new TPoint(138, 213, 0));
		g.addPoint(new TPoint(145, 215, 0));
		g.addPoint(new TPoint(155, 218, 0));
		g.addPoint(new TPoint(164, 219, 0));
		g.addPoint(new TPoint(166, 219, 0));
		g.addPoint(new TPoint(177, 219, 0));
		g.addPoint(new TPoint(182, 218, 0));
		g.addPoint(new TPoint(192, 216, 0));
		g.addPoint(new TPoint(196, 213, 0));
		g.addPoint(new TPoint(199, 212, 0));
		g.addPoint(new TPoint(201, 211, 0));
		g.setInfo(new GestureInfo(0, null, "pigtail", 0));
		addTemplate("pigtail", g);

		//save();
	}
	*/
	//gestural keyboard
	void addSamples() {
		Gesture g = new Gesture();
		g.addPoint(new TPoint(0,0, 0));
		g.addPoint(new TPoint(-32,0, 0));
		g.addPoint(new TPoint(-64,0, 0));
		g.setInfo(new GestureInfo(0, null, "Left", 0));
		//addTemplate("left", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(0,0, 0));
		g.addPoint(new TPoint(32,0, 0));
		g.addPoint(new TPoint(64,0, 0));
		g.setInfo(new GestureInfo(0, null, "right", 0));
		//addTemplate("right", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(0,0, 0));
		g.addPoint(new TPoint(0,-32, 0));
		g.addPoint(new TPoint(0,-64, 0));
		g.setInfo(new GestureInfo(0, null, "up", 0));
	//	addTemplate("up", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(0,0, 0));
		g.addPoint(new TPoint(0,32, 0));
		g.addPoint(new TPoint(0,64, 0));
		g.setInfo(new GestureInfo(0, null, "down", 0));
	//	addTemplate("down", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(0,0, 0));
		g.addPoint(new TPoint(32,64, 0));
		g.addPoint(new TPoint(64,0, 0));
		g.setInfo(new GestureInfo(0, null, "paste", 0));
		//addTemplate("paste", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(0,0, 0));
		g.addPoint(new TPoint(32,48, 0));
		g.addPoint(new TPoint(64,0, 0));
		g.setInfo(new GestureInfo(0, null, "paste", 0));
		//addTemplate("paste", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(91,185, 0));
		g.addPoint(new TPoint(93,185, 0));
		g.addPoint(new TPoint(95,185, 0));
		g.addPoint(new TPoint(97,185, 0));
		g.addPoint(new TPoint(100,188, 0));
		g.addPoint(new TPoint(102,189, 0));
		g.addPoint(new TPoint(104,190, 0));
		g.addPoint(new TPoint(106,193, 0));
		g.addPoint(new TPoint(108,195, 0));
		g.addPoint(new TPoint(110,198, 0));
		g.addPoint(new TPoint(112,201, 0));
		g.addPoint(new TPoint(112,204, 0));
		g.addPoint(new TPoint(114,204, 0));
		g.addPoint(new TPoint(115,207, 0));
		g.addPoint(new TPoint(117,210, 0));
		g.addPoint(new TPoint(118,212, 0));
		g.addPoint(new TPoint(120,214, 0));
		g.addPoint(new TPoint(121,217, 0));
		g.addPoint(new TPoint(122,219, 0));
		g.addPoint(new TPoint(123,222, 0));
		g.addPoint(new TPoint(124,224, 0));
		g.addPoint(new TPoint(126,226, 0));
		g.addPoint(new TPoint(127,229, 0));
		g.addPoint(new TPoint(129,231, 0));
		g.addPoint(new TPoint(130,233, 0));
		g.addPoint(new TPoint(129,231, 0));
		g.addPoint(new TPoint(129,228, 0));
		g.addPoint(new TPoint(129,226, 0));
		g.addPoint(new TPoint(129,224, 0));
		g.addPoint(new TPoint(129,221, 0));
		g.addPoint(new TPoint(129,218, 0));
		g.addPoint(new TPoint(129,212, 0));
		g.addPoint(new TPoint(129,208, 0));
		g.addPoint(new TPoint(130,198, 0));
		g.addPoint(new TPoint(132,189, 0));
		g.addPoint(new TPoint(134,182, 0));
		g.addPoint(new TPoint(137,173, 0));
		g.addPoint(new TPoint(143,164, 0));
		g.addPoint(new TPoint(147,157, 0));
		g.addPoint(new TPoint(151,151, 0));
		g.addPoint(new TPoint(155,144, 0));
		g.addPoint(new TPoint(161,137, 0));
		g.addPoint(new TPoint(165,131, 0));
		g.addPoint(new TPoint(171,122, 0));
		g.addPoint(new TPoint(174,118, 0));
		g.addPoint(new TPoint(176,114, 0));
		g.addPoint(new TPoint(177,112, 0));
		g.addPoint(new TPoint(177,114, 0));
		g.addPoint(new TPoint(175,116, 0));
		g.addPoint(new TPoint(173,118, 0));
		g.setInfo(new GestureInfo(0, null, "Check", 0));
		//addTemplate("Check", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(79,245, 0));
		g.addPoint(new TPoint(79,242, 0));
		g.addPoint(new TPoint(79,239, 0));
		g.addPoint(new TPoint(80,237, 0));
		g.addPoint(new TPoint(80,234, 0));
		g.addPoint(new TPoint(81,232, 0));
		g.addPoint(new TPoint(82,230, 0));
		g.addPoint(new TPoint(84,224, 0));
		g.addPoint(new TPoint(86,220, 0));
		g.addPoint(new TPoint(86,218, 0));
		g.addPoint(new TPoint(87,216, 0));
		g.addPoint(new TPoint(88,213, 0));
		g.addPoint(new TPoint(90,207, 0));
		g.addPoint(new TPoint(91,202, 0));
		g.addPoint(new TPoint(92,200, 0));
		g.addPoint(new TPoint(93,194, 0));
		g.addPoint(new TPoint(94,192, 0));
		g.addPoint(new TPoint(96,189, 0));
		g.addPoint(new TPoint(97,186, 0));
		g.addPoint(new TPoint(100,179, 0));
		g.addPoint(new TPoint(102,173, 0));
		g.addPoint(new TPoint(105,165, 0));
		g.addPoint(new TPoint(107,160, 0));
		g.addPoint(new TPoint(109,158, 0));
		g.addPoint(new TPoint(112,151, 0));
		g.addPoint(new TPoint(115,144, 0));
		g.addPoint(new TPoint(117,139, 0));
		g.addPoint(new TPoint(119,136, 0));
		g.addPoint(new TPoint(119,134, 0));
		g.addPoint(new TPoint(120,132, 0));
		g.addPoint(new TPoint(121,129, 0));
		g.addPoint(new TPoint(122,127, 0));
		g.addPoint(new TPoint(124,125, 0));
		g.addPoint(new TPoint(126,124, 0));
		g.addPoint(new TPoint(129,125, 0));
		g.addPoint(new TPoint(131,127, 0));
		g.addPoint(new TPoint(132,130, 0));
		g.addPoint(new TPoint(136,139, 0));
		g.addPoint(new TPoint(141,154, 0));
		g.addPoint(new TPoint(145,166, 0));
		g.addPoint(new TPoint(151,182, 0));
		g.addPoint(new TPoint(156,193, 0));
		g.addPoint(new TPoint(157,196, 0));
		g.addPoint(new TPoint(161,209, 0));
		g.addPoint(new TPoint(162,211, 0));
		g.addPoint(new TPoint(167,223, 0));
		g.addPoint(new TPoint(169,229, 0));
		g.addPoint(new TPoint(170,231, 0));
		g.addPoint(new TPoint(173,237, 0));
		g.addPoint(new TPoint(176,242, 0));
		g.addPoint(new TPoint(177,244, 0));
		g.addPoint(new TPoint(179,250, 0));
		g.addPoint(new TPoint(181,255, 0));
		g.addPoint(new TPoint(182,257, 0));
		
		
		g.setInfo(new GestureInfo(0, null, "Caret", 0));
		//addTemplate("Caret", g);
		
		
		g = new Gesture();
		g.addPoint(new TPoint(0,0, 0));
		g.addPoint(new TPoint(48,48, 0));
		g.addPoint(new TPoint(40,56, 0));
		g.addPoint(new TPoint(32,64, 0));
		g.addPoint(new TPoint(24,56, 0));
		g.addPoint(new TPoint(16,48, 0));
		g.addPoint(new TPoint(64,0, 0));
		
		
		g.setInfo(new GestureInfo(0, null, "xDown", 0));
		//addTemplate("xDown", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(87,142, 0));
		g.addPoint(new TPoint(89,145, 0));
		g.addPoint(new TPoint(91,148, 0));
		g.addPoint(new TPoint(93,151, 0));
		g.addPoint(new TPoint(96,155, 0));
		g.addPoint(new TPoint(98,157, 0));
		g.addPoint(new TPoint(100,160, 0));
		g.addPoint(new TPoint(102,162, 0));
		g.addPoint(new TPoint(106,167, 0));
		g.addPoint(new TPoint(108,169, 0));
		g.addPoint(new TPoint(108,169, 0));
		g.addPoint(new TPoint(110,171, 0));
		g.addPoint(new TPoint(115,177, 0));
		g.addPoint(new TPoint(119,183, 0));
		g.addPoint(new TPoint(123,189, 0));
		g.addPoint(new TPoint(127,193, 0));
		g.addPoint(new TPoint(129,196, 0));
		g.addPoint(new TPoint(133,200, 0));
		g.addPoint(new TPoint(137,206, 0));
		g.addPoint(new TPoint(140,209, 0));
		g.addPoint(new TPoint(143,212, 0));
		g.addPoint(new TPoint(146,215, 0));
		g.addPoint(new TPoint(151,220, 0));
		g.addPoint(new TPoint(153,222, 0));
		g.addPoint(new TPoint(155,223, 0));
		g.addPoint(new TPoint(157,225, 0));
		g.addPoint(new TPoint(158,223, 0));
		g.addPoint(new TPoint(157,218, 0));
		g.addPoint(new TPoint(155,211, 0));
		g.addPoint(new TPoint(154,208, 0));
		g.addPoint(new TPoint(152,200, 0));
		g.addPoint(new TPoint(150,189, 0));
		g.addPoint(new TPoint(148,179, 0));
		g.addPoint(new TPoint(147,170, 0));
		g.addPoint(new TPoint(147,158, 0));
		g.addPoint(new TPoint(147,148, 0));
		g.addPoint(new TPoint(147,141, 0));
		g.addPoint(new TPoint(147,136, 0));
		
		g.addPoint(new TPoint(144,135, 0));
		g.addPoint(new TPoint(142,137, 0));
		g.addPoint(new TPoint(140,139, 0));
		g.addPoint(new TPoint(135,145, 0));
		g.addPoint(new TPoint(131,152, 0));
		g.addPoint(new TPoint(124,163, 0));
		g.addPoint(new TPoint(116,177, 0));
		g.addPoint(new TPoint(108,191, 0));
		g.addPoint(new TPoint(100,206, 0));
		g.addPoint(new TPoint(94,217, 0));
		g.addPoint(new TPoint(91,222, 0));
		g.addPoint(new TPoint(89,225, 0));
		g.addPoint(new TPoint(87,226, 0));
		g.addPoint(new TPoint(87,224, 0));

		
			
		g.setInfo(new GestureInfo(0, null, "cut", 0));
	//	addTemplate("cut", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(63,10, 0));
		g.addPoint(new TPoint(44,0, 0));
		g.addPoint(new TPoint(20,0, 0));
		g.addPoint(new TPoint(10,5, 0));
		g.addPoint(new TPoint(1,20, 0));
		g.addPoint(new TPoint(0,32, 0));
		g.addPoint(new TPoint(1,44, 0));
		g.addPoint(new TPoint(10,59, 0));
		g.addPoint(new TPoint(20,63, 0));
		g.addPoint(new TPoint(44,63, 0));
		g.addPoint(new TPoint(63,54, 0));

		
		g.setInfo(new GestureInfo(0, null, "copy", 0));
		//addTemplate("copy", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(63,0, 0));
		g.addPoint(new TPoint(20,32, 0));
		g.addPoint(new TPoint(63,66, 0));
		

		
		g.setInfo(new GestureInfo(0, null, "copy", 0));
		//addTemplate("copy", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(0,0, 0));
		g.addPoint(new TPoint(64,0, 0));
		g.addPoint(new TPoint(64,64, 0));
		g.addPoint(new TPoint(0,64, 0));
		
//da rivedere (dovrebbe essere una z)
		
		g.setInfo(new GestureInfo(0, null, "exit", 0));
		//addTemplate("exit", g);
		

		g = new Gesture();
		g.addPoint(new TPoint(832,134, 0));
		g.addPoint(new TPoint(826,134, 0));
		g.addPoint(new TPoint(826,128, 0));
		g.addPoint(new TPoint(793,107, 0));
		g.addPoint(new TPoint(753,98, 0));
		g.addPoint(new TPoint(706,100, 0));
		g.addPoint(new TPoint(618,129, 0));
		g.addPoint(new TPoint(574,158, 0));
		g.addPoint(new TPoint(529,210, 0));
		g.addPoint(new TPoint(482,275, 0));
		g.addPoint(new TPoint(449,348, 0));
		g.addPoint(new TPoint(435,423, 0));
		g.addPoint(new TPoint(444,489, 0));
		g.addPoint(new TPoint(486,551, 0));
		g.addPoint(new TPoint(568,606, 0));
		g.addPoint(new TPoint(621,628, 0));
		
		g.addPoint(new TPoint(741,636, 0));
		g.addPoint(new TPoint(857,608, 0));
		g.addPoint(new TPoint(937,556, 0));
		g.addPoint(new TPoint(991,496, 0));
		g.addPoint(new TPoint(1035,421, 0));
		g.addPoint(new TPoint(1066,350, 0));
		g.addPoint(new TPoint(1068,290, 0));
		g.addPoint(new TPoint(1033,223, 0));
		g.addPoint(new TPoint(980,160, 0));
		g.addPoint(new TPoint(883,103, 0));
		g.addPoint(new TPoint(743,82, 0));
		g.addPoint(new TPoint(684,92, 0));

		
		g.setInfo(new GestureInfo(0, null, "o", 0));
		addTemplate("o", g);
	
		g = new Gesture();
		g.addPoint(new TPoint(563,243, 0));
		g.addPoint(new TPoint(561,243, 0));
		g.addPoint(new TPoint(555,244, 0));
		g.addPoint(new TPoint(537,249, 0));
		g.addPoint(new TPoint(503,259, 0));
		g.addPoint(new TPoint(472,270, 0));
		g.addPoint(new TPoint(445,284, 0));
		g.addPoint(new TPoint(432,294, 0));
		g.addPoint(new TPoint(411,312, 0));
		g.addPoint(new TPoint(391,336, 0));
		g.addPoint(new TPoint(370,377, 0));
		g.addPoint(new TPoint(357,424, 0));
		g.addPoint(new TPoint(349,472, 0));
		g.addPoint(new TPoint(348,513, 0));
		g.addPoint(new TPoint(352,556, 0));
		g.addPoint(new TPoint(361,599, 0));
		
		g.addPoint(new TPoint(376,642, 0));
		g.addPoint(new TPoint(400,680, 0));
		g.addPoint(new TPoint(429,712, 0));
		g.addPoint(new TPoint(472,744, 0));
		g.addPoint(new TPoint(521,774, 0));
		g.addPoint(new TPoint(573,793, 0));
		g.addPoint(new TPoint(625,804, 0));
		g.addPoint(new TPoint(680,808, 0));
		g.addPoint(new TPoint(727,806, 0));
		g.addPoint(new TPoint(772,799, 0));
		g.addPoint(new TPoint(810,789, 0));
		g.addPoint(new TPoint(843,774, 0));
		
		g.addPoint(new TPoint(873,753, 0));
		g.addPoint(new TPoint(898,729, 0));
		g.addPoint(new TPoint(921,700, 0));
		g.addPoint(new TPoint(948,663, 0));
		g.addPoint(new TPoint(968,627, 0));
		g.addPoint(new TPoint(994,536, 0));
		g.addPoint(new TPoint(998,488, 0));
		g.addPoint(new TPoint(999,448, 0));
		g.addPoint(new TPoint(997,404, 0));
		g.addPoint(new TPoint(993,357, 0));
		g.addPoint(new TPoint(985,304, 0));
		g.addPoint(new TPoint(972,257, 0));

		g.addPoint(new TPoint(953,221, 0));
		g.addPoint(new TPoint(931,195, 0));
		g.addPoint(new TPoint(898,170, 0));
		g.addPoint(new TPoint(857,150, 0));
		g.addPoint(new TPoint(817,137, 0));
		g.addPoint(new TPoint(776,129, 0));
		g.addPoint(new TPoint(740,125, 0));
		g.addPoint(new TPoint(702,126, 0));
		g.addPoint(new TPoint(701,126, 0));
	
		
		g.setInfo(new GestureInfo(0, null, "o", 0));
		addTemplate("o", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(896,375, 0));
		g.addPoint(new TPoint(891,372, 0));
		g.addPoint(new TPoint(879,368, 0));
		g.addPoint(new TPoint(854,360, 0));
		g.addPoint(new TPoint(822,348, 0));
		g.addPoint(new TPoint(780,334, 0));
		g.addPoint(new TPoint(744,326, 0));
		g.addPoint(new TPoint(705,319, 0));
		g.addPoint(new TPoint(663,317, 0));
		g.addPoint(new TPoint(620,318, 0));
		g.addPoint(new TPoint(578,325, 0));
		g.addPoint(new TPoint(541,336, 0));
		g.addPoint(new TPoint(505,352, 0));
		g.addPoint(new TPoint(472,370, 0));
		g.addPoint(new TPoint(448,389, 0));
		g.addPoint(new TPoint(424,413, 0));
		
		g.addPoint(new TPoint(402,441, 0));
		g.addPoint(new TPoint(386,471, 0));
		g.addPoint(new TPoint(375,503, 0));
		g.addPoint(new TPoint(372,536, 0));
		g.addPoint(new TPoint(376,569, 0));
		g.addPoint(new TPoint(384,600, 0));
		g.addPoint(new TPoint(395,628, 0));
		g.addPoint(new TPoint(411,655, 0));
		g.addPoint(new TPoint(430,682, 0));
		g.addPoint(new TPoint(453,709, 0));
		g.addPoint(new TPoint(486,740, 0));
		g.addPoint(new TPoint(526,768, 0));
		
		g.addPoint(new TPoint(571,791, 0));
		g.addPoint(new TPoint(619,802, 0));
		g.addPoint(new TPoint(670,809, 0));
		g.addPoint(new TPoint(721,809, 0));
		g.addPoint(new TPoint(775,803, 0));
		g.addPoint(new TPoint(828,792, 0));
		g.addPoint(new TPoint(879,773, 0));
		g.addPoint(new TPoint(928,751, 0));
		g.addPoint(new TPoint(969,724, 0));
		g.addPoint(new TPoint(1003,691, 0));
		g.addPoint(new TPoint(1035,647, 0));
		g.addPoint(new TPoint(1059,592, 0));
	
		g.addPoint(new TPoint(1070,533, 0));
		g.addPoint(new TPoint(1068,471, 0));
		g.addPoint(new TPoint(1049,412, 0));
		g.addPoint(new TPoint(1014,365, 0));
		g.addPoint(new TPoint(939,322, 0));
		g.addPoint(new TPoint(825,304, 0));
		g.addPoint(new TPoint(688,319, 0));
		g.addPoint(new TPoint(563,352, 0));

		g.setInfo(new GestureInfo(0, null, "o", 0));
		addTemplate("o", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(513,524, 0));
		g.addPoint(new TPoint(515,524, 0));
		g.addPoint(new TPoint(524,524, 0));
		g.addPoint(new TPoint(545,521, 0));
		g.addPoint(new TPoint(579,512, 0));
		g.addPoint(new TPoint(614,494, 0));
		g.addPoint(new TPoint(653,468, 0));
		g.addPoint(new TPoint(691,436, 0));
		g.addPoint(new TPoint(722,395, 0));
		g.addPoint(new TPoint(753,350, 0));
		g.addPoint(new TPoint(776,299, 0));
		g.addPoint(new TPoint(791,258, 0));
		g.addPoint(new TPoint(799,221, 0));
		g.addPoint(new TPoint(804,190, 0));
		g.addPoint(new TPoint(805,169, 0));
		g.addPoint(new TPoint(804,156, 0));
		
		g.addPoint(new TPoint(803,148, 0));
		g.addPoint(new TPoint(801,143, 0));
		g.addPoint(new TPoint(799,139, 0));
		g.addPoint(new TPoint(797,135, 0));
		g.addPoint(new TPoint(794,133, 0));
		g.addPoint(new TPoint(790,131, 0));
		g.addPoint(new TPoint(786,131, 0));
		g.addPoint(new TPoint(781,131, 0));
		g.addPoint(new TPoint(777,134, 0));
		g.addPoint(new TPoint(771,139, 0));
		g.addPoint(new TPoint(762,156, 0));
		g.addPoint(new TPoint(750,196, 0));
		g.addPoint(new TPoint(742,253, 0));
		g.addPoint(new TPoint(739,316, 0));
		g.addPoint(new TPoint(740,382, 0));
		g.addPoint(new TPoint(747,452, 0));
		
		g.addPoint(new TPoint(754,524, 0));
		g.addPoint(new TPoint(762,592, 0));
		g.addPoint(new TPoint(768,648, 0));
		g.addPoint(new TPoint(771,696, 0));
		g.addPoint(new TPoint(773,733, 0));
		g.addPoint(new TPoint(772,762, 0));
		g.addPoint(new TPoint(768,784, 0));
		g.addPoint(new TPoint(762,802, 0));
		g.addPoint(new TPoint(754,814, 0));
		g.addPoint(new TPoint(734,823, 0));
		g.addPoint(new TPoint(688,825, 0));
		g.addPoint(new TPoint(628,812, 0));
		g.addPoint(new TPoint(577,798, 0));
		g.addPoint(new TPoint(545,787, 0));
		g.addPoint(new TPoint(528,777, 0));
		g.addPoint(new TPoint(520,767, 0));
		
		g.addPoint(new TPoint(516,752, 0));
		g.addPoint(new TPoint(521,732, 0));
		g.addPoint(new TPoint(533,709, 0));
		g.addPoint(new TPoint(565,678, 0));
		g.addPoint(new TPoint(612,645, 0));
		g.addPoint(new TPoint(678,617, 0));
		g.addPoint(new TPoint(752,595, 0));
		g.addPoint(new TPoint(827,577, 0));
		g.addPoint(new TPoint(904,558, 0));
		g.addPoint(new TPoint(979,536, 0));
		g.addPoint(new TPoint(1033,517, 0));
		



		g.setInfo(new GestureInfo(0, null, "f", 0));
		addTemplate("f", g);
		
		g = new Gesture();
		
		g.addPoint(new TPoint(433,570, 0));
		g.addPoint(new TPoint(436,569, 0));
		g.addPoint(new TPoint(448,567, 0));
		g.addPoint(new TPoint(475,564, 0));
		g.addPoint(new TPoint(514,556, 0));
		g.addPoint(new TPoint(571,539, 0));
		g.addPoint(new TPoint(634,513, 0));
		g.addPoint(new TPoint(694,480, 0));
		g.addPoint(new TPoint(752,443, 0));
		g.addPoint(new TPoint(800,401, 0));
		g.addPoint(new TPoint(838,360, 0));
		g.addPoint(new TPoint(868,313, 0));
		g.addPoint(new TPoint(886,265, 0));
		g.addPoint(new TPoint(887,218, 0));
		g.addPoint(new TPoint(875,174, 0));
		g.addPoint(new TPoint(851,131, 0));
		g.addPoint(new TPoint(823,98, 0));
		g.addPoint(new TPoint(782,74, 0));
		g.addPoint(new TPoint(744,65, 0));
		g.addPoint(new TPoint(711,73, 0));
		g.addPoint(new TPoint(687,93, 0));
		g.addPoint(new TPoint(665,133, 0));
		g.addPoint(new TPoint(654,188, 0));
		g.addPoint(new TPoint(655,253, 0));
		g.addPoint(new TPoint(663,328, 0));
		g.addPoint(new TPoint(677,400, 0));
		g.addPoint(new TPoint(692,472, 0));
		g.addPoint(new TPoint(705,538, 0));
		g.addPoint(new TPoint(713,598, 0));
		g.addPoint(new TPoint(717,654, 0));
		g.addPoint(new TPoint(714,704, 0));
		g.addPoint(new TPoint(701,752, 0));
		g.addPoint(new TPoint(681,792, 0));
		g.addPoint(new TPoint(657,822, 0));
		g.addPoint(new TPoint(632,842, 0));
		g.addPoint(new TPoint(605,855, 0));
		g.addPoint(new TPoint(575,854, 0));
		g.addPoint(new TPoint(553,839, 0));
		g.addPoint(new TPoint(531,797, 0));
		g.addPoint(new TPoint(518,746, 0));
		g.addPoint(new TPoint(520,694, 0));
		g.addPoint(new TPoint(534,646, 0));
		g.addPoint(new TPoint(559,604, 0));
		g.addPoint(new TPoint(599,567, 0));
		g.addPoint(new TPoint(672,530, 0));
		g.addPoint(new TPoint(754,510, 0));
		g.addPoint(new TPoint(840,493, 0));
		g.addPoint(new TPoint(913,471, 0));
		g.addPoint(new TPoint(965,444, 0));
		g.addPoint(new TPoint(971,439, 0));


		g.setInfo(new GestureInfo(0, null, "f", 0));
		addTemplate("f", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(599,698, 0));
		g.addPoint(new TPoint(599,695, 0));
		g.addPoint(new TPoint(599,694, 0));
		g.addPoint(new TPoint(606,690, 0));
		g.addPoint(new TPoint(633,677, 0));
		g.addPoint(new TPoint(676,651, 0));
		g.addPoint(new TPoint(726,615, 0));
		g.addPoint(new TPoint(774,573, 0));
		g.addPoint(new TPoint(820,525, 0));
		g.addPoint(new TPoint(866,471, 0));
		g.addPoint(new TPoint(901,420, 0));
		g.addPoint(new TPoint(922,375, 0));
		g.addPoint(new TPoint(931,337, 0));
		g.addPoint(new TPoint(927,306, 0));
		g.addPoint(new TPoint(912,280, 0));
		g.addPoint(new TPoint(879,257, 0));
		g.addPoint(new TPoint(832,239, 0));
		g.addPoint(new TPoint(778,236, 0));
		g.addPoint(new TPoint(732,243, 0));
		g.addPoint(new TPoint(678,262, 0));
		g.addPoint(new TPoint(645,283, 0));
		g.addPoint(new TPoint(622,305, 0));
		g.addPoint(new TPoint(608,328, 0));
		g.addPoint(new TPoint(604,352, 0));
		g.addPoint(new TPoint(610,380, 0));
		g.addPoint(new TPoint(629,411, 0));
		g.addPoint(new TPoint(667,445, 0));
		g.addPoint(new TPoint(725,487, 0));
		g.addPoint(new TPoint(780,524, 0));
		g.addPoint(new TPoint(827,560, 0));
		g.addPoint(new TPoint(866,597, 0));
		g.addPoint(new TPoint(897,644, 0));
		g.addPoint(new TPoint(915,698, 0));
		g.addPoint(new TPoint(919,754, 0));
		g.addPoint(new TPoint(911,806, 0));
		g.addPoint(new TPoint(885,851, 0));
		g.addPoint(new TPoint(847,886, 0));
		g.addPoint(new TPoint(781,917, 0));
		g.addPoint(new TPoint(710,925, 0));
		g.addPoint(new TPoint(646,916, 0));
		g.addPoint(new TPoint(593,896, 0));
		g.addPoint(new TPoint(558,874, 0));
		g.addPoint(new TPoint(540,854, 0));
		g.addPoint(new TPoint(529,827, 0));
		g.addPoint(new TPoint(541,781, 0));
		g.addPoint(new TPoint(582,719, 0));
		g.addPoint(new TPoint(667,633, 0));
		g.addPoint(new TPoint(795,525, 0));
		g.addPoint(new TPoint(944,413, 0));
		g.addPoint(new TPoint(1111,297, 0));
		g.addPoint(new TPoint(1145,274, 0));
	

		g.setInfo(new GestureInfo(0, null, "f", 0));
		addTemplate("f", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(568,288, 0));
		g.addPoint(new TPoint(568,288, 0));
		g.addPoint(new TPoint(568,288, 0));
		g.addPoint(new TPoint(569,291, 0));
		g.addPoint(new TPoint(577,313, 0));
		g.addPoint(new TPoint(592,369, 0));
		g.addPoint(new TPoint(610,452, 0));
		g.addPoint(new TPoint(643,634, 0));
		g.addPoint(new TPoint(675,766, 0));
		g.addPoint(new TPoint(686,801, 0));
		g.addPoint(new TPoint(692,816, 0));
		g.addPoint(new TPoint(694,820, 0));
		g.addPoint(new TPoint(695,821, 0));
		g.addPoint(new TPoint(694,819, 0));
		g.addPoint(new TPoint(695,816, 0));
		g.addPoint(new TPoint(699,809, 0));
		g.addPoint(new TPoint(709,787, 0));
		g.addPoint(new TPoint(729,748, 0));
		g.addPoint(new TPoint(764,687, 0));
		g.addPoint(new TPoint(811,607, 0));
		g.addPoint(new TPoint(867,516, 0));
		g.addPoint(new TPoint(931,417, 0));
		g.addPoint(new TPoint(996,320, 0));
		g.addPoint(new TPoint(1059,231, 0));
		g.addPoint(new TPoint(1108,169, 0));
		g.addPoint(new TPoint(1148,127, 0));
		
		g.setInfo(new GestureInfo(0, null, "v", 0));
		addTemplate("v", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(481,243, 0));
		g.addPoint(new TPoint(481,243, 0));
		g.addPoint(new TPoint(482,245, 0));
		g.addPoint(new TPoint(488,255, 0));
		g.addPoint(new TPoint(500,279, 0));
		g.addPoint(new TPoint(517,314, 0));
		g.addPoint(new TPoint(534,354, 0));
		g.addPoint(new TPoint(555,396, 0));
		g.addPoint(new TPoint(581,440, 0));
		g.addPoint(new TPoint(609,493, 0));
		g.addPoint(new TPoint(633,544, 0));
		g.addPoint(new TPoint(653,593, 0));
		g.addPoint(new TPoint(669,636, 0));
		g.addPoint(new TPoint(683,674, 0));
		g.addPoint(new TPoint(695,705, 0));
		g.addPoint(new TPoint(705,730, 0));
		g.addPoint(new TPoint(713,754, 0));
		g.addPoint(new TPoint(720,774, 0));
		g.addPoint(new TPoint(726,792, 0));
		g.addPoint(new TPoint(731,805, 0));
		g.addPoint(new TPoint(737,817, 0));
		g.addPoint(new TPoint(743,826, 0));
		g.addPoint(new TPoint(748,834, 0));
		g.addPoint(new TPoint(752,840, 0));
		g.addPoint(new TPoint(755,843, 0));
		g.addPoint(new TPoint(758,845, 0));
		g.addPoint(new TPoint(760,846, 0));
		g.addPoint(new TPoint(762,846, 0));
		g.addPoint(new TPoint(764,844, 0));
		g.addPoint(new TPoint(766,840, 0));
		g.addPoint(new TPoint(769,830, 0));
		g.addPoint(new TPoint(774,812, 0));
		g.addPoint(new TPoint(781,781, 0));
		g.addPoint(new TPoint(788,741, 0));
		g.addPoint(new TPoint(795,699, 0));
		g.addPoint(new TPoint(801,657, 0));
		g.addPoint(new TPoint(805,615, 0));
		g.addPoint(new TPoint(809,571, 0));
		g.addPoint(new TPoint(814,531, 0));
		g.addPoint(new TPoint(819,491, 0));
		g.addPoint(new TPoint(828,452, 0));
		g.addPoint(new TPoint(842,412, 0));
		g.addPoint(new TPoint(859,376, 0));
		g.addPoint(new TPoint(878,339, 0));
		g.addPoint(new TPoint(896,301, 0));
		g.addPoint(new TPoint(911,270, 0));
		g.addPoint(new TPoint(927,243, 0));
		g.addPoint(new TPoint(938,222, 0));
		g.addPoint(new TPoint(946,208, 0));
		g.addPoint(new TPoint(951,199, 0));
		g.addPoint(new TPoint(953,193, 0));
		g.addPoint(new TPoint(953,189, 0));
		g.addPoint(new TPoint(953,187, 0));
		g.addPoint(new TPoint(951,185, 0));
		g.addPoint(new TPoint(950,184, 0));
		g.setInfo(new GestureInfo(0, null, "v", 0));
		addTemplate("v", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(500,321, 0));
		g.addPoint(new TPoint(500,321, 0));
		g.addPoint(new TPoint(500,321, 0));
		g.addPoint(new TPoint(502,325, 0));
		g.addPoint(new TPoint(519,361, 0));
		g.addPoint(new TPoint(548,441, 0));
		g.addPoint(new TPoint(583,539, 0));
		g.addPoint(new TPoint(615,635, 0));
		g.addPoint(new TPoint(630,685, 0));
		g.addPoint(new TPoint(651,751, 0));
		g.addPoint(new TPoint(664,794, 0));
		g.addPoint(new TPoint(672,816, 0));
		g.addPoint(new TPoint(675,823, 0));
		g.addPoint(new TPoint(676,826, 0));
		g.addPoint(new TPoint(676,826, 0));
		g.addPoint(new TPoint(676,824, 0));
		g.addPoint(new TPoint(678,819, 0));
		g.addPoint(new TPoint(686,802, 0));
		g.addPoint(new TPoint(705,763, 0));
		g.addPoint(new TPoint(734,701, 0));
		g.addPoint(new TPoint(772,626, 0));
		g.addPoint(new TPoint(813,549, 0));
		g.addPoint(new TPoint(863,467, 0));
		g.addPoint(new TPoint(912,394, 0));
		g.addPoint(new TPoint(956,342, 0));
		g.addPoint(new TPoint(998,294, 0));
		g.addPoint(new TPoint(1030,265, 0));
		g.addPoint(new TPoint(1053,245, 0));
		g.addPoint(new TPoint(1063,239, 0));
		g.addPoint(new TPoint(1067,237, 0));
		g.addPoint(new TPoint(1068,237, 0));
		g.addPoint(new TPoint(1068,239, 0));
		g.addPoint(new TPoint(1068,241, 0));
		g.setInfo(new GestureInfo(0, null, "v", 0));
		addTemplate("v", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(954,98, 0));
		g.addPoint(new TPoint(949,95, 0));
		g.addPoint(new TPoint(922,84, 0));
		g.addPoint(new TPoint(864,73, 0));
		g.addPoint(new TPoint(785,76, 0));
		g.addPoint(new TPoint(694,101, 0));
		g.addPoint(new TPoint(624,139, 0));
		g.addPoint(new TPoint(580,168, 0));
		g.addPoint(new TPoint(552,199, 0));
		g.addPoint(new TPoint(538,235, 0));
		g.addPoint(new TPoint(550,269, 0));
		g.addPoint(new TPoint(591,305, 0));
		g.addPoint(new TPoint(679,338, 0));
		g.addPoint(new TPoint(780,356, 0));
		g.addPoint(new TPoint(883,371, 0));
		g.addPoint(new TPoint(969,386, 0));
		g.addPoint(new TPoint(1042,411, 0));
		g.addPoint(new TPoint(1078,437, 0));
		g.addPoint(new TPoint(1095,476, 0));
		g.addPoint(new TPoint(1080,529, 0));
		g.addPoint(new TPoint(1035,582, 0));
		g.addPoint(new TPoint(957,639, 0));
		g.addPoint(new TPoint(862,683, 0));
		g.addPoint(new TPoint(755,711, 0));
		g.addPoint(new TPoint(646,724, 0));
		g.addPoint(new TPoint(527,732, 0));
		g.addPoint(new TPoint(411,740, 0));
		g.addPoint(new TPoint(315,747, 0));
		
		g.setInfo(new GestureInfo(0, null, "s", 0));
		addTemplate("s", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(937,129, 0));
		g.addPoint(new TPoint(935,128, 0));
		g.addPoint(new TPoint(927,125, 0));
		g.addPoint(new TPoint(904,121, 0));
		g.addPoint(new TPoint(855,112, 0));
		g.addPoint(new TPoint(788,104, 0));
		g.addPoint(new TPoint(714,98, 0));
		g.addPoint(new TPoint(668,94, 0));
		g.addPoint(new TPoint(616,91, 0));
		g.addPoint(new TPoint(570,90, 0));
		g.addPoint(new TPoint(533,96, 0));
		g.addPoint(new TPoint(500,110, 0));
		g.addPoint(new TPoint(473,132, 0));
		g.addPoint(new TPoint(447,162, 0));
		g.addPoint(new TPoint(427,197, 0));
		g.addPoint(new TPoint(414,230, 0));
		g.addPoint(new TPoint(408,259, 0));
		g.addPoint(new TPoint(407,285, 0));
		g.addPoint(new TPoint(409,311, 0));
		g.addPoint(new TPoint(414,332, 0));
		g.addPoint(new TPoint(421,347, 0));
		g.addPoint(new TPoint(429,358, 0));
		g.addPoint(new TPoint(439,366, 0));
		g.addPoint(new TPoint(454,375, 0));
		g.addPoint(new TPoint(478,385, 0));
		g.addPoint(new TPoint(512,394, 0));
		g.addPoint(new TPoint(552,404, 0));
		g.addPoint(new TPoint(596,416, 0));
		g.addPoint(new TPoint(641,428, 0));
		g.addPoint(new TPoint(688,439, 0));
		g.addPoint(new TPoint(736,451, 0));
		g.addPoint(new TPoint(785,461, 0));
		g.addPoint(new TPoint(834,468, 0));
		g.addPoint(new TPoint(880,474, 0));
		g.addPoint(new TPoint(930,482, 0));
		g.addPoint(new TPoint(977,494, 0));
		g.addPoint(new TPoint(1023,514, 0));
		g.addPoint(new TPoint(1059,536, 0));
		g.addPoint(new TPoint(1086,563, 0));
		g.addPoint(new TPoint(1109,602, 0));
		g.addPoint(new TPoint(1118,640, 0));
		g.addPoint(new TPoint(1118,674, 0));
		g.addPoint(new TPoint(1106,707, 0));
		g.addPoint(new TPoint(1081,741, 0));
		g.addPoint(new TPoint(1037,778, 0));
		g.addPoint(new TPoint(972,814, 0));
		g.addPoint(new TPoint(904,840, 0));
		g.addPoint(new TPoint(838,857, 0));
		g.addPoint(new TPoint(777,866, 0));
		g.addPoint(new TPoint(721,868, 0));
		g.addPoint(new TPoint(664,863, 0));
		g.addPoint(new TPoint(610,853, 0));
		g.addPoint(new TPoint(556,841, 0));
		g.addPoint(new TPoint(509,829, 0));
		g.addPoint(new TPoint(470,818, 0));
		g.addPoint(new TPoint(440,808, 0));
		g.addPoint(new TPoint(415,799, 0));
		g.addPoint(new TPoint(397,791, 0));
		g.addPoint(new TPoint(386,783, 0));
		g.addPoint(new TPoint(379,779, 0));
		g.addPoint(new TPoint(375,775, 0));
		g.addPoint(new TPoint(371,773, 0));
		g.setInfo(new GestureInfo(0, null, "s", 0));
		addTemplate("s", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(943,300, 0));
		g.addPoint(new TPoint(940,297, 0));
		g.addPoint(new TPoint(931,290, 0));
		g.addPoint(new TPoint(902,275, 0));
		g.addPoint(new TPoint(850,259, 0));
		g.addPoint(new TPoint(783,250, 0));
		g.addPoint(new TPoint(726,250, 0));
		g.addPoint(new TPoint(666,255, 0));
		g.addPoint(new TPoint(611,268, 0));
		g.addPoint(new TPoint(564,287, 0));
		g.addPoint(new TPoint(530,307, 0));
		g.addPoint(new TPoint(507,327, 0));
		g.addPoint(new TPoint(492,349, 0));
		g.addPoint(new TPoint(489,372, 0));
		g.addPoint(new TPoint(498,396, 0));
		g.addPoint(new TPoint(528,419, 0));
		g.addPoint(new TPoint(582,445, 0));
		g.addPoint(new TPoint(658,464, 0));
		g.addPoint(new TPoint(735,469, 0));
		g.addPoint(new TPoint(813,469, 0));
		g.addPoint(new TPoint(886,471, 0));
		g.addPoint(new TPoint(952,480, 0));
		g.addPoint(new TPoint(1006,495, 0));
		g.addPoint(new TPoint(1050,514, 0));
		g.addPoint(new TPoint(1078,534, 0));
		g.addPoint(new TPoint(1099,564, 0));
		g.addPoint(new TPoint(1112,604, 0));
		g.addPoint(new TPoint(1110,646, 0));
		g.addPoint(new TPoint(1096,684, 0));
		g.addPoint(new TPoint(1070,716, 0));
		g.addPoint(new TPoint(1019,754, 0));
		g.addPoint(new TPoint(943,787, 0));
		g.addPoint(new TPoint(854,808, 0));
		g.addPoint(new TPoint(760,810, 0));
		g.addPoint(new TPoint(654,802, 0));
		g.addPoint(new TPoint(552,786, 0));
		g.addPoint(new TPoint(464,767, 0));
		g.addPoint(new TPoint(394,753, 0));
		g.addPoint(new TPoint(345,743, 0));
		g.addPoint(new TPoint(309,737, 0));
		g.addPoint(new TPoint(302,736, 0));
		g.setInfo(new GestureInfo(0, null, "s", 0));
		addTemplate("s", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(528,394, 0));
		g.addPoint(new TPoint(529,394, 0));
		g.addPoint(new TPoint(549,396, 0));
		g.addPoint(new TPoint(595,391, 0));
		g.addPoint(new TPoint(639,375, 0));
		g.addPoint(new TPoint(672,348, 0));
		g.addPoint(new TPoint(692,313, 0));
		g.addPoint(new TPoint(702,280, 0));
		g.addPoint(new TPoint(701,251, 0));
		g.addPoint(new TPoint(693,225, 0));
		g.addPoint(new TPoint(679,204, 0));
		g.addPoint(new TPoint(660,188, 0));
		g.addPoint(new TPoint(636,177, 0));
		g.addPoint(new TPoint(604,174, 0));
		g.addPoint(new TPoint(571,180, 0));
		g.addPoint(new TPoint(543,195, 0));
		g.addPoint(new TPoint(512,218, 0));
		g.addPoint(new TPoint(483,247, 0));
		g.addPoint(new TPoint(456,288, 0));
		g.addPoint(new TPoint(438,329, 0));
		g.addPoint(new TPoint(424,369, 0));
		g.addPoint(new TPoint(417,409, 0));
		g.addPoint(new TPoint(416,450, 0));
		g.addPoint(new TPoint(422,496, 0));
		g.addPoint(new TPoint(435,546, 0));
		g.addPoint(new TPoint(450,593, 0));
		g.addPoint(new TPoint(472,632, 0));
		g.addPoint(new TPoint(496,664, 0));
		g.addPoint(new TPoint(518,686, 0));
		g.addPoint(new TPoint(543,706, 0));
		g.addPoint(new TPoint(577,719, 0));
		g.addPoint(new TPoint(608,726, 0));
		g.addPoint(new TPoint(644,728, 0));
		g.addPoint(new TPoint(680,724, 0));
		g.addPoint(new TPoint(713,714, 0));
		g.addPoint(new TPoint(751,695, 0));
		g.addPoint(new TPoint(786,667, 0));
		g.addPoint(new TPoint(827,629, 0));
		g.addPoint(new TPoint(870,581, 0));
		g.addPoint(new TPoint(906,533, 0));
		g.addPoint(new TPoint(943,485, 0));
		g.addPoint(new TPoint(974,438, 0));
		g.addPoint(new TPoint(1000,392, 0));
		g.addPoint(new TPoint(1020,349, 0));
		g.addPoint(new TPoint(1034,309, 0));
		g.addPoint(new TPoint(1044,267, 0));
		g.addPoint(new TPoint(1043,236, 0));
		g.addPoint(new TPoint(1037,204, 0));
		g.addPoint(new TPoint(1026,179, 0));
		g.addPoint(new TPoint(1013,158, 0));
		g.addPoint(new TPoint(999,140, 0));
		g.addPoint(new TPoint(984,124, 0));
		g.addPoint(new TPoint(960,112, 0));
		g.addPoint(new TPoint(934,106, 0));
		g.addPoint(new TPoint(913,108, 0));
		g.addPoint(new TPoint(891,118, 0));
		g.addPoint(new TPoint(866,135, 0));
		g.addPoint(new TPoint(843,156, 0));
		g.addPoint(new TPoint(822,189, 0));
		g.addPoint(new TPoint(808,226, 0));
		g.addPoint(new TPoint(802,269, 0));
		g.addPoint(new TPoint(802,315, 0));
		g.addPoint(new TPoint(807,363, 0));
		g.addPoint(new TPoint(817,415, 0));
		g.addPoint(new TPoint(833,468, 0));
		g.addPoint(new TPoint(855,517, 0));
		g.addPoint(new TPoint(880,568, 0));
		g.addPoint(new TPoint(903,614, 0));
		g.addPoint(new TPoint(931,658, 0));
		g.addPoint(new TPoint(960,695, 0));
		g.addPoint(new TPoint(992,732, 0));
		g.addPoint(new TPoint(1029,768, 0));
		g.addPoint(new TPoint(1066,800, 0));
		g.addPoint(new TPoint(1102,828, 0));
		g.addPoint(new TPoint(1138,851, 0));
		g.addPoint(new TPoint(1154,857, 0));
		g.setInfo(new GestureInfo(0, null, "cl", 0));
		addTemplate("cl", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(579,134, 0));
		g.addPoint(new TPoint(579,133, 0));
		g.addPoint(new TPoint(579,133, 0));
		g.addPoint(new TPoint(578,133, 0));
		g.addPoint(new TPoint(571,134, 0));
		g.addPoint(new TPoint(550,139, 0));
		g.addPoint(new TPoint(511,149, 0));
		g.addPoint(new TPoint(463,159, 0));
		g.addPoint(new TPoint(408,171, 0));
		g.addPoint(new TPoint(360,185, 0));
		g.addPoint(new TPoint(311,209, 0));
		g.addPoint(new TPoint(282,231, 0));
		g.addPoint(new TPoint(253,263, 0));
		g.addPoint(new TPoint(229,304, 0));
		g.addPoint(new TPoint(211,352, 0));
		g.addPoint(new TPoint(202,402, 0));
		g.addPoint(new TPoint(200,448, 0));
		g.addPoint(new TPoint(203,495, 0));
		g.addPoint(new TPoint(212,541, 0));
		g.addPoint(new TPoint(227,586, 0));
		g.addPoint(new TPoint(245,627, 0));
		g.addPoint(new TPoint(266,665, 0));
		g.addPoint(new TPoint(292,697, 0));
		g.addPoint(new TPoint(326,727, 0));
		g.addPoint(new TPoint(373,754, 0));
		g.addPoint(new TPoint(421,773, 0));
		g.addPoint(new TPoint(481,779, 0));
		g.addPoint(new TPoint(538,777, 0));
		g.addPoint(new TPoint(597,762, 0));
		g.addPoint(new TPoint(653,742, 0));
		g.addPoint(new TPoint(706,720, 0));
		g.addPoint(new TPoint(760,692, 0));
		g.addPoint(new TPoint(807,659, 0));
		g.addPoint(new TPoint(854,622, 0));
		g.addPoint(new TPoint(894,582, 0));
		g.addPoint(new TPoint(930,533, 0));
		g.addPoint(new TPoint(958,480, 0));
		g.addPoint(new TPoint(975,434, 0));
		g.addPoint(new TPoint(986,388, 0));
		g.addPoint(new TPoint(990,352, 0));
		g.addPoint(new TPoint(988,317, 0));
		g.addPoint(new TPoint(982,289, 0));
		g.addPoint(new TPoint(973,266, 0));
		g.addPoint(new TPoint(963,253, 0));
		g.addPoint(new TPoint(952,243, 0));
		g.addPoint(new TPoint(941,237, 0));
		g.addPoint(new TPoint(929,236, 0));
		g.addPoint(new TPoint(918,237, 0));
		g.addPoint(new TPoint(906,242, 0));
		g.addPoint(new TPoint(891,248, 0));
		g.addPoint(new TPoint(874,258, 0));
		g.addPoint(new TPoint(859,269, 0));
		g.addPoint(new TPoint(845,282, 0));
		g.addPoint(new TPoint(831,301, 0));
		g.addPoint(new TPoint(821,323, 0));
		g.addPoint(new TPoint(815,351, 0));
		g.addPoint(new TPoint(815,387, 0));
		g.addPoint(new TPoint(821,425, 0));
		g.addPoint(new TPoint(834,465, 0));
		g.addPoint(new TPoint(873,537, 0));
		g.addPoint(new TPoint(898,571, 0));
		g.addPoint(new TPoint(925,599, 0));
		g.addPoint(new TPoint(961,622, 0));
		g.addPoint(new TPoint(995,637, 0));
		g.addPoint(new TPoint(1033,648, 0));
		g.addPoint(new TPoint(1070,659, 0));
		g.addPoint(new TPoint(1111,672, 0));
		g.addPoint(new TPoint(1150,684, 0));
		g.addPoint(new TPoint(1190,696, 0));
		g.addPoint(new TPoint(1230,707, 0));
		g.addPoint(new TPoint(1270,719, 0));
		g.addPoint(new TPoint(1284,723, 0));
		g.setInfo(new GestureInfo(0, null, "cl", 0));
		addTemplate("cl", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(732,335, 0));
		g.addPoint(new TPoint(730,334, 0));
		g.addPoint(new TPoint(727,331, 0));
		g.addPoint(new TPoint(717,329, 0));
		g.addPoint(new TPoint(686,328, 0));
		g.addPoint(new TPoint(636,332, 0));
		g.addPoint(new TPoint(585,339, 0));
		g.addPoint(new TPoint(533,349, 0));
		g.addPoint(new TPoint(483,361, 0));
		g.addPoint(new TPoint(439,375, 0));
		g.addPoint(new TPoint(397,395, 0));
		g.addPoint(new TPoint(362,416, 0));
		g.addPoint(new TPoint(334,436, 0));
		g.addPoint(new TPoint(302,465, 0));
		g.addPoint(new TPoint(278,496, 0));
		g.addPoint(new TPoint(263,522, 0));
		g.addPoint(new TPoint(254,548, 0));
		g.addPoint(new TPoint(251,575, 0));
		g.addPoint(new TPoint(254,601, 0));
		g.addPoint(new TPoint(263,627, 0));
		g.addPoint(new TPoint(281,653, 0));
		g.addPoint(new TPoint(306,680, 0));
		g.addPoint(new TPoint(342,707, 0));
		g.addPoint(new TPoint(387,729, 0));
		g.addPoint(new TPoint(483,753, 0));
		g.addPoint(new TPoint(534,757, 0));
		g.addPoint(new TPoint(589,755, 0));
		g.addPoint(new TPoint(645,744, 0));
		g.addPoint(new TPoint(702,727, 0));
		g.addPoint(new TPoint(763,705, 0));
		g.addPoint(new TPoint(822,677, 0));
		g.addPoint(new TPoint(877,648, 0));
		g.addPoint(new TPoint(927,617, 0));
		g.addPoint(new TPoint(969,585, 0));
		g.addPoint(new TPoint(1005,550, 0));
		g.addPoint(new TPoint(1036,511, 0));
		g.addPoint(new TPoint(1060,472, 0));
		g.addPoint(new TPoint(1074,438, 0));
		g.addPoint(new TPoint(1079,402, 0));
		g.addPoint(new TPoint(1076,370, 0));
		g.addPoint(new TPoint(1067,344, 0));
		g.addPoint(new TPoint(1051,321, 0));
		g.addPoint(new TPoint(1030,302, 0));
		g.addPoint(new TPoint(1005,286, 0));
		g.addPoint(new TPoint(974,277, 0));
		g.addPoint(new TPoint(940,277, 0));
		g.addPoint(new TPoint(911,283, 0));
		g.addPoint(new TPoint(878,301, 0));
		g.addPoint(new TPoint(846,325, 0));
		g.addPoint(new TPoint(821,349, 0));
		g.addPoint(new TPoint(793,385, 0));
		g.addPoint(new TPoint(771,421, 0));
		g.addPoint(new TPoint(753,459, 0));
		g.addPoint(new TPoint(742,499, 0));
		g.addPoint(new TPoint(739,540, 0));
		g.addPoint(new TPoint(746,583, 0));
		g.addPoint(new TPoint(766,622, 0));
		g.addPoint(new TPoint(794,657, 0));
		g.addPoint(new TPoint(838,690, 0));
		g.addPoint(new TPoint(905,721, 0));
		g.addPoint(new TPoint(980,739, 0));
		g.addPoint(new TPoint(1055,745, 0));
		g.addPoint(new TPoint(1127,742, 0));
		g.addPoint(new TPoint(1184,731, 0));
		g.addPoint(new TPoint(1232,718, 0));
		g.addPoint(new TPoint(1246,712, 0));
		g.setInfo(new GestureInfo(0, null, "cl", 0));
		addTemplate("cl", g);
		
		g = new Gesture();
		
		g.addPoint(new TPoint(499,767, 0));
		g.addPoint(new TPoint(497,767, 0));
		g.addPoint(new TPoint(494,767, 0));
		g.addPoint(new TPoint(492,767, 0));
		g.addPoint(new TPoint(491,767, 0));
		g.addPoint(new TPoint(492,765, 0));
		g.addPoint(new TPoint(499,753, 0));
		g.addPoint(new TPoint(518,724, 0));
		g.addPoint(new TPoint(552,673, 0));
		g.addPoint(new TPoint(600,602, 0));
		g.addPoint(new TPoint(652,525, 0));
		g.addPoint(new TPoint(695,461, 0));
		g.addPoint(new TPoint(733,401, 0));
		g.addPoint(new TPoint(766,347, 0));
		g.addPoint(new TPoint(793,298, 0));
		g.addPoint(new TPoint(813,255, 0));
		g.addPoint(new TPoint(831,216, 0));
		g.addPoint(new TPoint(845,185, 0));
		g.addPoint(new TPoint(857,164, 0));
		g.addPoint(new TPoint(866,151, 0));
		g.addPoint(new TPoint(871,143, 0));
		g.addPoint(new TPoint(874,138, 0));
		g.addPoint(new TPoint(876,134, 0));
		g.addPoint(new TPoint(878,133, 0));
		g.addPoint(new TPoint(880,131, 0));
		g.addPoint(new TPoint(880,129, 0));
		g.setInfo(new GestureInfo(0, null, "slash", 0));
		addTemplate("slash", g);//comment
		
		g = new Gesture();
		g.addPoint(new TPoint(612,804, 0));
		g.addPoint(new TPoint(612,801, 0));
		g.addPoint(new TPoint(612,797, 0));
		g.addPoint(new TPoint(615,786, 0));
		g.addPoint(new TPoint(626,761, 0));
		g.addPoint(new TPoint(645,723, 0));
		g.addPoint(new TPoint(671,683, 0));
		g.addPoint(new TPoint(701,638, 0));
		g.addPoint(new TPoint(732,588, 0));
		g.addPoint(new TPoint(764,535, 0));
		g.addPoint(new TPoint(795,483, 0));
		g.addPoint(new TPoint(821,434, 0));
		g.addPoint(new TPoint(847,390, 0));
		g.addPoint(new TPoint(869,350, 0));
		g.addPoint(new TPoint(888,315, 0));
		g.addPoint(new TPoint(902,288, 0));
		g.addPoint(new TPoint(910,268, 0));
		g.addPoint(new TPoint(913,255, 0));
		g.addPoint(new TPoint(915,248, 0));
		g.addPoint(new TPoint(915,243, 0));
		g.addPoint(new TPoint(915,241, 0));
		g.addPoint(new TPoint(915,239, 0));
		g.addPoint(new TPoint(915,237, 0));
		g.addPoint(new TPoint(915,235, 0));
		g.setInfo(new GestureInfo(0, null, "slash", 0));
		addTemplate("slash", g);//comment
		
		g = new Gesture();
		g.addPoint(new TPoint(716,461, 0));
		g.addPoint(new TPoint(712,458, 0));
		g.addPoint(new TPoint(700,449, 0));
		g.addPoint(new TPoint(679,435, 0));
		g.addPoint(new TPoint(658,415, 0));
		g.addPoint(new TPoint(635,385, 0));
		g.addPoint(new TPoint(613,350, 0));
		g.addPoint(new TPoint(596,319, 0));
		g.addPoint(new TPoint(585,293, 0));
		g.addPoint(new TPoint(584,273, 0));
		g.addPoint(new TPoint(591,253, 0));
		g.addPoint(new TPoint(605,234, 0));
		g.addPoint(new TPoint(640,212, 0));
		g.addPoint(new TPoint(683,196, 0));
		g.addPoint(new TPoint(726,190, 0));
		g.addPoint(new TPoint(767,194, 0));
		g.addPoint(new TPoint(801,205, 0));
		g.addPoint(new TPoint(828,220, 0));
		g.addPoint(new TPoint(850,239, 0));
		g.addPoint(new TPoint(867,262, 0));
		g.addPoint(new TPoint(875,295, 0));
		g.addPoint(new TPoint(870,331, 0));
		g.addPoint(new TPoint(849,376, 0));
		g.addPoint(new TPoint(815,422, 0));
		g.addPoint(new TPoint(773,466, 0));
		g.addPoint(new TPoint(726,513, 0));
		g.addPoint(new TPoint(681,561, 0));
		g.addPoint(new TPoint(648,605, 0));
		g.addPoint(new TPoint(626,649, 0));
		g.addPoint(new TPoint(618,695, 0));
		g.addPoint(new TPoint(618,756, 0));
		g.addPoint(new TPoint(626,825, 0));
		g.addPoint(new TPoint(634,875, 0));
		g.setInfo(new GestureInfo(0, null, "qm", 0));
		addTemplate("qm", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(612,380, 0));
		g.addPoint(new TPoint(612,380, 0));
		g.addPoint(new TPoint(612,381, 0));
		g.addPoint(new TPoint(609,385, 0));
		g.addPoint(new TPoint(598,394, 0));
		g.addPoint(new TPoint(571,404, 0));
		g.addPoint(new TPoint(543,400, 0));
		g.addPoint(new TPoint(519,386, 0));
		g.addPoint(new TPoint(496,363, 0));
		g.addPoint(new TPoint(470,325, 0));
		g.addPoint(new TPoint(453,288, 0));
		g.addPoint(new TPoint(444,255, 0));
		g.addPoint(new TPoint(440,221, 0));
		g.addPoint(new TPoint(441,190, 0));
		g.addPoint(new TPoint(447,159, 0));
		g.addPoint(new TPoint(459,129, 0));
		g.addPoint(new TPoint(477,101, 0));
		g.addPoint(new TPoint(501,76, 0));
		g.addPoint(new TPoint(531,49, 0));
		g.addPoint(new TPoint(564,28, 0));
		g.addPoint(new TPoint(600,14, 0));
		g.addPoint(new TPoint(637,9, 0));
		g.addPoint(new TPoint(675,13, 0));
		g.addPoint(new TPoint(714,25, 0));
		g.addPoint(new TPoint(757,45, 0));
		g.addPoint(new TPoint(800,71, 0));
		g.addPoint(new TPoint(843,104, 0));
		g.addPoint(new TPoint(882,142, 0));
		g.addPoint(new TPoint(914,184, 0));
		g.addPoint(new TPoint(936,225, 0));
		g.addPoint(new TPoint(946,259, 0));
		g.addPoint(new TPoint(945,288, 0));
		g.addPoint(new TPoint(936,312, 0));
		g.addPoint(new TPoint(923,330, 0));
		g.addPoint(new TPoint(905,345, 0));
		g.addPoint(new TPoint(874,360, 0));
		g.addPoint(new TPoint(842,374, 0));
		g.addPoint(new TPoint(813,388, 0));
		g.addPoint(new TPoint(787,405, 0));
		g.addPoint(new TPoint(764,424, 0));
		g.addPoint(new TPoint(744,449, 0));
		g.addPoint(new TPoint(729,483, 0));
		g.addPoint(new TPoint(721,517, 0));
		g.addPoint(new TPoint(716,554, 0));
		g.addPoint(new TPoint(717,586, 0));
		g.addPoint(new TPoint(720,614, 0));
		g.addPoint(new TPoint(725,644, 0));
		g.addPoint(new TPoint(731,671, 0));
		g.addPoint(new TPoint(736,693, 0));
		g.addPoint(new TPoint(740,711, 0));
		g.addPoint(new TPoint(744,723, 0));
		g.addPoint(new TPoint(745,734, 0));
		g.addPoint(new TPoint(746,750, 0));
		g.addPoint(new TPoint(746,756, 0));
		g.setInfo(new GestureInfo(0, null, "qm", 0));
		addTemplate("qm", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(618,611, 0));
		g.addPoint(new TPoint(616,609, 0));
		g.addPoint(new TPoint(607,601, 0));
		g.addPoint(new TPoint(583,580, 0));
		g.addPoint(new TPoint(556,547, 0));
		g.addPoint(new TPoint(529,498, 0));
		g.addPoint(new TPoint(512,441, 0));
		g.addPoint(new TPoint(510,398, 0));
		g.addPoint(new TPoint(518,361, 0));
		g.addPoint(new TPoint(535,329, 0));
		g.addPoint(new TPoint(570,299, 0));
		g.addPoint(new TPoint(626,273, 0));
		g.addPoint(new TPoint(688,259, 0));
		g.addPoint(new TPoint(753,257, 0));
		g.addPoint(new TPoint(805,266, 0));
		g.addPoint(new TPoint(849,284, 0));
		g.addPoint(new TPoint(874,301, 0));
		g.addPoint(new TPoint(888,323, 0));
		g.addPoint(new TPoint(892,346, 0));
		g.addPoint(new TPoint(869,388, 0));
		g.addPoint(new TPoint(836,426, 0));
		g.addPoint(new TPoint(793,468, 0));
		g.addPoint(new TPoint(746,516, 0));
		g.addPoint(new TPoint(700,574, 0));
		g.addPoint(new TPoint(665,639, 0));
		g.addPoint(new TPoint(646,714, 0));
		g.addPoint(new TPoint(645,789, 0));
		g.addPoint(new TPoint(656,843, 0));
		g.addPoint(new TPoint(668,872, 0));
		g.addPoint(new TPoint(675,883, 0));
		g.addPoint(new TPoint(681,885, 0));
		g.addPoint(new TPoint(684,881, 0));
		g.setInfo(new GestureInfo(0, null, "qm", 0));
		addTemplate("qm", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(485,637, 0));
		g.addPoint(new TPoint(483,637, 0));
		g.addPoint(new TPoint(481,637, 0));
		g.addPoint(new TPoint(479,637, 0));
		g.addPoint(new TPoint(480,638, 0));
		g.addPoint(new TPoint(486,650, 0));
		g.addPoint(new TPoint(502,672, 0));
		g.addPoint(new TPoint(518,686, 0));
		g.addPoint(new TPoint(549,706, 0));
		g.addPoint(new TPoint(591,722, 0));
		g.addPoint(new TPoint(636,731, 0));
		g.addPoint(new TPoint(684,732, 0));
		g.addPoint(new TPoint(726,727, 0));
		g.addPoint(new TPoint(772,714, 0));
		g.addPoint(new TPoint(818,696, 0));
		g.addPoint(new TPoint(865,671, 0));
		g.addPoint(new TPoint(908,641, 0));
		g.addPoint(new TPoint(948,609, 0));
		g.addPoint(new TPoint(977,579, 0));
		g.addPoint(new TPoint(1000,549, 0));
		g.addPoint(new TPoint(1016,523, 0));
		g.addPoint(new TPoint(1026,499, 0));
		g.addPoint(new TPoint(1030,479, 0));
		g.addPoint(new TPoint(1028,458, 0));
		g.addPoint(new TPoint(1022,433, 0));
		g.addPoint(new TPoint(1011,406, 0));
		g.addPoint(new TPoint(995,377, 0));
		g.addPoint(new TPoint(975,352, 0));
		g.addPoint(new TPoint(948,328, 0));
		g.addPoint(new TPoint(920,309, 0));
		g.addPoint(new TPoint(890,292, 0));
		g.addPoint(new TPoint(860,279, 0));
		g.addPoint(new TPoint(832,270, 0));
		g.addPoint(new TPoint(803,265, 0));
		g.addPoint(new TPoint(775,265, 0));
		g.addPoint(new TPoint(747,268, 0));
		g.addPoint(new TPoint(728,276, 0));
		g.addPoint(new TPoint(711,287, 0));
		g.addPoint(new TPoint(693,306, 0));
		g.addPoint(new TPoint(673,332, 0));
		g.addPoint(new TPoint(654,365, 0));
		g.addPoint(new TPoint(640,402, 0));
		g.addPoint(new TPoint(632,443, 0));
		g.addPoint(new TPoint(630,486, 0));
		g.addPoint(new TPoint(632,519, 0));
		g.addPoint(new TPoint(639,553, 0));
		g.addPoint(new TPoint(652,580, 0));
		g.addPoint(new TPoint(673,606, 0));
		g.addPoint(new TPoint(696,629, 0));
		g.addPoint(new TPoint(729,657, 0));
		g.addPoint(new TPoint(766,684, 0));
		g.addPoint(new TPoint(804,705, 0));
		g.addPoint(new TPoint(849,723, 0));
		g.addPoint(new TPoint(892,735, 0));
		g.addPoint(new TPoint(940,743, 0));
		g.addPoint(new TPoint(983,749, 0));
		g.addPoint(new TPoint(1029,753, 0));
		g.addPoint(new TPoint(1070,756, 0));
		g.addPoint(new TPoint(1110,758, 0));
		g.addPoint(new TPoint(1145,760, 0));
		g.addPoint(new TPoint(1172,760, 0));
		g.addPoint(new TPoint(1197,760, 0));
		g.addPoint(new TPoint(1216,759, 0));
		g.addPoint(new TPoint(1230,757, 0));
		g.addPoint(new TPoint(1238,757, 0));
		g.addPoint(new TPoint(1242,757, 0));
		g.addPoint(new TPoint(1244,757, 0));
		g.addPoint(new TPoint(1246,757, 0));
		g.setInfo(new GestureInfo(0, null, "e", 0));
		addTemplate("e", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(376,667, 0));
		g.addPoint(new TPoint(376,665, 0));
		g.addPoint(new TPoint(380,666, 0));
		g.addPoint(new TPoint(401,664, 0));
		g.addPoint(new TPoint(443,658, 0));
		g.addPoint(new TPoint(500,644, 0));
		g.addPoint(new TPoint(557,626, 0));
		g.addPoint(new TPoint(670,565, 0));
		g.addPoint(new TPoint(720,530, 0));
		g.addPoint(new TPoint(768,491, 0));
		g.addPoint(new TPoint(808,456, 0));
		g.addPoint(new TPoint(845,423, 0));
		g.addPoint(new TPoint(876,387, 0));
		g.addPoint(new TPoint(898,355, 0));
		g.addPoint(new TPoint(914,319, 0));
		g.addPoint(new TPoint(918,286, 0));
		g.addPoint(new TPoint(915,254, 0));
		g.addPoint(new TPoint(905,223, 0));
		g.addPoint(new TPoint(888,196, 0));
		g.addPoint(new TPoint(862,172, 0));
		g.addPoint(new TPoint(826,151, 0));
		g.addPoint(new TPoint(787,136, 0));
		g.addPoint(new TPoint(749,129, 0));
		g.addPoint(new TPoint(714,130, 0));
		g.addPoint(new TPoint(679,137, 0));
		g.addPoint(new TPoint(648,150, 0));
		g.addPoint(new TPoint(621,169, 0));
		g.addPoint(new TPoint(594,198, 0));
		g.addPoint(new TPoint(568,234, 0));
		g.addPoint(new TPoint(547,276, 0));
		g.addPoint(new TPoint(538,309, 0));
		g.addPoint(new TPoint(534,345, 0));
		g.addPoint(new TPoint(536,378, 0));
		g.addPoint(new TPoint(547,408, 0));
		g.addPoint(new TPoint(566,440, 0));
		g.addPoint(new TPoint(591,474, 0));
		g.addPoint(new TPoint(619,505, 0));
		g.addPoint(new TPoint(655,538, 0));
		g.addPoint(new TPoint(697,574, 0));
		g.addPoint(new TPoint(745,608, 0));
		g.addPoint(new TPoint(790,634, 0));
		g.addPoint(new TPoint(844,661, 0));
		g.addPoint(new TPoint(892,683, 0));
		g.addPoint(new TPoint(940,701, 0));
		g.addPoint(new TPoint(981,714, 0));
		g.addPoint(new TPoint(1023,725, 0));
		g.addPoint(new TPoint(1061,732, 0));
		g.addPoint(new TPoint(1096,736, 0));
		g.addPoint(new TPoint(1126,738, 0));
		g.addPoint(new TPoint(1148,738, 0));
		g.addPoint(new TPoint(1164,738, 0));
		g.addPoint(new TPoint(1172,738, 0));
		
		g.setInfo(new GestureInfo(0, null, "e", 0));
		addTemplate("e", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(372,752, 0));
		g.addPoint(new TPoint(372,749, 0));
		g.addPoint(new TPoint(380,747, 0));
		g.addPoint(new TPoint(417,743, 0));
		g.addPoint(new TPoint(484,740, 0));
		g.addPoint(new TPoint(563,732, 0));
		g.addPoint(new TPoint(636,714, 0));
		g.addPoint(new TPoint(682,697, 0));
		g.addPoint(new TPoint(739,666, 0));
		g.addPoint(new TPoint(793,632, 0));
		g.addPoint(new TPoint(836,599, 0));
		g.addPoint(new TPoint(872,569, 0));
		g.addPoint(new TPoint(898,539, 0));
		g.addPoint(new TPoint(921,498, 0));
		g.addPoint(new TPoint(938,454, 0));
		g.addPoint(new TPoint(946,419, 0));
		g.addPoint(new TPoint(946,395, 0));
		g.addPoint(new TPoint(940,378, 0));
		g.addPoint(new TPoint(929,363, 0));
		g.addPoint(new TPoint(916,350, 0));
		g.addPoint(new TPoint(895,334, 0));
		g.addPoint(new TPoint(867,321, 0));
		g.addPoint(new TPoint(841,313, 0));
		g.addPoint(new TPoint(817,310, 0));
		g.addPoint(new TPoint(790,310, 0));
		g.addPoint(new TPoint(763,314, 0));
		g.addPoint(new TPoint(737,323, 0));
		g.addPoint(new TPoint(712,339, 0));
		g.addPoint(new TPoint(686,371, 0));
		g.addPoint(new TPoint(657,420, 0));
		g.addPoint(new TPoint(637,477, 0));
		g.addPoint(new TPoint(629,525, 0));
		g.addPoint(new TPoint(629,569, 0));
		g.addPoint(new TPoint(642,609, 0));
		g.addPoint(new TPoint(670,652, 0));
		g.addPoint(new TPoint(708,697, 0));
		g.addPoint(new TPoint(763,745, 0));
		g.addPoint(new TPoint(831,787, 0));
		g.addPoint(new TPoint(903,814, 0));
		g.addPoint(new TPoint(970,824, 0));
		g.addPoint(new TPoint(973,825, 0));

		g.setInfo(new GestureInfo(0, null, "e", 0));
		addTemplate("e", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(372,253, 0));
		g.addPoint(new TPoint(370,257, 0));
		g.addPoint(new TPoint(365,258, 0));
		g.addPoint(new TPoint(352,259, 0));
		g.addPoint(new TPoint(335,256, 0));
		g.addPoint(new TPoint(317,245, 0));
		g.addPoint(new TPoint(303,233, 0));
		g.addPoint(new TPoint(291,220, 0));
		g.addPoint(new TPoint(280,205, 0));
		g.addPoint(new TPoint(272,193, 0));
		g.addPoint(new TPoint(268,183, 0));
		g.addPoint(new TPoint(266,174, 0));
		g.addPoint(new TPoint(267,163, 0));
		g.addPoint(new TPoint(268,155, 0));
		g.addPoint(new TPoint(275,146, 0));
		g.addPoint(new TPoint(284,136, 0));
		g.addPoint(new TPoint(294,128, 0));
		g.addPoint(new TPoint(306,122, 0));
		g.addPoint(new TPoint(319,117, 0));
		g.addPoint(new TPoint(332,115, 0));
		g.addPoint(new TPoint(344,116, 0));
		g.addPoint(new TPoint(359,123, 0));
		g.addPoint(new TPoint(377,134, 0));
		g.addPoint(new TPoint(395,153, 0));
		g.addPoint(new TPoint(410,172, 0));
		g.addPoint(new TPoint(424,194, 0));
		g.addPoint(new TPoint(434,217, 0));
		g.addPoint(new TPoint(443,243, 0));
		g.addPoint(new TPoint(445,265, 0));
		g.addPoint(new TPoint(446,288, 0));
		g.addPoint(new TPoint(441,312, 0));
		g.addPoint(new TPoint(431,330, 0));
		g.addPoint(new TPoint(419,347, 0));
		g.addPoint(new TPoint(406,364, 0));
		g.addPoint(new TPoint(384,378, 0));
		g.addPoint(new TPoint(362,390, 0));
		g.addPoint(new TPoint(335,396, 0));
		g.addPoint(new TPoint(315,397, 0));
		g.addPoint(new TPoint(296,394, 0));
		g.addPoint(new TPoint(281,380, 0));
		g.addPoint(new TPoint(270,363, 0));
		g.addPoint(new TPoint(266,339, 0));
		g.addPoint(new TPoint(266,318, 0));
		g.addPoint(new TPoint(279,293, 0));
		g.addPoint(new TPoint(292,275, 0));
		g.addPoint(new TPoint(310,257, 0));
		g.addPoint(new TPoint(339,232, 0));
		g.addPoint(new TPoint(368,213, 0));
		g.addPoint(new TPoint(399,191, 0));
		g.addPoint(new TPoint(430,170, 0));
		g.addPoint(new TPoint(454,150, 0));
		g.addPoint(new TPoint(477,134, 0));
		g.addPoint(new TPoint(490,121, 0));
		g.addPoint(new TPoint(500,112, 0));
		g.addPoint(new TPoint(508,90, 0));
		
		g.setInfo(new GestureInfo(0, null, "cb", 0));
		//addTemplate("cb", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(434,160, 0));
		g.addPoint(new TPoint(431,163, 0));
		g.addPoint(new TPoint(430,164, 0));
		g.addPoint(new TPoint(423,166, 0));
		g.addPoint(new TPoint(411,170, 0));
		g.addPoint(new TPoint(395,174, 0));
		g.addPoint(new TPoint(368,175, 0));
		g.addPoint(new TPoint(339,176, 0));
		g.addPoint(new TPoint(315,175, 0));
		g.addPoint(new TPoint(295,173, 0));
		g.addPoint(new TPoint(280,174, 0));
		g.addPoint(new TPoint(274,174, 0));
		g.addPoint(new TPoint(269,176, 0));
		g.addPoint(new TPoint(265,179, 0));
		g.addPoint(new TPoint(262,183, 0));
		g.addPoint(new TPoint(260,191, 0));
		g.addPoint(new TPoint(260,202, 0));
		g.addPoint(new TPoint(260,217, 0));
		g.addPoint(new TPoint(260,238, 0));
		g.addPoint(new TPoint(259,267, 0));
		g.addPoint(new TPoint(257,294, 0));
		g.addPoint(new TPoint(256,322, 0));
		g.addPoint(new TPoint(256,349, 0));
		g.addPoint(new TPoint(256,369, 0));
		g.addPoint(new TPoint(256,386, 0));
		g.addPoint(new TPoint(256,398, 0));
		g.addPoint(new TPoint(256,406, 0));
		g.addPoint(new TPoint(256,411, 0));
		g.addPoint(new TPoint(256,416, 0));
		g.setInfo(new GestureInfo(0, null, "sb", 0));
		addTemplate("sb", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(1017,132, 0));
		g.addPoint(new TPoint(1014,129, 0));
		g.addPoint(new TPoint(1008,128, 0));
		g.addPoint(new TPoint(991,128, 0));
		g.addPoint(new TPoint(941,131, 0));
		g.addPoint(new TPoint(888,131, 0));
		g.addPoint(new TPoint(833,130, 0));
		g.addPoint(new TPoint(775,130, 0));
		g.addPoint(new TPoint(721,129, 0));
		g.addPoint(new TPoint(671,129, 0));
		g.addPoint(new TPoint(632,130, 0));
		g.addPoint(new TPoint(607,132, 0));
		g.addPoint(new TPoint(591,136, 0));
		g.addPoint(new TPoint(582,138, 0));
		g.addPoint(new TPoint(577,140, 0));
		g.addPoint(new TPoint(574,141, 0));
		g.addPoint(new TPoint(571,142, 0));
		g.addPoint(new TPoint(570,143, 0));
		g.addPoint(new TPoint(568,145, 0));
		g.addPoint(new TPoint(566,149, 0));
		g.addPoint(new TPoint(564,155, 0));
		g.addPoint(new TPoint(562,167, 0));
		g.addPoint(new TPoint(560,187, 0));
		g.addPoint(new TPoint(558,213, 0));
		g.addPoint(new TPoint(558,243, 0));
		g.addPoint(new TPoint(560,281, 0));
		g.addPoint(new TPoint(562,324, 0));
		g.addPoint(new TPoint(565,375, 0));
		g.addPoint(new TPoint(572,426, 0));
		g.addPoint(new TPoint(580,480, 0));
		g.addPoint(new TPoint(589,531, 0));
		g.addPoint(new TPoint(599,581, 0));
		g.addPoint(new TPoint(606,625, 0));
		g.addPoint(new TPoint(613,665, 0));
		g.addPoint(new TPoint(617,694, 0));
		g.addPoint(new TPoint(620,712, 0));
		g.addPoint(new TPoint(620,728, 0));
		g.addPoint(new TPoint(620,738, 0));
		g.addPoint(new TPoint(620,747, 0));
		g.addPoint(new TPoint(620,754, 0));
		g.addPoint(new TPoint(619,758, 0));
		g.addPoint(new TPoint(619,762, 0));
		g.addPoint(new TPoint(617,764, 0));
		g.addPoint(new TPoint(616,765, 0));
		g.addPoint(new TPoint(614,765, 0));
		g.addPoint(new TPoint(613,765, 0));
		g.setInfo(new GestureInfo(0, null, "sb", 0));
		addTemplate("sb", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(915,340, 0));
		g.addPoint(new TPoint(912,337, 0));
		g.addPoint(new TPoint(903,333, 0));
		g.addPoint(new TPoint(880,324, 0));
		g.addPoint(new TPoint(843,315, 0));
		g.addPoint(new TPoint(802,307, 0));
		g.addPoint(new TPoint(760,302, 0));
		g.addPoint(new TPoint(727,302, 0));
		g.addPoint(new TPoint(702,305, 0));
		g.addPoint(new TPoint(685,309, 0));
		g.addPoint(new TPoint(677,313, 0));
		g.addPoint(new TPoint(672,315, 0));
		g.addPoint(new TPoint(670,318, 0));
		g.addPoint(new TPoint(669,322, 0));
		g.addPoint(new TPoint(670,331, 0));
		g.addPoint(new TPoint(675,369, 0));
		g.addPoint(new TPoint(680,442, 0));
		g.addPoint(new TPoint(680,518, 0));
		g.addPoint(new TPoint(677,583, 0));
		g.addPoint(new TPoint(675,635, 0));
		g.addPoint(new TPoint(675,673, 0));
		g.addPoint(new TPoint(676,701, 0));
		g.addPoint(new TPoint(678,721, 0));
		g.addPoint(new TPoint(680,732, 0));
		g.addPoint(new TPoint(682,740, 0));
		g.addPoint(new TPoint(683,743, 0));
		g.addPoint(new TPoint(683,745, 0));
		g.addPoint(new TPoint(684,746, 0));
		g.setInfo(new GestureInfo(0, null, "sb", 0));
		addTemplate("sb", g);

		g = new Gesture();
		g.addPoint(new TPoint(364,67, 0));
		g.addPoint(new TPoint(361,71, 0));
		g.addPoint(new TPoint(356,75, 0));
		g.addPoint(new TPoint(349,81, 0));
		g.addPoint(new TPoint(342,88, 0));
		g.addPoint(new TPoint(336,98, 0));
		g.addPoint(new TPoint(330,109, 0));
		g.addPoint(new TPoint(324,121, 0));
		g.addPoint(new TPoint(319,135, 0));
		g.addPoint(new TPoint(314,149, 0));
		g.addPoint(new TPoint(309,165, 0));
		g.addPoint(new TPoint(305,182, 0));
		g.addPoint(new TPoint(302,198, 0));
		g.addPoint(new TPoint(299,215, 0));
		g.addPoint(new TPoint(297,232, 0));
		g.addPoint(new TPoint(297,246, 0));
		g.addPoint(new TPoint(297,260, 0));
		g.addPoint(new TPoint(297,273, 0));
		g.addPoint(new TPoint(297,289, 0));
		g.addPoint(new TPoint(297,303, 0));
		g.addPoint(new TPoint(297,317, 0));
		g.addPoint(new TPoint(297,331, 0));
		g.addPoint(new TPoint(297,345, 0));
		g.addPoint(new TPoint(298,356, 0));
		g.addPoint(new TPoint(300,367, 0));
		g.addPoint(new TPoint(302,377, 0));
		g.addPoint(new TPoint(307,391, 0));
		g.addPoint(new TPoint(310,399, 0));
		g.addPoint(new TPoint(313,407, 0));
		g.addPoint(new TPoint(317,414, 0));
		g.addPoint(new TPoint(322,421, 0));
		g.addPoint(new TPoint(326,428, 0));
		g.addPoint(new TPoint(332,433, 0));
		g.addPoint(new TPoint(336,437, 0));
		g.addPoint(new TPoint(340,441, 0));
		g.addPoint(new TPoint(346,446, 0));
		g.addPoint(new TPoint(351,450, 0));
		g.addPoint(new TPoint(359,454, 0));
		g.addPoint(new TPoint(364,457, 0));
		g.addPoint(new TPoint(370,460, 0));
		
		g.setInfo(new GestureInfo(0, null, "rb", 0));
		addTemplate("rb", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(737,67, 0));
		g.addPoint(new TPoint(736,67, 0));
		g.addPoint(new TPoint(733,67, 0));
		g.addPoint(new TPoint(727,67, 0));
		g.addPoint(new TPoint(713,69, 0));
		g.addPoint(new TPoint(691,76, 0));
		g.addPoint(new TPoint(661,93, 0));
		g.addPoint(new TPoint(629,116, 0));
		g.addPoint(new TPoint(612,129, 0));
		g.addPoint(new TPoint(584,152, 0));
		g.addPoint(new TPoint(558,178, 0));
		g.addPoint(new TPoint(532,210, 0));
		g.addPoint(new TPoint(507,245, 0));
		g.addPoint(new TPoint(487,280, 0));
		g.addPoint(new TPoint(471,313, 0));
		g.addPoint(new TPoint(460,342, 0));
		g.addPoint(new TPoint(452,369, 0));
		g.addPoint(new TPoint(449,395, 0));
		g.addPoint(new TPoint(447,418, 0));
		g.addPoint(new TPoint(447,440, 0));
		g.addPoint(new TPoint(447,466, 0));
		g.addPoint(new TPoint(448,496, 0));
		g.addPoint(new TPoint(449,527, 0));
		g.addPoint(new TPoint(451,560, 0));
		g.addPoint(new TPoint(453,592, 0));
		g.addPoint(new TPoint(456,627, 0));
		g.addPoint(new TPoint(461,667, 0));
		g.addPoint(new TPoint(468,705, 0));
		g.addPoint(new TPoint(477,745, 0));
		g.addPoint(new TPoint(487,780, 0));
		g.addPoint(new TPoint(497,810, 0));
		g.addPoint(new TPoint(509,840, 0));
		g.addPoint(new TPoint(535,893, 0));
		g.addPoint(new TPoint(572,939, 0));
		g.addPoint(new TPoint(590,956, 0));
		g.addPoint(new TPoint(606,970, 0));
		g.addPoint(new TPoint(618,978, 0));
		g.addPoint(new TPoint(629,984, 0));
		g.addPoint(new TPoint(642,990, 0));
		g.addPoint(new TPoint(657,995, 0));
		g.addPoint(new TPoint(672,1000, 0));
		g.addPoint(new TPoint(681,1004, 0));
		g.setInfo(new GestureInfo(0, null, "rb", 0));
		addTemplate("rb", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(1007,191, 0));
		g.addPoint(new TPoint(1004,188, 0));
		g.addPoint(new TPoint(990,185, 0));
		g.addPoint(new TPoint(955,181, 0));
		g.addPoint(new TPoint(903,178, 0));
		g.addPoint(new TPoint(837,178, 0));
		g.addPoint(new TPoint(772,181, 0));
		g.addPoint(new TPoint(708,190, 0));
		g.addPoint(new TPoint(644,206, 0));
		g.addPoint(new TPoint(581,228, 0));
		g.addPoint(new TPoint(524,256, 0));
		g.addPoint(new TPoint(474,289, 0));
		g.addPoint(new TPoint(431,329, 0));
		g.addPoint(new TPoint(388,380, 0));
		g.addPoint(new TPoint(357,434, 0));
		g.addPoint(new TPoint(344,486, 0));
		g.addPoint(new TPoint(345,540, 0));
		g.addPoint(new TPoint(366,596, 0));
		g.addPoint(new TPoint(406,650, 0));
		g.addPoint(new TPoint(474,700, 0));
		g.addPoint(new TPoint(563,736, 0));
		g.addPoint(new TPoint(663,753, 0));
		g.addPoint(new TPoint(762,758, 0));
		g.addPoint(new TPoint(852,754, 0));
		g.addPoint(new TPoint(917,746, 0));
		g.addPoint(new TPoint(970,738, 0));
		g.addPoint(new TPoint(998,733, 0));
		g.addPoint(new TPoint(1014,730, 0));
		g.addPoint(new TPoint(1020,730, 0));
		g.addPoint(new TPoint(1021,730, 0));
		g.setInfo(new GestureInfo(0, null, "rb", 0));
		addTemplate("rb", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(395,139, 0));
		g.addPoint(new TPoint(388,141, 0));
		g.addPoint(new TPoint(380,147, 0));
		g.addPoint(new TPoint(372,157, 0));
		g.addPoint(new TPoint(363,170, 0));
		g.addPoint(new TPoint(352,184, 0));
		g.addPoint(new TPoint(341,198, 0));
		g.addPoint(new TPoint(329,212, 0));
		g.addPoint(new TPoint(314,226, 0));
		g.addPoint(new TPoint(300,238, 0));
		g.addPoint(new TPoint(287,247, 0));
		g.addPoint(new TPoint(276,256, 0));
		g.addPoint(new TPoint(268,261, 0));
		g.addPoint(new TPoint(260,267, 0));
		g.addPoint(new TPoint(255,272, 0));
		g.addPoint(new TPoint(249,278, 0));
		g.addPoint(new TPoint(244,282, 0));
		g.addPoint(new TPoint(239,288, 0));
		g.addPoint(new TPoint(235,292, 0));
		g.addPoint(new TPoint(231,295, 0));
		g.addPoint(new TPoint(229,297, 0));
		g.addPoint(new TPoint(225,300, 0));
		g.addPoint(new TPoint(222,302, 0));
		g.addPoint(new TPoint(221,304, 0));
		g.addPoint(new TPoint(218,306, 0));
		g.addPoint(new TPoint(219,307, 0));
		g.addPoint(new TPoint(217,308, 0));
		g.addPoint(new TPoint(217,310, 0));
		g.addPoint(new TPoint(217,311, 0));
		g.addPoint(new TPoint(217,312, 0));
		g.addPoint(new TPoint(221,313, 0));
		g.addPoint(new TPoint(228,316, 0));
		g.addPoint(new TPoint(241,322, 0));
		g.addPoint(new TPoint(270,335, 0));
		g.addPoint(new TPoint(299,350, 0));
		g.addPoint(new TPoint(329,365, 0));
		g.addPoint(new TPoint(359,381, 0));
		g.addPoint(new TPoint(389,399, 0));
		g.addPoint(new TPoint(406,410, 0));
		g.addPoint(new TPoint(421,418, 0));
		g.addPoint(new TPoint(432,423, 0));
		g.addPoint(new TPoint(439,427, 0));
		g.addPoint(new TPoint(445,430, 0));
		g.addPoint(new TPoint(448,431, 0));
		g.addPoint(new TPoint(450,433, 0));
		g.addPoint(new TPoint(452,434, 0));
		g.addPoint(new TPoint(452,435, 0));
		g.addPoint(new TPoint(452,436, 0));
		g.addPoint(new TPoint(452,437, 0));
		g.setInfo(new GestureInfo(0, null, "lt", 0));
		addTemplate("lt", g);//opening ngle bracket
		
		g = new Gesture();
		g.addPoint(new TPoint(868,122, 0));
		g.addPoint(new TPoint(868,122, 0));
		g.addPoint(new TPoint(868,122, 0));
		g.addPoint(new TPoint(866,122, 0));
		g.addPoint(new TPoint(863,124, 0));
		g.addPoint(new TPoint(850,134, 0));
		g.addPoint(new TPoint(830,152, 0));
		g.addPoint(new TPoint(797,182, 0));
		g.addPoint(new TPoint(751,220, 0));
		g.addPoint(new TPoint(684,264, 0));
		g.addPoint(new TPoint(637,294, 0));
		g.addPoint(new TPoint(590,324, 0));
		g.addPoint(new TPoint(542,356, 0));
		g.addPoint(new TPoint(498,387, 0));
		g.addPoint(new TPoint(461,415, 0));
		g.addPoint(new TPoint(434,436, 0));
		g.addPoint(new TPoint(415,451, 0));
		g.addPoint(new TPoint(403,461, 0));
		g.addPoint(new TPoint(397,465, 0));
		g.addPoint(new TPoint(395,467, 0));
		g.addPoint(new TPoint(394,467, 0));
		g.addPoint(new TPoint(402,468, 0));
		g.addPoint(new TPoint(418,472, 0));
		g.addPoint(new TPoint(441,480, 0));
		g.addPoint(new TPoint(475,496, 0));
		g.addPoint(new TPoint(513,518, 0));
		g.addPoint(new TPoint(567,555, 0));
		g.addPoint(new TPoint(625,603, 0));
		g.addPoint(new TPoint(690,658, 0));
		g.addPoint(new TPoint(758,717, 0));
		g.addPoint(new TPoint(823,773, 0));
		g.addPoint(new TPoint(878,817, 0));
		g.addPoint(new TPoint(922,849, 0));
		g.addPoint(new TPoint(955,873, 0));
		g.addPoint(new TPoint(976,887, 0));
		g.addPoint(new TPoint(985,893, 0));
		g.addPoint(new TPoint(989,896, 0));
		g.addPoint(new TPoint(990,896, 0));
		g.addPoint(new TPoint(990,896, 0));
		g.addPoint(new TPoint(990,896, 0));
		g.addPoint(new TPoint(990,895, 0));
		g.setInfo(new GestureInfo(0, null, "lt", 0));
		addTemplate("lt", g);//opening ngle bracket
		
		g = new Gesture();
		g.addPoint(new TPoint(936,350, 0));
		g.addPoint(new TPoint(930,349, 0));
		g.addPoint(new TPoint(906,354, 0));
		g.addPoint(new TPoint(854,378, 0));
		g.addPoint(new TPoint(780,420, 0));
		g.addPoint(new TPoint(705,466, 0));
		g.addPoint(new TPoint(644,505, 0));
		g.addPoint(new TPoint(589,537, 0));
		g.addPoint(new TPoint(552,556, 0));
		g.addPoint(new TPoint(531,568, 0));
		g.addPoint(new TPoint(521,572, 0));
		g.addPoint(new TPoint(517,574, 0));
		g.addPoint(new TPoint(516,574, 0));
		g.addPoint(new TPoint(515,574, 0));
		g.addPoint(new TPoint(515,574, 0));
		g.addPoint(new TPoint(515,574, 0));
		g.addPoint(new TPoint(518,574, 0));
		g.addPoint(new TPoint(539,577, 0));
		g.addPoint(new TPoint(597,590, 0));
		g.addPoint(new TPoint(682,611, 0));
		g.addPoint(new TPoint(782,642, 0));
		g.addPoint(new TPoint(883,680, 0));
		g.addPoint(new TPoint(975,714, 0));
		g.addPoint(new TPoint(1055,741, 0));
		g.addPoint(new TPoint(1116,759, 0));
		g.addPoint(new TPoint(1157,770, 0));
		g.addPoint(new TPoint(1172,771, 0));
		g.addPoint(new TPoint(1177,771, 0));
		g.addPoint(new TPoint(1176,770, 0));
		g.addPoint(new TPoint(1174,770, 0));
		g.addPoint(new TPoint(1170,770, 0));
		g.addPoint(new TPoint(1169,770, 0));
		g.setInfo(new GestureInfo(0, null, "lt", 0));
		addTemplate("lt", g);//opening ngle bracket
		
		g = new Gesture();
		g.addPoint(new TPoint(274,124, 0));
		g.addPoint(new TPoint(279,124, 0));
		g.addPoint(new TPoint(282,124, 0));
		g.addPoint(new TPoint(287,126, 0));
		g.addPoint(new TPoint(299,134, 0));
		g.addPoint(new TPoint(318,147, 0));
		g.addPoint(new TPoint(344,166, 0));
		g.addPoint(new TPoint(381,185, 0));
		g.addPoint(new TPoint(421,207, 0));
		g.addPoint(new TPoint(465,228, 0));
		g.addPoint(new TPoint(506,247, 0));
		g.addPoint(new TPoint(529,259, 0));
		g.addPoint(new TPoint(543,268, 0));
		g.addPoint(new TPoint(551,275, 0));
		g.addPoint(new TPoint(556,280, 0));
		g.addPoint(new TPoint(562,286, 0));
		g.addPoint(new TPoint(564,290, 0));
		g.addPoint(new TPoint(568,295, 0));
		g.addPoint(new TPoint(571,298, 0));
		g.addPoint(new TPoint(574,302, 0));
		g.addPoint(new TPoint(575,306, 0));
		g.addPoint(new TPoint(578,311, 0));
		g.addPoint(new TPoint(579,317, 0));
		g.addPoint(new TPoint(580,322, 0));
		g.addPoint(new TPoint(580,329, 0));
		g.addPoint(new TPoint(566,339, 0));
		g.addPoint(new TPoint(541,354, 0));
		g.addPoint(new TPoint(506,369, 0));
		g.addPoint(new TPoint(460,385, 0));
		g.addPoint(new TPoint(412,396, 0));
		g.addPoint(new TPoint(373,405, 0));
		g.addPoint(new TPoint(336,410, 0));
		g.addPoint(new TPoint(313,414, 0));
		g.addPoint(new TPoint(297,415, 0));
		g.addPoint(new TPoint(283,417, 0));
		g.addPoint(new TPoint(274,418, 0));
		g.addPoint(new TPoint(267,420, 0));
		g.addPoint(new TPoint(260,420, 0));
		g.addPoint(new TPoint(257,421, 0));
		g.addPoint(new TPoint(254,422, 0));
		g.addPoint(new TPoint(251,422, 0));
		g.addPoint(new TPoint(250,422, 0));
		g.setInfo(new GestureInfo(0, null, "gt", 0));
		addTemplate("gt", g);//closing ngle bracket
		
		g = new Gesture();
		g.addPoint(new TPoint(539,167, 0));
		g.addPoint(new TPoint(549,171, 0));
		g.addPoint(new TPoint(587,192, 0));
		g.addPoint(new TPoint(638,224, 0));
		g.addPoint(new TPoint(697,257, 0));
		g.addPoint(new TPoint(758,289, 0));
		g.addPoint(new TPoint(788,306, 0));
		g.addPoint(new TPoint(835,332, 0));
		g.addPoint(new TPoint(878,352, 0));
		g.addPoint(new TPoint(911,368, 0));
		g.addPoint(new TPoint(935,381, 0));
		g.addPoint(new TPoint(951,390, 0));
		g.addPoint(new TPoint(962,397, 0));
		g.addPoint(new TPoint(968,400, 0));
		g.addPoint(new TPoint(972,404, 0));
		g.addPoint(new TPoint(976,410, 0));
		g.addPoint(new TPoint(978,418, 0));
		g.addPoint(new TPoint(980,424, 0));
		g.addPoint(new TPoint(981,431, 0));
		g.addPoint(new TPoint(980,436, 0));
		g.addPoint(new TPoint(977,444, 0));
		g.addPoint(new TPoint(972,451, 0));
		g.addPoint(new TPoint(963,460, 0));
		g.addPoint(new TPoint(949,471, 0));
		g.addPoint(new TPoint(930,485, 0));
		g.addPoint(new TPoint(908,500, 0));
		g.addPoint(new TPoint(884,515, 0));
		g.addPoint(new TPoint(857,532, 0));
		g.addPoint(new TPoint(829,551, 0));
		g.addPoint(new TPoint(797,572, 0));
		g.addPoint(new TPoint(762,596, 0));
		g.addPoint(new TPoint(728,618, 0));
		g.addPoint(new TPoint(692,641, 0));
		g.addPoint(new TPoint(656,663, 0));
		g.addPoint(new TPoint(619,683, 0));
		g.addPoint(new TPoint(586,699, 0));
		g.addPoint(new TPoint(559,711, 0));
		g.addPoint(new TPoint(537,718, 0));
		g.addPoint(new TPoint(522,724, 0));
		g.addPoint(new TPoint(509,727, 0));
		g.addPoint(new TPoint(500,731, 0));
		g.addPoint(new TPoint(493,733, 0));
		g.addPoint(new TPoint(488,735, 0));
		g.addPoint(new TPoint(486,735, 0));
		g.addPoint(new TPoint(484,735, 0));
		g.addPoint(new TPoint(482,735, 0));
		g.addPoint(new TPoint(480,735, 0));
		g.addPoint(new TPoint(479,735, 0));
		g.setInfo(new GestureInfo(0, null, "gt", 0));
		addTemplate("gt", g);//closing ngle bracket
		
		g = new Gesture();
		g.addPoint(new TPoint(477,300, 0));
		g.addPoint(new TPoint(481,301, 0));
		g.addPoint(new TPoint(498,307, 0));
		g.addPoint(new TPoint(553,329, 0));
		g.addPoint(new TPoint(621,354, 0));
		g.addPoint(new TPoint(703,377, 0));
		g.addPoint(new TPoint(787,396, 0));
		g.addPoint(new TPoint(868,411, 0));
		g.addPoint(new TPoint(946,425, 0));
		g.addPoint(new TPoint(1008,435, 0));
		g.addPoint(new TPoint(1058,444, 0));
		g.addPoint(new TPoint(1090,450, 0));
		g.addPoint(new TPoint(1112,454, 0));
		g.addPoint(new TPoint(1123,456, 0));
		g.addPoint(new TPoint(1128,458, 0));
		g.addPoint(new TPoint(1129,458, 0));
		g.addPoint(new TPoint(1129,458, 0));
		g.addPoint(new TPoint(1129,458, 0));
		g.addPoint(new TPoint(1129,458, 0));
		g.addPoint(new TPoint(1129,458, 0));
		g.addPoint(new TPoint(1127,458, 0));
		g.addPoint(new TPoint(1120,462, 0));
		g.addPoint(new TPoint(1088,482, 0));
		g.addPoint(new TPoint(1018,513, 0));
		g.addPoint(new TPoint(921,550, 0));
		g.addPoint(new TPoint(805,592, 0));
		g.addPoint(new TPoint(689,632, 0));
		g.addPoint(new TPoint(586,670, 0));
		g.addPoint(new TPoint(512,700, 0));
		g.addPoint(new TPoint(465,718, 0));
		g.addPoint(new TPoint(443,727, 0));
		g.addPoint(new TPoint(438,729, 0));
		g.addPoint(new TPoint(436,729, 0));
		g.addPoint(new TPoint(436,729, 0));
		g.addPoint(new TPoint(440,726, 0));
		g.addPoint(new TPoint(443,725, 0));
		g.addPoint(new TPoint(447,723, 0));
		g.addPoint(new TPoint(449,721, 0));
		g.addPoint(new TPoint(451,721, 0));
		g.addPoint(new TPoint(450,721, 0));
		g.setInfo(new GestureInfo(0, null, "gt", 0));
		addTemplate("gt", g);//closing ngle bracket

		g = new Gesture();
		g.addPoint(new TPoint(237,459, 0));
		g.addPoint(new TPoint(237,454, 0));
		g.addPoint(new TPoint(239,445, 0));
		g.addPoint(new TPoint(244,427, 0));
		g.addPoint(new TPoint(248,396, 0));
		g.addPoint(new TPoint(250,367, 0));
		g.addPoint(new TPoint(251,337, 0));
		g.addPoint(new TPoint(251,300, 0));
		g.addPoint(new TPoint(251,266, 0));
		g.addPoint(new TPoint(249,146, 0));
		g.addPoint(new TPoint(250,147, 0));
		g.addPoint(new TPoint(250,146, 0));
		g.addPoint(new TPoint(251,146, 0));
		g.addPoint(new TPoint(252,146, 0));
		g.addPoint(new TPoint(254,149, 0));
		g.addPoint(new TPoint(256,154, 0));
		g.addPoint(new TPoint(263,168, 0));
		g.addPoint(new TPoint(273,182, 0));
		g.addPoint(new TPoint(283,198, 0));
		g.addPoint(new TPoint(292,214, 0));
		g.addPoint(new TPoint(301,228, 0));
		g.addPoint(new TPoint(309,242, 0));
		g.addPoint(new TPoint(318,258, 0));
		g.addPoint(new TPoint(326,274, 0));
		g.addPoint(new TPoint(333,283, 0));
		g.addPoint(new TPoint(337,290, 0));
		g.addPoint(new TPoint(341,294, 0));
		g.addPoint(new TPoint(344,299, 0));
		g.addPoint(new TPoint(345,301, 0));
		g.addPoint(new TPoint(347,302, 0));
		g.addPoint(new TPoint(349,304, 0));
		g.addPoint(new TPoint(350,305, 0));
		g.addPoint(new TPoint(351,305, 0));
		g.addPoint(new TPoint(353,303, 0));
		g.addPoint(new TPoint(356,299, 0));
		g.addPoint(new TPoint(361,291, 0));
		g.addPoint(new TPoint(369,280, 0));
		g.addPoint(new TPoint(383,259, 0));
		g.addPoint(new TPoint(397,232, 0));
		g.addPoint(new TPoint(410,207, 0));
		g.addPoint(new TPoint(424,180, 0));
		g.addPoint(new TPoint(436,159, 0));
		g.addPoint(new TPoint(447,142, 0));
		g.addPoint(new TPoint(457,129, 0));
		g.addPoint(new TPoint(463,119, 0));
		g.addPoint(new TPoint(467,112, 0));
		g.addPoint(new TPoint(473,105, 0));
		g.addPoint(new TPoint(476,101, 0));
		g.addPoint(new TPoint(479,98, 0));
		g.addPoint(new TPoint(481,97, 0));
		g.addPoint(new TPoint(483,96, 0));
		g.addPoint(new TPoint(485,96, 0));
		g.addPoint(new TPoint(486,97, 0));
		g.addPoint(new TPoint(486,102, 0));
		g.addPoint(new TPoint(488,113, 0));
		g.addPoint(new TPoint(491,143, 0));
		g.addPoint(new TPoint(493,175, 0));
		g.addPoint(new TPoint(493,204, 0));
		g.addPoint(new TPoint(494,238, 0));
		g.addPoint(new TPoint(494,270, 0));
		g.addPoint(new TPoint(493,295, 0));
		g.addPoint(new TPoint(490,324, 0));
		g.addPoint(new TPoint(489,350, 0));
		g.addPoint(new TPoint(487,371, 0));
		g.addPoint(new TPoint(487,389, 0));
		g.addPoint(new TPoint(487,399, 0));
		g.addPoint(new TPoint(487,406, 0));
		g.addPoint(new TPoint(487,411, 0));
		g.addPoint(new TPoint(487,415, 0));
		g.addPoint(new TPoint(487,418, 0));
		g.addPoint(new TPoint(487,421, 0));
		g.addPoint(new TPoint(487,422, 0));
		g.addPoint(new TPoint(487,424, 0));
		
		g.setInfo(new GestureInfo(0, null, "m", 0));
		addTemplate("m", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(540,802, 0));
		g.addPoint(new TPoint(540,802, 0));
		g.addPoint(new TPoint(540,799, 0));
		g.addPoint(new TPoint(538,784, 0));
		g.addPoint(new TPoint(534,752, 0));
		g.addPoint(new TPoint(524,702, 0));
		g.addPoint(new TPoint(510,641, 0));
		g.addPoint(new TPoint(500,594, 0));
		g.addPoint(new TPoint(487,531, 0));
		g.addPoint(new TPoint(478,465, 0));
		g.addPoint(new TPoint(470,401, 0));
		g.addPoint(new TPoint(467,347, 0));
		g.addPoint(new TPoint(466,297, 0));
		g.addPoint(new TPoint(466,253, 0));
		g.addPoint(new TPoint(466,209, 0));
		g.addPoint(new TPoint(468,174, 0));
		g.addPoint(new TPoint(470,147, 0));
		g.addPoint(new TPoint(473,125, 0));
		g.addPoint(new TPoint(475,113, 0));
		g.addPoint(new TPoint(477,109, 0));
		g.addPoint(new TPoint(479,106, 0));
		g.addPoint(new TPoint(481,106, 0));
		g.addPoint(new TPoint(484,109, 0));
		g.addPoint(new TPoint(491,122, 0));
		g.addPoint(new TPoint(504,149, 0));
		g.addPoint(new TPoint(523,192, 0));
		g.addPoint(new TPoint(547,246, 0));
		g.addPoint(new TPoint(571,299, 0));
		g.addPoint(new TPoint(600,352, 0));
		g.addPoint(new TPoint(623,393, 0));
		g.addPoint(new TPoint(641,426, 0));
		g.addPoint(new TPoint(654,446, 0));
		g.addPoint(new TPoint(664,456, 0));
		g.addPoint(new TPoint(672,463, 0));
		g.addPoint(new TPoint(677,466, 0));
		g.addPoint(new TPoint(680,466, 0));
		g.addPoint(new TPoint(683,464, 0));
		g.addPoint(new TPoint(688,460, 0));
		g.addPoint(new TPoint(695,453, 0));
		g.addPoint(new TPoint(706,440, 0));
		g.addPoint(new TPoint(723,415, 0));
		g.addPoint(new TPoint(749,383, 0));
		g.addPoint(new TPoint(779,344, 0));
		g.addPoint(new TPoint(809,302, 0));
		g.addPoint(new TPoint(840,257, 0));
		g.addPoint(new TPoint(871,208, 0));
		g.addPoint(new TPoint(896,164, 0));
		g.addPoint(new TPoint(916,120, 0));
		g.addPoint(new TPoint(929,90, 0));
		g.addPoint(new TPoint(938,70, 0));
		g.addPoint(new TPoint(943,60, 0));
		g.addPoint(new TPoint(946,55, 0));
		g.addPoint(new TPoint(947,53, 0));
		g.addPoint(new TPoint(948,64, 0));
		g.addPoint(new TPoint(952,94, 0));
		g.addPoint(new TPoint(958,142, 0));
		g.addPoint(new TPoint(966,205, 0));
		g.addPoint(new TPoint(976,273, 0));
		g.addPoint(new TPoint(985,346, 0));
		g.addPoint(new TPoint(993,420, 0));
		g.addPoint(new TPoint(1002,485, 0));
		g.addPoint(new TPoint(1008,533, 0));
		g.addPoint(new TPoint(1013,573, 0));
		g.addPoint(new TPoint(1015,600, 0));
		g.addPoint(new TPoint(1016,621, 0));
		g.addPoint(new TPoint(1016,640, 0));
		g.addPoint(new TPoint(1014,658, 0));
		g.addPoint(new TPoint(1011,678, 0));
		g.addPoint(new TPoint(1008,693, 0));
		g.addPoint(new TPoint(1005,706, 0));
		g.addPoint(new TPoint(1003,717, 0));
		g.addPoint(new TPoint(1000,730, 0));
		g.addPoint(new TPoint(997,743, 0));

		g.setInfo(new GestureInfo(0, null, "m", 0));
		addTemplate("m", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(432,803, 0));
		g.addPoint(new TPoint(432,800, 0));
		g.addPoint(new TPoint(433,797, 0));
		g.addPoint(new TPoint(448,779, 0));
		g.addPoint(new TPoint(485,735, 0));
		g.addPoint(new TPoint(533,665, 0));
		g.addPoint(new TPoint(569,595, 0));
		g.addPoint(new TPoint(603,523, 0));
		g.addPoint(new TPoint(629,450, 0));
		g.addPoint(new TPoint(649,382, 0));
		g.addPoint(new TPoint(667,320, 0));
		g.addPoint(new TPoint(682,272, 0));
		g.addPoint(new TPoint(693,239, 0));
		g.addPoint(new TPoint(701,218, 0));
		g.addPoint(new TPoint(703,208, 0));
		g.addPoint(new TPoint(703,204, 0));
		g.addPoint(new TPoint(703,202, 0));
		g.addPoint(new TPoint(703,204, 0));
		g.addPoint(new TPoint(704,219, 0));
		g.addPoint(new TPoint(708,258, 0));
		g.addPoint(new TPoint(717,316, 0));
		g.addPoint(new TPoint(731,378, 0));
		g.addPoint(new TPoint(749,428, 0));
		g.addPoint(new TPoint(768,466, 0));
		g.addPoint(new TPoint(784,492, 0));
		g.addPoint(new TPoint(795,503, 0));
		g.addPoint(new TPoint(803,508, 0));
		g.addPoint(new TPoint(815,502, 0));
		g.addPoint(new TPoint(832,483, 0));
		g.addPoint(new TPoint(857,443, 0));
		g.addPoint(new TPoint(882,395, 0));
		g.addPoint(new TPoint(901,349, 0));
		g.addPoint(new TPoint(918,303, 0));
		g.addPoint(new TPoint(931,260, 0));
		g.addPoint(new TPoint(939,225, 0));
		g.addPoint(new TPoint(943,203, 0));
		g.addPoint(new TPoint(943,190, 0));
		g.addPoint(new TPoint(941,186, 0));
		g.addPoint(new TPoint(939,184, 0));
		g.addPoint(new TPoint(938,186, 0));
		g.addPoint(new TPoint(938,204, 0));
		g.addPoint(new TPoint(947,258, 0));
		g.addPoint(new TPoint(964,337, 0));
		g.addPoint(new TPoint(984,426, 0));
		g.addPoint(new TPoint(1007,509, 0));
		g.addPoint(new TPoint(1033,587, 0));
		g.addPoint(new TPoint(1061,660, 0));
		g.addPoint(new TPoint(1083,717, 0));
		g.addPoint(new TPoint(1097,755, 0));
		g.addPoint(new TPoint(1107,776, 0));
		g.addPoint(new TPoint(1112,783, 0));
		g.addPoint(new TPoint(1113,784, 0));
		g.addPoint(new TPoint(1113,785, 0));
		g.addPoint(new TPoint(1113,784, 0));
		g.setInfo(new GestureInfo(0, null, "m", 0));
		addTemplate("m", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(353,475, 0));
		g.addPoint(new TPoint(353,472, 0));
		g.addPoint(new TPoint(357,461, 0));
		g.addPoint(new TPoint(360,450, 0));
		g.addPoint(new TPoint(361,438, 0));
		g.addPoint(new TPoint(361,427, 0));
		g.addPoint(new TPoint(364,407, 0));
		g.addPoint(new TPoint(364,385, 0));
		g.addPoint(new TPoint(364,365, 0));
		g.addPoint(new TPoint(361,344, 0));
		g.addPoint(new TPoint(357,319, 0));
		g.addPoint(new TPoint(353,286, 0));
		g.addPoint(new TPoint(349,251, 0));
		g.addPoint(new TPoint(348,217, 0));
		g.addPoint(new TPoint(345,196, 0));
		g.addPoint(new TPoint(344,184, 0));
		g.addPoint(new TPoint(344,173, 0));
		g.addPoint(new TPoint(344,159, 0));
		g.addPoint(new TPoint(350,140, 0));
		g.addPoint(new TPoint(355,124, 0));
		g.addPoint(new TPoint(358,116, 0));
		g.addPoint(new TPoint(360,111, 0));
		g.addPoint(new TPoint(362,107, 0));
		g.addPoint(new TPoint(379,99, 0));
		g.addPoint(new TPoint(407,88, 0));
		g.addPoint(new TPoint(437,75, 0));
		g.addPoint(new TPoint(449,71, 0));
		g.addPoint(new TPoint(459,68, 0));
		g.addPoint(new TPoint(473,67, 0));
		g.addPoint(new TPoint(496,68, 0));
		g.addPoint(new TPoint(522,75, 0));
		g.addPoint(new TPoint(553,84, 0));
		g.addPoint(new TPoint(579,95, 0));
		g.addPoint(new TPoint(593,102, 0));
		g.addPoint(new TPoint(601,110, 0));
		g.addPoint(new TPoint(605,117, 0));
		g.addPoint(new TPoint(605,126, 0));
		g.addPoint(new TPoint(601,137, 0));
		g.addPoint(new TPoint(591,147, 0));
		g.addPoint(new TPoint(574,161, 0));
		g.addPoint(new TPoint(545,173, 0));
		g.addPoint(new TPoint(514,186, 0));
		g.addPoint(new TPoint(481,197, 0));
		g.addPoint(new TPoint(447,207, 0));
		g.addPoint(new TPoint(420,212, 0));
		g.addPoint(new TPoint(395,215, 0));
		g.addPoint(new TPoint(368,216, 0));
		g.addPoint(new TPoint(331,214, 0));
		g.addPoint(new TPoint(317,213, 0));
		g.setInfo(new GestureInfo(0, null, "p", 0));
		addTemplate("p", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(612,822, 0));
		g.addPoint(new TPoint(612,818, 0));
		g.addPoint(new TPoint(609,790, 0));
		g.addPoint(new TPoint(598,735, 0));
		g.addPoint(new TPoint(577,660, 0));
		g.addPoint(new TPoint(556,582, 0));
		g.addPoint(new TPoint(543,512, 0));
		g.addPoint(new TPoint(534,445, 0));
		g.addPoint(new TPoint(528,382, 0));
		g.addPoint(new TPoint(526,326, 0));
		g.addPoint(new TPoint(527,279, 0));
		g.addPoint(new TPoint(530,240, 0));
		g.addPoint(new TPoint(536,207, 0));
		g.addPoint(new TPoint(544,181, 0));
		g.addPoint(new TPoint(555,159, 0));
		g.addPoint(new TPoint(570,135, 0));
		g.addPoint(new TPoint(587,113, 0));
		g.addPoint(new TPoint(604,93, 0));
		g.addPoint(new TPoint(628,74, 0));
		g.addPoint(new TPoint(659,57, 0));
		g.addPoint(new TPoint(692,46, 0));
		g.addPoint(new TPoint(732,38, 0));
		g.addPoint(new TPoint(773,36, 0));
		g.addPoint(new TPoint(817,39, 0));
		g.addPoint(new TPoint(861,48, 0));
		g.addPoint(new TPoint(903,61, 0));
		g.addPoint(new TPoint(945,77, 0));
		g.addPoint(new TPoint(981,101, 0));
		g.addPoint(new TPoint(1014,133, 0));
		g.addPoint(new TPoint(1050,178, 0));
		g.addPoint(new TPoint(1075,232, 0));
		g.addPoint(new TPoint(1087,284, 0));
		g.addPoint(new TPoint(1087,330, 0));
		g.addPoint(new TPoint(1054,412, 0));
		g.addPoint(new TPoint(1018,442, 0));
		g.addPoint(new TPoint(955,474, 0));
		g.addPoint(new TPoint(882,497, 0));
		g.addPoint(new TPoint(811,512, 0));
		g.addPoint(new TPoint(745,519, 0));
		g.addPoint(new TPoint(684,517, 0));
		g.addPoint(new TPoint(633,511, 0));
		g.addPoint(new TPoint(590,502, 0));
		g.addPoint(new TPoint(551,489, 0));
		g.addPoint(new TPoint(521,478, 0));
		g.addPoint(new TPoint(498,470, 0));
		g.addPoint(new TPoint(485,466, 0));
		g.addPoint(new TPoint(481,464, 0));
		g.setInfo(new GestureInfo(0, null, "p", 0));
		addTemplate("p", g);
		
		g = new Gesture();
		g.addPoint(new TPoint(626,824, 0));
		g.addPoint(new TPoint(622,819, 0));
		g.addPoint(new TPoint(612,804, 0));
		g.addPoint(new TPoint(590,774, 0));
		g.addPoint(new TPoint(565,731, 0));
		g.addPoint(new TPoint(543,681, 0));
		g.addPoint(new TPoint(525,636, 0));
		g.addPoint(new TPoint(483,491, 0));
		g.addPoint(new TPoint(477,448, 0));
		g.addPoint(new TPoint(477,409, 0));
		g.addPoint(new TPoint(484,374, 0));
		g.addPoint(new TPoint(497,343, 0));
		g.addPoint(new TPoint(515,316, 0));
		g.addPoint(new TPoint(541,291, 0));
		g.addPoint(new TPoint(581,269, 0));
		g.addPoint(new TPoint(625,252, 0));
		g.addPoint(new TPoint(679,241, 0));
		g.addPoint(new TPoint(733,239, 0));
		g.addPoint(new TPoint(787,244, 0));
		g.addPoint(new TPoint(844,254, 0));
		g.addPoint(new TPoint(895,268, 0));
		g.addPoint(new TPoint(942,289, 0));
		g.addPoint(new TPoint(978,309, 0));
		g.addPoint(new TPoint(1003,330, 0));
		g.addPoint(new TPoint(1024,357, 0));
		g.addPoint(new TPoint(1038,389, 0));
		g.addPoint(new TPoint(1037,424, 0));
		g.addPoint(new TPoint(1022,457, 0));
		g.addPoint(new TPoint(997,490, 0));
		g.addPoint(new TPoint(948,524, 0));
		g.addPoint(new TPoint(876,552, 0));
		g.addPoint(new TPoint(794,566, 0));
		g.addPoint(new TPoint(700,570, 0));
		g.addPoint(new TPoint(613,561, 0));
		g.addPoint(new TPoint(541,551, 0));
		g.addPoint(new TPoint(490,544, 0));
		g.addPoint(new TPoint(461,537, 0));
		g.addPoint(new TPoint(451,535, 0));
		g.setInfo(new GestureInfo(0, null, "p", 0));
		addTemplate("p", g);
	}


}
