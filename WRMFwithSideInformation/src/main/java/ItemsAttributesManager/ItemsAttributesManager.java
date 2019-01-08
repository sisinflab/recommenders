package ItemsAttributesManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ItemsAttributesManager {
    private static HashMap<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();
    private static HashMap<Integer, String> featuresNames = new HashMap<>();
    private static String fileAttrPath = "";
    private static String featuresPath = "";
    private static String fileRatings = "";
    public static HashMap<Integer, ArrayList<Integer>> loadMap(String fileRatings, String fileAttrPath,String fNames, int threshold, String propertiesPath,boolean additive) throws IOException
    {

        map = Utils.Companion.loadAttributeFile(fileAttrPath,"\t",0);
        HashSet<Integer> items = loadItemsSet(fileRatings,"\t",1);
        featuresNames = loadFeaturesNames(fNames);
        ArrayList<String> properties = loadProperties(propertiesPath);
        map = reduceAttributesMapPropertySelection(map,items,properties,additive,threshold);

        return map;
    }
    public static HashSet<Integer> loadItemsSet(String fileIn, String separator, int itemPosition) throws IOException {
        HashSet<Integer> items = new HashSet<>();
        BufferedReader br = null;
        String cvsSplitBy = separator;
        String line = "";
        String[] pattern;

        br = new BufferedReader(new FileReader(fileIn));
        while ((line = br.readLine()) != null) {
            pattern = line.split(cvsSplitBy);

            Integer item = Integer.valueOf(pattern[itemPosition]);
            items.add(item);
        }
        return items;
    }
    public static HashMap<Integer, String> loadFeaturesNames(String fileIn) {
        HashMap<Integer, String> featuresNames = new HashMap<>();
        BufferedReader br = null;
        String cvsSplitBy = "\t";
        String line = "";
        String[] pattern;

        try {
            br = new BufferedReader(new FileReader(fileIn));
            while ((line = br.readLine()) != null) {
                pattern = line.split(cvsSplitBy);

                int featureId = Integer.parseInt(pattern[0]);
                String featureName = pattern[1];
                featuresNames.put(featureId, featureName);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return featuresNames;
    }
    public static HashMap<Integer, ArrayList<Integer>> loadAttributeFile(String fileIn, String separator, int itemPosition){
        HashMap<Integer, ArrayList<Integer>> localMap = new HashMap<>();
        BufferedReader br = null;
        String cvsSplitBy = separator;
        String line = "";
        String[] pattern;
        try {
            br = new BufferedReader(new FileReader(fileIn));
            while ((line = br.readLine()) != null) {
                pattern = line.split(cvsSplitBy);
                ArrayList<Integer> features = new ArrayList<Integer>();
                if (pattern[itemPosition+1].equals("null")){
                }else {
                    for (int i = itemPosition+1 ; i<pattern.length; i++){
                        features.add(Integer.parseInt(pattern[i]));
                    }
                    localMap.put(Integer.parseInt(pattern[itemPosition]), features);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return localMap;
    }
    @SuppressWarnings("Duplicates")
    public static HashMap<Integer, ArrayList<Integer>> reduceAttributesMapPropertySelection(HashMap<Integer, ArrayList<Integer>> map,HashSet<Integer> items,ArrayList<String> properties,boolean additive, int threshold) throws IOException {
        HashMap<Integer, ArrayList<Integer>> newMap = new HashMap<>();
        HashMap<Integer,Integer> featuresPopularity = new HashMap<>();
        HashSet<Integer> acceptableFeatures = new HashSet<>();
        if(properties.isEmpty()){
            acceptableFeatures = featuresNames.entrySet().stream().map(e -> e.getKey()).filter(Objects::nonNull).collect(Collectors.toCollection(HashSet::new));
        }else{
            acceptableFeatures = featuresNames.entrySet().stream().map(e -> {
                if(additive){
                    for(String s: properties){
                        if(e.getValue().indexOf(s)!=-1){
                            return e.getKey();
                        }
                    }
                    return null;
                }else{
                    for(String s: properties){
                        if(e.getValue().indexOf(s)!=-1){
                            return null;
                        }
                    }
                    return e.getKey();
                }
            }
            ).filter(Objects::nonNull).collect(Collectors.toCollection(HashSet::new));
        }
        System.out.println("acceptableFeatures: "+acceptableFeatures.size());
        System.out.println("Movielens 20M mapped items: "+map.size());
        for (Map.Entry<Integer, ArrayList<Integer>> entry : map.entrySet())
        {
            if (items.contains(entry.getKey())){
                //in the item
                ArrayList<Integer> itemFeatures = entry.getValue();
                Integer item = entry.getKey();
                if (itemFeatures==null){
                    System.out.println("Ho tentato di recuperare le feature di un movie ("+item+") non presente ");
                }else{
                    HashSet<Integer> bag = new HashSet<Integer>();
                    for (int i = 0; i < itemFeatures.size(); i++) {
                        //in the features list of the item

                        Integer featureNumber = itemFeatures.get(i);
                        if ((!bag.contains(featureNumber))&&acceptableFeatures.contains(featureNumber)){
                            //evito eventuali feature replicate

                            Integer numberOfItemsThatContainFeature = featuresPopularity.get(featureNumber);
                            //featuresPopularity -- feature --> count
                            if (numberOfItemsThatContainFeature==null){
                                numberOfItemsThatContainFeature = 1;
                            } else {
                                numberOfItemsThatContainFeature = numberOfItemsThatContainFeature+1;
                            }
                            featuresPopularity.put(featureNumber, numberOfItemsThatContainFeature);
                            bag.add(featureNumber);
                        }
                    }
                }
            }
        }
        for (Map.Entry<Integer, ArrayList<Integer>> entry : map.entrySet())
        {
            if (items.contains(entry.getKey())){
                ArrayList<Integer> newItemFeatures = new ArrayList<>();
                for (int feature : entry.getValue()){
                    if(acceptableFeatures.contains(feature)){
                        if(featuresPopularity.get(feature)>threshold){
                            newItemFeatures.add(feature);
                        }
                    }
                }
                if(newItemFeatures.size()>0)newMap.put(entry.getKey(),newItemFeatures);
            }
        }
        System.out.println("Movielens 1M mapped items in Training set: "+newMap.size());

        return newMap;
    }
    @SuppressWarnings("Duplicates")
    public static HashMap<Integer, ArrayList<Integer>> reduceAttributesMap(HashMap<Integer, ArrayList<Integer>> map,HashSet<Integer> items, int threshold){
        HashMap<Integer, ArrayList<Integer>> newMap = new HashMap<>();
        HashMap<Integer,Integer> featuresPopularity = new HashMap<>();
        System.out.println("Movielens 20M mapped items: "+map.size());
        for (Map.Entry<Integer, ArrayList<Integer>> entry : map.entrySet())
        {
            if (items.contains(entry.getKey())){
                //in the item
                ArrayList<Integer> itemFeatures = entry.getValue();
                Integer item = entry.getKey();
                if (itemFeatures==null){
                    System.out.println("Ho tentato di recuperare le feature di un movie ("+item+") non presente ");
                }else{
                    HashSet<Integer> bag = new HashSet<Integer>();
                    for (int i = 0; i < itemFeatures.size(); i++) {
                        //in the features list of the item

                        Integer featureNumber = itemFeatures.get(i);
                        if (!bag.contains(featureNumber)){
                            //evito eventuali feature replicate

                            Integer numberOfItemsThatContainFeature = featuresPopularity.get(featureNumber);
                            //featuresPopularity -- feature --> count
                            if (numberOfItemsThatContainFeature==null){
                                numberOfItemsThatContainFeature = 1;
                            } else {
                                numberOfItemsThatContainFeature = numberOfItemsThatContainFeature+1;
                            }
                            featuresPopularity.put(featureNumber, numberOfItemsThatContainFeature);
                            bag.add(featureNumber);
                        }
                    }
                }
            }
        }
        for (Map.Entry<Integer, ArrayList<Integer>> entry : map.entrySet())
        {
            if (items.contains(entry.getKey())){
                ArrayList<Integer> newItemFeatures = new ArrayList<>();
                for (int feature : entry.getValue()){
                    if(featuresPopularity.get(feature)>threshold){
                        newItemFeatures.add(feature);
                    }
                }
                newMap.put(entry.getKey(),newItemFeatures);
            }
        }
        System.out.println("Movielens 1M mapped items in Training set: "+newMap.size());
        return newMap;
    }
    public static ArrayList<String> loadProperties(String fileIn) {
        ArrayList<String> propertiesNames = new ArrayList<>();
        BufferedReader br = null;
        String line = "";

        try {
            br = new BufferedReader(new FileReader(fileIn));
            while ((line = br.readLine()) != null) {
                if(!line.startsWith("#"))propertiesNames.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return propertiesNames;
    }
    public static HashMap<Integer, HashMap<Integer, Double>> loadRatings(String fileIn, HashMap<Integer, ArrayList<Integer>> map){
        HashMap<Integer, HashMap<Integer, Double>> newRatingsMap = new HashMap<>();
        HashMap<Integer, HashMap<Integer, Double>> oldRatings = Utils.Companion.loadRatingsFile(fileIn,"\t",0,1,2);
        oldRatings.entrySet().stream().forEach(e ->{
            HashMap<Integer, Double> userVotes = new HashMap<>();
            e.getValue().entrySet().stream().forEach(e2-> {
                if(map.containsKey(e2.getKey()))userVotes.put(e2.getKey(),e2.getValue());
            });
            if(!userVotes.isEmpty()){
                newRatingsMap.put(e.getKey(),userVotes);
            }else{
                System.out.println("WARNING - Deleted user: "+e.getKey());
            }
        });
        return newRatingsMap;
    }
}
