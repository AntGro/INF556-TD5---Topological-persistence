import java.util.*;

class Simplex {
    float val;
    int dim;
    TreeSet<Integer> vert;

    Simplex(Scanner sc){
        val = sc.nextFloat();
        dim = sc.nextInt();
        vert = new TreeSet<Integer>();
        for (int i=0; i<=dim; i++)
            vert.add(sc.nextInt());
    }

    public String toString(){
        return "{val="+val+"; dim="+dim+"; "+vert+"}\n";
    }

    public Collection<Integer> getBoundaries(HashMap<Set<Integer>, Integer> simplToInd) {
        Collection<Integer> simp = new HashSet<> (vert);
        ArrayList<Integer> boundInd = new ArrayList<> ();
        for (Integer vertId : vert) {
            simp.remove(vertId);
            boundInd.add (simplToInd.get (simp));
            simp.add (vertId);
        }
        return boundInd;
    }
}
