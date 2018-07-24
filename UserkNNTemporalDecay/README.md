# User k Nearest Neighbors algorithm with Temporal Decay
In this simple code it can be found a basic implementation of User k-NN coherent with the MyMediaLite implementation, modified to consider an exponential decay function to discount users similarity in the item rating prediction phase. In details it was implemented considering the Binary Cosine Vector Similarity (for implicit feedbacks).

Usage: java -jar userkNN-TD.jar fileVotes fileRecs numberOfNeighs numberOfRecs

These parameters are set by default but you can override them passing alternative ones
