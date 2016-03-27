# minesweep-solver
This is a collection of java classes designed to solve Minesweeper puzzles.  A simple sample program for testing the solving algorithms is also included, although it now requires that the apache commons cli package is installed.
The Edge class is used for solving as much of a puzzle as possible without guessing.  It represents the tiles which are unknown but are next to known tiles so that there is some information about them.  The Board class can guess based on a probabilistic analysis, using information from an existing Edge.

The purpose of this was to formalize the idea of playing the game using the intersection and difference of sets.  With the exception of situations where there are only a few unknown tiles left and the total number of mines restricts the possible mine placements, repeated application of a simple rule for comparing overlapping sets of tiles appears to be able to determine everything that can be found without guessing.

A warning about the guessing algorithm.  The worst case behaviour is exponential in the size of the edge (not the total number of unknown tiles).  While I have tried to improve the performance in normal situations, it can still run out of memory if asked to guess with a long edge (around 70 exposed tiles).
