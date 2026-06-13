
  CPA — Projet individuel 2025-2026
  Algorithme de Welzl — Cercle Minimum Englobant
  Auteur  : ZEGHMICHE Dalila
  Cours   : CPA, Master 1 Informatique, Université Sorbonne
  Date    : Mars 2026


DESCRIPTION
-----------
Ce projet implante et compare deux algorithmes de calcul du cercle
minimum englobant (CME) pour un ensemble de points dans le plan :

  1. Algorithme naïf  O(n^4) : force brute, sert de référence exacte.
  2. Algorithme de Welzl O(n): randomisé, temps linéaire en espérance.

Les deux algorithmes sont évalués sur la base de tests VAROUMAS
(1664 instances, 256 points chacune).


CONTENU DE L'ARCHIVE
---------------------
  rapport/
    CPA_PETIT_PROJET.pdf       Rapport de recherche (~10 pages)

  src/
    algorithms/DefaultTeam.java   Code source des deux algorithmes
    benchmark/Benchmark.java      Programme de benchmarking

  bin/ (ou build/)
    algorithms/DefaultTeam.class  Fichiers compilés Java
    benchmark/Benchmark.class

  samples/
    

  graphs.py                     Script Python pour générer les graphiques
  build.xml                     Fichier Apache Ant (compilation + exécution)
  results.csv                   Résultats du dernier benchmark
  README.txt                    Ce fichier


PRÉREQUIS
---------
  - Java JDK 8 ou supérieur
  - Apache Ant (pour utiliser build.xml)
  - Python 3 + matplotlib + pandas (pour les graphiques, optionnel)


COMPILATION ET EXÉCUTION
------------------------

  Option 1 — Via Apache Ant (recommandé) :

    Compiler :
      ant compile

    Lancer l'interface visuelle DiameterRacer (appuyer sur 'c') :
      ant run

    Lancer le benchmark complet (1664 instances) :
      ant benchmark
      --> Génère results.csv dans le répertoire courant

    Générer les graphiques (nécessite Python + matplotlib) :
      python3 graphs.py
      --> Génère graph1_speedup_batons.png, graph2_temps_comparaison.png,
          graph3_distribution.png, graph4_welzl_zoom.png

    Nettoyer les fichiers compilés :
      ant clean

  Option 2 — Compilation manuelle :

    javac -cp jars/TME2_supportGUI.jar -d build src/algorithms/DefaultTeam.java
    javac -cp build;jars/TME2_supportGUI.jar -d build src/benchmark/Benchmark.java

    Lancer le benchmark :
    java -cp build;jars/TME2_supportGUI.jar benchmark.Benchmark


FORMAT DES FICHIERS .points
---------------------------
Chaque fichier contient 256 lignes, chaque ligne = "x y" (deux entiers).
Exemple :
  321 382
  429 376
  428 288
  ...

Note : le fichier test-1.points est une archive TAR concaténée avec des
en-têtes de métadonnées. Le parseur les ignore automatiquement.



STRUCTURE DU CODE
-----------------
  DefaultTeam.java contient :
    - calculCercleMin()         Point d'entrée principal (appelle Welzl)
    - calculCercleMinWelzl()    Algorithme de Welzl O(n)
    - calculCercleMinNaif()     Algorithme naïf O(n^4)
    - welzlRecursive()          Récursion de Welzl
    - minCircleFromBoundary()   Cas de base (trivialCircle)
    - ExactCircle               Classe interne (double précision)

  Benchmark.java contient :
    - main()                    Boucle principale du benchmark
    - readPoints()              Parseur de fichiers .points


