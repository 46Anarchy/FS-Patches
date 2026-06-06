# FS-Patches

FSPatches est un mod pour minecraft 1.7.10 qui facilite l'utilisation du modpack paladium V11 sur des serveurs tiers.

Il corrige des bugs, crashs et autres problemes lie a l'absence de l'infra paladium.

Ce mod a lui seul ne permet pas de creer ou de maintenir un clone de paladium, meme avec un serveur et une infrastructure fonctionelle.

Il fonctionne grace a de la reflection, de la manipulation de bytecode, des prieres et de l'espoir.

Ce projet a été créé et est maintenu par La Collecteuse et Anakoni.

---

## Utilisation

1. Téléchargez la dernière version notre serveur de distribution (https://cdn.46anarchy.fr/files/mods/fspatches.jar)
2. Placez le fichier `.jar` téléchargé dans le dossier `mods` d'une instance Minecraft avec forge 1.7.10.

Une fois installé, le mod applique automatiquement ses correctifs au démarrage. Aucune configuration supplémentaire n’est requise.

Si le modpack paladium est absent, il sera telecharge automatiquement si l'utilisateur accepte.

---

## Compilation

*cette section est genere par IA, mais les instructions sont correctes.*

Pour compiler le projet à partir du code :

1. Clonez ce repo :

   ```
   git clone https://github.com/46Anarchy/FS-Patches
   ```

2. Accédez au dossier du projet.

3. Créez un dossier `mods` dans l’environnement de développement/exécution approprié, puis placez-y **l’intégralité du modpack Paladium ainsi que toutes les bibliothèques nécessaires**.
   Cette étape est indispensable pour que le projet compile correctement, car la compilation dépend de la présence de ces mods et bibliothèques.

4. Exécutez la commande suivante pour configurer l’environnement de développement :

    * Sous Linux/macOS :

      ```
      ./gradlew setupDecompWorkspace idea
      ```
    * Sous Windows :

      ```
      .\gradlew.bat setupDecompWorkspace idea
      ```

5. Ouvrez le projet dans JetBrains IntelliJ IDEA.

### Génération du mod

*cette section est genere par IA, mais les instructions sont correctes.*

Pour compiler et générer le mod :

* Sous Linux/macOS :

  ```
  ./gradlew build
  ```
* Sous Windows :

  ```
  .\gradlew.bat build
  ```

Une fois la compilation terminée, le fichier `.jar` généré sera disponible dans le dossier :

```
./build/libs
```

### Patch list

- regle le jeu qui crash en essayant d'ouvrir un navigateur web (sur linux, pour de bon)
- regle le jeu qui essaye de rejoindre un serveur avec une version de protocol invalide
- rajout du support pour le replay mod (indev, il reste quelques trucs a fixer)
- changement du menu principal pour retirer des trucs qui marchent pas
- changement du serveur sur lequel le jeu essaye de se connecter
- fix un kick lie aux drawers
- retire les mesures anti-pirate des mods (le code qui empeche les gens d'utiliser un launcher tier)
- facilite l'installation du modpack paladium et des fixs requis
- customise l'overlay de liste de joueurs
- Fix un crash a l'ouverture des enclumes modees
- Retabli la physique des blocs dans la zone builder
- Fix un bug qui affichait des notifications palavote
- Fix un crash de worldguard

... Et autre trucs dont je me souviens pas forcement.

### Contenu original

- ajout de trois legendary stones custom
- rewrite complet du systeme de luckyblocks
- ajout de luckyblocks custom (non craftable pour le moment)