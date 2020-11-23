package controllers;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.neo4j.driver.v1.Record;

import java.util.*;

public class MongoDBController {
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public MongoDBController() {
        mongoClient = new MongoClient("192.168.56.50");
        database = mongoClient.getDatabase("dbDocuments");
    }

    public void createBase(ArrayList<Record> articles) {
        MongoCollection<Document> mongoCollection = database.getCollection("index");
        for (Record r : articles) {
            int id = r.get("id").asInt();
            String title = r.get("titre").asString().toLowerCase();
            StringTokenizer st = new StringTokenizer(title, ",‘-:;.()+[]{}?! ");
            BasicBSONList list = new BasicBSONList();
            int i=0;
            while (st.hasMoreTokens()) {
                String key = st.nextToken().trim();
                list.put(i, key);
                i++;
            }
            mongoCollection.insertOne(new Document("idDocument", id).append("motsCles", list));
        }
        mongoCollection.createIndex(Indexes.ascending("motsCles"));
    }

    @SuppressWarnings("unchecked")
	public void createStructureMiroir() {
        MongoCollection<Document> indexInverse = database.getCollection("indexInverse");
        MongoCollection<Document> index = database.getCollection("index");
        for (Document d : index.find()) {
            ArrayList<String> motsCles = (ArrayList<String>) d.get("motsCles");
            for (String s : motsCles) {
                if (null != indexInverse.find(Filters.eq("mot", s)).first()) {
                    indexInverse.updateOne(
                            Filters.eq("mot", s),
                            Updates.addToSet("documents", d.get("idDocument"))
                    );
                } else {
                    BasicBSONList list = new BasicBSONList();
                    list.put(0, d.get("idDocument"));
                    indexInverse.insertOne(
                            new Document("mot", s)
                                    .append("documents", list)
                    );
                }
            }
        }
        indexInverse.createIndex(Indexes.ascending("mot"));
    }

    @SuppressWarnings("unchecked")
	public ArrayList<Integer> rechercheMotUnique(String word) {
        MongoCollection<Document> indexInverse = database.getCollection("indexInverse");
        Document document = indexInverse.find(Filters.eq("mot", word)).first();
        if (null != document)
            return (ArrayList<Integer>) document.get("documents");
        else
            return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
	public ArrayList<Integer> rechercheMotsMultiples(String[] words) {
        MongoCollection<Document> indexInverse = database.getCollection("indexInverse");
        AggregateIterable<Document> result = indexInverse.aggregate(Collections.singletonList(
                Aggregates.match(Filters.in("mot", words))));
        Set<Integer> docIdList = new HashSet<>();
        for (Document d : result) {
            docIdList.addAll((List<Integer>) d.get("documents"));
        }
        return new ArrayList<>(docIdList);
    }

    public void close() {
        mongoClient.close();
    }
}
