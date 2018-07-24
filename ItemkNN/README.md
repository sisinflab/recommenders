# Item k Nearest Neighbors algorithm
In this simple code it can be found a basic implementation of Item k-NN coherent with the MyMediaLite implementation. In details various similarity metrics have been implemented:

- Cosine Vector Similarity considering ratings 
- Pearson Correlation
- Binary Cosine Vector Similarity (for implicit feedbacks)
- Jaccard Coefficient

Usage: java -jar ItemkNN.jar fileVotes fileRecs numberOfNeighs numberOfRecs

These parameters are set by default but you can override them passing alternative ones
