@file:JvmName("Utils")

import java.io.*
import java.util.HashMap
import java.util.AbstractMap.*
import java.util.ArrayList

class Utils {
    companion object {
        /**
         *
         */
        @JvmOverloads fun loadRatingsFile(fileIn: String,
                                       separator : String = "\t",
                                       userPosition: Int = 0,
                                       itemPosition : Int = 1,
                                       ratePosition : Int = 2
        ): HashMap<Int, HashMap<Int, Float>>
        {
            val localMap = HashMap<Int, HashMap<Int, Float>>()
            var pattern: Array<String?>
            val bufferedReader: BufferedReader = File(fileIn).bufferedReader()
            val lineList = mutableListOf<String>()
            bufferedReader.useLines { lines -> lines.forEach { lineList.add(it) } }
            lineList.forEach {
                pattern = it.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val userID = Integer.parseInt(pattern[userPosition])
                val itemID = Integer.parseInt(pattern[itemPosition])
                val rate : Float
                if (ratePosition!=-1){
                    rate = java.lang.Float.parseFloat(pattern[ratePosition])
                }else{
                    rate = 1f
                }
                var votes: HashMap<Int, Float>? = localMap[userID]
                if (votes == null) {
                    votes = HashMap()
                }
                votes.put(itemID, rate)
                localMap.put(userID, votes)
            }
            return localMap
        }
        fun loadRatingsFile(fileIn: String,
                                       separator : String = "\t",
                                       userPosition: Int = 0,
                                       itemPosition : Int = 1,
                                       ratePosition : Int = 2,
                                       timeStampPosition : Int = 3
        ): HashMap<Int, HashMap<Int, SimpleEntry<Float, Long>>>
        {
            val localMap = HashMap<Int, HashMap<Int, SimpleEntry<Float, Long>>>()
            var pattern: Array<String?>
            val bufferedReader: BufferedReader = File(fileIn).bufferedReader()
            val lineList = mutableListOf<String>()
            bufferedReader.useLines { lines -> lines.forEach { lineList.add(it) } }
            lineList.forEach {
                pattern = it.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val userID = Integer.parseInt(pattern[userPosition])
                val itemID = Integer.parseInt(pattern[itemPosition])
                val rate = java.lang.Float.parseFloat(pattern[ratePosition])
//                val timeStamp = Integer.parseInt(pattern[timeStampPosition])/ (1000*60*60*24)
                val timeStamp = java.lang.Long.parseLong(pattern[timeStampPosition])
                val pair : SimpleEntry<Float, Long> = SimpleEntry(rate,timeStamp)
                var votes: HashMap<Int, SimpleEntry<Float, Long>>? = localMap[userID]
                if (votes == null) {
                    votes = HashMap()
                }
                votes.put(itemID, pair)
                localMap.put(userID, votes)
            }
            return localMap
        }

        fun loadMulticriteriaRatingsFile(fileIn: String,
                            separator : String = "\t",
                            userPosition: Int = 0,
                            itemPosition : Int = 1,
                            firstRatePosition : Int = 2,
                            numberOfCriteria : Int = 3
        ): Map<Int, Map<Int, FloatArray>>
        {
            val localMap = HashMap<Int, MutableMap<Int, FloatArray>>()
            var pattern: Array<String?>
            val bufferedReader: BufferedReader = File(fileIn).bufferedReader()
            val lineList = mutableListOf<String>()
            bufferedReader.useLines { lines -> lines.forEach { lineList.add(it) } }
            lineList.forEach {
                pattern = it.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val userID = Integer.parseInt(pattern[userPosition])
                val itemID = Integer.parseInt(pattern[itemPosition])
                val criteria : FloatArray = FloatArray(size =  numberOfCriteria)
                for (i in firstRatePosition .. firstRatePosition + numberOfCriteria - 1){
                    if(pattern[i]!="null"){
                        criteria[i] = (pattern[i])!!.toFloat()
                    }
                }
                var votes: MutableMap<Int, FloatArray>? = localMap[userID]
                if (votes == null) {
                    votes = HashMap()
                }
                votes.put(itemID, criteria)
                localMap.put(userID, votes)
            }
            return localMap
        }

        @JvmOverloads fun loadAttributeFile(fileIn: String,
                                          separator : String = "\t",
                                            itemPosition: Int = 0
        ): Map<Int, ArrayList<Int>>
        {
            val localMap = HashMap<Int, ArrayList<Int>>()
            var pattern: Array<String?>
            val bufferedReader: BufferedReader = File(fileIn).bufferedReader()
            val lineList = mutableListOf<String>()
            bufferedReader.useLines { lines -> lines.forEach { lineList.add(it) } }
            lineList.forEach {
                pattern = it.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val itemID = Integer.parseInt(pattern[itemPosition])
                val features = ArrayList<Int>()
                for (i in 1 .. pattern.size - 1){
                    if(pattern[i]!="null"){
                        features.add(Integer.parseInt(pattern[i]))
                    }
                }
                localMap.put(itemID, features)
            }
            return localMap
        }

        @JvmOverloads fun loadCriteriaRatingsFile(fileIn: String,
                                          separator : String = "\t",
                                          userPosition: Int = 0,
                                          criteriaPosition : Int = 1,
                                          criteriaRatingPosition : Int = 2
        ): Map<Int, Map<Int, Float>>
        {
            val localMap = HashMap<Int, MutableMap<Int, Float>>()
            var pattern: Array<String?>
            val bufferedReader: BufferedReader = File(fileIn).bufferedReader()
            val lineList = mutableListOf<String>()
            bufferedReader.useLines { lines -> lines.forEach { lineList.add(it) } }
            lineList.forEach {
                pattern = it.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val userID = Integer.parseInt(pattern[userPosition])
                val criteriaID = Integer.parseInt(pattern[criteriaPosition])
                val criteriaRating : Float
                if (criteriaRatingPosition!=-1){
                    criteriaRating = java.lang.Float.parseFloat(pattern[criteriaRatingPosition])
                }else{
                    criteriaRating = 1f
                }
                var votes: MutableMap<Int, Float>? = localMap[userID]
                if (votes == null) {
                    votes = HashMap()
                }
                votes.put(criteriaID, criteriaRating)
                localMap.put(userID, votes)
            }
            return localMap
        }
    }
}