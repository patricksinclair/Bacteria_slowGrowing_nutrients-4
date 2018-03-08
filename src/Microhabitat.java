import java.util.ArrayList;

public class Microhabitat {

    private int K = 100, s, s_max;
    private double c;

    private final double K_prime = 33.;

    private ArrayList<Bacteria> population;


    public Microhabitat(double c, int S){
        this.c = c;
        this.s = S;
        this.s_max = S;
        this.population = new ArrayList<Bacteria>(K);
    }

    public double getC(){return c;}
    public void setC(double c){
        this.c = c;
    }

    public double getK_prime(){return K_prime;}

    public int getS(){return s;}
    public void setS(int S){this.s = S;}

    public int getS_max(){return s_max;}

    public int getN(){
        return population.size();
    }

    public ArrayList<Bacteria> getPopulation(){
        return population;
    }
    public Bacteria getBacteria(int i){
        return population.get(i);
    }


    public double getGrowthRate(){

        double mu = s/(K_prime+s);
        double mu_max = s_max/(K_prime+s_max);
        double beta =  1. + 9.*mu/mu_max;
        double factor =  1 - c/beta;
        if(factor < 0) return 0.;
        else {
            double phi_c = 0.5*(1 + Math.sqrt(factor));
            return phi_c*mu;
        }
    }

    public void fillWithWildType(){

        int initGenotype = 1;

        for(int i = 0; i < K; i++){
            population.add(new Bacteria(initGenotype));
        }
    }

    public void consumeNutrients(){
        if(s > 0) s--;
    }

    public void removeABacterium(int i){
        population.remove(i);
    }

    public void addABacterium(Bacteria newBact){
        population.add(newBact);
    }
}
