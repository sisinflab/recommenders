# Factorization Machines

This implementation of Factorization machines is a version explicitly designed for Recommender Systems. Only two different kinds of features have been considered: users and items. We implemented a 2d Factorization Machine to be optimized using the pair-wise learning to rank Bayesian Personalized Ranking criterion (BPR). 
For these reasons some parameters lose any sense with a pair-wise learning to rank algorithm such as the user bias, or the global bias for the RecSys scenario.

Usage: java -jar FactorizationMachines.jar filePath outPath

These parameters are set by default but you can override them passing alternative ones

The typical BPR parameters have to be modified within the code itself. As soon as a parameterized version will be available it will be updated.
