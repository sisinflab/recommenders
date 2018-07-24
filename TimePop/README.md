# TimePOP
TimePop is a simple and efficient algorithm that combines the notion of personalized popularity and temporal aspects. 

The main assumption behind a “most popular” approach is that global popularity is a characteristic influencing all the users.
In the approach we introduce here, we change this perspective and we analyze a more fine grained personalized version of  popularity by assuming that it is conditioned by the items that a user u already experienced in the past. To this extent, we look at a specific class of neighbors, that we name Precursors, defined as the users who already rated the same items of u in the past.

TIMEPOP usage:

 -b,--beta <arg>                  Beta value for temporal decay
 
 -N,--nItemsRecommended <arg>     Number of items recommended
 
 -rec,--recommender <arg>         Name of recommender
 
 -rf,--resultFile <arg>           Output result file with recommendations
 
 -rt,--referringTimestamp <arg>   Last rating timestamp in the training set
 
 -st,--startingTimestamp <arg>    first rating timestamp in the training set
 
 -tf,--trainfile <arg>            Training set path 

Example:

java -jar timepop.jar --recommender="TimePop" --trainfile="toys_amazon/trainingset.tsv" --nItemsRecommended="100" --resultFile="TimePop.tsv" --referringTimestamp="1377820800" 
