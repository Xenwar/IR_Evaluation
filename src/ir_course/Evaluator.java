package ir_course;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Evaluator {
	private Analyzer analyzer;
	private double relevantDocumentCount;
	private int TASK_NUMBER;
	private boolean r_stopw;
	private int selectAnalyzer;
	private Set<String> all_queries;
	public double[] onePrecisionRecall = new double[2];
	private List<double[]> MaxPrecisionsAtEachRecall = new ArrayList<double[]>();
	private List<Double> PrecisionsAtEachRecall = new ArrayList<Double>();
	private List<double[]> PrecisionRecall = new ArrayList<double[]>();
	private List<Document> resultSet = new LinkedList<Document>();
	public List<Document> getResultSet() {
		return resultSet;
	}

	private ScoreDoc[] hits;
	ConfigureAnalyzer confianlyz;

	public Set<String> getAll_queries() {
		return this.all_queries;
	}
	public void setAll_queries(List<DocumentInCollection> docs) {	
    // at least 2-3 queries,randomly chosen
	all_queries = new HashSet<String>();
	for (DocumentInCollection iter : docs) {
						String query = iter.getQuery();
						if (iter.getSearchTaskNumber() == this.getTASK_NUMBER())
							(this.all_queries).add(query);
					} // end of collecting queries.*/
	}//end of setAll_queires.

	public Evaluator(int stdVsEng,boolean stop,int taskNumber) {
		this.setselectAnalyzer(stdVsEng);
		this.setR_stopw(stop);
		this.setAnalyzer(stdVsEng,stop);
		this.setRelevantDocumentCount(10);
		this.setTASK_NUMBER(taskNumber);
	}
	
public double getRelevantDocumentCount() {
	return this.relevantDocumentCount;
}

public void setRelevantDocumentCount(double relevantDocumentCount) {
	this.relevantDocumentCount = relevantDocumentCount;
}

public int getTASK_NUMBER() {
	return this.TASK_NUMBER;
}

public void setTASK_NUMBER(int tASK_NUMBER) {
	this.TASK_NUMBER = 13;
}

public Analyzer getAnalyzer() {
	return this.analyzer;
}

public void setAnalyzer(int morpho,boolean stop) {
	this.confianlyz = new ConfigureAnalyzer(this.getselectAnalyzer(),this.isR_stopw());
	this.setAnalyzer(this.confianlyz.getAnalyzer());
}


public boolean isR_stopw() {
	return this.r_stopw;
}

public void setR_stopw(boolean r_stopw) {
	this.r_stopw = r_stopw;
}

public int getselectAnalyzer() {
	return this.selectAnalyzer;
}

public void setselectAnalyzer(int chooseAnalyzer) {
	this.selectAnalyzer = chooseAnalyzer;
}

public void setAnalyzer(Analyzer analyzer) {
	this.analyzer = analyzer;
}




/****************************************************************************
 *			Indexing
 ****************************************************************************/
public void index(List<DocumentInCollection> docs) throws IOException {
	/* Indexing Configuration */
	Directory indexdir = FSDirectory.open(Paths.get("./index"));
	IndexWriterConfig config = new IndexWriterConfig(this.getAnalyzer());
	// update or overwrite existing indexes Or-else duplicate entries
	config.setOpenMode(OpenMode.CREATE);
	IndexWriter writer = new IndexWriter(indexdir, config);
	/* Start indexing */
	for (DocumentInCollection iter : docs) {
		Document doc = new Document();
		if (iter.getSearchTaskNumber() == this.getTASK_NUMBER()) {
			String relevance = "";
			if (iter.isRelevant()) {
				relevance = "1";
				relevantDocumentCount += 1;
			}
			doc.add(new TextField("title", iter.getTitle(), Field.Store.YES));
			doc.add(new TextField("abstract", iter.getAbstractText(),Field.Store.YES));
			doc.add(new TextField("relevance", relevance, Field.Store.YES));
			writer.addDocument(doc);
		}
	}
	writer.close();
}
/****************************************************************************
 *			Searching.
 ****************************************************************************/
public List<Document> search(String query, int hitspage, String RankMethod) throws IOException {
	String indexPath = "./index";

	try {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		switch (RankMethod) {
		case "BM25":
			searcher.setSimilarity(new BM25Similarity());
			break;
		case "VSM":
			searcher.setSimilarity(new ClassicSimilarity());
			break;
		default:
			break;
		}

		BooleanQuery.Builder bq = new BooleanQuery.Builder();
		this.checkQuery(query, bq);
		TopDocs docs = searcher.search(bq.build(), hitspage);

		this.hits = docs.scoreDocs;

		for (int i = 0; i < this.hits.length; ++i) {
			int docId = this.hits[i].doc;
			this.resultSet.add(i, searcher.doc(docId));
		}
	} catch (Exception e) {
		System.out.println("Got an Exception: " + e.getMessage());
	}
	return this.resultSet;
}
/****************************************************************************
 *			Check query.
 ****************************************************************************/
public void checkQuery(String query, BooleanQuery.Builder bq) {
	QueryParser queryParser = new QueryParser("abstract", this.getAnalyzer());
	Query queryparts = null;
	try {
		queryparts = queryParser.parse(query);			
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	bq.add(queryparts, BooleanClause.Occur.MUST);
}

/****************************************************************************
 *			calculate precision and recall.
 ****************************************************************************/
public double[] calPrecisionRecall(List<Document> d_results, int limit) {
	double tp = 0;// true positive
	double fn = 0;// false negative
	double fp = 0;// false positive
	for (int i = 0; i < limit; i++) {
		String relevance = d_results.get(i).get("relevance");
		if (relevance.equals("1"))
			tp = tp + 1;
	}
	fn = relevantDocumentCount - tp;
	fp = limit - tp;

	// calculate precision
	if ((tp + fp) != 0)
		this.onePrecisionRecall[0] = tp / (tp + fp);
	// calculate recall
	if ((tp + fn) != 0)
		this.onePrecisionRecall[1] = tp / (tp + fn);
	return this.onePrecisionRecall;
}
/****************************************************************************
 *			calculate precision-recall- curve.
 ****************************************************************************/
// verbose: to decide if one should be shown with precision-recall curve
public void calPrecisionRecallCurve(List<Document> d_results) {
	int stepSize = 1;

	for (int i = 1; i < d_results.size(); i += stepSize) {
		this.PrecisionRecall.add(this.calPrecisionRecall(d_results, i));
	}
	// Find out at each recall level the maximal precision
	double recall = 1.0;
	for (double[] pr : this.PrecisionRecall) {
		if (pr[1] > recall) {
			MaxPrecisionsAtEachRecall.add(new double[] {
					Collections.max(this.PrecisionsAtEachRecall), recall });
			this.PrecisionsAtEachRecall.clear();
		}
		recall = pr[1];
		this.PrecisionsAtEachRecall.add(pr[0]);
		//System.out.printf("%f\t%f\n", pr[0], pr[1]);
	}
	this.MaxPrecisionsAtEachRecall.add(new double[] {
			Collections.max(this.PrecisionsAtEachRecall), recall });

}//end of calPrecisionRecallCurve

public void showMaxprecisionRecallCurve(){
	System.out.println("=======Max-precision-recall curve========\n");
	for (double[] pr : this.MaxPrecisionsAtEachRecall) {
		System.out.printf("%f\t%f\n", pr[0], pr[1]);
	}
}//end of printing Max-precision-recall curve
public void showPrecisionRecallCurve(){
	System.out.println("=======precision-recall curve========\n");
	for (double[] pr : this.PrecisionRecall) {
		System.out.printf("%f\t%f\n", pr[0], pr[1]);
	}
}//end of printing precision-recall curve
public void print_hist(int query){
	System.out.println("number of hits for Q = "+query +" : " + this.hits.length);

}
}