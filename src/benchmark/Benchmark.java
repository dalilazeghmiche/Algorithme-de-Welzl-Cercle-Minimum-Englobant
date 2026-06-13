package benchmark;

import algorithms.DefaultTeam;
import java.awt.Point;
import java.io.*;
import java.util.*;
import supportGUI.Circle;

/**

* FONCTIONNEMENT :
*   1. Lecture de tous les fichiers .points du dossier samples/.
*   2. Pour chaque instance (fichier), exécution des deux algorithmes avec mesure
*      du temps via System.nanoTime() (résolution en nanosecondes).
*   3. Calcul du speedup = Temps_Naïf / Temps_Welzl pour chaque instance.
*   4. Vérification de la correction : comparaison des rayons retournés par les
*      deux algorithmes (tolérance de 2 pixels pour l'arrondi entier de Circle).
*   5. Écriture des résultats dans results.csv (exploité ensuite par graphs.py).
*   6. Affichage d'un récapitulatif global (speedup moyen, taux de correction).
*
* GESTION DE LA BASE VAROUMAS :
*   Le parseur readPoints() ignore automatiquement les lignes contenant des lettres,
*   ce qui permet de traiter le fichier test-1.points (archive TAR concaténée)
*   sans traitement préalable. Il s'arrête après 256 coordonnées valides.
*
* SORTIE :
*   - Console : progression test par test + récapitulatif final.
*   - results.csv : une ligne par instance avec les colonnes :
*       Fichier, N, Temps_Naif_ms, Temps_Welzl_ms,
*       Rayon_Naif, Rayon_Welzl, Speedup, Correct
*


**/


public class Benchmark {

    private static final int MAX_NAIVE = 300;

    private static ArrayList<Point> readPoints(String filename) throws IOException {
        ArrayList<Point> points = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();

            // Ignorer les lignes vides ou qui contiennent des lettres
            // (en-têtes tar du type "test-100.txt 000644 ustar steven...")
            if (line.isEmpty() || line.matches(".*[a-zA-Z].*")) continue;

            // Ignorer les lignes avec autre chose que chiffres et espaces
            if (!line.matches("[0-9 \\t]+")) continue;

            String[] parts = line.split("\\s+");
            if (parts.length == 2) {
                try {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    points.add(new Point(x, y));
                    // Arrêter après 256 points (taille standard VAROUMAS)
                    if (points.size() == 256) break;
                } catch (NumberFormatException e) {
                    // ignorer
                }
            }
        }

        br.close();
        return points;
    }

    public static void main(String[] args) throws Exception {

        System.out.println("=================================================================");
        System.out.println("BENCHMARK - Cercle Minimum Englobant (Naïf vs Welzl)");
        System.out.println("Base de tests : VAROUMAS");
        System.out.println("=================================================================");

        File folder = new File("samples");
        if (!folder.exists()) {
            System.out.println("ERREUR: Dossier samples/ introuvable !");
            return;
        }

        File[] files = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) { return name.endsWith(".points"); }
        });

        if (files == null || files.length == 0) {
            System.out.println("ERREUR: Aucun fichier .points trouvé");
            return;
        }

        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) { return f1.getName().compareTo(f2.getName()); }
        });

        System.out.println("Fichiers trouvés : " + files.length);
        System.out.println("Limite naïf      : " + MAX_NAIVE + " points");
        System.out.println("=================================================================\n");

        DefaultTeam team = new DefaultTeam();

        PrintWriter csv = new PrintWriter(new FileWriter("results.csv"));
        csv.println("Fichier,N,Temps_Naif_ms,Temps_Welzl_ms,Rayon_Naif,Rayon_Welzl,Speedup,Correct");

        int count = 0, correctCount = 0, speedupCount = 0, errors = 0, skipped = 0;
        double totalSpeedup = 0;

        for (File file : files) {
            try {
                ArrayList<Point> points = readPoints(file.getAbsolutePath());

                if (points.isEmpty()) {
                    skipped++;
                    System.out.println("SKIP (vide) : " + file.getName());
                    continue;
                }

                int n = points.size();
                count++;
                System.out.println("Test " + count + " : " + file.getName() + " (" + n + " points)");

                // Welzl
                long startW = System.nanoTime();
                Circle cW = team.calculCercleMinWelzl(points);
                long endW = System.nanoTime();
                double timeW   = (endW - startW) / 1_000_000.0;
                double radiusW = (cW != null) ? cW.getRadius() : 0;
                System.out.println("  Welzl : " + String.format("%.3f", timeW) + " ms, rayon=" + radiusW);

                double timeN = -1, radiusN = -1, speedup = -1;
                boolean correct = true;

                if (n <= MAX_NAIVE) {
                    long startN = System.nanoTime();
                    Circle cN = team.calculCercleMinNaif(points);
                    long endN = System.nanoTime();
                    timeN   = (endN - startN) / 1_000_000.0;
                    radiusN = (cN != null) ? cN.getRadius() : 0;

                    System.out.println("  Naïf  : " + String.format("%.3f", timeN) + " ms, rayon=" + radiusN);
                    speedup = timeN / timeW;
                    System.out.println("  Speedup : " + String.format("%.1f", speedup) + "x");

                    correct = Math.abs(radiusN - radiusW) <= 2.0;
                    System.out.println("  Correct : " + (correct ? "OK" : "ERREUR"));

                    if (correct) correctCount++;
                    totalSpeedup += speedup;
                    speedupCount++;
                } else {
                    System.out.println("  Naïf  : ignoré (trop grand)");
                }

                System.out.println();

                csv.println(file.getName() + "," + n + "," + timeN + "," + timeW + ","
                          + radiusN + "," + radiusW + "," + speedup + "," + correct);
                csv.flush();

            } catch (Exception e) {
                errors++;
                System.out.println("ERREUR sur " + file.getName() + " : " + e.getMessage());
            }
        }

        csv.close();

        System.out.println("=================================================================");
        System.out.println("RÉSULTATS GLOBAUX");
        System.out.println("=================================================================");
        System.out.println("Fichiers traités     : " + count);
        System.out.println("Fichiers ignorés     : " + skipped);
        System.out.println("Erreurs              : " + errors);
        System.out.println("Comparaisons naïf/W  : " + speedupCount);
        if (speedupCount > 0) {
            System.out.println("Speedup moyen        : " +
                String.format("%.1f", totalSpeedup / speedupCount) + "x");
            System.out.println("Taux de correction   : " + correctCount + "/" + speedupCount +
                " (" + String.format("%.1f", 100.0 * correctCount / speedupCount) + "%)");
        }
        System.out.println("=================================================================");
        System.out.println("Fichier CSV créé     : results.csv");
    }
}