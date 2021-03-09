package it.unisa.di.cluelab.polyrec.gdt;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Objects;

import com.github.cluelab.dollar.Point;
import com.github.cluelab.dollar.PointCloudRecognizer;
import com.github.cluelab.dollar.PointCloudRecognizerPlus;
import com.github.cluelab.dollar.QPointCloudRecognizer;

import it.unisa.di.cluelab.polyrec.Gesture;
import it.unisa.di.cluelab.polyrec.Polyline;
import it.unisa.di.cluelab.polyrec.Recognizer;
import it.unisa.di.cluelab.polyrec.Result;
import it.unisa.di.cluelab.polyrec.TPoint;

/**
 * $P, $P+, $Q recognizers.
 */
public class DollarRecognizer extends Recognizer implements GDTRecognizer {
    static {
        QPointCloudRecognizer.UseEarlyAbandoning = false;
        QPointCloudRecognizer.UseLowerBounding = false;
    }

    /**
     * Recognizer type.
     */
    public enum Type {
        P(12, "PointCloudRecognizer"), P_PLUS(45, "PointCloudRecognizerPlus"), Q(60, "QPointCloudRecognizer");
        private final int maxDist;
        private final String className;

        Type(int maxDist, String className) {
            this.maxDist = maxDist;
            this.className = className;
        }
    }

    private final Type type;

    public DollarRecognizer(Type type) {
        Objects.requireNonNull(type);
        this.type = type;
        System.out.println(type);
        this.templates = new TreeMap<>();
    }

    public Type getType() {
        return type;
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
        final Gesture gNorm = GDTRecognizer.normalizeGesture(gesture, Canvas.WIDTH, Canvas.HEIGHT, 150);
        ArrayList<Polyline> templateClass = templates.get(name);
        if (templateClass == null) {
            templateClass = new ArrayList<Polyline>();
            templates.put(name, templateClass);
        }
        templateClass.add(gNorm.getPoly(Arrays.asList(0, gNorm.getPoints().size() - 1)));
        return templateClass.size();
    }

    @Override
    public Result recognize(Gesture gesture) {
        return recognizeExt(gesture);
    }

    @Override
    public synchronized ExtendedResult recognizeExt(Gesture gesture) {
        final com.github.cluelab.dollar.Gesture cand = new com.github.cluelab.dollar.Gesture(
                gesture.getPoints().stream().map(p -> new Point((float) p.x, (float) p.y, 0)).toArray(Point[]::new),
                type == Type.Q);

        float minDistance = Float.MAX_VALUE;
        String gestureClass = null;

        final TreeMap<String, double[]> ranking = new TreeMap<String, double[]>();

        for (final Entry<String, ArrayList<Polyline>> e : templates.entrySet()) {
            final String className = e.getKey();
            float classDistance = Float.MAX_VALUE;
            for (Polyline template : e.getValue()) {
                final float dist = compare(cand, template.getGesture());

                if (dist < classDistance) {
                    classDistance = dist;
                }
                if (dist < minDistance) {
                    minDistance = dist;
                    gestureClass = className;
                }
            }

            if (classDistance != Float.MAX_VALUE) {
                final double classScore = (type.maxDist - classDistance) / (double) type.maxDist;
                ranking.put(className, new double[] {classDistance, Math.round(classScore * 10000) / 100.});
            }
        }

        if (gestureClass != null) {
            final Double score = (type.maxDist - minDistance) / (double) type.maxDist;
            return new ExtendedResult(gestureClass, score, ranking);
        }

        return null;
    }

    @Override
    public synchronized List<ExtendedResult> verifyTemplate(Polyline u, String className, int scorelimit) {
        final com.github.cluelab.dollar.Gesture cand = new com.github.cluelab.dollar.Gesture(u.getGesture().getPoints()
                .stream().map(p -> new Point((float) p.x, (float) p.y, 0)).toArray(Point[]::new), type == Type.Q);

        final ArrayList<ExtendedResult> res = new ArrayList<>();

        for (final Entry<String, ArrayList<Polyline>> e : templates.entrySet()) {
            final String tClassName = e.getKey();
            if (!Objects.equals(className, tClassName)) {
                for (int i = 0; i < e.getValue().size(); i++) {
                    final Polyline template = e.getValue().get(i);
                    final float dist = compare(cand, template.getGesture());

                    final ExtendedResult er = new ExtendedResult(tClassName,
                            (type.maxDist - dist) / (double) type.maxDist, i);
                    if (er.getScore() > scorelimit) {
                        res.add(er);
                    }
                }
            }
        }

        return res;
    }

    @Override
    public synchronized Double checkTemplate(Polyline u, Polyline t) {
        return (double) compare(
                new com.github.cluelab.dollar.Gesture(u.getGesture().getPoints().stream()
                        .map(p -> new Point((float) p.x, (float) p.y, 0)).toArray(Point[]::new), type == Type.Q),
                t.getGesture());
    }

    @Override
    public List<Polyline> getTemplate(String name) {
        return templates.get(name);
    }

    @Override
    public void removeClass(String name) {
        templates.remove(name);
    }

    @Override
    public void editClassName(String oldname, String newname) {
        final ArrayList<Polyline> polylines = templates.get(oldname);
        templates.remove(oldname);
        templates.put(newname.toLowerCase(), polylines);
    }

    @Override
    public void removePolyline(String name, int index) {
        final ArrayList<Polyline> polylines = templates.get(name);
        polylines.remove(index);
    }

    @Override
    public void addClass(String name) {
        if (!getClassNames().contains(name.toLowerCase())) {
            templates.put(name.replace('_', '-').toLowerCase(), new ArrayList<Polyline>());
        }
    }

    @Override
    public int getTemplatesNumber() {
        int templatesNum = 0;
        final String[] classes = getClassNames().toArray(new String[0]);
        for (int m = 0; m < classes.length; m++) {
            templatesNum += getTemplate(classes[m]).size();
        }
        return templatesNum;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadTemplatesPGS(InputStream is, boolean removeExistent) throws IOException {
        final Map<String, ArrayList<Polyline>> mapFromFile;
        final ObjectInputStream objectinputstream = new ObjectInputStream(is);
        try {
            mapFromFile = (Map<String, ArrayList<Polyline>>) objectinputstream.readObject();
        } catch (ClassNotFoundException e1) {
            objectinputstream.close();
            throw new RuntimeException(e1);
        }
        objectinputstream.close();
        if (removeExistent) {
            this.templates = mapFromFile;
        } else {
            for (Map.Entry<String, ArrayList<Polyline>> e : mapFromFile.entrySet()) {
                final ArrayList<Polyline> cur = templates.get(e.getKey());
                if (cur == null) {
                    templates.put(e.getKey(), e.getValue());
                } else {
                    cur.addAll(e.getValue());
                }
            }
        }
    }

    @Override
    public void saveTemplatesPGS(OutputStream os) throws IOException {
        final ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(templates);
        oos.close();
    }

    @Override
    public String exportJava() {
        final StringBuilder out = new StringBuilder();
        out.append("\t// Add your actions and call this method from your code with the gesture performed by the user as"
                + " parameter\n" + "\tprivate void handleGesture(com.github.cluelab.dollar.Gesture drawnGesture) {\n"
                + "\t\tString r = com.github.cluelab.dollar." + type.className
                + ".Classify(drawnGesture, gestureSet);\n" + "\t\tswitch(r) {\n");
        for (final String cn : templates.keySet()) {
            @SuppressWarnings("deprecation")
            final String e = org.apache.commons.lang3.StringEscapeUtils.escapeJava(cn);
            out.append("\t\t\tcase \"" + e + "\":\n\t\t\t\t// TODO add action for: " + cn + "\");\n\t\t\t\tbreak;\n");
        }

        out.append("\t\t}\n\t}\n\n" + "\t// Instantiating the gesture set\n"
                + "\tprivate com.github.cluelab.dollar.Gesture[] gestureSet = "
                + "new com.github.cluelab.dollar.Gesture[] {\n");
        for (final Entry<String, ArrayList<Polyline>> e : templates.entrySet()) {
            @SuppressWarnings("deprecation")
            final String cname = org.apache.commons.lang3.StringEscapeUtils.escapeJava(e.getKey());
            for (final Polyline poly : e.getValue()) {
                final Gesture g = poly.getGesture();
                out.append("\t\ttoGesture(\"" + cname + "\", \"");
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

                for (final TPoint p : g.getPoints()) {
                    @SuppressWarnings("deprecation")
                    final String t = org.apache.commons.lang3.StringEscapeUtils
                            .escapeJava((char) Math.round((p.x - minX) * scale)
                                    + String.valueOf((char) Math.round((p.y - minY) * scale)));
                    out.append(t);
                }
                out.append("\"),\n");
            }
            out.append('\n');
        }
        out.setLength(out.length() - 3);
        out.append("\n\t};\n\n" + "\t// You do not need to change this method\n"
                + "\tprivate static com.github.cluelab.dollar.Gesture toGesture(String name, String xys) {\n"
                + "\t\tcom.github.cluelab.dollar.Point[] pts = new com.github.cluelab.dollar.Point[xys.length()/2];\n"
                + "\t\tfor (int i = 0, j = 0; i < pts.length; i++, j += 2) {\n"
                + "\t\t\tpts[i] = new com.github.cluelab.dollar.Point(xys.charAt(j), xys.charAt(j + 1), 0);\n"
                + "\t\t}\n" + "\t\treturn new com.github.cluelab.dollar.Gesture(pts, name, true);\n" + "\t}");
        return out.toString();
    }

    @SuppressWarnings("checkstyle:returncount")
    private float compare(com.github.cluelab.dollar.Gesture gesture1, Gesture gesture2) {
        final com.github.cluelab.dollar.Gesture g2 = new com.github.cluelab.dollar.Gesture(
                gesture2.getPoints().stream().map(p -> new Point((float) p.x, (float) p.y, 0)).toArray(Point[]::new),
                type == Type.Q);
        switch (type) {
            case Q:
                return QPointCloudRecognizer.GreedyCloudMatch(gesture1, g2);
            case P_PLUS:
                return PointCloudRecognizerPlus.GreedyCloudMatch(gesture1, g2);
            default:
                return PointCloudRecognizer.GreedyCloudMatch(gesture1, g2);
        }
    }
}
