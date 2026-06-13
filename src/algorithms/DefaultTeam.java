package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import supportGUI.Circle;

/**
 * Implémentation du cercle minimum englobant.
 *
 * Deux algorithmes :
 *   - Naïf  : O(n^4) force brute (référence exacte)
 *   - Welzl : O(n) en espérance (randomisé)
 *
 * IMPORTANT : les calculs internes de Welzl utilisent des coordonnées double
 * précision (via la classe interne ExactCircle) pour éviter les erreurs
 * d'arrondi qui surviennent si l'on stocke les centres en entiers au milieu
 * de la récursion. La conversion en Circle (entiers) n'a lieu qu'à la fin.
 */
public class DefaultTeam {

    private static final double EPSILON = 1e-6;
    private final Random random = new Random(42);

    
    // Classe interne : cercle exact en double précision
    

    private static class ExactCircle {
        final double cx, cy, r;

        ExactCircle(double cx, double cy, double r) {
            this.cx = cx;  this.cy = cy;  this.r = r;
        }

        /** Vrai si le point p est à l'intérieur ou sur le bord. */
        boolean contains(Point p) {
            double dx = p.x - cx, dy = p.y - cy;
            return dx * dx + dy * dy <= (r + EPSILON) * (r + EPSILON);
        }

        /** Conversion vers la classe Circle du framework (entiers). */
        Circle toCircle() {
            return new Circle(
                new Point((int) Math.round(cx), (int) Math.round(cy)),
                (int) Math.ceil(r));
        }
    }

    
    // Point d'entrée principal
    

    public Circle calculCercleMin(ArrayList<Point> points) {
        if (points == null || points.isEmpty()) return null;
        return calculCercleMinWelzl(points);
    }


    // Welzl — O(n) en espérance
    

    public Circle calculCercleMinWelzl(ArrayList<Point> points) {
        if (points == null || points.isEmpty()) return null;
        if (points.size() == 1) return new Circle(points.get(0), 1);

        ArrayList<Point> shuffled = new ArrayList<>(points);
        Collections.shuffle(shuffled, random);

        ExactCircle ec = welzlRecursive(shuffled, new ArrayList<Point>(), shuffled.size());
        return (ec != null) ? ec.toCircle() : null;
    }

    private ExactCircle welzlRecursive(ArrayList<Point> P, ArrayList<Point> R, int n) {
        if (n == 0 || R.size() == 3) return minCircleFromBoundary(R);

        Point p = P.get(n - 1);
        ExactCircle circle = welzlRecursive(P, R, n - 1);

        if (circle != null && circle.contains(p)) return circle;

        R.add(p);
        circle = welzlRecursive(P, R, n - 1);
        R.remove(R.size() - 1);
        return circle;
    }

    /**
     * Cercle minimum passant par tous les points de R (|R| <= 3).
     * Tous les calculs sont en double précision.
     */
    private ExactCircle minCircleFromBoundary(ArrayList<Point> R) {
        switch (R.size()) {
            case 0: return new ExactCircle(0, 0, 0);
            case 1: return new ExactCircle(R.get(0).x, R.get(0).y, 0);
            case 2: return exactFrom2(R.get(0), R.get(1));
            case 3: {
                ExactCircle c = exactFrom3(R.get(0), R.get(1), R.get(2));
                if (c != null && containsAll(c, R)) return c;

                // Points quasi-colinéaires : chercher le meilleur cercle à 2 pts
                ExactCircle best = null;
                ExactCircle[] cands = {
                    exactFrom2(R.get(0), R.get(1)),
                    exactFrom2(R.get(0), R.get(2)),
                    exactFrom2(R.get(1), R.get(2))
                };
                for (ExactCircle cand : cands) {
                    if (cand != null && containsAll(cand, R)) {
                        if (best == null || cand.r < best.r) best = cand;
                    }
                }
                return (best != null) ? best : cands[0]; // failsafe
            }
            default: return null;
        }
    }

    
    // Naïf — O(n^4) force brute
    

    public Circle calculCercleMinNaif(ArrayList<Point> points) {
        if (points == null || points.isEmpty()) return null;
        if (points.size() == 1) return new Circle(points.get(0), 1);

        int n = points.size();
        double minR = Double.MAX_VALUE, bestCx = 0, bestCy = 0;

        // Phase 1 : cercles de diamètre (i, j)
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Point p = points.get(i), q = points.get(j);
                double cx = (p.x + q.x) / 2.0;
                double cy = (p.y + q.y) / 2.0;
                double r2 = ((p.x-q.x)*(p.x-q.x) + (p.y-q.y)*(p.y-q.y)) / 4.0;
                if (allInDisc(points, cx, cy, r2)) {
                    double r = Math.sqrt(r2);
                    if (r < minR) { minR = r; bestCx = cx; bestCy = cy; }
                }
            }
        }

        // Phase 2 : cercles circonscrits aux triplets (i, j, k)
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                for (int k = j + 1; k < n; k++) {
                    Point p = points.get(i), q = points.get(j), s = points.get(k);
                    double ax = p.x, ay = p.y;
                    double bx = q.x, by = q.y;
                    double cx = s.x, cy = s.y;

                    double d = 2*(ax*(by-cy)+bx*(cy-ay)+cx*(ay-by));
                    if (Math.abs(d) < EPSILON) continue;

                    double ux = ((ax*ax+ay*ay)*(by-cy)+(bx*bx+by*by)*(cy-ay)+(cx*cx+cy*cy)*(ay-by))/d;
                    double uy = ((ax*ax+ay*ay)*(cx-bx)+(bx*bx+by*by)*(ax-cx)+(cx*cx+cy*cy)*(bx-ax))/d;
                    double r2 = (p.x-ux)*(p.x-ux)+(p.y-uy)*(p.y-uy);
                    double rad = Math.sqrt(r2);

                    if (rad < minR && allInDisc(points, ux, uy, r2)) {
                        minR = rad; bestCx = ux; bestCy = uy;
                    }
                }
            }
        }

        if (minR == Double.MAX_VALUE) return null;
        return new Circle(
            new Point((int) Math.round(bestCx), (int) Math.round(bestCy)),
            (int) Math.ceil(minR));
    }

    
    // Fonctions géométriques auxiliaires
    
    
    //Calcule le cercle dont le diamètre est le segment [pq]
    private ExactCircle exactFrom2(Point p, Point q) {
        double cx = (p.x + q.x) / 2.0, cy = (p.y + q.y) / 2.0;
        double r  = Math.sqrt((p.x-q.x)*(p.x-q.x)+(p.y-q.y)*(p.y-q.y)) / 2.0;
        return new ExactCircle(cx, cy, r);
    }

    //Calcule le cercle circonscrit
    private ExactCircle exactFrom3(Point p1, Point p2, Point p3) {
        double ax = p1.x, ay = p1.y;
        double bx = p2.x, by = p2.y;
        double cx = p3.x, cy = p3.y;
        double d = 2*(ax*(by-cy)+bx*(cy-ay)+cx*(ay-by));
        if (Math.abs(d) < EPSILON) return null;
        double ux = ((ax*ax+ay*ay)*(by-cy)+(bx*bx+by*by)*(cy-ay)+(cx*cx+cy*cy)*(ay-by))/d;
        double uy = ((ax*ax+ay*ay)*(cx-bx)+(bx*bx+by*by)*(ax-cx)+(cx*cx+cy*cy)*(bx-ax))/d;
        double r  = Math.sqrt((p1.x-ux)*(p1.x-ux)+(p1.y-uy)*(p1.y-uy));
        return new ExactCircle(ux, uy, r);
    }
    
    //Vérifie si tous les points sont dans le disque de centre (cx, cy) et de rayon au carré r²
    private boolean allInDisc(ArrayList<Point> pts, double cx, double cy, double r2) {
        for (Point s : pts) {
            double dx = s.x-cx, dy = s.y-cy;
            if (dx*dx+dy*dy > r2+EPSILON) return false;
        }
        return true;
    }

    //Même rôle que allInDisc, mais pour un ExactCircle en double précision
    private boolean containsAll(ExactCircle c, ArrayList<Point> pts) {
        for (Point p : pts) if (!c.contains(p)) return false;
        return true;
    }
}