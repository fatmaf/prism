package thts.testing.testsuitehelper;

import java.util.ArrayList;

public class TestFileInfo {
    String filename;
    String filelocation;
    String propertiesfile;
    ArrayList<String> filenames;
    int fsp;
    int numDoors=0;
    int numRobots;
    int numGoals;

    public int getNumGoals() {
        return numGoals;
    }

    public void setNumGoals(int numGoals) {
        this.numGoals = numGoals;
    }

    public int getNumRobots() {
        return numRobots;
    }

    public void setNumRobots(int numRobots) {
        this.numRobots = numRobots;
    }

    public int getNumDoors() {
        return numDoors;
    }

    public void setNumDoors(int numDoors) {
        this.numDoors = numDoors;
    }

    public TestFileInfo(String testsLocation, String fn, String propsuffix, String loc, int numModels,int numDoors) {
        filename = fn;
        filelocation = loc;
        filenames = new ArrayList<>();

        for (int numModel = 0; numModel < numModels; numModel++) {
            String modelFileName = testsLocation + fn + numModel + ".prism";
            filenames.add(modelFileName);
        }
        propertiesfile = testsLocation + fn + propsuffix + ".prop";
        this.numDoors = numDoors;


    }
    public TestFileInfo(String testsLocation, String fn, String propsuffix, String loc, int numModels, int fsp,int numDoors) {
        filename = fn;
        filelocation = loc;
        filenames = new ArrayList<>();

        for (int numModel = 0; numModel < numModels; numModel++) {
            String modelFileName = testsLocation + fn + numModel + ".prism";
            filenames.add(modelFileName);
        }
        propertiesfile = testsLocation + fn + propsuffix + ".prop";

        this.fsp = fsp;
        this.numDoors = numDoors;

    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilelocation() {
        return filelocation;
    }

    public void setFilelocation(String filelocation) {
        this.filelocation = filelocation;
    }

    public String getPropertiesfile() {
        return propertiesfile;
    }

    public void setPropertiesfile(String propertiesfile) {
        this.propertiesfile = propertiesfile;
    }

    public ArrayList<String> getFilenames() {
        return filenames;
    }

    public void setFilenames(ArrayList<String> filenames) {
        this.filenames = filenames;
    }


    public int getFsp() {
        return fsp;
    }

    public void setFsp(int fsp) {
        this.fsp = fsp;
    }
}