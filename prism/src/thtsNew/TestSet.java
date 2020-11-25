package thtsNew;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class TestSet {
    String testSetID;
    ArrayList<Test> tests;
    String location;

    public TestSet(String fn) {
        setTestIDFromFileName(fn);
        tests = parseTestSetFile(fn);
    }

    public TestSet(){}
    public void addTest(Test t)
    {
        if(tests==null)
            tests = new ArrayList<>();
        tests.add(t);
    }
    public void setTestSetID(String id)
    {
        testSetID = id;
    }
    public String setTestIDFromFileName(String fn) {
        String[] splitStrings = fn.split("/");

        testSetID = splitStrings[splitStrings.length - 1].split("_")[0];
        return testSetID;
    }

    public String getTestSetID() {
        return testSetID;
    }

    public ArrayList<Test> parseTestSetFile(String fn) {
        ArrayList<Test> testsList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fn));
            String line = br.readLine();
            line = br.readLine();
            while (line != null) {
                Test t = new Test();
//                System.out.println(line);
                t.parseTestFile(line);
                testsList.add(t);
                //each line is a test //except the first line
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return testsList;
    }

    public void findAllTestFiles(String loc)
    {
        for(Test t: tests)
        {
            System.out.println(t.id);
            t.findTestFiles(loc);
        }
    }
    public ArrayList<String> getStrings()
    {
        ArrayList<String> toret = new ArrayList<>();
        for(Test t: tests)
        {

            toret.add(testSetID+Test.delim+t.getString());
        }
        return toret;
    }
}