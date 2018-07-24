package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Utils {
	

	public static Map<Integer,Long> getLastTimestampMap(Map<Integer,Map<Integer,RatingStructure>> scores){
		Map<Integer,Long> timestampMap = new HashMap<>();
		scores.entrySet().stream().forEach(entry -> {
			Integer userId = entry.getKey();
			Map<Integer,RatingStructure> ratings = entry.getValue();
	        Map<Integer, RatingStructure> sortedRatings = ratings.entrySet().stream()
	                .sorted(Entry.comparingByValue(Comparator.naturalOrder()))
	                .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
	                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
			List<Entry<Integer,RatingStructure>> entryList =
				    new ArrayList<Entry<Integer,RatingStructure>>(sortedRatings.entrySet());
			long lasttpr = entryList.get(entryList.size()-1).getValue().getTimestamp();
			timestampMap.put(userId,lasttpr);
			
		});	
		return timestampMap;
	}
	

	public static Map<Integer, Map<Integer,RatingStructure>> getScores(String file) throws IOException {		
		Map<Integer, Map<Integer,RatingStructure>> scores = new HashMap<>();		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = "";
		Map<Integer,RatingStructure> innerMap = new HashMap<>();
		while ((line = reader.readLine()) != null) {
			String[] splittedLine = line.split("\t");
			Integer userId = Integer.valueOf(splittedLine[0]);
			Integer itemId =  Integer.valueOf(splittedLine[1]);
			float scoreValue = Float.valueOf(splittedLine[2]);
			long timestamp = Long.valueOf(splittedLine[3]);
						
			innerMap = scores.get(userId);
			if(innerMap == null){
				innerMap = new HashMap<Integer,RatingStructure>();
				innerMap.put(itemId,new RatingStructure(timestamp,scoreValue));
			}
			innerMap.put(itemId,new RatingStructure(timestamp,scoreValue));
			scores.put(userId, innerMap);
			
		}
		reader.close();
		return scores;
	}

}
