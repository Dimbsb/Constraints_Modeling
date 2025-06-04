include "alldifferent.mzn";

output ["\n-----------------------------------P-DISPERSION-----------------------------------\n"];

% ----------------------------
% 1. Define 5x5 grid 
% ----------------------------
int: grid_size = 3;
int: n_points = grid_size * grid_size;

% Generate grid points as indices from 1 to n_points
set of int: Points = 1..n_points;

% Precompute coordinates
array[Points] of int: x = [ (i-1) div grid_size | i in Points ];
array[Points] of int: y = [ (i-1) mod grid_size | i in Points ];

% ----------------------------
% 2x2 Distance matrix D
% Calculate the Manhattan distance between each pair of points
% ----------------------------
array[Points, Points] of int: D = array2d(Points, Points, [ abs(x[i]-x[j]) + abs(y[i]-y[j]) | i in Points, j in Points ]);

% ----------------------------
% F: the set of facilities to be located
% p: the number of facilities to be located
% ----------------------------
int: p = 5;
array[1..p] of var Points: F;

% MinimumDistance declaration from 0 to max 
var 0..max(D): MinimumDistance;

% Constraint to ensure that the minimum distance is less or equal to the distance between pairs
constraint forall(i, j in 1..p where i < j) (MinimumDistance <= D[F[i], F[j]]);

% Constraint (all facilities will be at different points)
constraint all_different(F);

% ----------------------------
% Solve the model  
% ----------------------------
solve maximize MinimumDistance;

% Output
output [
  "OPTIMAL FACILITY LOCATIONS:\n"] ++ 
  [ "Facility \(i): (" ++ show(x[F[i]]) ++ "," ++ show(y[F[i]]) ++ ")\n" | i in 1..p ] ++
  ["MAXIMIZED MINIMUM DISTANCE: ", show(MinimumDistance), "\n"];
