import util.Tuple;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Predicate;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.Leaf;
import com.github.davidmoten.rtree.Node;
import com.github.davidmoten.rtree.NonLeaf;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.HasGeometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;

public class BBSkyline {
    private RTree<Tuple<Double, Double>, Point> rtree;
    private ArrayList<Point> s;

    /**
     * Wrapper class for PQ element
     *
     * @param <T> Wrap object with HasGeometry as an upper bound
     */

    private class HeapElement<T extends HasGeometry> implements Comparable<HeapElement<T>> {
        private T elem;

        private HeapElement(T elem) {
            this.elem = elem;
        }

        private T getElement() {
            return elem;
        }

        /**
         * Compare w.r.t (0,0)
         *
         * @param other other HeapElement
         */
        public int compareTo(HeapElement<T> other) {
            return (int) ((elem.geometry().mbr().x1() + elem.geometry().mbr().y1()) - (other.getElement().geometry().mbr().x1() + other.getElement().geometry().mbr().y1()));
        }
    }

    /**
     * BB Skyline constructor
     *
     * @throws IOException
     */

    public BBSkyline() throws IOException {
        rtree = RTree.create();
        s = new ArrayList<Point>();
        // Read data and load into rtree
        ArrayList<Tuple<Double, Double>> points = loadDb(findDbPath());

        // Add each point into rtree
        for (Tuple<Double, Double> p: points) {
            rtree = rtree.add(p, Geometries.point(p.getFirst(), p.getSecond()));
        }

        // BBS
        skyline(rtree.root().get(), false, -1, -1, -1, -1);
    }

    /**
     * Getter for rtree
     *
     * @return most recent rtree
     */

    public RTree<Tuple<Double, Double>, Point> getTree() {
        return rtree;
    }

    public ArrayList<Point> getSkyline() {
        return s;
    }

    /**
     * Find "dataset1.txt" path under current working directory
     *
     * @return Full path to "dataset1.txt"
     * @throws IOException
     */

    public String findDbPath() throws IOException {
        File PWD = new File("./");

        // Find db folder and dataset1.txt file
        ArrayDeque<File> queue = new ArrayDeque<File>();
        queue.add(PWD);

        while (!queue.isEmpty()) {
            File nextFile = queue.remove();

            if (nextFile.isDirectory()) {
                if (nextFile.getName().equals("db")) {
                    try {
                        return nextFile.listFiles(new FileFilter() {

                            @Override
                            public boolean accept(File f) {
                                return f.getName().equals("dataset1.txt");
                            }
                        })[0].getCanonicalPath();
                    } catch (Exception e) {
                        System.err.print("dataset1.txt not found here: ");
                        System.err.println(nextFile.getCanonicalPath());
                    }
                } else {
                    for (File f : nextFile.listFiles()) {
                        // Add subdirectory
                        if (f.isDirectory())
                            queue.add(f);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Load specified db file
     *
     * @param dbpath Full path to db file
     * @return ArrayList containing points loaded from db file
     * @throws IOException
     */

    public ArrayList<Tuple<Double, Double>> loadDb(String dbpath) throws IOException {
        final File DB = new File(dbpath);
        ArrayList<Tuple<Double, Double>> points = new ArrayList<Tuple<Double,Double>>();
        BufferedReader br = new BufferedReader(new FileReader(DB));

        String line = "";

        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(" ");
            // Process only with 2 tokens
            if (tokens.length == 2) {
                points.add(new Tuple<Double,Double>(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1])));
            }
        }

        br.close();

        return points;
    }

    /**
     * Check whether the given node is dominated by the given (skyline) point
     *
     * @param <T> Any object with HasGeometry as an upper bound
     * @param node HasGeometry object to check against the point for domination
     * @param p Point to check for domination over HasGeometry object
     * @return Domination result
     */

    private <T extends HasGeometry> boolean isDominated(T node, Point p) {
        return ((node.geometry().mbr().x1() >= p.x()) && (node.geometry().mbr().y1() >= p.y())) ? true : false ;
    }

    /**
     * Check whether the given node is dominated by the given (skyline) points
     *
     * @param <T> Any object with HasGeometry as an upper bound
     * @param node HasGeometry object to check against the points for domination
     * @param li Points to check for domination over HasGeometry object
     * @return Domination result
     */

    private <T extends HasGeometry> boolean isDominated(T node, List<Point> li) {

        for (Point p: li) {
            if (isDominated(node, p)) return true;
        }

        return false;
    }

    /**
     * Skyline algorithm
     *
     * @param start Node to start the algorithm
     * @param regionBound Flag indicating whether to impose upper bound
     * @param x1 x-coordinate of lower bound point
     * @param y1 y-coordinate of lower bound point
     * @param x2 x-coordinate of upper bound point
     * @param y2 y-coordinate of upper bound point
     */

    @SuppressWarnings("unchecked")

    public void skyline(Node<Tuple<Double, Double>, Point> start, boolean regionBound, double x1, double y1, double x2, double y2) {
        PriorityQueue<HeapElement<? extends HasGeometry>> pq = new PriorityQueue<HeapElement<? extends HasGeometry>>();
        Rectangle region = Geometries.rectangle(x1, y1, x2, y2);

        // Consider if no bound or intersect with bound
        if (!regionBound || region.intersects(Geometries.rectangle(start.geometry().mbr().x1(), start.geometry().mbr().y1(), start.geometry().mbr().x2(), start.geometry().mbr().y2())));
        pq.add(new HeapElement<Node<Tuple<Double, Double>, Point>>(start));

        while (!pq.isEmpty()) {
            HeapElement<? extends HasGeometry> current = pq.remove();

            if (!isDominated(current.getElement(), s)) {
                if (current.getElement() instanceof NonLeaf) {
                    NonLeaf<Tuple<Double, Double>, Point> nl = (NonLeaf<Tuple<Double, Double>, Point>) current.getElement();
                    for (Node<Tuple<Double, Double>, Point> node: nl.children()) {
                        if (!isDominated(node, s) && (!regionBound || region.intersects(Geometries.rectangle(node.geometry().mbr().x1(), node.geometry().mbr().y1(), node.geometry().mbr().x2(), node.geometry().mbr().y2())))) {
                            pq.add(new HeapElement<Node<Tuple<Double, Double>, Point>>(node));
                        }
                    }
                }
                else if (current.getElement() instanceof Leaf) {
                    Leaf<Tuple<Double, Double>, Point> l = (Leaf<Tuple<Double, Double>, Point>) current.getElement();
                    for (Entry<Tuple<Double, Double>, Point> entry: l.entries()) {
                        if (!isDominated(entry.geometry(), s) && (!regionBound || region.intersects(Geometries.rectangle(entry.geometry().mbr().x1(), entry.geometry().mbr().y1(), entry.geometry().mbr().x2(), entry.geometry().mbr().y2()))))
                            pq.add(new HeapElement<Point>(entry.geometry()));
                    }
                }
                else {
                    // This is point
                    Point toAdd = ((HeapElement<Point>) current).getElement();
                    if (!s.contains(toAdd) && (!regionBound || region.contains(toAdd.x(), toAdd.y())))
                        s.add(toAdd);
                }
            }
        }
    }

    /**
     * Insert the given point
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */

    public void insert(double x, double y) {
        Tuple<Double, Double> inserted = new Tuple<Double, Double>(x, y);
        // System.out.println("inserting " + x + ", " + y);
        rtree = rtree.add(inserted, Geometries.point(inserted.getFirst(), inserted.getSecond()));

        ArrayList<Point> toRemove = new ArrayList<Point>();

        boolean dominated = false;
        for (Point p: s){
            if (x <= p.x() && y <= p.y()){
                toRemove.add(p);
            }
            else if (p.x() <= x && p.y() <= y){
                dominated = true;
            }
        }

        if (toRemove.size() > 0){
            s.removeAll(toRemove);
        }

        if (!dominated){
            s.add(Geometries.point(x, y));
        }
    }

    /**
     * Delete the given point
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @param all flag indicating remove all occurrences
     */

    public void delete(double x, double y, boolean all) {
        final Point toDelete = Geometries.point(x, y);
        rtree = rtree.delete(new Tuple<Double, Double>(x, y), toDelete, all);

        // Need additional process if delete skyline point
        if (s.contains(toDelete)) {
            Collections.sort(s, new Comparator<Point>() {
                public int compare(Point p1, Point p2) {
                    if (p1.x() < p2.x())
                        return -1;
                    else if (p1.x() > p2.x())
                        return 1;
                    else {
                        if (p1.y() > p2.y())
                            return -1;
                        else if (p1.y() < p2.y())
                            return 1;
                        return 0;
                    }
                }
            });

            int index = s.indexOf(toDelete);
            double x2 = -1;
            double y2 = -1;
            boolean regionBound = false;
            try {
                x2 = s.get(index+1).x();
                y2 = s.get(index-1).y();
                regionBound = true;
            } catch (IndexOutOfBoundsException e) {

            }

            s.removeIf(new Predicate<Point>() {
                public boolean test(Point p) {
                    return p.equals(toDelete);
                }
            });

            skyline(rtree.root().get(), regionBound, toDelete.x(), toDelete.y(), x2, y2);
        }
    }

    /**
     * Delete all occurrences of the given point
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */

    public void deleteAll(double x, double y) {
        delete(x, y, true);
    }
}
