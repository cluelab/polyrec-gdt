package it.unisa.di.cluelab.polyrec.gdt;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

import it.unisa.di.cluelab.polyrec.DouglasPeuckerReducer;
import it.unisa.di.cluelab.polyrec.Gesture;
import it.unisa.di.cluelab.polyrec.PolyRecognizerGSS;
import it.unisa.di.cluelab.polyrec.Polyline;
import it.unisa.di.cluelab.polyrec.PolylineAligner;
import it.unisa.di.cluelab.polyrec.PolylineFinder;
import it.unisa.di.cluelab.polyrec.TPoint;
import it.unisa.di.cluelab.polyrec.Vector;

/**
 * Extended version of PolyRecognizerGSS.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public class ExtendedPolyRecognizerGSS extends PolyRecognizerGSS implements GDTRecognizer {
    private static final Double[] DPR_PARAMS = new Double[] {26d, 22d};
    private static final boolean VERBOSE = false;
    private static final boolean GSS = true;

    public ExtendedPolyRecognizerGSS() {
    }

    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:executablestatementcount", "checkstyle:javancss",
        "checkstyle:npathcomplexity", "checkstyle:returncount"})
    @Override
    public synchronized ExtendedResult recognizeExt(Gesture gesture) {
        // this.rInvariant = _gesture.isRotInv(); if (this.rInvariant)
        // this.angle = ANGLE_INVARIANT; else this.angle = ANGLE_SENSITIVE;

        final PolylineFinder pf = new DouglasPeuckerReducer(gesture, DPR_PARAMS);
        // polyline del gesto da riconoscere
        final Polyline u = pf.find();
        if (u.getIndexes().isEmpty()) {
            return null;
        }

        Double a = Double.POSITIVE_INFINITY;
        String templateName = null;
        Polyline t = null;

        final TreeMap<String, double[]> ranking = new TreeMap<String, double[]>();

        for (final Entry<String, ArrayList<Polyline>> e : templates.entrySet()) {
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

                ranking.put(e.getKey(), new double[] {classdistance, Math.round(classscore * 10000) / 100.});
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
     * Verifica similarità tra un template di una classe e i template di tutte le altre classi.
     * 
     * @param u
     *            Template da veriticare
     * @param className
     *            Classe del template da verificare
     * @param scorelimit
     *            Score al di sopra del cui i template sono troppo simili
     * @return risultati della verifica
     */
    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:executablestatementcount",
        "checkstyle:npathcomplexity"})
    @Override
    public synchronized ArrayList<ExtendedResult> verifyTemplate(Polyline u, String className, int scorelimit) {
        // this.rInvariant = _gesture.isRotInv(); if (this.rInvariant)
        // this.angle = ANGLE_INVARIANT; else this.angle = ANGLE_SENSITIVE;

        // PolylineFinder pf = new DouglasPeuckerReducer(_gesture, params);
        // Polyline u = pf.find();

        Double a = Double.POSITIVE_INFINITY;
        // String templateName = null;
        Polyline t = null;

        final ArrayList<ExtendedResult> results = new ArrayList<ExtendedResult>();

        // per tutte le classi
        for (final Entry<String, ArrayList<Polyline>> en : templates.entrySet()) {
            // classe diversa da quella del template da controllare
            final String key = en.getKey();
            if (!Objects.equals(key, className)) {
                final ArrayList<Polyline> tempTemplates = en.getValue();
                for (int i = 0; i < tempTemplates.size(); i++) {
                    t = tempTemplates.get(i);

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
                    final Double distance = penalty * bestDist;
                    if (VERBOSE) {
                        System.out.println("Confronto con Gesture " + key + " ROTINV:"
                                + template.getGesture().isRotInv() + " SCORE:" + (2.0f - distance) / 2);
                    }

                    final Double score = (2.0f - distance) / 2;
                    final ExtendedResult result = new ExtendedResult(key, score, i);

                    // template troppo simili
                    if (result.getScore() > scorelimit) {
                        results.add(result);

                    }
                    if (VERBOSE) {
                        System.out.println(
                                "Template troppo simile a polyline " + i + " ROTINV:" + template.getGesture().isRotInv()
                                        + " della classe " + key + "  (SCORE:" + (2.0f - distance) / 2 + ")");
                    }
                    if (distance < a) {
                        a = distance;
                        // templateName = key;
                    }

                }
            }

        }

        return results;
    }

    /**
     * Confronto tra due template.
     * 
     * @param u
     *            Primo dei template da confrontare
     * @param t
     *            Secondo dei template da confrontare
     * @return Distanza tra i due template
     */
    @Override
    public synchronized Double checkTemplate(Polyline u, Polyline t) {
        final PolylineAligner aligner = new PolylineAligner(u, t);
        final AbstractMap.SimpleEntry<Polyline, Polyline> polyPair = aligner.align();

        final int addedAngles = aligner.getAddedAngles();
        final double penalty = 1 + (double) addedAngles / (double) (addedAngles + aligner.getMatches());
        // da riconoscere
        final Polyline unknown = polyPair.getKey();
        // confrontato con
        final Polyline template = polyPair.getValue();

        if (VERBOSE) {
            System.out.println(unknown.getVectors());
        }
        if (VERBOSE) {
            System.out.println(template.getVectors());
        }
        Double bestDist = null;
        if (!GSS) {
            final double uAngle = unknown.getGesture().getIndicativeAngle(!unknown.getGesture().isRotInv());
            if (VERBOSE) {
                System.out.println("Indicative angle = " + uAngle);
            }
            final double tAngle = template.getGesture().getIndicativeAngle(!template.getGesture().isRotInv());
            if (VERBOSE) {
                System.out.println("Indicative angleT = " + tAngle);
            }
            bestDist = getDistanceAtAngle(unknown.getVectors(), template.getVectors(), -uAngle, -tAngle);
            if (VERBOSE) {
                System.out.println("Distance at = " + (-uAngle) + "; dist = " + bestDist);
            }
        } else {
            bestDist = getDistanceAtBestAngle(unknown, template, template.getGesture().isRotInv());
        }
        final Double distance = penalty * bestDist;
        return distance;

    }

    // copiato da PolyRecognizerGSS
    private static Double getDistanceAtAngle(List<Vector> v1, List<Vector> v2, double theta1, double theta2) {
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

    /**
     * Get template list for class name.
     * 
     * @param name
     *            Class name
     * @return
     * 
     */
    @Override
    public ArrayList<Polyline> getTemplate(String name) {
        return templates.get(name);

    }

    /**
     * remove a class.
     * 
     * @param name
     *            class name
     */
    @Override
    public void removeClass(String name) {
        templates.remove(name);
        // this.save();
    }

    /**
     * edit class name.
     */
    @Override
    public void editClassName(String oldname, String newname) {
        final ArrayList<Polyline> polylines = templates.get(oldname);
        templates.remove(oldname);
        templates.put(newname.toLowerCase(), polylines);

        // this.save();
    }

    /**
     * remove polyline from set.
     * 
     * @param name
     *            Classname
     * @param index
     *            Index of polyline to remove
     */
    @Override
    public void removePolyline(String name, int index) {
        final ArrayList<Polyline> polylines = templates.get(name);
        polylines.remove(index);
    }

    /**
     * add a class to the set.
     * 
     * @param name
     *            Class name
     */
    @Override
    public void addClass(String name) {
        if (!getClassNames().contains(name.toLowerCase())) {
            templates.put(name.replace('_', '-').toLowerCase(), new ArrayList<Polyline>());
            // this.save();
        }
    }

    @Override
    public int addTemplate(String name, Polyline polyline) {

        if (!templates.containsKey(name)) {
            templates.put(name, new ArrayList<Polyline>());
        }

        final ArrayList<Polyline> templateClass = templates.get(name);

        templateClass.add(polyline);
        return templateClass.size();
    }

    @Override
    public int addTemplate(String name, Gesture gesture) {
        return super.addTemplate(name, GDTRecognizer.normalizeGesture(gesture, Canvas.WIDTH, Canvas.HEIGHT, 150));
    }

    /**
     * get number of templates in set.
     * 
     * @return templatesNum Number of templates
     */
    @Override
    public int getTemplatesNumber() {
        int templatesNum = 0;
        final String[] classes = getClassNames().toArray(new String[0]);
        for (int m = 0; m < classes.length; m++) {
            templatesNum += getTemplate(classes[m]).size();
        }
        return templatesNum;
    }

    @Override
    public String exportJava() {
        final StringBuilder out = new StringBuilder();
        out.append("\t// Instantiating the gesture recognizer\n"
                + "\tprivate it.unisa.di.cluelab.polyrec.PolyRecognizerGSS recognizer = initRecognizer();\n\n"
                + "\t// Add your actions and call this method from your code with the gesture performed by the user as"
                + " parameter\n" + "\tprivate void handleGesture(it.unisa.di.cluelab.polyrec.Gesture drawnGesture) {\n"
                + "\t\tit.unisa.di.cluelab.polyrec.Result r = recognizer.recognize(drawnGesture);\n"
                + "\t\tif (r == null || r.getScore() < 0) {\n"
                + "\t\t\t// TODO (if needed) use a threshold in the condition above to handle imprecise gestures\n"
                + "\t\t\treturn;\n\t\t}\n" + "\t\tswitch(r.getName()) {\n");
        for (final String cn : templates.keySet()) {
            @SuppressWarnings("deprecation")
            final String e = org.apache.commons.lang3.StringEscapeUtils.escapeJava(cn);
            out.append("\t\t\tcase \"" + e + "\":\n\t\t\t\t// TODO add action for: " + cn + "\");\n\t\t\t\tbreak;\n");
        }

        out.append("\t\t}\n\t}\n\n" + "\t// You do not need to change this method\n"
                + "\tprivate static it.unisa.di.cluelab.polyrec.PolyRecognizerGSS initRecognizer() {\n"
                + "\t\tit.unisa.di.cluelab.polyrec.PolyRecognizerGSS r = "
                + "new it.unisa.di.cluelab.polyrec.PolyRecognizerGSS();\n" + "\t\tString cname;\n");
        for (final Entry<String, ArrayList<Polyline>> e : templates.entrySet()) {
            @SuppressWarnings("deprecation")
            final String cname = org.apache.commons.lang3.StringEscapeUtils.escapeJava(e.getKey());
            out.append("\t\tcname = \"" + cname + "\";\n");
            for (final Polyline poly : e.getValue()) {
                final Gesture g = poly.getGesture();
                out.append("\t\taddTemplate(r, cname, " + g.isRotInv() + ", " + g.getPointers() + ", \"");
                double minX = Double.POSITIVE_INFINITY;
                double minY = Double.POSITIVE_INFINITY;
                double maxX = Double.NEGATIVE_INFINITY;
                double maxY = Double.NEGATIVE_INFINITY;
                for (final TPoint p : g.getPoints()) {
                    minX = Math.min(minX, p.x);
                    minY = Math.min(minY, p.y);
                    maxX = Math.max(maxX, p.x);
                    maxY = Math.max(maxY, p.y);
                }
                final double scale = Character.MAX_VALUE / Math.max(maxX - minX, maxY - minY);
                long lastT = -1;
                for (final TPoint p : g.getPoints()) {
                    @SuppressWarnings("deprecation")
                    final String t = org.apache.commons.lang3.StringEscapeUtils
                            .escapeJava((char) Math.round((p.x - minX) * scale)
                                    + String.valueOf((char) Math.round((p.y - minY) * scale))
                                    + (lastT == -1 ? '\0' : (char) Math.max(0, p.time - lastT)));
                    lastT = p.time;
                    out.append(t);
                }
                out.append("\");\n");
            }
            out.append('\n');
        }
        out.append("\t\treturn r;\n" + "\t}\n\n" + "\t// You do not need to change this method\n"
                + "\tprivate static void addTemplate(it.unisa.di.cluelab.polyrec.PolyRecognizerGSS rec, "
                + "String name, boolean rotInv, int numPointers, String xyts) {\n"
                + "\t\tit.unisa.di.cluelab.polyrec.Gesture g = new it.unisa.di.cluelab.polyrec.Gesture();\n"
                + "\t\tlong lastT = 0;\n" + "\t\tfor (int i = 2, n = xyts.length(); i < n; i += 3) {\n"
                + "\t\t\tlastT += xyts.charAt(i);\n" + "\t\t\tg.addPoint(new it.unisa.di.cluelab.polyrec.TPoint"
                + "(xyts.charAt(i - 2), xyts.charAt(i - 1), lastT));\n" + "\t\t}\n" + "\t\tg.setRotInv(rotInv);\n"
                + "\t\tg.setPointers(numPointers);\n" + "\t\trec.addTemplate(name, g);\n" + "\t}");
        return out.toString();
    }

}
