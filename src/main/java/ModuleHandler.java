import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Each module information is an ArrayList(size: 2. Because 2 semester) of ArrayList except member "weeks".
 * Semester 1                       : Semester 2
 * ArrayList of available classes   : ArrayList of available classes
 * <br>
 * Indexes of the ArrayList matches. For eg, classNumber.get(semester).get(0) belongs to lessonType.get(semester).get(0)
 * held on day.get(semester).get(0) at startTime.get(semester).get(0) and endTime.get(semester).get(0)
 * <br>
 * "weeks" is an ArrayList(size: 2. Because 2 semester) of a 2d ArrayList:
 * <br>
 * Eg: week[0].get(2) = [1, 3, 4, 5] corresponds to:
 * Semester 1, class 2 has lessons in week 1, 3, 4, 5.
 */
public class ModuleHandler {
    JsonArray semesterData;
    private ArrayList<ArrayList<String>> classNumber; // can be repeated.
    private ArrayList<ArrayList<String>> startTime;
    private ArrayList<ArrayList<String>> endTime;
    private ArrayList<ArrayList<String>> lessonType; //tutorial, lecture etc
    private ArrayList<ArrayList<String>> day; // one day per classNumber
    private ArrayList<ArrayList<ArrayList<String>>> weeks;
    private String moduleName;

    public ModuleHandler(String moduleName) {
        this.moduleName = moduleName;
        this.classNumber = new ArrayList<>();
        this.startTime = new ArrayList<>();
        this.endTime = new ArrayList<>();
        this.lessonType = new ArrayList<>();
        this.weeks = new ArrayList<>();
        this.day = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            this.classNumber.add(new ArrayList<>());
            this.startTime.add(new ArrayList<>());
            this.endTime.add(new ArrayList<>());
            this.lessonType.add(new ArrayList<>());
            this.day.add(new ArrayList<>());
            this.weeks.add(new ArrayList<>());
        }
    }

    public static void main(String[] args) {
        ModuleHandler myModuleHandler = new ModuleHandler("CG1111");
        myModuleHandler.generateModule();

        System.out.println(myModuleHandler.getClassNumber());
        System.out.println(myModuleHandler.getStartTime());
        System.out.println(myModuleHandler.getEndTime());
        System.out.println(myModuleHandler.getLessonType());
        System.out.println(myModuleHandler.getWeeks());
    }

    // TODO split code into smaller methods.

    /**
     * Generate an ArrayList of module information- classNumber, lessonType, startTime, endTime, day, weeks.
     */
    public void generateModule() {
        ModuleApiParser myModuleApiParser = new ModuleApiParser(moduleName);
        try {
            semesterData = myModuleApiParser.parse();
        } catch (IOException e) {
            System.out.println("Unable to access api, using auxiliary module data");
        }

        //TODO SET UP FAKE DATA HERE IF UNABLE TO ACCESS API
        for (int i = 0; i < semesterData.size(); i++) {
            JsonObject semesterDataObj = semesterData.get(i).getAsJsonObject();
            // get semester number from json
            Integer semester = Integer.parseInt(semesterDataObj.get("semester").toString()) - 1;
            if (!(semester == 0 || semester == 1)) {
                continue;
            }
            JsonArray timetable = (JsonArray) semesterDataObj.get("timetable");
            for (int k = 0; k < timetable.size(); k++) { // For each classes
                JsonObject lesson = timetable.get(k).getAsJsonObject();
                // replaceAll() trims the quotes left behind by json parsing via regex
                this.classNumber.get(semester).add(lesson.get("classNo").toString().replaceAll("^.|.$", ""));
                this.lessonType.get(semester).add(lesson.get("lessonType").toString().replaceAll("^.|.$", ""));
                this.startTime.get(semester).add(lesson.get("startTime").toString().replaceAll("^.|.$", ""));
                this.endTime.get(semester).add(lesson.get("endTime").toString().replaceAll("^.|.$", ""));
                this.day.get(semester).add(lesson.get("day").toString().replaceAll("^.|.$", ""));
                JsonArray weeksJsonArray = (JsonArray) lesson.get("weeks");
                ArrayList<String> weeksData = new ArrayList<>();
                for (int j = 0; j < weeksJsonArray.size(); j++) {
                    weeksData.add(weeksJsonArray.get(j).toString());
                }
                this.weeks.get(semester).add(weeksData);
            }
        }
    }

    public ArrayList<ArrayList<String>> getClassNumber() {
        return this.classNumber;
    }

    public ArrayList<ArrayList<String>> getStartTime() {
        return this.startTime;
    }

    public ArrayList<ArrayList<String>> getEndTime() {
        return this.endTime;
    }

    public ArrayList<ArrayList<String>> getLessonType() {
        return this.lessonType;
    }

    public ArrayList<ArrayList<String>> getDay() {
        return this.day;
    }

    public ArrayList<ArrayList<ArrayList<String>>> getWeeks() {
        return this.weeks;
    }
}
