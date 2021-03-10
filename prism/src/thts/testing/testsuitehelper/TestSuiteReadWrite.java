package thts.testing.testsuitehelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;

public class TestSuiteReadWrite {
    static String delim = ",";
    public String id;
    public String location;
    public int numModels;
    public int numProps;
    public ArrayList<String> modelFiles;
    public ArrayList<String> propertiesFiles;
    public int numGoals;
    public int numRobots;
    public ArrayList<Integer> robotsList;
    public ArrayList<Integer> goalsList;
    public int numdoors;
    public int fsp;
    public long timeInMS;


    @Override
    public int hashCode()
    {
        final int prime = 17;
        int result = 1;
        result = prime * result + this.id.hashCode();
        result = prime * result + this.numModels;
        result = prime * result + this.numProps;
        result = prime * result + this.numRobots;
        result = prime * result + this.numGoals;
        result = prime * result + this.numdoors;
        result = prime * result + this.fsp;
        result = prime * result + this.robotsList.hashCode();
        result = prime * result + this.goalsList.hashCode();



        return result;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        else {
            if (o instanceof TestSuiteReadWrite) {
                TestSuiteReadWrite t2 = ((TestSuiteReadWrite) o);
                //a test is equal to another if
                //the id, location, nummodels, numprops, numgoals, numrobots, robotslist, goalslist, numdoors and fsp are the same
                if (this.id.contentEquals(t2.id)) {
                    if (this.numModels == t2.numModels) {
                        if (this.numProps == t2.numProps) {
                            if (this.numRobots == t2.numRobots) {
                                if (this.numGoals == t2.numGoals) {
                                    if (this.numdoors == t2.numdoors) {
                                        if (this.fsp == t2.fsp) {
                                            if (this.robotsList.equals(t2.robotsList)) {
                                                if (this.goalsList.equals(t2.goalsList)) {
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            } else {
                return false;
            }
        }

    }


    public void newPropertiesFile(String folderloc) {
        String fn = null;
        for (String p : propertiesFiles) {
            if (p.endsWith(".prop")) {
                fn = p;
//						break;
            }
            if (p.contains("mult.prop")) {
                fn = null;
                break;
            }

        }
        if (fn != null) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(folderloc + fn));
                String line;
                line = br.readLine();

                String fnn = fn.replace(".prop", "mult.prop");
                PrintWriter writer = new PrintWriter(folderloc + fnn);
                String updatedLine = null;
                while (line != null) {
                    updatedLine = line.replace("partial(", "");
                    updatedLine = updatedLine.replace("],", "]\n");
                    updatedLine = updatedLine.replace("])", "]");

//							System.out.println(line);
//							System.out.println(updatedLine);
                    writer.println(updatedLine);
                    line = br.readLine();
                }

                br.close();
                writer.close();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    ArrayList<Integer> splitToList(String line, String delim) {
        String uline = line.replace("\"[\'[", "");
        uline = uline.replace("]\']\"", "");
        String[] numbers = uline.split(delim);
        ArrayList<Integer> numbersList = new ArrayList<>();
        if (!uline.contentEquals("[]")) {
            for (String s : numbers) {
                numbersList.add(Integer.parseInt(s));
            }
        }
        return numbersList;
    }

    int splitToListFromStringList(String[] line, String delim, int startIndex, ArrayList<Integer> list) {
        int endIndex = startIndex;
        int currentIndex = startIndex;
        //start at index index startindex
        //keep going till you get to ]
        //then return that index
        if (line[currentIndex].contains("\"[\'[")) {
            while (!line[currentIndex].contains("]\']\"")) {
                String num = line[currentIndex].replace("\"[\'[", "");
                list.add((Integer.parseInt(num)));
                currentIndex = currentIndex + 1;
                if (currentIndex > line.length)
                    break;
            }
            if (currentIndex < line.length) {
                String num = line[currentIndex].replace("]\']\"", "");
                list.add((Integer.parseInt(num)));
                //currentIndex = currentIndex + 1;

            }
        }
        endIndex = currentIndex;
        return endIndex;
    }

    public void parseTestFile(String line) {
        String[] split = line.split(delim);
        //filename = 1,
        String fn = split[1];
        // String robots = split[2];
        robotsList = new ArrayList<>();
        int nextVal = splitToListFromStringList(split, delim, 2, robotsList);
        goalsList = new ArrayList<>();
        nextVal = splitToListFromStringList(split, delim, nextVal + 1, goalsList);
        if (nextVal + 1 < split.length) {
            numdoors = Integer.parseInt(split[nextVal + 1]);
        } else
            numdoors = 0;
        nextVal++;
        nextVal++;
        //fsp and actualTime in ms
        fsp = Integer.parseInt(split[nextVal++]);
        timeInMS = Long.parseLong(split[nextVal++]);
        id = fn;
        numRobots = robotsList.size();
        numGoals = goalsList.size();


    }

    public String getString() {
        String toret = id + delim + location + delim + modelFiles.size() + delim + propertiesFiles.size() + delim + modelFiles.toString() + delim + propertiesFiles.toString();
        toret += delim + numRobots + delim + numGoals + delim + robotsList.toString() + delim + goalsList.toString() + delim + numdoors + delim + fsp + delim + timeInMS;
        return toret;
    }

    public static String getCSVHeader() {
        String toret = "TestID" + delim + "TestLocation" + delim + "NumModelFiles" + delim + "NumPropFiles" + delim + "ModelFiles" + delim + "PropFiles" +
                delim + "NumRobots" + delim + "NumGoals" + delim + "Robots" + delim + "Goals" + delim + "NumDoors" + delim + "FSP" + delim + "TimeInMS";
        return toret;
    }

    public void findTestFiles(String loc) {
        Stack<File> folders = new Stack<>();
        File path = new File(loc);
        folders.add(path);
        while (!folders.isEmpty()) {
            File fs = folders.pop();
            for (File f : fs.listFiles()) {
                if (f.isFile()) {
                    String fname = f.getName();
                    if (fname.contains(id)) {
                        if (fname.endsWith(".prism") || fname.endsWith(".prop") || fname.endsWith(".props")) {
                            if (location == null) {
                                location = f.getAbsolutePath().replace(fname, "");
                            } else {
                                if (!location.contentEquals(f.getAbsolutePath().replace(fname, ""))) {
                                    System.out.println("Error!!!! Multiple test files: " + location + " , " + f.getAbsolutePath().replace(fname, ""));
                                    if ((f.getAbsolutePath().contains("smallerwhdoors"))) {
                                        location = f.getAbsolutePath().replace(fname, "");
                                        System.out.println("Replacing with " + location);
                                    }
                                }
                            }
                            if (modelFiles == null) {
                                modelFiles = new ArrayList<>();
                            }
                            if (propertiesFiles == null) {
                                propertiesFiles = new ArrayList<>();
                            }
                            if (fname.endsWith(".prism")) {
                                modelFiles.add(f.getName());
                            } else {

                                newPropertiesFile(location);
                                propertiesFiles.add(f.getName());
                            }
                        }
                    }
                } else {
                    if (!f.getName().contains("compare") && !f.getName().contains("old") && !f.getName().contains("results") &&
                            !f.getName().contains("visualisation") && !f.getName().contains("guiFiles") &&
                            !f.getName().contains("xaiTests") && !f.getName().contains(".idea") && !f.getName().contains(".ipynb"))
                        folders.add(f);
                }
            }
        }
    }
}