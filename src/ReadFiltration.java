import java.io.File;
import java.io.FileNotFoundException;
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

    HashMap<Integer, HashSet<Integer>> buildMatrix (Vector<Simplex> v, HashMap<Set<Integer>, Integer> simplToInd) {
        HashMap<Integer, HashSet<Integer>>  B = new HashMap<> ();
        v.sort(new Comparator<Simplex>() {
            @Override
            public int compare(Simplex s1, Simplex s2) {
                if (s1.val > s2.val) return 1;
                if (s1.val < s2.val) return -1;
                if (s1.dim > s2.dim) return 1;
                if (s1.dim < s2.dim) return -1;
                return (s1.vert.equals(s2.vert)? 0 : 1);
            }
        });

        int ind = 0;
        for (Simplex simplex : v) {
            simplToInd.put (simplex.vert, ind);

            for (int simplId : simplex.getBoundaries(simplToInd)) {
                if (!B.containsKey(simplId)) B.put (simplId, new HashSet<> ());
                B.get (simplId).add (ind);
            }

            ind++;
        }
        return B;
    }

    static HashMap<Integer, HashSet<Integer>> reduceMatrix (HashMap<Integer, HashSet<Integer>> B, int n) {
        HashMap<Integer, HashSet<Integer>> Bt = new HashMap<> ();
        B.keySet().forEach (k -> B.get (k).forEach (v -> {
            if (!Bt.containsKey(v)) Bt.put (v, new HashSet<> ());
            Bt.get (v).add (k);
        }));
        HashMap<Integer, Integer> pivot = new HashMap<> ();

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
                potPivot = (Bt.containsKey (ind)) ? Bt.get (ind).stream().max (Integer::compareTo).get () : -1;
            }
            if (potPivot >= 0) {
                pivot.put (potPivot, ind);
            }
        }
        return B;
    }


    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1) {
            System.out.println("Syntax: java ReadFiltration <filename>");
            System.exit(0);
        }

        System.out.println(readFiltration(args[0]));
    }
}
