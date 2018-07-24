# TimeSVD++
This implementation of TimeSVD++ differs from the classical one since all unrated items per user have been estimated for a fixed timestamp and then reordered in decreasing order to obtain @N lists.

Usage: java -jar TimeSVD.jar trainingSetFile outputFile testFile trainFile crossFile referringTimestamp.

These parameters are set by default but you can override them passing alternative ones.

The typical TimeSVD++ parameters have to be modified within the code itself. As soon as a parameterized version will be available it will be updated.
