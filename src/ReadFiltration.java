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

        int ind = 0;
        for (Simplex simplex : v) {
            simplToInd.put (simplex.vert, ind);
            indToTime[ind] = simplex.val;

            for (int simplId : simplex.getBoundaries(simplToInd)) {
                if (!B.containsKey(simplId)) B.put (simplId, new HashSet<> ());
                B.get (simplId).add (ind);
            }

            ind++;
        }
        return B;
    }

    static void reduceMatrix (HashMap<Integer, HashSet<Integer>> B, HashMap<Integer, HashSet<Integer>> Bt, HashMap<Integer, Integer> pivot, int n) {
        B.keySet().forEach (k -> B.get (k).forEach (v -> {
            if (!Bt.containsKey(v)) Bt.put (v, new HashSet<> ());
            Bt.get (v).add (k);
        }));

        for (int ind = 0; ind < n; ind ++) {
            int potPivot;
            potPivot = (Bt.containsKey (ind)) ? Bt.get (ind).stream().max (Integer::compareTo).get () : -1;
            while (potPivot > -1 && pivot.containsKey (potPivot)) {
                int j = pivot.get (potPivot);
                for (int i : Bt.get (j)) {
                    if (Bt.get (ind).contains (i)) {
                        Bt.get (ind).remove(i);
                        B.get (i).remove (ind);
                    }
                    else {
                        Bt.get (ind).add(i);
                        if (!B.containsKey (i)) B.put (i, new HashSet<> ());
                        B.get (i).remove (ind);
                    }
                }
                potPivot = (Bt.containsKey (ind)) ? Bt.get (ind).stream().max (Integer::compareTo).orElse (-1) : -1;
            }
            if (potPivot >= 0) {
                pivot.put (potPivot, ind);
            }
        }
    }

    private static void buildBarcode(HashMap<Integer, Integer> pivot, int n, int[] indToDim, float[] indToTime, PrintWriter writer) {
        HashMap<Integer, Integer> revPivot = new HashMap<> ();
        pivot.forEach ((i, j) -> revPivot.put (j, i));

        for (int j = 0; j < n; j++) {
            if (revPivot.containsKey (j)) {
                int i = revPivot.get (j);
                writer.print (indToDim[i]);
                writer.println (" " + indToTime[i] + " " + indToTime[j]);
                continue;
            }
            if (!pivot.containsKey (j)) {
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

        Vector<Simplex> filtration = readFiltration (args[0]);
        HashMap<Set<Integer>, Integer> simplToInd = new HashMap<> ();
        float[] indToTime = new float[filtration.size ()];
        HashMap<Integer, HashSet<Integer>> B = buildMatrix (filtration, simplToInd, indToTime);
        HashMap<Integer, HashSet<Integer>> Bt = new HashMap<> ();
        HashMap<Integer, Integer> pivot = new HashMap<> ();
        reduceMatrix (B, Bt, pivot, filtration.size ());
        int[] indToDim = new int[filtration.size ()];
        simplToInd.forEach((k, v) -> indToDim[v] = k.size () - 1);
        PrintWriter writer = new PrintWriter("Resource/out.txt", "UTF-8");

        buildBarcode (pivot, filtration.size (), indToDim, indToTime, writer);
    }

    
}
