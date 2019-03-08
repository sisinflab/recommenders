# Introduction

This project is a collection of the implementation of well-known recommendation algorithms and techniques that is currently documented in several publications. 

Most of the code has been programmed with Java 8 and kotlin. We took advantage of many useful features of the language, such as the use of lambda functions, Stream's and facilities for automatic parallelization of the code. 

The publicly available version of this project includes implementations of several recommendation algorithms. The modules published to date are the following:

- **FactorizationMachines** -2d Factorization Machines for recommender systems optimized using BPR criterion.
- **ItemkNN** -item-kNN (Similarities: Cosine, Binary Cosine, Pearson Correlation, Jaccard Coefficient)
- **To be uploaded** - user-kNN (Similarities: Cosine, Binary Cosine, Pearson Correlation, Jaccard Coefficient)
- **itemkNNTemporalDecay** -item-kNN-TD (Similarities: Cosine, Binary Cosine)
- **UserkNNTemporalDecay** -user-kNN-TD (Similarities: Binary Cosine)
- **TimeSVD** -timeSVD (adapted for top@N recommendation)
- **BPRMF** - BPRMF
- **BiasedMatrixFactorization** -Biased Matrix Factorization
- **WRMF** -WRMF
- **WRMFwithSideInformation** -WRMF with side information
- **SoftMarginRankingMF** -Soft Margin Ranking Matrix Factorization
- **SoftMarginRankingMFwithSideInformation** -Soft Margin Ranking Matrix Factorization side information
- **TimePop** -TimePOP - Temporal Popularity baseline

## Reference
If you publish research that uses one of these implementations please use the following two works:
~~~
@inproceedings{Anelli2019Local,
  author    = {Vito Walter Anelli and
               Tommaso Di Noia and
               Eugenio Di Sciascio and
               Azzurra Ragone and
               Joseph Trotta},
  title     = {Local Popularity and Time in top-N Recommendation},
  booktitle = {Proceedings of the 41st European Conference on Information Retrieval, 14th - 18th April 2019, Cologne, Germany},
  year      = {2019}
}
~~~
The full paper describing the overall approach is available here [PDF](https://github.com/vitowalteranelli/TimePOP/blob/master/ECIR2019_paper_306.pdf)

~~~
@inproceedings{Anelli2019Importance,
  author    = {Vito Walter Anelli and
               Tommaso Di Noia and
               Eugenio Di Sciascio and
               Azzurra Ragone and
               Joseph Trotta},
  title     = {The importance of being dissimilar in Recommendation},
  booktitle = {Proceedings of the 34th ACM/SIGAPP Symposium on Applied Computing (SAC '19), April 8--12, 2019, Limassol, Cyprus},
  year      = {2019},
  doi       = {10.1145/3240323.3240338},
  isbn      = {978-1-4503-5933-7/19/04}
}
~~~
The full paper describing the overall approach is available here [PDF](https://github.com/vitowalteranelli/The-importance-of-being-dissimilar-in-Recommendation/blob/master/SAC2019_being_dissimilar_in_recommendation.pdf).

## Credits
This algorithm has been developed by Vito Walter Anelli and Joseph Trotta while working at [SisInf Lab](http://sisinflab.poliba.it) under the supervision of Tommaso Di Noia.  

## Contacts

   Tommaso Di Noia, tommaso [dot] dinoia [at] poliba [dot] it  
   
   Vito Walter Anelli, vitowalter [dot] anelli [at] poliba [dot] it 
   
   Joseph Trotta, joseph [dot] trotta [at] poliba [dot] it 
