package com.example;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

public class PDispersionChocoDistance {

    public static void main(String[] args) {

        // ----------------------------
        // 1. Define 5x5 grid
        // ----------------------------
        int gridSize = 5;
        int nPoints = gridSize * gridSize;

        // Create coordinate pairs
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
        // Random distance constraints generation
        // ----------------------------

        int[][] LowerValue = new int[p][p];
        int[][] UpperValue = new int[p][p];
        java.util.Random rand = new java.util.Random();

        // d2: Random lower bound [1, MaximumDistance/3]
        // d1: Random upper bound [d2+2, MaximumDistance]
        for (int i = 0; i < p; i++) {
            for (int j = 0; j < p; j++) {
                if (i == j) {
                    LowerValue[i][j] = 0;
                    UpperValue[i][j] = Dmax;
                } else if (i < j) {
                    int d2 = 1 + rand.nextInt(Dmax / 3);
                    int d1 = (d2 + 2) + rand.nextInt(Dmax - (d2 + 2) + 1);
                    LowerValue[i][j] = d2;
                    UpperValue[i][j] = d1;
                    LowerValue[j][i] = d2;
                    UpperValue[j][i] = d1;
                }
            }
        }

        // Print bounds
        System.out.println("LOWER DISTANCE BOUNDS:");
        for (int i = 0; i < p; i++) {
            for (int j = 0; j < p; j++) {
                System.out.print(LowerValue[i][j] + "\t");
            }
            System.out.println();
        }

        System.out.println("\nUPPER DISTANCE BOUNDS:");
        for (int i = 0; i < p; i++) {
            for (int j = 0; j < p; j++) {
                System.out.print(UpperValue[i][j] + "\t");
            }
            System.out.println();
        }

        /*
         * int[][] LowerValue = {
         * {0, 1, 2, 1, 1, 1, 2, 1, 1, 2},
         * {1, 0, 2, 2, 2, 2, 1, 2, 2, 2},
         * {2, 2, 0, 1, 2, 2, 1, 2, 1, 2},
         * {1, 2, 1, 0, 1, 2, 2, 2, 1, 2},
         * {1, 2, 2, 1, 0, 1, 2, 2, 2, 2},
         * {1, 2, 2, 2, 1, 0, 1, 2, 1, 2},
         * {2, 1, 1, 2, 2, 1, 0, 2, 2, 2},
         * {1, 2, 2, 2, 2, 2, 2, 0, 1, 1},
         * {1, 2, 1, 1, 2, 1, 2, 1, 0 ,1},
         * {2 ,2 ,2 ,2 ,2 ,2 ,2 ,1 ,1 ,0}
         * };
         * 
         * int[][] UpperValue = {
         * {8, 7, 7, 4, 3, 4, 4, 4, 5, 8},
         * {7, 8, 4, 7, 5, 5, 7, 8, 7, 6},
         * {7, 4, 8, 4, 5, 7, 7, 7, 7, 6},
         * {4, 7, 4, 8, 8, 7, 7, 7, 4, 7},
         * {3, 5, 5, 8, 8, 7, 4, 8, 8 ,7},
         * {4 ,5 ,7 ,7 ,7 ,8 ,8 ,7 ,3 ,7},
         * {4 ,7 ,7 ,7 ,4 ,8 ,8 ,7 ,5 ,6},
         * {4 ,8 ,7 ,7 ,8 ,7 ,7 ,8 ,5 ,4},
         * {5 ,7 ,7 ,4 ,8 ,3 ,5 ,5 ,8 ,5},
         * {8 ,6 ,6 ,7 ,7 ,7 ,6 ,4 ,5 ,8}
         * };
         * 
         */

        // ----------------------------
        // 5. Constraints
        // ----------------------------
        for (int i = 0; i < p; i++) {
            for (int j = i + 1; j < p; j++) {

                IntVar dij = model.intVar("dij_" + i + "_" + j, 0, Dmax);

                IntVar index = model.intVar("index_" + i + "_" + j, 0, nPoints * nPoints - 1);
                model.scalar(new IntVar[] { F[i], F[j] }, new int[] { nPoints, 1 }, "=", index).post();

                model.element(dij, D_flat, index).post();

                // Distance bound constraints
                model.arithm(dij, ">", LowerValue[i][j]).post();
                model.arithm(dij, "<", UpperValue[i][j]).post();

                // Objective constraint
                model.arithm(MinimumDistance, "<=", dij).post();
            }
        }

        // Ensure all facilities are at different points
        model.allDifferent(F).post();

        // Objective: maximize the minimum distance
        model.setObjective(Model.MAXIMIZE, MinimumDistance);

        // ----------------------------
        // 6. Solve and Time
        // ----------------------------
        long startTime = System.nanoTime(); // Start timing

        Solution solution = model.getSolver().findOptimalSolution(MinimumDistance, Model.MAXIMIZE);

        long endTime = System.nanoTime(); // End timing
        double TotalTime = (endTime - startTime) / 1_000_000_000.0;

        // ----------------------------
        // 7. Output Results
        // ----------------------------
        if (solution != null) {
            System.out.println("\nOPTIMAL FACILITY LOCATIONS - FINAL SOLUTION:");
            for (int i = 0; i < p; i++) {
                int idx = solution.getIntVal(F[i]);
                System.out.printf("FACILITY %d: (%d, %d)\n", i, P[idx][0], P[idx][1]);
            }
            System.out.println("MAXIMIZED DISTANCE: " + solution.getIntVal(MinimumDistance));
        } else {
            System.out.println("NO SOLUTION FOUND");
        }

        System.out.printf("TIME TAKEN: %.3f seconds\n", TotalTime);
    }
}