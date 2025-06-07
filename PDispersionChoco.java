
package com.example;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

public class PDispersionChoco {

    public static void main(String[] args) {
        int gridSize = 5;
        int nPoints = gridSize * gridSize;
        int p = 5; // Number of facilities to locate

        // Create grid points
        int[][] P = new int[nPoints][2];
        for (int i = 0; i < nPoints; i++) {
            P[i][0] = i / gridSize; // row
            P[i][1] = i % gridSize; // col
        }

        // Print grid points
        System.out.println("GRID POINTS:");
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                System.out.print("(" + i + "," + j + ") ");
            }
            System.out.println();
        }

        // Print candidate locations
        System.out.println("\nCANDIDATE FACILITY LOCATIONS P ARE: " + nPoints);
        for (int i = 0; i < nPoints; i++) {
            System.out.printf("%2d: (%d,%d)\n", i, P[i][0], P[i][1]);
        }

        // Distance matrix (Manhattan)
        int[][] D = new int[nPoints][nPoints];
        for (int i = 0; i < nPoints; i++) {
            for (int j = 0; j < nPoints; j++) {
                D[i][j] = Math.abs(P[i][0] - P[j][0]) + Math.abs(P[i][1] - P[j][1]);
            }
        }

        // Print distance matrix
        System.out.println("\nDISTANCE MATRIX D (Manhattan distances):");
        for (int i = 0; i < nPoints; i++) {
            for (int j = 0; j < nPoints; j++) {
                System.out.printf("%2d ", D[i][j]);
            }
            System.out.println();
        }

        // Model
        Model model = new Model("P-Dispersion");
        // Facility variables: indices of chosen locations
        IntVar[] F = model.intVarArray("F", p, 0, nPoints - 1, false);

        // AllDifferent constraint: facilities at unique locations
        model.allDifferent(F).post();

        // Minimum distance variable to maximize
        int maxDist = 2 * (gridSize - 1); // Maximum possible Manhattan distance
        IntVar minDist = model.intVar("MinimumDistance", 0, maxDist);

        // Flatten the distance matrix
        int[] D_flat = new int[nPoints * nPoints];
        for (int k = 0; k < nPoints; k++) {
            for (int l = 0; l < nPoints; l++) {
                D_flat[k * nPoints + l] = D[k][l];
            }
        }

        // For each pair of facilities, ensure minimum distance is respected
        for (int i = 0; i < p; i++) {
            for (int j = i + 1; j < p; j++) {
                IntVar fi = F[i];
                IntVar fj = F[j];

                // Auxiliary variable for index calculation: index = fi * nPoints + fj
                IntVar index = model.intVar("index_" + i + "_" + j, 0, nPoints * nPoints - 1);
                model.scalar(new IntVar[]{fi, fj}, new int[]{nPoints, 1}, "=", index).post();

                // Distance variable for this pair
                IntVar dij = model.intVar("dij_" + i + "_" + j, 0, maxDist);

                // Element constraint: dij = D_flat[index]
                model.element(dij, D_flat, index).post();

                // minDist <= dij for all pairs
                model.arithm(minDist, "<=", dij).post();
            }
        }

        // Objective: maximize the minimum distance between any two facilities
        model.setObjective(Model.MAXIMIZE, minDist);

        // Solve
        Solution solution = model.getSolver().findOptimalSolution(minDist, Model.MAXIMIZE);
        if (solution != null) {
            System.out.println("\nOPTIMAL FACILITY LOCATIONS - FINAL SOLUTION:");
            for (int i = 0; i < p; i++) {
                int idx = solution.getIntVal(F[i]);
                System.out.printf("FACILITY %d: (%d, %d)\n", i, P[idx][0], P[idx][1]);
            }
            System.out.println("MAXIMIZED DISTANCE: " + solution.getIntVal(minDist));
        } else {
            System.out.println("NO SOLUTION FOUND");
        }
    }
}