import numpy as np
from cpmpy import *
from cpmpy.solvers.ortools import CPM_ortools
from cpmpy.expressions.globalconstraints import Element

# ----------------------------
# 1. Define 5x5 grid 
# ----------------------------

grid_size = 5

# P: the set of candidate facility locations.
P = [(i, j) for i in range(grid_size) for j in range(grid_size)]
n_points = len(P)

# Print the grid points 
print("GRID POINTS:") 
for i in range(grid_size):
    print(" ".join(f"{P[i * grid_size + j]}" for j in range(grid_size)))
print()  

# Print the candidate facility locations P 
print ("CANDIDATE FACILITY LOCATIONS P ARE: "f"{n_points}")
for idx, coord in enumerate(P):
    print(f"{idx:2}: {coord}")
    
# ----------------------------
# F: the set of facilities to be located
# p: the number of facilities to be located
# ----------------------------

p = 6
F = [intvar(0, n_points-1, name=f"F{i}") for i in range(p)]

# Print the facility variables 
print("\nFACILITY VARIABLES F:")
for i in range(p):
    print(f"{F[i]}")
 
# ----------------------------
# 2x2 Distance matrix D
# ----------------------------
 
# Calculate the Manhattan distance between each pair of points
coords = np.array(P)
D = np.abs(coords[:, None, :] - coords[None, :, :]).sum(axis=2)

# Print the Manhattan distance matrix D
print("\nDISTANCE MATRIX D (Manhattan distances):")
for i in range(n_points):
    print(" ".join(f"{D[i][j]:2}" for j in range(n_points)))
    
# ----------------------------
# Optimization function to maximize the minimum distance between any two facilities
# ----------------------------

# Initialize the model
model = Model()

# Make D flat (Element)
D_flat = [d for row in D for d in row]

# MinimumDistance declaration from 0 to max
MinimumDistance= intvar(0, max(max(row) for row in D), name="MinimumDistance")

for i in range(p):
    for j in range(i+1, p):
        # DistanceBetweenPairs declaration from 0 to max
        DistanceBetweenPairs = intvar(0, max(max(row) for row in D), name=f"dist_{i}_{j}")
        # Get distance between pairs from array D (Element)
        model += [DistanceBetweenPairs == Element(D_flat, F[i]*n_points + F[j])]
        # Constraint to ensure that the minimum distance is less or equal to the distance between pairs
        model += [MinimumDistance <= DistanceBetweenPairs]

# Constraint (all facilities will be at different points)
model += [F[i] != F[j] for i in range(p) for j in range(i + 1, p)]

# Use the model to maximize the minimum distance
model.maximize(MinimumDistance)

# ----------------------------
# Solve the model with CPM_ortools and print the solution
# ----------------------------
print("\nCALL THE SOLVER...")
solver = CPM_ortools(model)
if solver.solve():
    print("\nOPTIMAL FACILITY LOCATIONS - FINAL SOLUTION:")
    for i in range(p):
        print(f"FACILITY {i}: {P[F[i].value()]}")
    print(f"MAXIMIZED DISTANCE: {MinimumDistance.value()}")
else:
    print("NO SOLUTION FOUND")

 
 