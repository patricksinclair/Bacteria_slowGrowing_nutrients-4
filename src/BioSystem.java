import java.util.ArrayList;
import java.util.Random;

public class BioSystem {

    private int L, K, s, s_max;

    private double c, alpha, timeElapsed;

    private boolean populationDead = false;

    private Microhabitat[] microhabitats;

    Random rand = new Random();

    public BioSystem(int L, int S, double alpha){

        this.L = L;
        this.s = S;
        this.s_max = S;
        this.alpha = alpha;

        this.microhabitats = new Microhabitat[L];
        this.timeElapsed = 0.;

        for(int i = 0; i < L; i++){

            double c_i = Math.exp(alpha*(double)i) - 1.;
            microhabitats[i] = new Microhabitat(c_i, S);
        }
        microhabitats[0].fillWithWildType();
    }

    public int getL(){
        return L;
    }

    public double getTimeElapsed(){
        return timeElapsed;
    }
    public void setTimeElapsed(double timeElapsed){
        this.timeElapsed = timeElapsed;
    }

    public boolean getPopulationDead(){
        return populationDead;
    }


    public int getCurrentPopulation(){
        int runningTotal = 0;

        for(Microhabitat m : microhabitats) {
            runningTotal += m.getN();
        }
        return runningTotal;
    }

    public int[] getSpatialDistributionArray(){
        int[] mh_pops = new int[L];
        for(int i = 0; i < L; i++){
            mh_pops[i] = microhabitats[i].getN();
        }
        return mh_pops;
    }

    public double[] getGrowthRatesArray(){
        double[] mh_gRates = new double[L];
        for(int i = 0; i < L; i++){
            mh_gRates[i] = microhabitats[i].getGrowthRate();
        }
        return mh_gRates;
    }

    public void migrate(int currentL, int bacteriumIndex){

        double direction = rand.nextDouble();

        if(direction < 0.5 && currentL < (L - 1)) {


            ArrayList<Bacteria> source = microhabitats[currentL].getPopulation();
            ArrayList<Bacteria> destination = microhabitats[currentL + 1].getPopulation();

            destination.add(source.remove(bacteriumIndex));

        }else if(direction > 0.5 && currentL > (0)){

            ArrayList<Bacteria> source = microhabitats[currentL].getPopulation();
            ArrayList<Bacteria> destination = microhabitats[currentL - 1].getPopulation();

            destination.add(source.remove(bacteriumIndex));
        }
    }

    public void die(int currentL, int bacteriumIndex){

        microhabitats[currentL].removeABacterium(bacteriumIndex);
        if(getCurrentPopulation() == 0) populationDead = true;
    }


    public void replicate(int currentL, int bacteriumIndex){
        //a nutrient unit is consumed for every replication
        microhabitats[currentL].consumeNutrients();
        //the bacterium which is going to be replicated and its associated properties
        Bacteria parentBac = microhabitats[currentL].getBacteria(bacteriumIndex);
        int m = parentBac.getM();

        Bacteria childBac = new Bacteria(m);
        microhabitats[currentL].addABacterium(childBac);
    }

    public void performAction(){

        //selects a random bacteria from the total population
        if(!populationDead) {

            int randomIndex = rand.nextInt(getCurrentPopulation());
            int indexCounter = 0;
            int microHabIndex = 0;
            int bacteriaIndex = 0;

            forloop:
            for(int i = 0; i < getL(); i++) {

                if((indexCounter + microhabitats[i].getN()) <= randomIndex) {

                    indexCounter += microhabitats[i].getN();
                    continue forloop;
                } else {
                    microHabIndex = i;
                    bacteriaIndex = randomIndex - indexCounter;
                    break forloop;
                }
            }

            Microhabitat randMicroHab = microhabitats[microHabIndex];

            int s = randMicroHab.getS(), s_max = randMicroHab.getS_max();
            double K_prime = randMicroHab.getK_prime(), c = randMicroHab.getC();
            Bacteria randBac = randMicroHab.getBacteria(bacteriaIndex);

            double migRate = randBac.getB();
            double deaRate = randBac.getD();
            double repliRate = randBac.replicationRate(c, s, s_max);
            double R_max = 1.2;
            double rando = rand.nextDouble()*R_max;

            if(rando < migRate) migrate(microHabIndex, bacteriaIndex);
            else if(rando >= migRate && rando < (migRate + deaRate)) die(microHabIndex, bacteriaIndex);
            else if(rando >= (migRate + deaRate) && rando < (migRate + deaRate + repliRate))
                replicate(microHabIndex, bacteriaIndex);

            timeElapsed += 1./((double) getCurrentPopulation()*R_max);
            //move this to the death() method
        }
    }




    public static void spatialAndNutrientDistributions(double input_alpha){

        int L = 500, nReps = 10;
        int nTimeMeasurements = 20;
        double duration = 2000., interval = duration/(double)nTimeMeasurements;
        double alpha = input_alpha;
        int S = 500;

        String filename = "slowGrowers-alpha="+String.valueOf(alpha)+"-spatialDistribution";
        String filename_gRate = "slowGrowers-alpha="+String.valueOf(alpha)+"-gRateDistribution";

        int[][][] allMeasurements = new int[nReps][][];
        double[][][] allGRateMeasurements = new double[nReps][][];

        for(int r = 0; r < nReps; r++){

            boolean alreadyRecorded = false;
            int[][] popsOverTime = new int[nTimeMeasurements+1][];
            double[][] gRatesOverTime = new double[nTimeMeasurements+1][];
            int timerCounter = 0;

            BioSystem bs = new BioSystem(L, S, alpha);

            while(bs.timeElapsed <= duration){

                bs.performAction();

                if((bs.getTimeElapsed()%interval >= 0. && bs.getTimeElapsed()%interval <= 0.01) && !alreadyRecorded){

                    System.out.println("rep: "+r+"\ttime elapsed: "+String.valueOf(bs.getTimeElapsed()));
                    popsOverTime[timerCounter] = bs.getSpatialDistributionArray();
                    gRatesOverTime[timerCounter] = bs.getGrowthRatesArray();

                    alreadyRecorded = true;
                    timerCounter++;
                }
                if(bs.getTimeElapsed()%interval >= 0.1) alreadyRecorded = false;
            }

            allMeasurements[r] = popsOverTime;
            allGRateMeasurements[r] = gRatesOverTime;
        }

        double[][] averagedPopDistributions = Toolbox.averagedResults(allMeasurements);
        double[][] averagedGRateDistributions = Toolbox.averagedResults(allGRateMeasurements);
        Toolbox.printAveragedResultsToFile(filename, averagedPopDistributions);
        Toolbox.printAveragedResultsToFile(filename_gRate, averagedGRateDistributions);
    }
}
