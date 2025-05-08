package cz.ragnarok.ragnarok.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.ragnarok.ragnarok.rest.dto.ChromaDbQueryEntity;
import cz.ragnarok.ragnarok.rest.dto.ChromaDbResponseEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class Test {

    @Autowired
    @Qualifier("chromaDb")
    private VectorStore vectorStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Value("${vector-db.collection-id}")
    private String collectionId;

    @Value("${vector-db.address}")
    private String address;

    private final ObjectMapper mapper = new ObjectMapper();

    public float[] embedWithLegaleseAverage(String userQuestion,String legalQuestion) {

        float[] embUser  = embeddingModel.embed(new Document(userQuestion, Map.of()));
        float[] embLegal = embeddingModel.embed(new Document(legalQuestion, Map.of()));

        return average(embUser, embLegal);

    }


    private float[] average(float[] a, float[] b) {
        if (a.length != b.length) throw new IllegalArgumentException("Embedding size mismatch");
        float[] avg = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            avg[i] = (a[i] + b[i]) / 2f;
        }
        return avg;
    }


    public List<Document> search(String query, String legalQuery, Integer maxSize) throws Exception {
        float[] qEmb = embedWithLegaleseAverage(query,legalQuery);

        ChromaDbQueryEntity resp = queryChroma(qEmb, maxSize * 3);

        List<Document> chunks = new ArrayList<>();
        for (int i = 0; i < resp.getDocuments().get(0).size(); i++) {
            String text = resp.getDocuments().get(0).get(i);
            ChromaDbQueryEntity.Metadata m = resp.getMetadatas().get(0).get(i);
            Map<String,Object> meta = new HashMap<>();
            meta.put("paragraph",       m.getParagraph());
            meta.put("head",            m.getHead());
            meta.put("section",         m.getSection());
            meta.put("title",           m.getTitle());
            meta.put("paragraphSubtitle", m.getParagraphSubtitle());
            meta.put("part",            m.getPart());
            meta.put("designation",     m.getDesignation());
            meta.put("date",            m.getDate());

            Document d = new Document(text, meta);
            List<Float> embList = resp.getEmbeddings().get(0).get(i);
            float[] embArr = new float[embList.size()];
            for (int j = 0; j < embArr.length; j++) embArr[j] = embList.get(j);
            d.setEmbedding(embArr);

            chunks.add(d);
        }

        Map<String,Double> scoreByPara = new HashMap<>();
        for (Document c : chunks) {
            String pid = c.getMetadata().get("paragraph").toString();
            float[] emb = (float[])c.getMetadata().getOrDefault("embedding", c.getEmbedding());
            double sim = cosine(qEmb, emb);
            scoreByPara.merge(pid, sim, Double::sum);
        }

        List<String> topParas = scoreByPara.entrySet().stream()
                .sorted(Map.Entry.<String,Double>comparingByValue().reversed())
                .limit(maxSize)
                .map(Map.Entry::getKey)
                .toList();

        Map<String,StringBuilder> textByPara = new LinkedHashMap<>();
        for (Document c : chunks) {
            String pid = c.getMetadata().get("paragraph").toString();
            if (!topParas.contains(pid)) continue;
            textByPara.computeIfAbsent(pid, k->new StringBuilder())
                    .append(c.getFormattedContent()).append("\n");
        }

        Map<String,StringBuilder> textByPara2 = new LinkedHashMap<>();
        Map<String,Map<String, Object>> metadata = new LinkedHashMap<>();
        for (Document c : chunks) {
            String pid = c.getMetadata().get("paragraph").toString();
            if (!topParas.contains(pid)) continue;
            textByPara2.computeIfAbsent(pid, k->new StringBuilder())
                    .append(c.getContent()).append("\n");
            if(!metadata.containsKey(pid)){
                metadata.put(pid, c.getMetadata());
            }
        }

        List<Document> results = new ArrayList<>();
        for (var entry : textByPara2.entrySet()) {
            //Map<String,Object> meta = Map.of("paragraph", entry.getKey());
            results.add(new Document(entry.getValue().toString(), metadata.get(entry.getKey())));
        }
        return results;
    }

    private ChromaDbQueryEntity queryChroma(float[] qEmb, int topK) throws Exception {
        String url = address + "/api/v1/collections/" + collectionId + "/query";
        Map<String,Object> body = Map.of(
                "query_embeddings", List.of(qEmb),
                "n_results", topK,
                "include", List.of("documents","metadatas","embeddings", "distances")
        );
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type","application/json");
        post.setEntity(new StringEntity(mapper.writeValueAsString(body), StandardCharsets.UTF_8));
        try (var client = HttpClients.createDefault()) {
            String json = EntityUtils.toString(client.execute(post).getEntity(), StandardCharsets.UTF_8);
            return mapper.readValue(json, ChromaDbQueryEntity.class);
        }
    }

    private double cosine(float[] a, float[] b) {
        double dot=0, na=0, nb=0;
        for(int i=0;i<a.length;i++){
            dot += a[i]*b[i];
            na  += a[i]*a[i];
            nb  += b[i]*b[i];
        }
        return dot/Math.sqrt(na*nb);
    }

    public ChromaDbResponseEntity parseAndFlatten(String json) throws IOException {
        JsonNode root = mapper.readTree(json);

        // extract inner arrays
        JsonNode idsNode        = root.get("ids").get(0);
        JsonNode docsNode       = root.get("documents").get(0);
        JsonNode urisNode       = root.get("uris").get(0);
        JsonNode dataNode       = root.get("data").get(0);
        JsonNode metasNode      = root.get("metadatas").get(0);
        JsonNode includeNode    = root.get("included").get(0);
        JsonNode embsOuterNode  = root.get("embeddings").get(0);

        // convert to the flat types your DTO declares
        List<String> ids        = mapper.convertValue(idsNode,    new TypeReference<List<String>>() {});
        List<String> docs       = mapper.convertValue(docsNode,   new TypeReference<List<String>>() {});
        List<String> uris       = mapper.convertValue(urisNode,   new TypeReference<List<String>>() {});
        List<String> data       = mapper.convertValue(dataNode,   new TypeReference<List<String>>() {});
        List<ChromaDbResponseEntity.Metadata> metas =
                mapper.convertValue(metasNode,  new TypeReference<List<ChromaDbResponseEntity.Metadata>>() {});
        List<String> included   = mapper.convertValue(includeNode, new TypeReference<List<String>>() {});
        List<List<Float>> embs  = mapper.convertValue(embsOuterNode, new TypeReference<List<List<Float>>>() {});

        // build and return your unchanged DTO
        ChromaDbResponseEntity e = new ChromaDbResponseEntity();
        e.setIds(ids);
        e.setDocuments(docs);
        e.setUris(uris);
        e.setData(data);
        e.setMetadatas(metas);
        e.setIncluded(included);
        e.setEmbeddings(embs);
        return e;
    }


}
