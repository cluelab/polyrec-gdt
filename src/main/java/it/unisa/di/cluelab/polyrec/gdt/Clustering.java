package it.unisa.di.cluelab.polyrec.gdt;

import java.util.ArrayList;
import java.util.Random;

import it.unisa.di.cluelab.polyrec.Polyline;

/**
 * @author rbufano
 *
 */
public class Clustering {
    private final ExtendedPolyRecognizerGSS recognizer;

    public Clustering(ExtendedPolyRecognizerGSS recognizer) {
        super();
        this.recognizer = recognizer;
    }

    /**
     * trova il medoide della classe.
     */
    int overallmedoid(String className) {

        final ArrayList<Polyline> polylines = recognizer.getTemplate(className);
        double minDistance = Double.MAX_VALUE;
        int medoid = -1;
        for (int i = 0; i < polylines.size(); i++) {
            double totaldistanceFromMedoid = 0;

            for (int j = 0; j < polylines.size(); j++) {
                if (i != j) {
                    final double distanceFromMedoid = recognizer.checkTemplate(recognizer.getTemplate(className).get(i),
                            recognizer.getTemplate(className).get(j));

                    // System.out.println("check template " + j + " distance from medoid " + i + "(distance"
                    // + distanceFromMedoid + ")");

                    totaldistanceFromMedoid += distanceFromMedoid;

                }
            }
            // System.out.println("overallmedoid totaldistance from "+i+":
            // "+totaldistanceFromMedoid);
            if (totaldistanceFromMedoid < minDistance) {
                minDistance = totaldistanceFromMedoid;
                medoid = i;
            }
        }
        // System.out.println("overallmedoid: "+medoid);
        return medoid;
    }

    /**
     * Algoritmo k-medoids.
     */
    @SuppressWarnings("checkstyle:executablestatementcount")
    ClusteringResult kmedoids(String className, int k, int maxIter) {
        final int maxIt = maxIter == 0 ? 40 : maxIter;
        final ArrayList<Polyline> polylines = recognizer.getTemplate(className);

        // matrice del clustering
        final ArrayList<ArrayList<Integer>> clusteringMatrix = new ArrayList<ArrayList<Integer>>();

        // non-medoidi
        final ArrayList<Integer> nonmedoids = new ArrayList<Integer>();
        for (int i = 0; i < polylines.size(); i++) {
            nonmedoids.add(i);
        }

        // scelta casuale dei medoidi iniziali
        final ArrayList<Integer> medoids = new ArrayList<Integer>();
        for (int i = 0; i < k; i++) {
            final Random rand = new Random();
            final Integer randomInt = nonmedoids.get(rand.nextInt(nonmedoids.size()));

            medoids.add(i, randomInt);
            nonmedoids.remove(randomInt);

            final ArrayList<Integer> cluster = new ArrayList<Integer>();
            cluster.add(medoids.get(i));
            clusteringMatrix.add(cluster);

        }

        // assegnazione dei non medoidi al cluster con medoide più vicino
        double cost = assignNonMedoid(polylines, nonmedoids, clusteringMatrix, k);

        // iterazioni
        int it = 0;

        while (it < maxIt && nonmedoids.size() > 0) {
            // selezione di un oggetto non medoide
            final Random rand = new Random();
            final Integer randomInt = nonmedoids.get(rand.nextInt(nonmedoids.size()));

            final ArrayList<ArrayList<Integer>> tempClusteringMatrix = new ArrayList<ArrayList<Integer>>();
            final ArrayList<Integer> tempnonmedoids = new ArrayList<Integer>();
            tempnonmedoids.addAll(nonmedoids);
            tempnonmedoids.remove(randomInt);
            // inserisci il nuovo medoide nella matrice temporanea
            for (int i = 0; i < k; i++) {
                final ArrayList<Integer> cluster = new ArrayList<Integer>();

                if (clusteringMatrix.get(i).contains(randomInt)) {
                    tempnonmedoids.add(clusteringMatrix.get(i).get(0));
                    cluster.add(randomInt);
                } else {
                    cluster.add(clusteringMatrix.get(i).get(0));
                }

                tempClusteringMatrix.add(cluster);

            }

            final double newcost = assignNonMedoid(polylines, tempnonmedoids, tempClusteringMatrix, k);

            // se il nuovo costo del clustering è migliore del precedente,
            if (newcost < cost) {
                nonmedoids.clear();
                nonmedoids.addAll(tempnonmedoids);
                cost = newcost;
                clusteringMatrix.clear();
                clusteringMatrix.addAll(tempClusteringMatrix);

            }

            it++;
        }

        for (int i = 0; i < clusteringMatrix.size(); i++) {
            medoids.add(i, clusteringMatrix.get(i).get(0));
        }

        return new ClusteringResult(cost, clusteringMatrix);

    }

    // assign non memoids
    private double assignNonMedoid(ArrayList<Polyline> polylines, ArrayList<Integer> nonmedoids,
            ArrayList<ArrayList<Integer>> clusteringMatrix, int k) {
        // inizializza costo del clustering
        double cost = 0;

        // per ogni template non medoide della classe
        for (final Integer nonmedoid : nonmedoids) {

            final double[] distances = new double[k];
            // calcola distanza da medoid
            for (int j = 0; j < k; j++) {
                distances[j] = recognizer.checkTemplate(polylines.get(clusteringMatrix.get(j).get(0)),
                        polylines.get(nonmedoid));
            }

            double minDistance = distances[0];
            int minValueIndex = 0;
            for (int count = 1; count < distances.length; count++) {
                if (distances[count] < minDistance) {
                    minDistance = distances[count];
                    minValueIndex = count;
                }
            }

            // assegna a cluster con medoide più vicino
            clusteringMatrix.get(minValueIndex).add(nonmedoid);

            cost += minDistance;

        }
        return cost;
    }

    /**
     * algoritmo silhouette per scegliere il miglior k.
     */
    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:executablestatementcount", "checkstyle:javancss",
        "checkstyle:nestedfordepth", "checkstyle:npathcomplexity"})
    public int silhouette(String classname) {
        final double[] s = new double[recognizer.getTemplate(classname).size()];
        for (int k = 2; k <= recognizer.getTemplate(classname).size() - 1 && k <= 10; k++) {
            s[k] = 0;

            double minCost = Double.MAX_VALUE;
            ClusteringResult bestResult = null;
            // eseguo kmedoids per più volte per evitare il problema del minimo
            // locale

            for (int i = 0; i < recognizer.getTemplate(classname).size() / 2 && i <= 4; i++) {

                // eseguo kmedoids
                final ClusteringResult result = kmedoids(classname, k, 0);
                // System.out.println("risultato "+i+" cost:"+result.getCost()+"
                // result:"+Arrays.toString(result.getMedoids()));
                if (result.getCost() < minCost) {
                    minCost = result.getCost();
                    bestResult = result;
                }

            }
            // ClusteringResult result = kmedoids(classname, k, 0);
            final ArrayList<ArrayList<Integer>> clusters = bestResult.getMatrix();
            for (int i = 0; i < clusters.size(); i++) {
                System.out.println(clusters.get(i));
            }
            final double[] a = new double[recognizer.getTemplate(classname).size()];
            final double[] b = new double[recognizer.getTemplate(classname).size()];
            final double[] si = new double[recognizer.getTemplate(classname).size()];
            for (int count = 0; count < clusters.size(); count++) {
                final ArrayList<Integer> cluster = clusters.get(count);
                for (int i = 0; i < cluster.size(); i++) {
                    // calcolo silhouette per elemento i-esimo del cluster
                    a[cluster.get(i)] = 0;
                    b[cluster.get(i)] = 0;
                    si[cluster.get(i)] = 0;
                    // confronto within cluster
                    if (cluster.size() == 1) {
                        si[cluster.get(i)] = 0;
                    } else {

                        for (int j = 0; j < cluster.size(); j++) {
                            if (i != j) {

                                final double distance = recognizer.checkTemplate(
                                        recognizer.getTemplate(classname).get(cluster.get(i)),
                                        recognizer.getTemplate(classname).get(cluster.get(j)));

                                a[cluster.get(i)] += distance;

                            }

                        }
                        // distanza media within cluster per il gesto i-esmo
                        a[cluster.get(i)] = a[cluster.get(i)] / (cluster.size() - 1);

                        // confronto between cluster
                        final double[] distancefromOtherClauster = new double[clusters.size()];
                        for (int j = 0; j < clusters.size(); j++) {
                            if (j != count) {
                                // confronto per tutti tra cluster i-esimo e j-esimo con i dievero da j;
                                final ArrayList<Integer> othercluster = clusters.get(j);
                                distancefromOtherClauster[j] = 0;
                                for (int l = 0; l < othercluster.size(); l++) {
                                    final double distance = recognizer.checkTemplate(
                                            recognizer.getTemplate(classname).get(cluster.get(i)),
                                            recognizer.getTemplate(classname).get(othercluster.get(l)));
                                    distancefromOtherClauster[j] += distance;

                                }
                                // distanza media rispetto agli elementi del cluster j-esimo
                                distancefromOtherClauster[j] = distancefromOtherClauster[j] / othercluster.size();

                            }

                        }
                        // calcola distanza con cluster più vicino (neighbour)
                        b[cluster.get(i)] = Double.MAX_VALUE;
                        for (int occount = 0; occount < distancefromOtherClauster.length; occount++) {
                            if (distancefromOtherClauster[occount] != 0) {
                                if (distancefromOtherClauster[occount] < b[cluster.get(i)]) {
                                    b[cluster.get(i)] = distancefromOtherClauster[occount];
                                }
                            }

                        }

                        // coefficiente di clustering dell'i-esimo gesto
                        si[cluster.get(i)] = (b[cluster.get(i)] - a[cluster.get(i)])
                                / Math.max(a[cluster.get(i)], b[cluster.get(i)]);

                    }
                    s[k] += si[cluster.get(i)];
                }

            }
            // coefficiente del clustering (Average silhouette width)
            s[k] = s[k] / recognizer.getTemplate(classname).size();

        }

        // calcola miglior k
        int betterK = 2;

        for (int k = 3; k < recognizer.getTemplate(classname).size(); k++) {
            if (s[k] > s[betterK]) {
                betterK = k;
            }
        }

        // for (int k = 2; k <= recognizer.getTemplate(classname).size() - 1; k++)
        // System.out.println("coefficiente silhouette" + k + ":" + s[k]);

        return betterK;
    }

    // selezione dei k medoidi iniziali
    @SuppressWarnings("unused")
    private ArrayList<Integer> selectFarthest(String classname, int k) {

        final Random rand = new Random();

        final ArrayList<Integer> medoids = new ArrayList<Integer>();
        medoids.add(rand.nextInt(recognizer.getTemplate(classname).size()));

        // System.out.println("-----START---");
        // System.out.println("MEDOIDE:" + medoids.get(0));

        int farthestTemplate = -1;
        double bestDistance = 0;
        for (int count = 1; count < k; count++) {

            for (int i = 0; i < recognizer.getTemplate(classname).size(); i++) {

                double distanceij = 0;
                if (!medoids.contains(i)) {
                    // System.out.println(i+" non sta in medoids");
                    for (int j = 0; j < medoids.size(); j++) {
                        // System.out.println("distanza di"+i+" dai medoidi
                        // "+j+":"+recognizer.checkTemplate(recognizer.getTemplate(classname).get(i),
                        // recognizer.getTemplate(classname).get(j)));
                        distanceij += recognizer.checkTemplate(recognizer.getTemplate(classname).get(i),
                                recognizer.getTemplate(classname).get(medoids.get(j)));

                    }

                    // System.out.println("somma distance" + i + " dai medoidi:" + distanceij);
                    // System.out.println("bestDistance:" + bestDistance);

                    if (distanceij > bestDistance) {
                        bestDistance = distanceij;
                        farthestTemplate = i;
                    }

                }
            }

            // System.out.println("-------------------");
            // System.out.println("bestDistance" + bestDistance);
            // System.out.println("farthestTemplate" + farthestTemplate);

            medoids.add(farthestTemplate);
        }
        System.out.println("medoididi iniziali:" + medoids);
        // System.out.println("---------END--");
        return medoids;

    }

}
