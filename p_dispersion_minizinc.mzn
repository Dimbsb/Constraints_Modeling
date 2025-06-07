include "alldifferent.mzn";

output ["\n-----------------------------------P-DISPERSION-----------------------------------\n"];

% ----------------------------
% 1. Define 5x5 grid and grid points
% ----------------------------
int: grid_size = 5;
int: n_points = grid_size * grid_size;
set of int: Points = 1..n_points;

% ----------------------------
% P: the set of candidate facility locations.(CONSTRUCTION)
% Px and Py
% ----------------------------
array[Points] of int: Px = [ (i-1) div grid_size | i in Points ];
array[Points] of int: Py = [ (i-1) mod grid_size | i in Points ];

% ----------------------------
% F: the set of facilities to be located
% p: the number of facilities to be located
% ----------------------------
int: p = 5;
array[1..p] of var Points: F;

% ----------------------------
% 2x2 Distance matrix D
% Calculate the Manhattan distance between each pair of points
% ----------------------------
array[Points, Points] of int: D = array2d(Points, Points, [ abs(Px[i]-Px[j]) + abs(Py[i]-Py[j]) | i in Points, j in Points ]);

% MinimumDistance declaration from 0 to max 
var 0..max(D): MinimumDistance;

% Constraint to ensure that the minimum distance is less or equal to the distance between pairs
% Get distance between pairs from array D (Like Element constraint from Python - D[F[i], F[j]])
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
  [ "Facility \(i): (" ++ show(Px[F[i]]) ++ "," ++ show(Py[F[i]]) ++ ")\n" | i in 1..p ] ++
  ["MAXIMIZED MINIMUM DISTANCE: ", show(MinimumDistance), "\n"];
