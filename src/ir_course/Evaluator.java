package ir_course;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
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
	/*****************************************************************************
	 * fields - names are descriptive of their purpose.
	 ****************************************************************************/
	private Analyzer analyzer;
	private double relevantDocumentCount;
	private int TASK_NUMBER;
	private boolean r_stopw;
	private int selectAnalyzer;
	private Set<String> all_queries;
	private ScoreDoc[] hits;
	private ConfigureAnalyzer confianlyz;

	private List<double[]> InterpPrecionRecallCurve = new ArrayList<double[]>();
	private List<double[]> Pt11PrecionRecallCurve = new ArrayList<double[]>();
	private List<Document> resultSet = new LinkedList<Document>();
	/*****************************************************************************
	 * constructor
	 * creator passed Analyzer selector, word removal indicator and task number
	 ****************************************************************************/
	public Evaluator(int stdVsEng, boolean stop, int taskNumber) {
		this.setselectAnalyzer(stdVsEng);
		this.setR_stopw(stop);
		this.setAnalyzer(stdVsEng, stop);
		this.setRelevantDocumentCount(10);
		this.setTASK_NUMBER(taskNumber);
	}
	/*****************************************************************************
	 * Getters and Settors 
	 ****************************************************************************/
	public List<double[]> getPt11PrecionRecallCurve() {
		return Pt11PrecionRecallCurve;
	}

	public void setPt11PrecionRecallCurve(List<double[]> pt11PrecionRecallCurve) {
		Pt11PrecionRecallCurve = pt11PrecionRecallCurve;
	}

	public ScoreDoc[] getHits() {
		return hits;
	}

	public void setHits(ScoreDoc[] hits) {
		this.hits = hits;
	}

	public List<Document> getResultSet() {
		return resultSet;
	}

	public void clearResultSet() {
		resultSet.clear();
	}

	public Set<String> getAll_queries() {
		return this.all_queries;
	}

	public List<double[]> getInterpPrecionRecallCurve() {
		return InterpPrecionRecallCurve;
	}

	public void setInterpPrecionRecallCurve(List<double[]> interpPrecionRecallCurve) {
		InterpPrecionRecallCurve = interpPrecionRecallCurve;
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

	public Analyzer getAnalyzer() {
		return this.analyzer;
	}
	public ConfigureAnalyzer getConfianlyz() {
		return confianlyz;
	}

	public void setConfianlyz(ConfigureAnalyzer confianlyz) {
		this.confianlyz = confianlyz;
	}
	/*****************************************************************************
	 * collects all queries for a given task number in the corpse
	 ****************************************************************************/
	public void setAll_queries(List<DocumentInCollection> docs) {
		// at least 2-3 queries,randomly chosen
		all_queries = new HashSet<String>();
		for (DocumentInCollection iter : docs) {
			String query = iter.getQuery();
			if (iter.getSearchTaskNumber() == this.getTASK_NUMBER())
				(this.all_queries).add(query);
		} // end of collecting queries.*/
	}// end of setAll_queires.
	/*****************************************************************************
	 * project work group selector.
	 * hard coded on purpose. 
	 ****************************************************************************/
	public void setTASK_NUMBER(int tASK_NUMBER) {
		this.TASK_NUMBER = 13;
	}
	/*****************************************************************************
	 * delegates analyzer creation to ConfigureAnalyzer. 
	 * passed analyzer selector and stop word removal indicator. 
	 * hard coded on purpose. 
	 ****************************************************************************/
	public void setAnalyzer(int morpho, boolean stop) {
		this.setConfianlyz(new ConfigureAnalyzer(this.getselectAnalyzer(), this.isR_stopw()));
		this.setAnalyzer(this.getConfianlyz().getAnalyzer());
	}
	/****************************************************************************
	 * Indexing
	 * Takes document collections
	 * Takes path to folder for saving indexes, unique for each combination.
	 * @param indexStorageFolder
	 ****************************************************************************/
	public void index(List<DocumentInCollection> docs, String indexStorageFolder) throws IOException {
		/* Indexing Configuration */
		Directory indexdir = FSDirectory.open(Paths.get(indexStorageFolder));
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
				doc.add(new TextField("abstract", iter.getAbstractText(), Field.Store.YES));
				doc.add(new TextField("relevance", relevance, Field.Store.YES));
				writer.addDocument(doc);
			}
		}
		writer.close();
	}

	/****************************************************************************
	 * Searching.
	 * selects between BM25 and VSM, based on the caller
	 * delegates building the query
	 * saves result set. 
	 * 
	 * @param indexStorageFolder
	 ****************************************************************************/
	public List<Document> search(String query, int hitspage, String RankMethod, String indexStorageFolder)
			throws IOException {
		String indexPath = indexStorageFolder;

		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
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

			this.setHits(docs.scoreDocs);

			for (int i = 0; i < this.getHits().length; ++i) {
				int docId = this.getHits()[i].doc;
				this.resultSet.add(i, searcher.doc(docId));
			}
		} catch (Exception e) {
			System.out.println("Got an Exception: " + e.getMessage());
		}
		return this.resultSet;
	}

	/****************************************************************************
	 * selects analyzer, 
	 * takes query sting to build a boolean query
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
	 * calculate precision and recall.
	 ****************************************************************************/
	public double[] calPrecisionRecall(int max_hit) {
		double tp = 0;// true positive
		double fn = 0;// false negative
		double fp = 0;// false positive
		double[] onePrecisionRecall = new double[2];
		for (int i = 0; i < max_hit; i++) {
			String relevance = resultSet.get(i).get("relevance");
			if (relevance.equals("1"))
				tp = tp + 1;
		}
		fn = relevantDocumentCount - tp;
		fp = max_hit - tp;

		// calculate precision
		if ((tp + fp) != 0)
			onePrecisionRecall[0] = tp / (tp + fp);
		// calculate recall
		if ((tp + fn) != 0)
			onePrecisionRecall[1] = tp / (tp + fn);
		return onePrecisionRecall;
	}

	/*****************************************************************************
	 * calculate 11-point Interpolated Precision Recall Curve
	 ****************************************************************************/
	public void cal11ptPrecisionRecallCurve(List<Document> d_results) {
		// 11-pt recall level
		List<double[]> PrecisionRecall = new ArrayList<double[]>();
		double[] desired_recall_levels = { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0 };
		for (int i = 1; i < d_results.size(); i++) {
			PrecisionRecall.add(this.calPrecisionRecall(i));
		}
		for (double recall_level : desired_recall_levels) {
			double max_precision = 0.0;
			// computing interpolated precision at a certain recall_level
			// defined as
			// the highest precision found for any recall level >= recall_level
			// please refer to
			// http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-ranked-retrieval-results-1.html
			// for details
			for (double[] pr : PrecisionRecall) {
				double precision = pr[0];
				double recall = pr[1];
				if (recall >= recall_level) {
					if (precision > max_precision)
						max_precision = precision;
				}
			}
			this.getPt11PrecionRecallCurve().add(new double[] { recall_level, max_precision });
		}
	}

	/*****************************************************************************
	 * calculate all-point Interpolated Precision Recall Curve
	 ****************************************************************************/
	public void calInterpolatedPrecisionRecallCurve() {
		// 11-pt recall level
		List<double[]> PrecisionRecall = new ArrayList<double[]>();
		for (int i = 1; i < resultSet.size(); i++) {
			PrecisionRecall.add(this.calPrecisionRecall(i));
		}
		for (int i = 0; i < PrecisionRecall.size(); i++) {
			double recall_level = PrecisionRecall.get(i)[1];
			double max_precision = PrecisionRecall.get(i)[0];
			// computing interpolated precision at a certain recall_level
			// defined as
			// the highest precision found for any recall level >= recall_level
			// please refer to
			// http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-ranked-retrieval-results-1.html
			// for details
			for (int j = i + 1; j < PrecisionRecall.size(); j++) {
				double precision = PrecisionRecall.get(j)[0];
				double recall = PrecisionRecall.get(j)[1];
				if (recall >= recall_level) {
					if (precision > max_precision)
						max_precision = precision;
				}
			}
			this.getInterpPrecionRecallCurve().add(new double[] { recall_level, max_precision });
		}
	}// calculate all-point Interpolated Precision Recall Curve == NEWLY ADDED
}