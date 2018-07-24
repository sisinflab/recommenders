package recommenders;


import org.apache.commons.lang3.time.StopWatch;
import utils.CandidateItemStructure;
import utils.RatingStructure;
import utils.UserItemStructure;
import utils.Utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;


public class TimePop {

	
	private static PrintWriter out;
	private static StopWatch watch;
	private static Map<Integer,Map<Integer, RatingStructure>> scores;
	private static Map<Integer,ArrayList<Integer>> userMap;
	private static Map<Integer,Integer> popularityMap;
	private static Map<Integer, Integer> sortedPopularityMap;
	private static Map<Integer,Long> lastTimestampsMap;
	private static long referringTimestamp;
	private static double beta;
	private static int nRecc;
	private static int debug = 1;
	private static String trainingSetPath;
	private static String candidateItemsPath;
	
	
	public TimePop(double b, long rt, int nr, String input, String output) {
		beta = b;
		referringTimestamp = rt;
		nRecc = nr;
		trainingSetPath = input;
		candidateItemsPath = output;
	}
	
		
	public void launchTimePop() throws IOException{


		watch = new StopWatch();
		watch.start();
		
		scores = Utils.getScores(trainingSetPath);
		makePrecursors(trainingSetPath,candidateItemsPath);
		
	}
		
	private void makePrecursors(String trainingSetPath,String candidateItemsPath) throws IOException{
		
		HashSet<ArrayList<String>> results = new HashSet<>();
		
		out = new PrintWriter(candidateItemsPath);
			
		//Map<item,popularityValue>. It is not ordered
		popularityMap = getPopularityMap();
		
		Map<Integer,ArrayList<Integer>> itemMap = getItemMap();
		userMap = getUserMap();
		
		ArrayList<String> popularityList = getPopularityList();

		// map<user,timestamp> of the last ratings.
		lastTimestampsMap = Utils.getLastTimestampMap(scores);
		
					
		// create a list with all user IDs
		ArrayList<Integer> users = new ArrayList<Integer>();
		scores.forEach((userId,list) -> users.add(userId));
			
		System.out.println("Load ended:\t" + watch);	
		
		scores.entrySet().parallelStream().map(entry ->{	
			Integer userId = entry.getKey();				 
	      
			Map<Integer,RatingStructure> innerMovies = entry.getValue();
			// Find candidate precursors of a user only if she has more than 1 rating
			if(innerMovies.size()>1) {
			
				
				// create a list of all candidate precursors(can be repeated)
				Iterator<Integer> it = innerMovies.keySet().iterator();				
				ArrayList<Integer> tempPrecursorList = new ArrayList<Integer>(); 				
				while(it.hasNext()) {
					Integer movieId = it.next();
					ArrayList<Integer> itemUsersList = itemMap.get(movieId);
					int currentUserIndex = itemUsersList.indexOf(userId);
					for(int i = 0;i < currentUserIndex;i++) {
						Integer precursor = itemUsersList.get(i);
						tempPrecursorList.add(precursor);
						
					}						
				}
						
				// for the current user compute the total number of candidate precursors and the number of unique candidate precursors
				int usersNumber = tempPrecursorList.size();			
				Set<Integer> uniqueUsers = new HashSet<Integer>(tempPrecursorList);
				int uniqueUsersNumber = uniqueUsers.size();
						
				// this mean will be used as a threshold to create the final precursors list 
				float mean = (float)usersNumber/(float)uniqueUsersNumber;
									
				// precursors list for the current user
				ArrayList<Integer> precursorsList = new ArrayList<Integer>();	
				for(Integer unique:uniqueUsers) {
					int occurrences = Collections.frequency(tempPrecursorList, unique);
					if(occurrences >= mean) {
						precursorsList.add(unique);
					}
				}
				
				// if no precursors are found, MostPopular is used 
				if(precursorsList.size() == 0) {
					ArrayList<String> result = new ArrayList<>();
					for(String pop:popularityList) {
						result.add(userId + "\t" + pop);
					}
					return result;
				}
						
				ArrayList<String> result = null;
				try {
					result = makeCandidateItems(userId, precursorsList,innerMovies);
				} catch (IOException e) {
					e.printStackTrace();
				}	
				System.out.println(debug);
				debug++;
				return result;
			}else {
				ArrayList<String> result = new ArrayList<>();
				for(String pop:popularityList) {
					result.add(userId + "\t" + pop);
				}
				return result;
			}
		}).forEach(results::add);
		
		System.out.println("Compute ended:\t" + watch);
		System.out.println(results.size());
		for(ArrayList<String> result:results) {
			for(String rec:result) {
				out.println(rec);
			}
		}
		
		
		
		watch.stop();
		System.out.println("Total time:\t" + watch);
		out.close();
	}
		
	private ArrayList<String> makeCandidateItems(Integer userId,ArrayList<Integer> precursorsList,Map<Integer,RatingStructure> innerMovies) throws IOException{

		Map<Integer,Double> candidateItemsMap = new HashMap<>();

		ArrayList<String> recommendations = new ArrayList<String>();

		// for each precursor of the current user
		for(Integer precursor:precursorsList){

			//arraylist of precursor movies ordered by timestamp	
			ArrayList<Integer> precursorsMovies = userMap.get(precursor);

			Map<Integer,RatingStructure> tempPrecursorsMap = scores.get(precursor);

			// timestamp associated to the last item l rated by the current precursor.
			long lastTpr = lastTimestampsMap.get(precursor);

			// for each precursor's item a score is given
			for(int i = 0;i < precursorsMovies.size();i++) {

				Integer candidateItem = precursorsMovies.get(i);
				// timestamp associated to the current precursor rating on the candidate item
				long tprci = tempPrecursorsMap.get(candidateItem).getTimestamp();
				
				long diffLast = Math.abs(referringTimestamp + tprci - 2*lastTpr);
				int ndaysLast = Math.round(diffLast/(24*3600));
				Double decayLast = Math.exp(-(beta*(ndaysLast)));
				Double occurrenceValue = decayLast;
				if(!(innerMovies.containsKey(candidateItem))) { // discard items that the target user has in his training set

					Double occurrences = candidateItemsMap.get(candidateItem);
					if(occurrences == null) {
						candidateItemsMap.put(candidateItem, occurrenceValue);
					}else {
						occurrences = occurrences + occurrenceValue;
						candidateItemsMap.put(candidateItem, occurrences);
					}

				}								
			}


		}

		// if less than a prefixed number of ratings is recommended, mostpopular is used ( // TODO 10 should be parameterized)
		if(candidateItemsMap.size() < 10) {
			// sort candidate precursor's items based on their popularity
			ArrayList<CandidateItemStructure> sortingList = new ArrayList<>();
			Iterator<Integer> candidateIt = candidateItemsMap.keySet().iterator();
			while(candidateIt.hasNext()) {
				Integer itemId = candidateIt.next();
				int pop = popularityMap.get(itemId);
				sortingList.add(new CandidateItemStructure(itemId, pop));
			}
			Collections.sort(sortingList);

			// add most popular items to the recommendation list until nRecc is reached
			Iterator<Integer> popularityIt = sortedPopularityMap.keySet().iterator();
			int exit = sortingList.size();
			while(popularityIt.hasNext() && exit <= nRecc) {
				Integer itemId = popularityIt.next();
				if(!(candidateItemsMap.containsKey(itemId))) {
					int pop = popularityMap.get(itemId);
					sortingList.add(new CandidateItemStructure(itemId, pop));
					exit++;
				}
			}
			for(CandidateItemStructure itemStructure:sortingList) {
				recommendations.add(userId + "\t" + itemStructure.getItemId() + "\t" + itemStructure.getOccurrences());
			}


		}else {
			// Sort candidate items in decreasing order based on the score and save only the first nRecc
			Map<Integer, Double> sortedCandidateItemsMap = candidateItemsMap.entrySet().stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
							(oldValue, newValue) -> oldValue, LinkedHashMap::new));

			Iterator<Integer> it = sortedCandidateItemsMap.keySet().iterator();
			int exit = 1;
			while(it.hasNext() && exit<=nRecc){
				exit++;
				Integer itemId = it.next();
				Double occurrences = sortedCandidateItemsMap.get(itemId);						
				String recommendationString = userId + "\t" + itemId + "\t" + occurrences;
				recommendations.add(recommendationString);
			}	
		}

		return recommendations;
	}
	
	private Map<Integer,Integer> getPopularityMap(){
		Map<Integer,Integer> popularityMap = new HashMap<>();
		Iterator<Integer> usersIt = scores.keySet().iterator();		
		while(usersIt.hasNext()) {
			Integer userId = usersIt.next();
			Map<Integer,RatingStructure> innerMovies = scores.get(userId);
			Iterator<Integer> it = innerMovies.keySet().iterator();
			while(it.hasNext()) {
				Integer movieId = it.next();
				popularityMap.putIfAbsent(movieId, 0);
				Integer popularity = popularityMap.get(movieId);
				popularity++;
				popularityMap.put(movieId, popularity);
			}
			
		}		
		return popularityMap;
	}

	private Map<Integer,ArrayList<Integer>> getItemMap (){
		Map<Integer,ArrayList<UserItemStructure>> itemMap = new HashMap<>();
		Map<Integer,ArrayList<Integer>> finalItemMap = new HashMap<>();
		
		Iterator<Integer> usersIt = scores.keySet().iterator();		
		while(usersIt.hasNext()) {
			Integer userId = usersIt.next();
			Map<Integer,RatingStructure> innerMovies = scores.get(userId);
			Iterator<Integer> movieIt = innerMovies.keySet().iterator();
			while(movieIt.hasNext()) {
				Integer movieId = movieIt.next();
				RatingStructure rating = innerMovies.get(movieId);
				itemMap.putIfAbsent(movieId, new ArrayList<>());
				itemMap.get(movieId).add(new UserItemStructure(userId,rating.getTimestamp()));
			}
		}
		
		Iterator<Integer> it = itemMap.keySet().iterator();
		while(it.hasNext()) {
			Integer itemId = it.next();
			ArrayList<UserItemStructure> userInfo = itemMap.get(itemId);
			Collections.sort(userInfo);
			ArrayList<Integer> temp = new ArrayList<>();
			for(UserItemStructure user:userInfo) {
				temp.add(user.getUserItem());
			}
			finalItemMap.put(itemId, temp);
		}
		
		
		return finalItemMap;
	}
	
	
	private Map<Integer,ArrayList<Integer>> getUserMap(){
		Map<Integer,ArrayList<Integer>> userMap = new HashMap<>();
		Iterator<Integer> usersIt = scores.keySet().iterator();
		while(usersIt.hasNext()) {
			Integer userId = usersIt.next();
			Map<Integer,RatingStructure> innerMovies = scores.get(userId);
			Iterator<Integer> movieIt = innerMovies.keySet().iterator();
			ArrayList<UserItemStructure> itemsList = new ArrayList<>();
			while(movieIt.hasNext()) {
				Integer movieId = movieIt.next();
				RatingStructure rating = innerMovies.get(movieId);
				itemsList.add(new UserItemStructure(movieId, rating.getTimestamp()));
			}
			Collections.sort(itemsList);
			ArrayList<Integer> temp = new ArrayList<>();
			for(UserItemStructure item:itemsList) {
				temp.add(item.getUserItem());
			}
			userMap.put(userId, temp);
		}	
		return userMap;
	}
	
	private ArrayList<String> getPopularityList() {
		ArrayList<String> popularityList = new ArrayList<>();
		sortedPopularityMap = popularityMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))

                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
		Iterator<Integer> popIt = sortedPopularityMap.keySet().iterator();
		int exit = 1;
		while(popIt.hasNext() && exit<=nRecc) {
			Integer movieId = popIt.next();
			Integer pop = sortedPopularityMap.get(movieId);
			popularityList.add(movieId + "\t" + pop);
			exit++;
		}
		return popularityList;
	}
	
}