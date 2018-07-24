# Item k Nearest Neighbors algorithm with Temporal Decay
In this simple code it can be found a basic implementation of Item k-NN coherent with the MyMediaLite implementation, modified to consider an exponential decay function to discount items similarity in the rating prediction phase. In details various similarity metrics have been implemented:

- Cosine Vector Similarity considering ratings 
- Binary Cosine Vector Similarity (for implicit feedbacks)

Usage: java -jar ItemkNN-TD.jar fileVotes fileRecs numberOfNeighs numberOfRecs

These parameters are set by default but you can override them passing alternative ones
