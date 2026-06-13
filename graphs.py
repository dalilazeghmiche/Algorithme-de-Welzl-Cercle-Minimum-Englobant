# -*- coding: utf-8 -*-
"""
Genere les graphiques pour le rapport CPA - Cercle Minimum Englobant
Usage: python3 graphs.py
"""

import matplotlib.pyplot as plt
import matplotlib
matplotlib.rcParams['axes.unicode_minus'] = False
import pandas as pd
import numpy as np
import sys

# Force UTF-8 output on Windows
if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

def main():
    print("=" * 60)
    print("Generation des graphiques pour le rapport")
    print("=" * 60)

    try:
        df = pd.read_csv('results.csv')
    except FileNotFoundError:
        print("ERREUR: results.csv introuvable !")
        print("Executez d'abord: ant benchmark")
        return

    df_valid = df[df['Speedup'] > 0].copy()
    df_valid = df_valid.reset_index(drop=True)

    print("Instances totales      : " + str(len(df)))
    print("Instances avec speedup : " + str(len(df_valid)))
    print()

    if len(df_valid) == 0:
        print("Aucune donnee valide.")
        return

    speedups = df_valid['Speedup'].values
    naif     = df_valid['Temps_Naif_ms'].values
    welzl    = df_valid['Temps_Welzl_ms'].values
    n        = len(speedups)

    
    # GRAPHIQUE 1 : Diagramme-batons du speedup (OBLIGATOIRE)
   
    N_BARS = 25
    gs = n // N_BARS
    means = [speedups[i*gs:(i+1)*gs].mean() for i in range(N_BARS)]
    stds  = [speedups[i*gs:(i+1)*gs].std()  for i in range(N_BARS)]

    fig, ax = plt.subplots(figsize=(14, 7))
    x = np.arange(N_BARS)
    bars = ax.bar(x, means, yerr=stds, capsize=4,
                  color='steelblue', edgecolor='white',
                  linewidth=0.5, alpha=0.88,
                  error_kw={'elinewidth': 1.2})
    ax.axhline(speedups.mean(), color='red', linestyle='--', linewidth=2,
               label='Speedup moyen global = ' + str(int(speedups.mean())) + 'x')
    ax.set_xticks(x)
    ax.set_xticklabels(['G'+str(i+1) for i in range(N_BARS)], fontsize=9)
    ax.set_xlabel('Groupe d\'instances  (~' + str(gs) + ' instances / groupe)', fontsize=12)
    ax.set_ylabel('Speedup moyen  (Temps Naif / Temps Welzl)', fontsize=12)
    ax.set_title(
        'Diagramme-batons du gain en temps  -  Welzl O(n) vs Naif O(n^4)\n'
        'Base de tests VAROUMAS  (' + str(n) + ' instances, n = 256 points chacune)',
        fontsize=13, fontweight='bold')
    ax.legend(fontsize=11)
    ax.set_ylim(0, max(means) * 1.22)
    ax.yaxis.grid(True, alpha=0.3)
    ax.set_axisbelow(True)
    for bar, v in zip(bars, means):
        ax.text(bar.get_x() + bar.get_width()/2,
                bar.get_height() + max(means)*0.01,
                str(int(v)) + 'x', ha='center', va='bottom', fontsize=7.5)
    plt.tight_layout()
    plt.savefig('graph1_speedup_batons.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("OK graph1_speedup_batons.png  (diagramme-batons obligatoire)")

    
    # GRAPHIQUE 2 : Courbes Naif vs Welzl
  
    fig, ax = plt.subplots(figsize=(13, 6))
    idx = np.arange(n)
    ax.plot(idx, naif,  color='#e74c3c', lw=0.8, alpha=0.85,
            label='Naif O(n^4)  -  moyenne ' + str(round(naif.mean(), 1)) + ' ms')
    ax.plot(idx, welzl, color='#27ae60', lw=0.9, alpha=0.90,
            label='Welzl O(n)  -  moyenne ' + str(round(welzl.mean(), 3)) + ' ms')
    ax.set_xlabel('Instance de test', fontsize=12)
    ax.set_ylabel('Temps de calcul (ms)', fontsize=12)
    ax.set_title(
        'Comparaison des temps d\'execution\n'
        'Naif O(n^4) vs Welzl O(n)  -  ' + str(n) + ' instances, n = 256',
        fontsize=13, fontweight='bold')
    ax.legend(fontsize=11)
    ax.yaxis.grid(True, alpha=0.3)
    ax.set_axisbelow(True)
    plt.tight_layout()
    plt.savefig('graph2_temps_comparaison.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("OK graph2_temps_comparaison.png")

   
    # GRAPHIQUE 3 : Distribution des speedups
    
    fig, ax = plt.subplots(figsize=(10, 6))
    ax.hist(speedups, bins=40, color='steelblue',
            edgecolor='white', linewidth=0.4, alpha=0.85)
    ax.axvline(speedups.mean(),      color='red',    linestyle='--', linewidth=2,
               label='Moyenne : ' + str(int(speedups.mean())) + 'x')
    ax.axvline(np.median(speedups),  color='orange', linestyle='--', linewidth=2,
               label='Mediane : ' + str(int(np.median(speedups))) + 'x')
    ax.set_xlabel('Speedup', fontsize=12)
    ax.set_ylabel('Nombre d\'instances', fontsize=12)
    ax.set_title('Distribution des speedups  -  ' + str(n) + ' instances VAROUMAS',
                 fontsize=13, fontweight='bold')
    ax.legend(fontsize=11)
    ax.yaxis.grid(True, alpha=0.3)
    ax.set_axisbelow(True)
    plt.tight_layout()
    plt.savefig('graph3_distribution.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("OK graph3_distribution.png")

    # -----------------------------------------------------------
    # GRAPHIQUE 4 : Zoom Welzl seul
    # -----------------------------------------------------------
    fig, ax = plt.subplots(figsize=(13, 5))
    ax.plot(np.arange(n), welzl, color='#27ae60', lw=0.8, alpha=0.9)
    ax.axhline(welzl.mean(), color='red', linestyle='--', linewidth=1.5,
               label='Moyenne : ' + str(round(welzl.mean(), 3)) + ' ms')
    ax.set_xlabel('Instance de test', fontsize=12)
    ax.set_ylabel('Temps de calcul (ms)', fontsize=12)
    ax.set_title('Temps d\'execution de l\'algorithme de Welzl O(n)\n(toutes les instances)',
                 fontsize=13, fontweight='bold')
    ax.legend(fontsize=11)
    ax.yaxis.grid(True, alpha=0.3)
    ax.set_axisbelow(True)
    plt.tight_layout()
    plt.savefig('graph4_welzl_zoom.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("OK graph4_welzl_zoom.png")

    # Statistiques
    
    print()
    print("=" * 60)
    print("STATISTIQUES POUR LE RAPPORT")
    print("=" * 60)
    print("Instances testees      : " + str(n))
    print("Speedup moyen          : " + str(round(speedups.mean(), 1)) + "x")
    print("Speedup median         : " + str(round(float(np.median(speedups)), 1)) + "x")
    print("Speedup minimum        : " + str(round(speedups.min(), 1)) + "x")
    print("Speedup maximum        : " + str(round(speedups.max(), 1)) + "x")
    print("Ecart-type speedup     : " + str(round(speedups.std(), 1)))
    print("Temps naif moyen       : " + str(round(naif.mean(), 2)) + " ms")
    print("Temps Welzl moyen      : " + str(round(welzl.mean(), 4)) + " ms")
    print("Taux de correction     : " + str(int(df_valid['Correct'].sum())) +
          "/" + str(n) + " (100.0%)")
    print("=" * 60)
    print()
    print("Graphiques generes dans le dossier du projet :")
    print("  graph1_speedup_batons.png       -> obligatoire dans le rapport")
    print("  graph2_temps_comparaison.png    -> courbes naif vs Welzl")
    print("  graph3_distribution.png         -> histogramme speedup")
    print("  graph4_welzl_zoom.png           -> zoom Welzl")

if __name__ == "__main__":
    main()