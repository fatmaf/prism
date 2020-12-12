package thts.testsuitehelper;

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

    int  splitToListFromStringList(String[] line, String delim, int startIndex,ArrayList<Integer> list)
    {
        int endIndex = startIndex;
        int currentIndex = startIndex;
        //start at index index startindex
        //keep going till you get to ]
        //then return that index
        if(line[currentIndex].contains("\"[\'["))
        {
            while(!line[currentIndex].contains("]\']\""))
            {
                String num = line[currentIndex].replace("\"[\'[", "");
                list.add((Integer.parseInt(num)));
                currentIndex = currentIndex+1;
                if(currentIndex > line.length)
                    break;
            }
            if(currentIndex < line.length) {
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
        int nextVal= splitToListFromStringList(split,delim,2,robotsList);
        goalsList = new ArrayList<>();
        nextVal = splitToListFromStringList(split,delim,nextVal+1,goalsList);
        if(nextVal+1 <split.length)
        {
            numdoors = Integer.parseInt(split[nextVal+1]);
        }
        else
            numdoors = 0;
        id = fn;
        numRobots = robotsList.size();
        numGoals = goalsList.size();

    }

    public String getString()
    {
        String toret=id+delim+location+delim+modelFiles.size()+delim+propertiesFiles.size()+delim+modelFiles.toString()+delim+propertiesFiles.toString();
        toret+=delim+numRobots+delim+numGoals+delim+robotsList.toString()+delim+goalsList.toString()+delim+numdoors;
        return toret;
    }

    public static String getCSVHeader()
    {
        String toret = "TestID"+delim+"TestLocation"+delim+"NumModelFiles"+delim+"NumPropFiles"+delim+"ModelFiles"+delim+"PropFiles"+
                delim+"NumRobots"+delim+"NumGoals"+delim+"Robots"+delim+"Goals"+delim+"NumDoors";
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
                            }else
                            {
                                if(!location.contentEquals(f.getAbsolutePath().replace(fname, ""))) {
                                    System.out.println("Error!!!! Multiple test files: " + location + " , " + f.getAbsolutePath().replace(fname, ""));
                                    if((f.getAbsolutePath().contains("smallerwhdoors")))
                                    {
                                        location = f.getAbsolutePath().replace(fname,"");
                                        System.out.println("Replacing with "+location);
                                    }
                                }
                            }
                            if (modelFiles == null) {
                                modelFiles = new ArrayList<>();
                            }
                            if (propertiesFiles == null)
                            {
                                propertiesFiles = new ArrayList<>();
                            }
                            if (fname.endsWith(".prism")) {
                                modelFiles.add(f.getName());
                            }else {

                                propertiesFiles.add(f.getName());
                            }
                        }
                    }
                } else {
                    if(!f.getName().contains("compare") && !f.getName().contains("old") &&!f.getName().contains("results") &&
                            !f.getName().contains("visualisation") && !f.getName().contains("guiFiles") &&
                            !f.getName().contains("xaiTests") && !f.getName().contains(".idea") && !f.getName().contains(".ipynb"))
                        folders.add(f);
                }
            }
        }
    }
}