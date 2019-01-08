@file:JvmName("BPRMF.Utils")

package ItemsAttributesManager

import java.io.BufferedReader
import java.io.File
import java.util.AbstractMap.SimpleEntry

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
        ): HashMap<Int, HashMap<Int, Double>>
        {
            val localMap = HashMap<Int, HashMap<Int, Double>>()
            var pattern: Array<String?>
            val bufferedReader: BufferedReader = File(fileIn).bufferedReader()
            val lineList = mutableListOf<String>()
            bufferedReader.useLines { lines -> lines.forEach { lineList.add(it) } }
            lineList.forEach {
                pattern = it.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val userID = Integer.parseInt(pattern[userPosition])
                val itemID = Integer.parseInt(pattern[itemPosition])
                val rate : Double
                if (ratePosition!=-1){
                    rate = pattern[ratePosition]!!.toDouble()
                }else{
                    rate = 1.0
                }
                var votes: HashMap<Int, Double>? = localMap[userID]
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
        ): HashMap<Int, HashMap<Int, SimpleEntry<Float, Int>>>
        {
            val localMap = HashMap<Int, HashMap<Int, SimpleEntry<Float, Int>>>()
            var pattern: Array<String?>
            val bufferedReader: BufferedReader = File(fileIn).bufferedReader()
            val lineList = mutableListOf<String>()
            bufferedReader.useLines { lines -> lines.forEach { lineList.add(it) } }
            lineList.forEach {
                pattern = it.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val userID = Integer.parseInt(pattern[userPosition])
                val itemID = Integer.parseInt(pattern[itemPosition])
                val rate = java.lang.Float.parseFloat(pattern[ratePosition])
                val timeStamp = Integer.parseInt(pattern[timeStampPosition])/ (1000*60*60*24)
                val pair : SimpleEntry<Float, Int> = SimpleEntry(rate,timeStamp)
                var votes: HashMap<Int, SimpleEntry<Float, Int>>? = localMap[userID]
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
                            numberOfCriteria : Int = 1
        ): HashMap<Int, HashMap<Int, FloatArray>>
        {
            val localMap = HashMap<Int, HashMap<Int, FloatArray>>()
            var pattern: Array<String?>
            val bufferedReader: BufferedReader = File(fileIn).bufferedReader()
            val lineList = mutableListOf<String>()
            bufferedReader.useLines { lines -> lines.forEach { lineList.add(it) } }
            lineList.forEach {
                pattern = it.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val userID = Integer.parseInt(pattern[userPosition])
                val itemID = Integer.parseInt(pattern[itemPosition])
                val criteria : FloatArray = FloatArray(numberOfCriteria)
                var criteriumNumber = 0
                for (i in firstRatePosition .. firstRatePosition + numberOfCriteria - 1){
                    if(pattern[i]!="null"){
                        criteria[criteriumNumber] = (pattern[i])!!.toFloat()
                        criteriumNumber++
                    }
                }
                var votes: HashMap<Int, FloatArray>? = localMap[userID]
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
        ): HashMap<Int, ArrayList<Int>>
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
                        features.add(pattern[i]!!.toInt())
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