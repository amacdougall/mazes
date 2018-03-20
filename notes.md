# Hybrid Aldous-Broder / Wilson's Algorithm

The Aldous-Broder algorithm is fast when working with a relatively unlinked
maze, but later in its run, it can wander for a long time before finding the few
remaining unlinked cells. Wilson's algorithm is the opposite: early on, it can
wander indefinitely before randomly connecting with existing maze segments, but
when a substantial amount of maze has already been linked up, Wilson's algorithm
can operate efficiently.

An ideal algorithm might operate using Aldous-Broder until half the map has been
linked, and then switch to Wilson's algorithm. This is not at all unreasonable!
This will be a lot easier to deal with if we can get each algorithm to use the
same step values; but we could even transform from Aldous-Broder-style step
values to Wilson ones when making the algorithm transition.

Wilson step values: map with keys :grid, :current, :next, :unconnected
A-B step values: positional args grid, cell, unvisited
