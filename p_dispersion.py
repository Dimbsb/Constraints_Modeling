import numpy as np
from cpmpy import *
from cpmpy.solvers.ortools import CPM_ortools
import itertools
from cpmpy.expressions.globalconstraints import Element

# ----------------------------
# 1. Define 5x5 grid 
# ----------------------------

grid_size = 5

# P is a list of tuples representing the coordinates of each point in the grid
points = [(i, j) for i in range(grid_size) for j in range(grid_size)]
P = points
n_points = len(points)

# Print the grid points 
print("GRID POINTS:") 
for i in range(grid_size):
    print(" ".join(f"{points[i * grid_size + j]}" for j in range(grid_size)))
print()  

# Print the candidate facility locations P 
print ("CANDIDATE FACILITY LOCATIONS P ARE: "f"{n_points}")
for idx, coord in enumerate(P):
    print(f"{idx:2}: {coord}")
    
    
# ----------------------------
# F: the set of facilities to be located
# p: the number of facilities to be located
# ----------------------------

p = 2  # Number of facilities to place
F = [intvar(0, n_points-1, name=f"F{i}") for i in range(p)]
# Print the facility variables 
print("\nFACILITY VARIABLES F:")
for i in range(p):
    print(f"{F[i]}")
 
# ----------------------------
# 2. Create distance matrix D[i][j] = Manhattan distance between point i and j
# D will be an 25 x 25 matrix for the 5x5 grid where i must declare the distance from each point to every other point
# ----------------------------

D = [[0 for _ in range(n_points)] for _ in range(n_points)]
# Calculate the Manhattan distance between each pair of points
for i in range(n_points):
    for j in range(n_points):
        D[i][j] = abs(points[i][0] - points[j][0]) + abs(points[i][1] - points[j][1])

# Print the Manhattan distance matrix D
print("\nDISTANCE MATRIX D (Manhattan distances):")
for i in range(n_points):
    print(" ".join(f"{D[i][j]:2}" for j in range(n_points)))
 