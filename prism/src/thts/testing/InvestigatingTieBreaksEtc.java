package thts.treesearch.configs;




import thts.treesearch.configs.labelled.egreedy.elugreedy.ConfigLeLUGreedy;
import thts.treesearch.configs.labelled.egreedy.elugreedy.ConfigLeLUGreedyRandom;
import thts.treesearch.configs.labelled.greedy.lugreedy.ConfigLluGreedy;
import thts.treesearch.configs.labelled.greedy.lugreedy.ConfigLluGreedyRandom;
import thts.treesearch.configs.plain.egreedy.elugreedy.ConfigeLUGreedy;
import thts.treesearch.configs.plain.egreedy.elugreedy.ConfigeLUGreedyRandom;
import thts.treesearch.configs.plain.greedy.lugreedy.ConfigLUGreedy;
import thts.treesearch.configs.plain.greedy.lugreedy.ConfigLUGreedyRandom;


import java.util.ArrayList;

//PRISM_MAINCLASS=thts.treesearch.configs.InvestigatingTieBreaksEtc prism/bin/prism
public class InvestigatingTieBreaksEtc {

    public static ArrayList<Configuration> getSelectedConfigsLUGreedy(boolean timeBound, boolean dointervalvi, long timeLimit) {
        ArrayList<Configuration> configs = new ArrayList<>();

        Configuration greedyLU_L = new ConfigLluGreedy(false,false,true,false);
        configs.add(greedyLU_L);

        Configuration greedyLURandom_L = new ConfigLluGreedyRandom(false,false,true,false);
        configs.add(greedyLURandom_L);

        Configuration greedyLU_P = new ConfigLUGreedy(false,false,true,false);
        configs.add(greedyLU_P);

        Configuration greedyLURandom_P = new ConfigLUGreedyRandom(false,false,true,false);
        configs.add(greedyLURandom_P);

        Configuration greedyLU_Lx = new ConfigLluGreedy(false,false,true,false);
        ((ConfigLluGreedy)greedyLU_Lx).doGreedyPolActSel();
        configs.add(greedyLU_Lx);

        Configuration greedyLURandom_Lx = new ConfigLluGreedyRandom(false,false,true,false);
        ((ConfigLluGreedyRandom)greedyLURandom_Lx).doGreedyPolActSel();
        configs.add(greedyLURandom_Lx);

        Configuration greedyLU_Px = new ConfigLUGreedy(false,false,true,false);
        ((ConfigLUGreedy)greedyLU_Px).doGreedyPolActSel();
        configs.add(greedyLU_Px);

        Configuration greedyLURandom_Px = new ConfigLUGreedyRandom(false,false,true,false);
        ((ConfigLUGreedyRandom)greedyLURandom_Px).doGreedyPolActSel();
        configs.add(greedyLURandom_Px);


        Configuration greedyLU_Lxy = new ConfigLluGreedy(false,false,true,false,true,false);
        ((ConfigLluGreedy)greedyLU_Lxy).doGreedyPolActSel();
        configs.add(greedyLU_Lxy);

        Configuration greedyLURandom_Lxy = new ConfigLluGreedyRandom(false,false,true,false,true,false);
        ((ConfigLluGreedyRandom)greedyLURandom_Lxy).doGreedyPolActSel();
        configs.add(greedyLURandom_Lxy);

        Configuration greedyLU_Pxy = new ConfigLUGreedy(false,false,true,false,true,false);
        ((ConfigLUGreedy)greedyLU_Pxy).doGreedyPolActSel();
        configs.add(greedyLU_Pxy);

        Configuration greedyLURandom_Pxy = new ConfigLUGreedyRandom(false,false,true,false,true,false);
        ((ConfigLUGreedyRandom)greedyLURandom_Pxy).doGreedyPolActSel();
        configs.add(greedyLURandom_Pxy);


        Configuration greedyLU_Lxyz = new ConfigLluGreedy(false,false,true,false,true,true);
        ((ConfigLluGreedy)greedyLU_Lxyz).doGreedyPolActSel();
        configs.add(greedyLU_Lxyz);

        Configuration greedyLURandom_Lxyz = new ConfigLluGreedyRandom(false,false,true,false,true,
                true);
        ((ConfigLluGreedyRandom)greedyLURandom_Lxyz).doGreedyPolActSel();
        configs.add(greedyLURandom_Lxyz);

        Configuration greedyLU_Pxyz = new ConfigLUGreedy(false,false,true,false,true,
                true);
        ((ConfigLUGreedy)greedyLU_Pxyz).doGreedyPolActSel();
        configs.add(greedyLU_Pxyz);

        Configuration greedyLURandom_Pxyz = new ConfigLUGreedyRandom(false,false,true,false,true,
                true);
        ((ConfigLUGreedyRandom)greedyLURandom_Pxyz).doGreedyPolActSel();
        configs.add(greedyLURandom_Pxyz);

        if (timeBound && timeLimit > 0) {
            for (Configuration config : configs) {
                config.setTimeTimeLimitInMS(timeLimit);
            }
        }
        return configs;
    }


    public static ArrayList<Configuration> getSelectedConfigseLUGreedy(boolean timeBound, boolean dointervalvi, long timeLimit) {
        ArrayList<Configuration> configs = new ArrayList<>();

        Configuration greedyeLU_L = new ConfigLeLUGreedy(false,false,true,false);
        configs.add(greedyeLU_L);

        Configuration greedyeLURandom_L = new ConfigLeLUGreedyRandom(false,false,true,false);
        configs.add(greedyeLURandom_L);

        Configuration greedyeLU_P = new ConfigeLUGreedy(false,false,true,false);
        configs.add(greedyeLU_P);

        Configuration greedyeLURandom_P = new ConfigeLUGreedyRandom(false,false,true,false);
        configs.add(greedyeLURandom_P);

        Configuration greedyeLU_Lx = new ConfigLeLUGreedy(false,false,true,false);
        ((ConfigLeLUGreedy)greedyeLU_Lx).doGreedyPolActSel();
        configs.add(greedyeLU_Lx);

        Configuration greedyeLURandom_Lx = new ConfigLeLUGreedyRandom(false,false,true,false);
        ((ConfigLeLUGreedyRandom)greedyeLURandom_Lx).doGreedyPolActSel();
        configs.add(greedyeLURandom_Lx);

        Configuration greedyeLU_Px = new ConfigeLUGreedy(false,false,true,false);
        ((ConfigeLUGreedy)greedyeLU_Px).doGreedyPolActSel();
        configs.add(greedyeLU_Px);

        Configuration greedyeLURandom_Px = new ConfigeLUGreedyRandom(false,false,true,false);
        ((ConfigeLUGreedyRandom)greedyeLURandom_Px).doGreedyPolActSel();
        configs.add(greedyeLURandom_Px);


        Configuration greedyeLU_Lxy = new ConfigLeLUGreedy(false,false,true,false,true,false);
        ((ConfigLeLUGreedy)greedyeLU_Lxy).doGreedyPolActSel();
        configs.add(greedyeLU_Lxy);

        Configuration greedyeLURandom_Lxy = new ConfigLeLUGreedyRandom(false,false,true,false,true,false);
        ((ConfigLeLUGreedyRandom)greedyeLURandom_Lxy).doGreedyPolActSel();
        configs.add(greedyeLURandom_Lxy);

        Configuration greedyeLU_Pxy = new ConfigeLUGreedy(false,false,true,false,true,false);
        ((ConfigeLUGreedy)greedyeLU_Pxy).doGreedyPolActSel();
        configs.add(greedyeLU_Pxy);

        Configuration greedyeLURandom_Pxy = new ConfigeLUGreedyRandom(false,false,true,false,true,false);
        ((ConfigeLUGreedyRandom)greedyeLURandom_Pxy).doGreedyPolActSel();
        configs.add(greedyeLURandom_Pxy);

        Configuration greedyeLU_Lxyz = new ConfigLeLUGreedy(false,false,true,false,true,true);
        ((ConfigLeLUGreedy)greedyeLU_Lxyz).doGreedyPolActSel();
        configs.add(greedyeLU_Lxyz);

        Configuration greedyeLURandom_Lxyz = new ConfigLeLUGreedyRandom(false,false,true,false,true,true);
        ((ConfigLeLUGreedyRandom)greedyeLURandom_Lxyz).doGreedyPolActSel();
        configs.add(greedyeLURandom_Lxyz);

        Configuration greedyeLU_Pxyz = new ConfigeLUGreedy(false,false,true,false,true,true);
        ((ConfigeLUGreedy)greedyeLU_Pxyz).doGreedyPolActSel();
        configs.add(greedyeLU_Pxyz);

        Configuration greedyeLURandom_Pxyz = new ConfigeLUGreedyRandom(false,false,true,false,true,true);
        ((ConfigeLUGreedyRandom)greedyeLURandom_Pxyz).doGreedyPolActSel();
        configs.add(greedyeLURandom_Pxyz);

        if (timeBound && timeLimit > 0) {
            for (Configuration config : configs) {
                config.setTimeTimeLimitInMS(timeLimit);
            }
        }
        return configs;
    }


    public static void filterConfigs()
    {
        ArrayList<String> selConfigs = new ArrayList<>();
        selConfigs.add("L_LUGreedyRandom_GAllActions_ASBU");
        selConfigs.add("L_LUGreedy_GAllActions_ASBU");
    }
    public static void runSmallExampleSelConfigs() {

        String resFolderExt = "tro_examples/";
        String filename = "tro_example_new_small";
        boolean hasSharedState = true;
        boolean timeBound = false;
        boolean dointervalvi = false;
        int maxRuns = 10;
        boolean debug = true;
        String resSuffix = "_investigatingshit_";

        ArrayList<Configuration> configs = getSelectedConfigsLUGreedy(timeBound, dointervalvi, 0);
        configs.addAll(getSelectedConfigseLUGreedy(timeBound,dointervalvi,0));
        for (int i = 0; i < configs.size(); i++) {
            Configuration config = configs.get(i);
            RunConfiguration runconfig = new RunConfiguration();
            try {

                System.out.println("\n\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + "\n");
                runconfig.run(resFolderExt, config,
                        2, 3, filename, debug, resSuffix, "_mult", maxRuns, 0, 1);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }



    public static void main(String[] args) {


        runSmallExampleSelConfigs();

    }


}
