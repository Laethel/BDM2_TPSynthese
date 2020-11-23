package app;

import java.util.ArrayList;
import java.util.Scanner;

import controllers.MongoDBController;
import controllers.Neo4JController;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static ArrayList<Integer> listeIds;
    static ArrayList<String> listeTitre;
    
	static Neo4JController neo4J = new Neo4JController();
    static MongoDBController mongoDB = new MongoDBController();
        
    
    public static void main(String[] args) {
    	mongoDB.createBase(neo4J.getArticles());
    	mongoDB.createStructureMiroir();
		displayMenu();
	}

	private static void displayMenu() {
		System.out.println("---------- MONGO & NEO4J APP ----------");
		System.out.println("1 - Rechercher un titre de document à partir d'un mot clé");
		System.out.println("2 - Afficher les auteurs ayant écrit le plus d'articles");
		System.out.println("3 - Rechercher un titre de document à partir de plusieurs mots clés");
		System.out.println("0 - Quitter l'application");
		System.out.println("-------------------------------");
		switch(sc.nextInt()) {
		case 1 :
			rechercherDocMotCleUnique();
			break;
		case 2 :
			afficherAuteurs();
			break;
		case 3 :
			rechercherDocMotsClesMultiples();
			break;
		case 0 :
			System.out.println("Bye bye");
			sc.close();
			neo4J.close();
			mongoDB.close();
			break;
		default :
			System.out.println("Choix non reconnu.");
			displayMenu();
		}
	}

	private static void rechercherDocMotsClesMultiples() {
        System.out.println("Entrez le mot clé : ");
        String keyword = sc.nextLine();
        listeIds = mongoDB.searchByWord(keyword);
        if (!listeIds.isEmpty()) {
        	listeTitre = neo4J.getArticlesTitleByDocumentIdList(listeIds);
            for (String s : listeTitre)
                System.out.println(s);

        } else {
        	System.out.println("Aucun document n'a été trouvé.");
        }	
	}

	private static void afficherAuteurs() {
        ArrayList<String> authorList = neo4J.getTopAuteurs();
        for (String s : authorList) {
        	System.out.println(s);
        }	
	}

	private static void rechercherDocMotCleUnique() {
        System.out.println("Entrez la liste des mots-clés, séparés par une virgule ',' : ");
        String rawKeywords = sc.nextLine();
        String[] keywords = rawKeywords.split(",");
        listeIds = mongoDB.searchByWordList(keywords);
        if (!listeIds.isEmpty()) {
        	listeTitre = neo4J.getArticlesTitleByDocumentIdList(listeIds);
            for (String s : listeTitre) {
            	System.out.println(s);
            }     
        } else {
        	System.out.println("Aucun document n'a été trouvé.");
        }      
	}
}
