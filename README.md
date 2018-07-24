# Introduction

This project is a collection of the implementation of well-known recommendation algorithms and techniques that is currently documented in several publications. 

Most of the code has been programmed with Java 8 and kotlin. We took advantage of many useful features of the language, such as the use of lambda functions, Stream's and facilities for automatic parallelization of the code. 

The publicly available version of this project includes implementations of several recommendation algorithms. The modules published to date are the following:

- 2d Factorization Machines for recommender systems optimized using BPR criterion.
- item-kNN (Similarities: Cosine, Binary Cosine, Pearson Correlation, Jaccard Coefficient)
- user-kNN (Similarities: Cosine, Binary Cosine, Pearson Correlation, Jaccard Coefficient)
- item-kNN-TD (Similarities: Cosine, Binary Cosine)
- user-kNN-TD (Similarities: Binary Cosine)
- timeSVD (adapted for top@N recommendation)
- TimePOP - Temporal Popularity baseline

If you find these implementation useful for your research please cite:

@article{DBLP:journals/corr/abs-1807-04204,
  author    = {Vito Walter Anelli and
               Joseph Trotta and
               Tommaso Di Noia and
               Eugenio Di Sciascio and
               Azzurra Ragone},
  title     = {Time and Local Popularity in top-N Recommendation},
  journal   = {CoRR},
  volume    = {abs/1807.04204},
  year      = {2018},
  url       = {http://arxiv.org/abs/1807.04204},
  archivePrefix = {arXiv},
  eprint    = {1807.04204},
  timestamp = {Mon, 23 Jul 2018 13:39:30 +0200},
  biburl    = {https://dblp.org/rec/bib/journals/corr/abs-1807-04204},
  bibsource = {dblp computer science bibliography, https://dblp.org}
}
