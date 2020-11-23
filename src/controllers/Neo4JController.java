package controllers;

import org.neo4j.driver.v1.*;

import java.util.ArrayList;

public class Neo4JController {
    private final Driver driver;

    public Neo4JController() {
        driver = GraphDatabase.driver("bolt://192.168.56.50");
    }
    
    public ArrayList<Record> getArticles() {
        Session s = getSession();
        String rq = "match(a:Article) return a.titre as titre, a.annee as annee, id(a) as id";
        StatementResult result = s.run(rq);
        s.close();
        return new ArrayList<>(result.list());
    }

    private Session getSession() {
        return driver.session();
    }

    public ArrayList<String> getArticlesTitleByDocumentIdList(ArrayList<Integer> docId) {
        StringBuilder docIdList = new StringBuilder("[");
        for (int id: docId) {
            docIdList.append(id).append(",");
        }
        docIdList.deleteCharAt(docIdList.length() - 1);
        docIdList.append("]");
        Session s = getSession();
        String rq = "match(a:Article) where id(a) in " + docIdList + " return a.titre as title order by title";
        StatementResult result = s.run(rq);
        s.close();
        ArrayList<String> titles = new ArrayList<>();
        while (result.hasNext()) {
            Record r = result.next();
            titles.add(r.get("title").asString());
        }
        return titles;
    }

    public ArrayList<String> getTopAuteurs() {
        Session session = getSession();
        String rq = "MATCH (a:Auteur)-[r:Ecrire]->(ar:Article) RETURN count(ar) as nbTitre, a.nom as nom order by nbTitre desc, nom limit 10";
        StatementResult result = session.run(rq);
        session.close();
        ArrayList<String> list = new ArrayList<>();
        while (result.hasNext()) {
            Record record = result.next();
            list.add(record.get("nbTitre") + " - " + record.get("nom").asString());
        }
        return list;
    }
    
    public void close() {
        driver.close();
    }
}
