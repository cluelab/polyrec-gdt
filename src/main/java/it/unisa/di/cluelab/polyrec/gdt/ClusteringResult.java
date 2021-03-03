package it.unisa.di.cluelab.polyrec.gdt;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Clustering result.
 */
public class ClusteringResult {

    private double cost;
    private int[] medoids;
    private ArrayList<ArrayList<Integer>> matrix;

    public ClusteringResult(double cost, int[] medoids, ArrayList<ArrayList<Integer>> matrix) {
        this.cost = cost;
        this.medoids = Arrays.copyOf(medoids, medoids.length);
        this.matrix = matrix;
    }

    public ClusteringResult(double cost, ArrayList<ArrayList<Integer>> matrix) {
        this.cost = cost;

        this.matrix = matrix;
        this.medoids = new int[matrix.size()];
        for (int i = 0; i < matrix.size(); i++) {
            this.medoids[i] = matrix.get(i).get(0);
        }
        Arrays.sort(medoids);
    }

    public double getCost() {
        return cost;
    }

    public ArrayList<ArrayList<Integer>> getMatrix() {
        return matrix;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setMatrix(ArrayList<ArrayList<Integer>> matrix) {
        this.matrix = matrix;
    }

    public int[] getMedoids() {
        return Arrays.copyOf(medoids, medoids.length);
    }

    public void setMedoids(int[] medoids) {
        this.medoids = Arrays.copyOf(medoids, medoids.length);
    }

    @Override
    public String toString() {
        return "";
    }

}
