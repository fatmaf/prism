package thts.testing.testsuitehelper;

import java.util.ArrayList;

public class TestFileInfo {
    String filename;
    String filelocation;
    String propertiesfile;
    ArrayList<String> filenames;
    boolean hasSharedState;
    int fsp;

    public TestFileInfo(String testsLocation, String fn, String propsuffix, String loc, int numModels, boolean hasSharedState) {
        filename = fn;
        filelocation = loc;
        filenames = new ArrayList<>();

        for (int numModel = 0; numModel < numModels; numModel++) {
            String modelFileName = testsLocation + fn + numModel + ".prism";
            filenames.add(modelFileName);
        }
        propertiesfile = testsLocation + fn + propsuffix + ".prop";
        this.hasSharedState = hasSharedState;

    }
    public TestFileInfo(String testsLocation, String fn, String propsuffix, String loc, int numModels, boolean hasSharedState,int fsp) {
        filename = fn;
        filelocation = loc;
        filenames = new ArrayList<>();

        for (int numModel = 0; numModel < numModels; numModel++) {
            String modelFileName = testsLocation + fn + numModel + ".prism";
            filenames.add(modelFileName);
        }
        propertiesfile = testsLocation + fn + propsuffix + ".prop";
        this.hasSharedState = hasSharedState;
        this.fsp = fsp;

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

    public boolean isHasSharedState() {
        return hasSharedState;
    }

    public void setHasSharedState(boolean hasSharedState) {
        this.hasSharedState = hasSharedState;
    }

    public int getFsp() {
        return fsp;
    }

    public void setFsp(int fsp) {
        this.fsp = fsp;
    }
}