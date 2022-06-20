/**
 * Etude 7 GeoMapMaker
 * This Program will read in a serires of lines from either command line or a plaintext file and attempt to convert
 * said line to a standard form coordinate. The program attempts to match as many various user inputs as possible
 * including non-standard coordinate formats such as DMS and accounting for varying Cardinal inputs. If the program
 * is successful in converting a line a "marker" will be created which represents one location successfully read.
 * If the program is unsuccessful in converting a line will print an error message "Unable to process: " followed
 * by the best fitting reason for the invalid entry.
 *
 * This program is mainly based around the use of Regex statements attempting to be as inclusive as possible.
 * After all lines have been read in and markers created they will be written to a formatted geojson file "Map example".
 * We then with the newly written file go to https://geojson.io/#map=2/20.0/0.0 to visualise the markers on our map.
 *
 * Once in geojson.io go open->file->Map example.geojson and you will see the formatted
 * file put the markers on the map.
 *
 *Imports are used for file reading/writing, regex patterns, and absolute value finding
 *
 * @Author Luka Didham
 * */
import java.io.File
import java.io.FileWriter
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

//Global arraylist holding each individual location referred to as "markers" on the map
var geoJSONMarkerArray: ArrayList<String> = arrayListOf<String>()

/**
 * Main method which gives the user 3 options for map creation,
 * 1) Command line entry
 * 2) Example inputs from brief
 * 3) Exhaustive testing file
 * The main method loops while input is avaliable calling "inputChecker()" method which is where the guts of the program
 * lies. Main returns the success/failure of each individual line with true = success and false = failure in the command
 * line. After all lines read in the main calls createMap() which adds all markers to the map and writes to geojson file.
 */
fun main() {
  println("Please choose scanning option by entering int 1-3\n1: Command line input\n2: Example Inputs.txt\n3: Test Inputs")
  val optionScanner = Scanner(System.`in`)
  var option:Int = optionScanner.nextInt()
  if(option==1){
    val inputScanner = Scanner(System.`in`)
    println("input coordinates")
    while(inputScanner.hasNextLine()) {
      println(inputChecker(inputScanner.nextLine()))
    }
    createMap()
  }

  if(option==2){
    var count = 1
    val inputFile = File("exampleInputs.txt")
    val inputScanner = Scanner(inputFile)
    while(inputScanner.hasNextLine()) {
      println("Example" + count + ": " + inputChecker(inputScanner.nextLine()))
      count++
    }
    createMap()
  }

  if(option==3){
    var count = 1
    val inputFile = File("testInputs.txt")
    val inputScanner = Scanner(inputFile)
    while(inputScanner.hasNextLine()) {
      println("Test" + count + ": " + inputChecker(inputScanner.nextLine()))
      count++
    }
    createMap()
  }

  }

/**
 * CreateMap() is called after all lines have been read and converted into markers within the geoJSONMarkerArray.
 * CreateMap() then loops through said array writing all markers with appropriate geojson formatting to a raw Sting
 * which is then added to the header and footer og the geojson file (also as a raw string) and the entire final
 * raw string "geoJSONOutput" is then written to the file "Map example.geojson" with the raw string retaining formatting.
 */
fun createMap() {
  var allMarkers:String = ""
  for(marker in geoJSONMarkerArray){
    allMarkers+= marker
    allMarkers+=","
    }
  allMarkers = allMarkers.dropLast(1) //Last extra comma
  var geoJSONOutput: String= """{
  "type": "FeatureCollection",
  "features": [$allMarkers]
}"""
  var writer = FileWriter("Map example.geojson")
  writer.write(geoJSONOutput)
  writer.close()
  println("File Writing Complete!");
}
fun createMarker(longitude:Double, latitude:Double):Boolean{
  val format = DecimalFormat("0.000000")
  var longitudeOutput = format.format(loopLongitude(longitude))
  var latitudeOutput = format.format(loopLatitude(latitude))
  geoJSONMarkerArray.add("""{
      "type": "Feature",
      "properties": {},
      "geometry": {
        "type": "Point",
        "coordinates": [
          $longitudeOutput,
          $latitudeOutput
        ]
      }
    }""")
  return true
}

/**
 * Create marker takes in a name, longitude and latitude assumed to represent a location. This method first adjusts the
 * decimal format and checks if the Longitude and Latitude exceeds standard form limits of 90.000000 and 180.000000
 * respectivly. If they do exceed these levels the vales are looped representing a full rotation of the earth. After
 * these adjustments the final values are written to a string array geoJSONMarkerArray holding the markers values
 * and geojson specific formatting.
 * @param name represents name of the marker
 * @param longitude represents standard form longitude coordinate of marker
 * @param latitude represents standard form latitude coordinate of marker
 * @return returns true if marker successfully added to array
 */
fun createMarker(name: String, longitude:Double, latitude:Double):Boolean{
  val format = DecimalFormat("0.000000")
  var longitudeOutput = format.format(loopLongitude(longitude))
  var latitudeOutput = format.format(loopLatitude(latitude))
  geoJSONMarkerArray.add("""{
      "type": "Feature",
      "properties": {
        "marker-color": "#7e7e7e",
        "marker-size": "medium",
        "marker-symbol": "",
        "Name": "$name"
      },
      "geometry": {
        "type": "Point",
        "coordinates": [
          $longitudeOutput,
          $latitudeOutput
        ]
      }
    }
  """)
  return true
}

/**
 * inputChecker is the guts of the program doing the majority of the data checking. The method is based around 5 regex
 * patterns in which we use matchers to find viable coordiates out of inconsistent user input. We can find standard, DMS
 * and cardinal inputs in nearly any mixed order and is very robust to extra spaces, random characters etc. The method will loop
 * through one line of user input at a time. When a viable longitude, latitude and label[optional] are found createMarker()
 * is called adding the relevant information to the marker array.
 * @param input is a string which represents the user entered string
 * @return true or false if line can be successfully converted to a marker
 */
fun inputChecker(input: String): Boolean{
  var longitude:Double = Double.NaN
  var latitude:Double = Double.NaN
  var label:String = ""
  var inputString = input
  var fullStandardPattern = Pattern.compile("-?([0-9]+\\.)?[0-9]+\\s+-?([0-9]+\\.)?[0-9]+\\s*([A-Za-z]+)?\\s*")
  var standardPattern = Pattern.compile("-?([0-9]+\\.)?[0-9]+(\\s*[NWSE])?")
  var minutesPattern = Pattern.compile("([0-9]+\\.)?[0-9]+\\s*[°d\\s+]\\s*([0-9]+\\.)?[0-9]+\\s*['m]?\\s*([NWSE])?\\s*")
  var secondsPattern = Pattern.compile("([0-9]+\\.)?[0-9]+\\s*[°d\\s+]\\s*([0-9]+\\.)?[0-9]+\\s*['m\\s+]\\s*([0-9]+\\.)?[0-9]+\\s*[\"s]?\\s*([NWSE])?\\s*")
  var labelPattern = Pattern.compile("[a-zA-z]+")

    var fullstandardMatcher = fullStandardPattern.matcher(inputString)
    if(fullstandardMatcher.matches()){
      var coorString = fullstandardMatcher.group()
      var coorArray = coorString.split(" ")
      latitude = coorArray[0].toDouble()
      longitude = coorArray[1].toDouble()
      if(coorArray.size>2) {
        label = coorArray[2]
      }
      if(coorArray.contains("N")){
        var temp = latitude
        latitude = longitude
        longitude = temp
      }
      if(coorArray.contains("S")){
        var temp = latitude
        latitude = longitude * -1.0
        longitude = temp
      }
      if(coorArray.contains("W")){
          longitude *= -1.0
      }
    }else {

      var secondsMatcher = secondsPattern.matcher(inputString)
      while (secondsMatcher.find()) {
        var coorString = secondsMatcher.group()
        inputString = inputString.replaceFirst(coorString, "")
        var coorArray = coorString.split("[°d\\s'm\"s[NWSE]?]+".toRegex())
        var degrees = coorArray[0].toDouble()
        var minutes = coorArray[1].toDouble()
        var seconds = coorArray[2].toDouble()
        if (coorString.contains("[NWSE]".toRegex())) {
          if (coorString.contains('N')) {
            if (latitude.isNaN()) {
              latitude = secondConvert(degrees, minutes, seconds)
            } else {
              longitude = latitude
              latitude = secondConvert(degrees, minutes, seconds)
            }
          }
          if (coorString.contains('S')) {
            if (latitude.isNaN()) {
              latitude = secondConvert(degrees, minutes, seconds) * -1.0
            } else {
              longitude = latitude
              latitude = secondConvert(degrees, minutes, seconds) * -1.0
            }
          }
          if (coorString.contains('E')) {
            if (longitude.isNaN()) {
              longitude = secondConvert(degrees, minutes, seconds)
            } else {
              errorFound(input, "Conflicting Cardinal Inputs")
              return false
            }
          }
          if (coorString.contains('W')) {
            if (longitude.isNaN()) {
              longitude = secondConvert(degrees, minutes, seconds) * -1.0
            } else {
              errorFound(input, "Conflicting Cardinal Inputs")
              return false
            }
          }
        } else {
          if (latitude.isNaN()) {
            latitude = secondConvert(degrees, minutes, seconds)
          } else if (longitude.isNaN()) {
            longitude = secondConvert(degrees, minutes, seconds)
          } else {
            errorFound(input, "Too many Coordinates found")
            return false
          }
        }
      }

      var minutesMatcher = minutesPattern.matcher(inputString)
      while (minutesMatcher.find()) {
        var coorString = minutesMatcher.group()
        inputString = inputString.replaceFirst(coorString, "")
        var coorArray = coorString.split("[°d\\s'm[NWSE]?]+".toRegex())
        var degrees = coorArray[0].toDouble()
        var minutes = coorArray[1].toDouble()
        if (coorString.contains("[NWSE]".toRegex())) {
          if (coorString.contains('N')) {
            if (latitude.isNaN()) {
              latitude = minuteConvert(degrees, minutes)
            } else {
              longitude = latitude
              latitude = minuteConvert(degrees, minutes)
            }
          }
          if (coorString.contains('S')) {
            if (latitude.isNaN()) {
              latitude = minuteConvert(degrees, minutes) * -1.0
            } else {
              longitude = latitude
              latitude = minuteConvert(degrees, minutes) * -1.0
            }
          }
          if (coorString.contains('E')) {
            if (longitude.isNaN()) {
              longitude = minuteConvert(degrees, minutes)
            } else {
              errorFound(input, "Too many Coordinates found")
              return false
            }
          }
          if (coorString.contains('W')) {
            if (longitude.isNaN()) {
              longitude = minuteConvert(degrees, minutes) * -1.0
            } else {
              errorFound(input, "Too many Coordinates found")
              return false
            }
          }
        } else {
          if (latitude.isNaN()) {
            latitude = minuteConvert(degrees, minutes)
          } else if (longitude.isNaN()) {
            longitude = minuteConvert(degrees, minutes)
          } else {
            errorFound(input, "Too many Coordinates found")
            return false //having too many matches
          }
        }
      }


      var standardMatcher = standardPattern.matcher(inputString)
      while (standardMatcher.find()) {
        var coorString = standardMatcher.group()
        inputString = inputString.replaceFirst(coorString, "")
        var coorArray = coorString.split("[\\s[NWSE]?]+".toRegex())
        var standard = coorArray[0].toDouble()
        if (coorString.contains("[NWSE]".toRegex())) {
          if (coorString.contains('N')) {
            if (latitude.isNaN()) {
              latitude = standard
            } else {
              longitude = latitude
              latitude = standard
            }
          }
          if (coorString.contains('S')) {
            if (latitude.isNaN()) {
              latitude = standard * -1.0
            } else {
              longitude = latitude
              latitude = standard * -1.0
            }
          }
          if (coorString.contains('E')) {
            if (longitude.isNaN()) {
              longitude = standard
            } else {
              errorFound(input, "Too many Coordinates found")
              return false
            }
          }
          if (coorString.contains('W')) {
            if (longitude.isNaN()) {
              longitude = standard * -1.0
            } else {
              errorFound(input, "Too many Coordinates found")
              return false
            }
          }
        } else {
          if (latitude.isNaN()) {
            latitude = standard
          } else if (longitude.isNaN()) {
            longitude = standard
          } else {
            errorFound(input, "Too many Coordinates found")
            return false //having too many matches
          }
        }
      }

      var labelMatcher = labelPattern.matcher(inputString)
      if (labelMatcher.find()) {
        label = labelMatcher.group()
      }
    }

  if(!latitude.isNaN()&&!longitude.isNaN()&& label.isNotEmpty()) {
    return (createMarker(label, longitude, latitude))
  }else if((!latitude.isNaN()&&!longitude.isNaN())){
    return (createMarker(longitude, latitude))
  }else{
    errorFound(input, "Unable to convert input into Longitude, Latitude and optional Label")
    return false
  }
}

/**
 * secondConvert() method takes in a entry in degrees, minutes, and seconds formats and converts it to standard form.
 * @param degrees degrees value
 * @param minutes minutes value
 * @param seconds seconds value
 * @return standard form double
 */
fun secondConvert(degrees: Double, minutes: Double, seconds:Double):Double{
  return (degrees+minutes/60+seconds/3600)
}
/**
 * minuteConvert() method takes in a entry in degrees and minutes formats and converts it to standard form.
 * @param degrees degrees value
 * @param minutes minutes value
 * @return standard form double
 */
fun minuteConvert(degrees: Double, minutes: Double):Double{
  return (degrees+minutes/60)
}

/**
 * General error message which is called when line can not be converted to a marker. Provides specific error message
 * and user line which failed.
 * @param offendingLine user entered line which failed
 * @param specificError closet fitting reason for failure in conversion
 */
fun errorFound(offendingLine:String, specificError:String){
  println("Unable to process [$offendingLine]: $specificError")
}

/**
 * Method which loops latitude when value exceeds 90.000000 or -90.000000. Deals with this data by looping the value around
 * the world until valid value is found.
 * @param latitude prelooped value
 * @return double representing looped value within 90.000000--90.000000
 */
fun loopLatitude(latitude: Double):Double{
  var lat = latitude
  if(lat>90.000000 || lat<90.000000) {
    var loopCount:Int = abs((lat / 90.0).toInt())
    var remainder = lat % 90.000000
    lat = remainder
    while(loopCount>0){
      lat*= -1.0
      loopCount--
    }
    return lat
  }else{
    return lat
  }
  }
/**
 * Method which loops longitude when value exceeds 180.000000 or -180.000000. Deals with this data by looping the value around
 * the world until a valid value is found.
 * @param longitude prelooped value
 * @return double representing looped longitude value within 180.000000--180.000000
 */
fun loopLongitude(longitude: Double):Double{
  var long = longitude
  if(long>180.000000 || long<180.0000000) {
    var loopCount:Int = abs((long / 180.0000000).toInt())
    var remainder = long % 180.0000000
    long = remainder
    while(loopCount>0){
      long*= -1.0
      loopCount--
    }
    return long
  }else{
    return long
  }
}









