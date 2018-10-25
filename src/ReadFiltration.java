import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class ReadFiltration {

    static Vector<Simplex> readFiltration (String filename) throws FileNotFoundException {
        Vector<Simplex> F = new Vector<Simplex>();
        Scanner sc = new Scanner(new File(filename));
        sc.useLocale(Locale.US);
        while (sc.hasNext())
            F.add(new Simplex(sc));
        sc.close();
        return F;
    }

    static HashMap<Integer, HashSet<Integer>> buildMatrix (Vector<Simplex> v, HashMap<Set<Integer>, Integer> simplToInd,  float[] indToTime) {
        HashMap<Integer, HashSet<Integer>>  B = new HashMap<> ();
        v.sort(new Comparator<Simplex>() {
            @Override
            public int compare(Simplex s1, Simplex s2) {
                if (s1.val > s2.val) return 1;
                if (s1.val < s2.val) return -1;
                if (s1.dim > s2.dim) return 1;
                if (s1.dim < s2.dim) return -1;
                return 0;
            }
        });

        //Fill simplToInd, indToTime and build B
        int ind = 0; //used to number simplices
        for (Simplex simplex : v) {
            simplToInd.put (simplex.vert, ind);
            indToTime[ind] = simplex.val;

            // put non-zero element of column ind indices if they exist (if simplex has a non trivial boundary)
            if (simplex.dim > 0) {
                B.put (ind, new HashSet<> (simplex.getBoundaries(simplToInd)));
            }
            ind++;
        }
        return B;
    }

    static void reduceMatrix (HashMap<Integer, HashSet<Integer>> B, HashMap<Integer, Integer> pivot, int n) { // O(n**3) where n is the number of simplices in the filtration

        for (int ind = 0; ind < n; ind ++) { //-> n loops
            int potPivot;
            potPivot = (B.containsKey (ind)) ? B.get (ind).stream().max (Integer::compareTo).get () : -1; // potential pivot of column ind
            while (potPivot > -1 && pivot.containsKey (potPivot)) { // this loop is executed at most m times since potPivot decreases by at least one each time -> O(n)
                int j = pivot.get (potPivot);
                for (int i : B.get (j)) {
                    if (B.get (ind).contains (i)) {
                        B.get (ind).remove(i);
                    }
                    else {
                        B.get (ind).add(i);
                    }
                }
                potPivot = (B.containsKey (ind)) ? B.get (ind).stream().max (Integer::compareTo).orElse (-1) : -1; // compute max -> O(n)
            }
            if (potPivot >= 0) { //if potpivot is actually a valid pivot -> update pivot table
                pivot.put (potPivot, ind);
            } else { //column ind of B is empty
                B.remove (ind);
            }
        }
    }

    private static void buildBarcode(HashMap<Integer, Integer> pivot, int n, int[] indToDim, float[] indToTime, PrintWriter writer) {
        HashMap<Integer, Integer> revPivot = new HashMap<> (); //revPivot.get (j) = i <=> there is a pivot in column j in line i
        pivot.forEach ((i, j) -> revPivot.put (j, i));

        for (int j = 0; j < n; j++) {
            if (revPivot.containsKey (j)) { // there is a pivot in column j -> there is a new bar
                int i = revPivot.get (j);
                writer.print (indToDim[i]);
                writer.println (" " + indToTime[i] + " " + indToTime[j]);
                continue;
            }
            if (!pivot.containsKey (j)) { // we only add a bar if there is no pivot in line j
                writer.print (indToDim[j]);
                writer.println (" " + indToTime[j] + " inf");
            }
        }
        writer.close();
    }


    public static Vector<Simplex>[] dSB(int maxDimension, int type){
        Vector[] res = new Vector[maxDimension+1];
        res[0] = new Vector<Simplex>();
        res[0].add(new Simplex(0,0, new TreeSet<Integer>(){{add(0);}}));
        res[1] = new Vector<Simplex>();
        if (type == 0){
            res[1].add(new Simplex(0,0, new TreeSet<Integer>(){{add(0);}}));
            res[1].add(new Simplex(0,0, new TreeSet<Integer>(){{add(1);}}));
        }
        else {
            res[1].add(new Simplex(0,0, new TreeSet<Integer>(){{add(0);}}));
            res[1].add(new Simplex(0,0, new TreeSet<Integer>(){{add(1);}}));
            res[1].add(new Simplex(1,1, new TreeSet<Integer>(){{add(0); add(1);}}));
        }
        for (int d = 2; d<=maxDimension; d++){
            res[d] = new Vector<Simplex>();
            res[d] = (Vector<Simplex>)res[d-1].clone();
            int finalD = d;
            res[d].add(new Simplex(0,0, new TreeSet<Integer>(){{add(finalD);}}));
            if (type == 0){
                res[d].add(new Simplex(finalD-1,finalD-1, new TreeSet<Integer>(){{
                    for (int j = 0; j<=finalD-1; j++){
                        add(j);
                    }
                }}));
            }
            Iterator it = res[d-1].iterator();
            while (it.hasNext()) {
                Simplex curr = (Simplex)it.next();
                res[d].add((curr.incr(finalD)));
            }
        }
        return res;

    }

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        if (args.length != 1) {
            System.out.println("Syntax: java ReadFiltration <filename>");
            System.exit(0);
        }
        long t = System.nanoTime ();    //To measure time execution
        Vector<Simplex> filtration = readFiltration (args[0]);
        System.out.println (filtration.size ());
        HashMap<Set<Integer>, Integer> simplToInd = new HashMap<> ();   //This HashMap will be filled during the building of the matrix B -> it matches simplices with their corresponding row/column index in B
        float[] indToTime = new float[filtration.size ()];  // indToTime[i] stores the birth time of simplex denoted by i in simplToInd
        HashMap<Integer, HashSet<Integer>> B = buildMatrix (filtration, simplToInd, indToTime); //keys are column indices
        HashMap<Integer, Integer> pivot = new HashMap<> (); //pivot will be filled during the reducing of the matrix B, its keys correspond to the indices matching the simplices
        reduceMatrix (B, pivot, filtration.size ());
        int[] indToDim = new int[filtration.size ()];   //intToDim[i] stores the dimension of simplex denoted by i in simplToInd
        simplToInd.forEach((k, v) -> indToDim[v] = k.size () - 1);
        PrintWriter writer = new PrintWriter("Resource/" + "barcode_" + args[0].substring(args[0].lastIndexOf("/") + 1), "UTF-8");

        buildBarcode (pivot, filtration.size (), indToDim, indToTime, writer);

        System.out.println ((System.nanoTime() - t) / 1000000000);
    }
    
}
