package org.pairprogrammingai.apireciper.core.adviser;

import org.pairprogrammingai.apireciper.core.data.ApiInfo;
import org.pairprogrammingai.apireciper.core.data.SequenceInfo;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SearchLucene {

    private static boolean isAndSearch = true;

    public List<SequenceInfo> search(String indexPath, String searchText) {
        try {
            // 検索文字列を解析するためのパーサーを生成する
            //Analyzer analyzer = new StandardAnalyzer();
            Analyzer analyzer = new EnglishAnalyzer();

            // 検索対象のフィールドを第二引数で指定している
            QueryParser parser = new QueryParser("contents", analyzer);
            if (isAndSearch) {
                parser.setDefaultOperator(QueryParser.AND_OPERATOR);
            }

            if (!searchText.isEmpty()) {
                // 検索文字列を解析する
                Query query = parser.parse(searchText);
//		        System.out.println(query.toString());
                // 検索で使用する IndexSearcher を生成する
                Directory indexDir = FSDirectory.open(Paths.get(indexPath));
                IndexReader indexReader = DirectoryReader.open(indexDir);
                IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                indexSearcher.setSimilarity(new ClassicSimilarity());

                TopScoreDocCollector result = TopScoreDocCollector.create(1000000);
                indexSearcher.search(query, result);

                TopDocs results = result.topDocs();
                //System.out.println(results.totalHits + "/" + indexReader.getDocCount("contents"));
                ScoreDoc[] scoreDocs = results.scoreDocs;

                List<SequenceInfo> models = new ArrayList<SequenceInfo>();
                for (ScoreDoc scoreDoc : scoreDocs) {
                    Document doc = indexSearcher.doc(scoreDoc.doc);
                    String seq = doc.get("seq");
                    SequenceInfo seqInfo = getSequenceInfo(seq);
                    if (seqInfo.getApiCount() != 0) {
                        models.add(seqInfo);
                    }
                }
//		        System.out.println(models.size());
                return models;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SequenceInfo getSequenceInfo(String seq){
        SequenceInfo newSeqInfo = new SequenceInfo("", new ApiInfo("", "", 0, 0, 0));
        String[] apis = seq.split("\n");
        for(int i = 0; i < apis.length; i++){
            String[] apiInfo = apis[i].split("->");
            if(apiInfo.length == 3){
                if(Integer.parseInt(apiInfo[2]) == 1){
                    // Method Only(except Variable and Enum)
                    ApiInfo newApiInfo = new ApiInfo(apiInfo[0], apiInfo[1], Integer.parseInt(apiInfo[2]), 0, 0);
                    newSeqInfo.add(newApiInfo);
                }
            }
        }
        return newSeqInfo;
    }
}
