import java.util.*;

class Simplex {
    float val;
    int dim;
    TreeSet<Integer> vert;

    public Simplex(float value, int dimension, TreeSet<Integer> vertices){
        val = value;
        dim = dimension;
        vert = vertices;
    }

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

    public Collection<Integer> getBoundaries(HashMap<Set<Integer>, Integer> simplToInd) { //given a simplex (this) and a map matching simplices to indices, return the indices of the element of the simplex boudary
        Collection<Integer> simp = new HashSet<> (vert);
        ArrayList<Integer> boundInd = new ArrayList<> ();
        for (Integer vertId : vert) {
            simp.remove(vertId);
            if (!simp.isEmpty()) boundInd.add (simplToInd.get (simp));
            simp.add (vertId);
        }
        return boundInd;
    }

    public Simplex incr(int d){
        TreeSet<Integer> vertices = (TreeSet)vert.clone();
        vertices.add(d);
        return new Simplex(val +1, dim +1, vertices);
    }
}
