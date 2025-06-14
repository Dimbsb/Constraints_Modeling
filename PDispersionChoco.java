package com.example;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

public class PDispersionChoco {

    public static void main(String[] args) {

        // ----------------------------
        // 1. Define 5x5 grid
        // ----------------------------
        int gridSize = 5;
        int nPoints = gridSize * gridSize;

        // P: the set of candidate facility locations.
        int[][] P = new int[nPoints][2];
        for (int i = 0; i < nPoints; i++) {
            P[i][0] = i / gridSize; // row
            P[i][1] = i % gridSize; // col
        }

        // ----------------------------
        // 2x2 Distance matrix D
        // ----------------------------

        // Calculate the Manhattan distance between each pair of points
        int[][] D = new int[nPoints][nPoints];
        for (int i = 0; i < nPoints; i++) {
            for (int j = 0; j < nPoints; j++) {
                D[i][j] = Math.abs(P[i][0] - P[j][0]) + Math.abs(P[i][1] - P[j][1]);
            }
        }

        // ----------------------------
        // Model
        // F: the set of facilities to be located
        // p: the number of facilities to be located
        // ----------------------------
        int p = 5;
        Model model = new Model("P-Dispersion");
        IntVar[] F = model.intVarArray("F", p, 0, nPoints - 1, false);

        // Maximum distance variable
        int Dmax = 0;
        for (int i = 0; i < nPoints; i++) {
            for (int j = 0; j < nPoints; j++) {
                if (D[i][j] > Dmax) {
                    Dmax = D[i][j];
                }
            }
        }

        // MinimumDistance declaration from 0 to max
        IntVar MinimumDistance = model.intVar("MinimumDistance", 0, Dmax);

        // Flatten the distance matrix
        int[] D_flat = new int[nPoints * nPoints];
        for (int k = 0; k < nPoints; k++) {
            for (int l = 0; l < nPoints; l++) {
                D_flat[k * nPoints + l] = D[k][l];
            }
        }

        // ----------------------------
        // 5. Constraints
        // ----------------------------
        for (int i = 0; i < p; i++) {
            for (int j = i + 1; j < p; j++) {

                // dij declaration from 0 to max... Value from Manhattan
                IntVar dij = model.intVar("dij_" + i + "_" + j, 0, Dmax);
                // Index variable will represent a position (e.g., 0 to 24 for a 5x5 grid)
                // F[i] is the index of the first facility and F[j] is the index of the second
                // index = F[i] * nPoints + F[j]...nPoints = 5, F[i] = 2, F[j] = 3...index = 2 * 5 + 3 = 13
                // D_flat[13] gives the distance between points 2 and 3
                IntVar index = model.intVar("index_" + i + "_" + j, 0, nPoints * nPoints - 1);
                model.scalar(new IntVar[] { F[i], F[j] }, new int[] { nPoints, 1 }, "=", index).post();

                // Get distance between pairs from array D (Element)
                model.element(dij, D_flat, index).post();
                // Constraint to ensure that the minimum distance is less or equal to the
                // distance between pairs
                model.arithm(MinimumDistance, "<=", dij).post();
            }
        }

        // Constraint (all facilities will be at different points)
        model.allDifferent(F).post();

        // Use the model to maximize the minimum distance
        model.setObjective(Model.MAXIMIZE, MinimumDistance);

        // ----------------------------
        // 5. Solve and Time
        // ----------------------------
        long startTime = System.nanoTime();

        Solution solution = model.getSolver().findOptimalSolution(MinimumDistance, Model.MAXIMIZE);

        long endTime = System.nanoTime();
        double TotalTime = (endTime - startTime) / 1_000_000_000.0;

        // ----------------------------
        // 6. Output
        // ----------------------------
        if (solution != null) {
            System.out.println("\nOPTIMAL FACILITY LOCATIONS - FINAL SOLUTION:");
            for (int i = 0; i < p; i++) {
                int idx = solution.getIntVal(F[i]);
                System.out.printf("FACILITY %d: (%d, %d)\n", i, P[idx][0], P[idx][1]);
            }
            System.out.println("MAXIMIZED MINIMUM DISTANCE: " + solution.getIntVal(MinimumDistance));
        } else {
            System.out.println("NO SOLUTION FOUND");
        }

        System.out.printf("EXECUTION TIME: %.3f seconds\n", TotalTime);
    }
}