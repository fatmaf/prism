package thts.testing;

import acceptance.AcceptanceOmega;
import acceptance.AcceptanceType;
import automata.DA;
import explicit.LTLModelChecker;
import explicit.MDP;
import explicit.MDPModelChecker;
import explicit.ProbModelChecker;
import explicit.rewards.ConstructRewards;
import explicit.rewards.MDPRewardsSimple;
import parser.State;
import parser.VarList;
import parser.ast.Expression;
import parser.ast.ExpressionQuant;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.*;
import simulator.ModulesFileModelGenerator;
import thts.modelgens.MultiAgentNestedProductModelGenerator;
import thts.testing.testsuitehelper.TestFileInfo;
import thts.treesearch.configs.Configuration;
import thts.treesearch.configs.RunConfiguration;
import thts.treesearch.utils.THTSRunInfo;
import thts.utils.PolicyCreator;
import thts.vi.MDPValIter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class VIOnSmallExample {

    private String currentDir;
    private String testsLocation;

    public String getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }

    public String getTestsLocation() {
        return testsLocation;
    }

    public void setTestsLocation(String testsLocation) {
        this.testsLocation = testsLocation;
    }

    public String getLogFilesLocation() {
        return logFilesLocation;
    }

    public void setLogFilesLocation(String logFilesLocation) {
        this.logFilesLocation = logFilesLocation;
    }

    public String getResultsLocation() {
        return resultsLocation;
    }

    public void setResultsLocation(String resultsLocation) {
        this.resultsLocation = resultsLocation;
    }

    private String logFilesLocation;
    private String resultsLocation;

    public static void main(String[] args)
    {
        VIOnSmallExample vionsmallexample = new VIOnSmallExample();//.runSmallExample();
    //    vionsmallexample.runPaperSmallExampleVar1();
    //    vionsmallexample.runPaperSmallExampleVar2();
   //     vionsmallexample.runSmallExample();
        vionsmallexample.runGrid();
    }
    public  void runPaperSmallExampleVar1() {

        String resFolderExt = "tro_examples/";
        String filename = "tro_example_paper_var1_";

        String resSuffix = "_investigatingshit_";

        boolean debug = false;


        try {


            run(resFolderExt,
                    2, 4, filename, debug, resSuffix, "_mult", 0, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public  void runPaperSmallExampleVar2() {

        String resFolderExt = "tro_examples/";
        String filename = "tro_example_paper_var2_";

        String resSuffix = "_investigatingshit_";

        boolean debug = true;


        try {


            run(resFolderExt,
                    2, 4, filename, debug, resSuffix, "_mult", 0, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public  void runPaperSmallExample() {

        String resFolderExt = "tro_examples/";
        String filename = "tro_example_paper";

        String resSuffix = "_investigatingshit_";

        boolean debug = true;


        try {


            run(resFolderExt,
                    3, 4, filename, debug, resSuffix, "_mult", 0, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public  void runSmallExample() {

        String resFolderExt = "tro_examples/";
        String filename = "tro_example_new_small";

        String resSuffix = "_investigatingshit_";

        boolean debug = true;


        try {


            run(resFolderExt,
                    2, 3, filename, debug, resSuffix, "_mult", 0, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public  void runGrid() {

        String[] examples= {"r10_g10_a1_grid_5_fsp_0_0_", "r10_g10_a1_grid_5_fsp_10_1_"
                ,"r10_g10_a1_grid_5_fsp_20_2_","r10_g10_a1_grid_5_fsp_30_3_","r10_g10_a1_grid_5_fsp_40_4_"
                ,"r10_g10_a1_grid_5_fsp_50_5_","r10_g10_a1_grid_5_fsp_60_6_","r10_g10_a1_grid_5_fsp_70_7_"
                ,"r10_g10_a1_grid_5_fsp_80_8_","r10_g10_a1_grid_5_fsp_90_9_","r10_g10_a1_grid_5_fsp_100_0_"};

       // int fsp = 100;
       // int numRobots = 1;
        //int numGoals = 1;
        ArrayList<String> logList = new ArrayList<>();
        String logString = "\nFN\tR\tG\tFSP\tStates\tTransitions\tChoices\tTime(ms)";
        logList.add(logString);
        int[] fsps = {0,30,60,90};
        int[] numRobots_choices= {1,2,3,4};
        int[] numGoals_choices = {1,2,3,4};
        for(int fsp: fsps) {
            for (int numRobots : numRobots_choices) {
                for (int numGoals : numGoals_choices) {

                    String resFolderExt = "grid5/" + fsp + "/";
                    // String filename = "tro_example_new_small";

                    String resSuffix = "_doingVI_";

                    boolean debug = false;


                    String filename = examples[fsp / 10];//r10_g10_a1_grid_5_fsp_0_0_9//"tro_example_new_small";
                    logString = String.format("\n%s\t%d\t%d\t%d",filename,numRobots,numGoals,fsp);

                    try {


                        logString+=run(resFolderExt,
                                numRobots, numGoals, filename, debug, resSuffix, "mult", fsp, 0);

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(String.format("Failed: R %d G %d FSP %d", numGoals, numGoals, fsp));
                        logString+=" FAILED ";
                    }
                    logList.add(logString);
                    for(String l: logList)
                        System.out.print(l);
                    saveLogList(logList);

                }
            }
        }
        for(String l: logList)
            System.out.print(l);

    }

    public void saveLogList(ArrayList<String> logList)
    {
        PrismFileLog out = new PrismFileLog(resultsLocation+"vi.csv");
        for(String l: logList)
            out.print(l);
        out.close();
    }


    public String run(String resFolderExt, int numRobots, int numGoals,
                    String filename, boolean debug,
                    String fnSuffix, String propsuffix,
                    int fsp, int numdoors) throws Exception {
        String logString = "";
        //formaking things pretty
        int numConsoleChars = 80;
        int numCharsSoFar = 0;
        long startTime = System.currentTimeMillis();
        String logFilesExt = "results/configs/" + "vi" + "/";
        String resFileName = filename + "_" + "vi" + fnSuffix;
        initialiseResultsLocations(resFolderExt, logFilesExt);

        TestFileInfo tfi = new TestFileInfo(testsLocation, filename, propsuffix, testsLocation,
                numRobots, fsp, numdoors);
        ArrayList<Integer> robotArr = new ArrayList<>();
        for(int r = 0; r<numRobots; r++)
            robotArr.add(r);
        ArrayList<Integer> goalArr = new ArrayList<>();
        for(int g = 0; g<numGoals; g++)
            goalArr.add(g);
        tfi.setRobots(robotArr);
        tfi.setGoals(goalArr);
        PrismLog mainLog;
        if (debug)
            mainLog = new PrismFileLog(logFilesLocation + filename+"_debuglog_vi" + ".txt");
        else
            mainLog = new PrismDevNullLog();


        Prism prism = new Prism(mainLog);
        PrismLog fileLog;

        fileLog = new PrismFileLog(logFilesLocation +filename+ "_log_vi" + ".txt");

        prism.initialise();
        prism.setEngine(Prism.EXPLICIT);

        MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog, tfi.getFilenames(),
                tfi.getPropertiesfile(), logFilesLocation,
                tfi.getNumDoors(), tfi.getRobots(), tfi.getGoals());

        prism.loadModelGenerator(maModelGen);
        prism.buildModel();
        MDP mdp = (MDP) prism.getBuiltModelExplicit();
        mdp.exportToDotFile(resultsLocation+"/"+filename+"mdp.dot");
        fileLog.println("MDP initial state "+mdp.getFirstInitialState()+" "+mdp.getStatesList().get(mdp.getFirstInitialState()));
        //VarList vl = maModelGen.createVarList();
        mdp.exportStates(0,       mdp.getVarList(),fileLog);

        mdp.exportToPrismExplicitTra(fileLog);

        System.out.println(mdp.infoStringTable());
        logString+=String.format("\t%d\t%d\t%d",mdp.getNumStates(),mdp.getNumTransitions(),mdp.getNumChoices());
        List<State> statesList = mdp.getStatesList();
        BitSet accStates = new BitSet();
        BitSet avoidStates = new BitSet();
        for (int i = 0; i < statesList.size(); i++) {
            State s = statesList.get(i);
            if (maModelGen.isAccState(s))
                accStates.set(i);
            if (maModelGen.isAvoidState(s))
                avoidStates.set(i);

        }
        mainLog.println("AccStates:" + accStates.toString());
        mainLog.println("AvoidStates:" + avoidStates.toString());
        explicit.ProbModelChecker pmc = new ProbModelChecker(prism);


        MDPModelChecker mdpmc = new MDPModelChecker(pmc);
        ConstructRewards constructRewards = new ConstructRewards(pmc);

        MDPRewardsSimple tasksModel = (MDPRewardsSimple) constructRewards.buildRewardStructure(mdp,
                (RewardGenerator) maModelGen, -1);


        MDPRewardsSimple costsModel = (MDPRewardsSimple) constructRewards.buildRewardStructure(mdp,
                (RewardGenerator) maModelGen, 0);


        //printing out the task rewards for a particular state


        MDPValIter vi = new MDPValIter();
        ArrayList<MDPRewardsSimple> rewardsList = new ArrayList<>();
        rewardsList.add(tasksModel);
        rewardsList.add(costsModel);
        ArrayList<Boolean> minRewards = new ArrayList<>();
        minRewards.add(false);
        minRewards.add(true);
        BitSet remain = (BitSet) avoidStates.clone();
        remain.flip(0, mdp.getNumStates());
        BitSet statesToIgnoreForVI = (BitSet) avoidStates.clone();
        statesToIgnoreForVI.or(accStates);

        vi.debug=false;
        MDPValIter.ModelCheckerMultipleResult result = vi.computeNestedValIterArray(mdpmc, mdp, accStates, remain,
                rewardsList, null, minRewards, statesToIgnoreForVI, 1, null, mainLog);

        System.out.println("Probability: " + result.solns.get(0)[mdp.getFirstInitialState()]);
        System.out.println("Task Completition: " + result.solns.get(1)[mdp.getFirstInitialState()]);
        System.out.println("Cost: " + result.solns.get(2)[mdp.getFirstInitialState()]);

        PolicyCreator pc = new PolicyCreator();
//		pc.createPolicy(mdp, result.strat);
        ArrayList<Double> resVals = pc.createPolicyPrintValues(mdp, result, fileLog);
        pc.savePolicy(resultsLocation, filename+"vires");

        mainLog.close();
        fileLog.close();
        prism.closeDown();
        long endTime = System.currentTimeMillis();
        logString+=String.format("\t%d",endTime-startTime);
        return logString;

    }

    public MultiAgentNestedProductModelGenerator createNestedMultiAgentModelGen(Prism prism, PrismLog mainLog,
                                                                                ArrayList<String> filenames,
                                                                                String propertiesFileName,
                                                                                String resultsLocation, int numDoors, ArrayList<Integer> robotsList,
                                                                                ArrayList<Integer> goalsList)
            throws PrismException, IOException {

        AcceptanceType[] allowedAcceptance = {AcceptanceType.RABIN, AcceptanceType.REACH};

        String initString = "Initial States: [ ";
        // step 1
        // create the modulesfilemodelgenerators
        ArrayList<ModulesFileModelGenerator> mfmodgens = new ArrayList<>();
        ModulesFile modulesFile = null; // just here so we can use the last modules file for our properties

        for (int i = 0; i < filenames.size(); i++) {
            if (robotsList != null) {
                if (!robotsList.contains(i))
                    continue;
            }
            String modelFileName = filenames.get(i);
            mainLog.println("Loading model gen for " + modelFileName);
            modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform
            // we might have to find a way to change this later
            ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
            initString += modGen.getInitialState() + " ";
            mfmodgens.add(modGen);
        }
        initString += "]";
        // step 2
        // load all the exprs and remember to check them
        mainLog.println("Loading properties ");
        PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propertiesFileName));
        List<Expression> processedExprs = new ArrayList<>();
        int safetydaind = -1;
        Expression safetyexpr = null;
        if (goalsList != null) {
            if (!goalsList.contains(propertiesFile.getNumProperties() - 1)) {
                goalsList.add(propertiesFile.getNumProperties() - 1);
            }
        }

        for (int i = 0; i < propertiesFile.getNumProperties(); i++) {
            if (goalsList != null) {
                if (!goalsList.contains(i))
                    continue;
            }
            mainLog.println(propertiesFile.getProperty(i));

            // so reward + safety
            boolean isSafeExpr = false;
            Expression exprHere = propertiesFile.getProperty(i);

            Expression daExpr = ((ExpressionQuant) exprHere).getExpression();
            isSafeExpr = !Expression.isCoSafeLTLSyntactic(daExpr, true);
            if (isSafeExpr) {
                if (safetyexpr != null) {
                    // two safety exprs? lets and this stuff
                    // TODO: could this cause problems ? //like if one was min and max since we're
                    // ignoring those
                    safetyexpr = Expression.And(safetyexpr, daExpr);
                } else
                    safetyexpr = daExpr;

            }

            if (!isSafeExpr) {
                processedExprs.add(daExpr);
            }
        }

        // we've got the safety stuff left
        // we need to not it
        if (safetyexpr != null) {
            Expression notsafetyexpr = Expression.Not(safetyexpr);
            safetydaind = processedExprs.size();
            processedExprs.add(notsafetyexpr);
        }
        mainLog.println(initString);
        mainLog.println("Properties " + processedExprs.toString());
//		System.in.read();
        // hmmmm so this is important I guess
        // and we have a single safety da okay
        // oooo reward structures we don't have to care about
        // right o -lets do this
        // for the honor of greyskull

        explicit.LTLModelChecker ltlMC = new LTLModelChecker(prism);

        ArrayList<List<Expression>> labelExprsList = new ArrayList<List<Expression>>();
        ArrayList<DA<BitSet, ? extends AcceptanceOmega>> das = new ArrayList<DA<BitSet, ? extends AcceptanceOmega>>();
        for (int i = 0; i < processedExprs.size(); i++) {
            List<Expression> labelExprs = new ArrayList<Expression>();

            Expression expr = (Expression) processedExprs.get(i);
            // this will need to be changed if you've got different variables accross models
            // then its better to do the v=5 stuff in the prop files and just ignore labels

            expr = (Expression) expr.expandPropRefsAndLabels(propertiesFile, modulesFile.getLabelList());
            DA<BitSet, ? extends AcceptanceOmega> da = ltlMC.constructExpressionDAForLTLFormula(expr, labelExprs,
                    allowedAcceptance);
            da.setDistancesToAcc();

            labelExprsList.add(labelExprs);
            das.add(da);
            mainLog.println("Created DA for " + expr.toString());
        }

        ArrayList<String> sharedStateVars = new ArrayList<String>();
        for (int i = 0; i < numDoors; i++)
            sharedStateVars.add("door" + i);

        MultiAgentNestedProductModelGenerator mapmg = new MultiAgentNestedProductModelGenerator(mfmodgens, das,
                labelExprsList, safetydaind, sharedStateVars);

        return mapmg;
    }

    void initialiseResultsLocations(String resFolderext, String logFilesExt) {
        currentDir = System.getProperty("user.dir");
        if (currentDir.contains("/prism/prism"))
            currentDir = currentDir.replace("/prism/", "/");
        testsLocation = currentDir + "/tests/wkspace/" + resFolderext;
        logFilesLocation = testsLocation + logFilesExt;
        resultsLocation = testsLocation + "results/configs/csvs/";
        createDirIfNotExist(logFilesLocation);
        createDirIfNotExist(resultsLocation);

    }


    public void createDirIfNotExist(String directoryName) {
        File directory = new File(directoryName);
        if (!directory.exists()) {

            directory.mkdirs();
        }

    }
}
