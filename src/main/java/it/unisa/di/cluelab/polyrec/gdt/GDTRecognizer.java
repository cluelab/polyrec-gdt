package it.unisa.di.cluelab.polyrec.gdt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import java.util.Set;

import it.unisa.di.cluelab.polyrec.Gesture;
import it.unisa.di.cluelab.polyrec.Polyline;
import it.unisa.di.cluelab.polyrec.Result;
import it.unisa.di.cluelab.polyrec.TPoint;
import it.unisa.di.cluelab.polyrec.geom.Rectangle2D;

/**
 * Extended version of PolyRecognizerGSS.
 */
public interface GDTRecognizer {

    /**
     * Adds a new template.
     * 
     * @param name
     *            class name
     * @param gesture
     *            the gesture
     * @return size of list of templates of the class
     */
    int addTemplate(String name, Gesture gesture);

    int addTemplate(String name, Polyline polyline);

    /**
     * @param gesture
     *            the gesture
     * @return The recognition result
     */
    Result recognize(Gesture gesture);

    /**
     * @param name
     *            class name
     * @param templates
     *            list of template gestures
     */
    void addTemplates(String name, List<Gesture> templates);

    /**
     * @return The templates currently used by the recognizer
     */
    Map<String, List<Gesture>> getTemplates();

    /**
     * Remove all of the templates.
     */
    void clear();

    /**
     * @return The name of the recognizer
     */
    String getMethod();

    /**
     * @return The names of the classes
     */
    Set<String> getClassNames();

    /**
     * Load template gestures in .xml format. Any existing gestures will be removed.
     * 
     * @param is
     *            InputStream of the file to be loaded
     * @throws IOException
     *             if an I/O error occurs.
     */
    void loadTemplatesXML(InputStream is) throws Exception;

    /**
     * Load template gestures in .xml format.
     * 
     * @param is
     *            InputStream containing the content to be parsed.
     * @param removeExistent
     *            whether to remove any existing gesture
     * @throws IOException
     *             if an I/O error occurs.
     */
    void loadTemplatesXML(InputStream is, boolean removeExistent) throws Exception;

    /**
     * Save the template gestures in .xml format.
     * 
     * @param os
     *            Destination stream.
     * @throws IOException
     *             if an I/O error occurs.
     */
    void saveTemplatesXML(OutputStream os) throws IOException;

    ExtendedResult recognizeExt(Gesture gesture);

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
    List<ExtendedResult> verifyTemplate(Polyline u, String className, int scorelimit);

    /**
     * Confronto tra due template.
     * 
     * @param u
     *            Primo dei template da confrontare
     * @param t
     *            Secondo dei template da confrontare
     * @return Distanza tra i due template
     */
    Double checkTemplate(Polyline u, Polyline t);

    static Gesture normalizeGesture(Gesture gesture, double targetWidth, double targetHeight, int padding) {
        final Rectangle2D.Double bbox = gesture.getBoundingBox();
        final double zoom = Math.max(targetHeight - padding, targetWidth - padding) / Math.max(bbox.height, bbox.width);

        final Gesture normalizedGesture = new Gesture();
        normalizedGesture.setInfo(gesture.getInfo());
        normalizedGesture.setRotInv(gesture.isRotInv());
        normalizedGesture.setPointers(gesture.getPointers());

        for (final TPoint p : gesture.getPoints()) {
            normalizedGesture.addPoint(new TPoint(p.getX() * zoom, p.getY() * zoom, p.getTime()));
        }

        return normalizedGesture;
    }

    /**
     * Get template list for class name.
     * 
     * @param name
     *            Class name
     * @return
     * 
     */
    List<Polyline> getTemplate(String name);

    /**
     * remove a class.
     * 
     * @param name
     *            class name
     */
    void removeClass(String name);

    /**
     * edit class name.
     */
    void editClassName(String oldname, String newname);

    /**
     * remove all classes.
     */
    default void removeClasses() {
        clear();
    }

    /**
     * remove polyline from set.
     * 
     * @param name
     *            Classname
     * @param index
     *            Index of polyline to remove
     */
    void removePolyline(String name, int index);

    /**
     * add a class to the set.
     * 
     * @param name
     *            Class name
     */
    void addClass(String name);

    /**
     * get number of templates in set.
     * 
     * @return templatesNum Number of templates
     */
    int getTemplatesNumber();

    default void addTemplatesPl(String className, List<Polyline> templates) {
        for (final Polyline p : templates) {
            addTemplate(className, p);
        }
    }

    /**
     * Load template gestures in .pgs format.
     * 
     * @param is
     *            InputStream containing the .psg data.
     * @param removeExistent
     *            whether to remove any existing gesture
     * @throws IOException
     *             if an I/O error occurs.
     */
    void loadTemplatesPGS(InputStream is, boolean removeExistent) throws IOException;

    /**
     * Save the template gestures in .psg format.
     * 
     * @param os
     *            Destination stream.
     * @throws IOException
     *             if an I/O error occurs.
     */
    void saveTemplatesPGS(OutputStream os) throws IOException;

    String exportJava();

}
