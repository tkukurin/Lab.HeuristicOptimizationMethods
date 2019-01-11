# HMO genetic algorithm solver

## TODOs

* Output results and fitness after running (1, 5, +inf) minutes
* Try out making a "smarter" finisher in Main
* Test out different GA parameters
* Try adding other mutation / combination methods in `SmarterGenerator` (for instance, would be
cool to try out sorting tracks by evaluating them in different ways and using that metric to
combine)
* Maybe add a breaking condition to the algorithm, which would stop iterating once the solution
hasn't improved for some time (or at least make it more diverse). Then you can re-run another
instance of the GA.
* Write documentation