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

% ----------------------------
% Manually Set Distance Constraints
% LowerValue and UpperValue matrices
% ----------------------------
array[1..p, 1..p] of int: LowerValue = array2d(1..p, 1..p,
[ 
0, 1, 1, 1, 2,
1, 0, 2, 2, 1,
1, 2, 0, 1, 1,
1, 2, 1, 0, 2,
2, 1, 1, 2, 0
]);

array[1..p, 1..p] of int: UpperValue = array2d(1..p, 1..p,
[
8, 5, 6, 4, 6,
5, 8, 4, 7, 8,
6, 4, 8, 5, 5,
4, 7, 5, 8, 5,
6, 8, 5, 5, 8
]);

% b declaration from 0 to max 
var 0..max(D): b;

% ----------------------------
% Constraints
% ----------------------------
constraint forall(i, j in 1..p where i < j) (b <= D[F[i], F[j]]);

constraint forall(i, j in 1..p where i < j) (D[F[i], F[j]] > LowerValue[i, j]);

constraint forall(i, j in 1..p where i < j) (D[F[i], F[j]] < UpperValue[i, j]);

constraint all_different(F);

% ----------------------------
% Solve the model  
% ----------------------------
solve maximize b;

% Output
output [
  "OPTIMAL FACILITY LOCATIONS:\n"] ++ 
  [ "Facility \(i): (" ++ show(Px[F[i]]) ++ "," ++ show(Py[F[i]]) ++ ")\n" | i in 1..p ] ++
  ["MAXIMIZED MINIMUM DISTANCE: ", show(b), "\n"];
