/*
 * Skeleton class for the Lucene search program implementation
 *
 * Created on 2011-12-21
 * * Jouni Tuominen <jouni.tuominen@aalto.fi>
 * 
 * Modified on 2015-30-12
 * * Esko Ikkala <esko.ikkala@aalto.fi>
 * 
 */
package ir_course;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
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

public class LuceneSearchApp {

	public static List<String> stopWords = Arrays.asList("0", "1", "2", "3",
			"4", "5", "6", "7", "8", "9", "000", "$", "about", "after", "all",
			"also", "an", "and", "another", "any", "are", "as", "at", "be",
			"because", "been", "before", "being", "between", "both", "but",
			"by", "came", "can", "come", "could", "did", "do", "does", "each",
			"else", "for", "from", "get", "got", "has", "had", "he", "have",
			"her", "here", "him", "himself", "his", "how", "if", "in", "into",
			"is", "it", "its", "just", "like", "make", "many", "me", "might",
			"more", "most", "much", "must", "my", "never", "now", "of", "on",
			"only", "or", "other", "our", "out", "over", "re", "said", "same",
			"see", "should", "since", "so", "some", "still", "such", "take",
			"than", "that", "the", "their", "them", "then", "there", "these",
			"they", "this", "those", "through", "to", "too", "under", "up",
			"use", "very", "want", "was", "way", "we", "well", "were", "what",
			"when", "where", "which", "while", "who", "will", "with", "would",
			"you", "your", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
			"k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w",
			"x", "y", "z");

	private double relevantDocumentCount = 0;
	public static int TASK_NUMBER = 13;

	public LuceneSearchApp() {

	}

	/*--------------------------------------Indexing-------------------------*/
	public void index(List<DocumentInCollection> docs, boolean stopOrNotStop,
			boolean porterEnglish) throws IOException {

		Directory indexdir = FSDirectory.open(Paths.get("./index"));
		Analyzer analyzer = null;
		CharArraySet stopSet = new CharArraySet(stopWords, true);
		// not porter/english stemmer , then choose between stop and standard
		// ones
		if (porterEnglish == false) {
			if (stopOrNotStop)
				analyzer = new StopAnalyzer(stopSet);
			else
				analyzer = new StandardAnalyzer();
		} else {// porter/english stemmer but check if you can you can use stop
				// words.
			if (stopOrNotStop) {
				analyzer = new EnglishAnalyzer(stopSet);
			} // end of chckeing stop workds.
			else {// create non-usable stop word list.
				List<String> uselessWords = Arrays.asList("asdfgh", "adfa");
				CharArraySet uselesslist = new CharArraySet(uselessWords, true);
				analyzer = new EnglishAnalyzer(uselesslist);
			}
		}

		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(OpenMode.CREATE); // update or overwrite existing
												// indexes Or-else duplicate
												// entries
		IndexWriter writer = new IndexWriter(indexdir, config);

		for (DocumentInCollection iter : docs) {
			Document doc = new Document();
			if (iter.getSearchTaskNumber() == TASK_NUMBER) {
				String relevance = "";
				if (iter.isRelevant()) {
					relevance = "1";
					relevantDocumentCount += 1;
				}
				doc.add(new TextField("title", iter.getTitle(), Field.Store.YES));
				doc.add(new TextField("abstract", iter.getAbstractText(),
						Field.Store.YES));
				doc.add(new TextField("relevance", relevance, Field.Store.YES));
				writer.addDocument(doc);
			}
		}
		writer.close();
	}

	/*--------------------------------------Searching-------------------------
	 * String morphological[] = a combination of options.
	 * the first element considers using stop words, if true
	 * the second one decides on using Porter Stemmer
	 * all subsequent elements refer to other stemmers. 
	 * ------------------------------------------------------------------------*/
	public List<Document> search(String query, int hitspage, String RankMethod,
			boolean removeStops, boolean porter) throws IOException {
		// List<String> results = new LinkedList<String>();
		List<Document> d = new LinkedList<Document>();

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
			this.checkQuery(query, bq, removeStops, porter);
			TopDocs docs = searcher.search(bq.build(), hitspage);

			ScoreDoc[] hits = docs.scoreDocs;
			System.out.println("number of hits: " + hits.length);

			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				d.add(i, searcher.doc(docId));
			}
		} catch (Exception e) {
			System.out.println("Got an Exception: " + e.getMessage());
		}

		return d;
	}

	public void checkQuery(String query, BooleanQuery.Builder bq,
			boolean stopOrNotStop, boolean porterEnglish) {

		Analyzer analyzer = null;
		CharArraySet stopSet = new CharArraySet(stopWords, true);

		// not porter/english stemmer , then choose between stop and standard
		// ones
		if (porterEnglish == false) {
			if (stopOrNotStop)
				analyzer = new StopAnalyzer(stopSet);
			else
				analyzer = new StandardAnalyzer();
		} else {// porter/english stemmer but check if you can you can use stop
				// words.
			if (stopOrNotStop) {
				analyzer = new EnglishAnalyzer(stopSet);
			} // end of chckeing stop workds.
			else {// create non-usable stop word list.
				List<String> uselessWords = Arrays.asList("asdfgh", "adfa");
				CharArraySet uselesslist = new CharArraySet(uselessWords, true);
				analyzer = new EnglishAnalyzer(uselesslist);
			}
		}

		QueryParser queryParser = new QueryParser("abstract", analyzer);
		Query queryparts = null;
		try {
			queryparts = queryParser.parse(query);
			// System.out.println(queryparts);
			;

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bq.add(queryparts, BooleanClause.Occur.MUST);
	}

	/*--------------------------------------print query-------------------------*/

	public void printQuery(String query) {
		if (query != null) {
			;// System.out.print("Search ("+query+"):\n");
		}
	}

	/*--------------------------------------print result set-------------------------*/
	public void printResults(List<Document> d_results) {
		List<String> results = new LinkedList<String>();
		if (d_results.size() > 0) {
			for (Document doc : d_results) {
				results.add(doc.get("title"));
			}
			Collections.sort(results);
			for (int i = 0; i < results.size(); i++)
				System.out.println(" " + (i + 1) + ". " + results.get(i));
		} else
			System.out.println("no results");
	}

	/*--------------------------------------calculate precision-------------------------*/
	public double[] calPrecisionRecall(List<Document> d_results, int limit) {
		double tp = 0;// true positive
		double fn = 0;// false negative
		double fp = 0;// false positive
		double[] PrecisionRecall = new double[2];
		for (int i = 0; i < limit; i++) {
			String relevance = d_results.get(i).get("relevance");
			if (relevance.equals("1"))
				tp = tp + 1;
		}
		fn = relevantDocumentCount - tp;
		fp = limit - tp;

		// calculate precision
		if ((tp + fp) != 0)
			PrecisionRecall[0] = tp / (tp + fp);
		// calculate recall
		if ((tp + fn) != 0)
			PrecisionRecall[1] = tp / (tp + fn);
		return PrecisionRecall;
	}

	// verbose: to decide if one should be shown with precision-recall curve
	public void calPrecisionRecallCurve(List<Document> d_results,
			boolean verbose) {
		int stepSize = 1;
		List<double[]> MaxPrecisionsAtEachRecall = new ArrayList<double[]>();
		List<double[]> PrecisionRecall = new ArrayList<double[]>();
		for (int i = 1; i < d_results.size(); i += stepSize) {
			PrecisionRecall.add(this.calPrecisionRecall(d_results, i));
		}
		// Find out at each recall level the maximal precision
		double recall = 1.0;
		List<Double> PrecisionsAtEachRecall = new ArrayList<Double>();
		for (double[] pr : PrecisionRecall) {
			if (pr[1] > recall) {
				MaxPrecisionsAtEachRecall.add(new double[] {
						Collections.max(PrecisionsAtEachRecall), recall });
				PrecisionsAtEachRecall.clear();
			}
			recall = pr[1];
			PrecisionsAtEachRecall.add(pr[0]);
			// System.out.printf("%f\t%f\n", pr[0], pr[1]);
		}
		MaxPrecisionsAtEachRecall.add(new double[] {
				Collections.max(PrecisionsAtEachRecall), recall });
		if (verbose) {
			System.out.println("=======precision-recall curve========\n");
			for (double[] pr : PrecisionRecall) {
				System.out.printf("%f\t%f\n", pr[0], pr[1]);
			}
			System.out.println("=======Max-precision-recall curve========\n");
			for (double[] pr : MaxPrecisionsAtEachRecall) {
				System.out.printf("%f\t%f\n", pr[0], pr[1]);
			}
		}
	}

	/*--------------------------------------Driver code-------------------------*/
	public static void main(String[] args) throws IOException {
		if (args.length > 0) {
			boolean removingStops[] = { false };
			boolean porter[] = { false };
			for (boolean stop : removingStops) {
				for (boolean port : porter) {
					// EnglishMinimalStemFilter, KStemFilter and
					// PorterStemFilter
					int number_of_queries = 3;// at least two or three
					int max_result_size = 500;// at least two or three
					LuceneSearchApp engine = new LuceneSearchApp();
					DocumentCollectionParser parser = new DocumentCollectionParser();
					parser.parse(args[0]);
					List<DocumentInCollection> docs = parser.getDocuments();
					engine.index(docs, stop, port);
					// at least 2-3 queries,randomly chosen
					Set<String> all_queries = new HashSet<String>();// saving
																	// all
																	// queries.
					for (DocumentInCollection iter : docs) {						
						String query = iter.getQuery();
						if (iter.getSearchTaskNumber() == TASK_NUMBER)
							all_queries.add(query);
					} // end of collecting queries.

					/*
					 * WE CANNOT HARD CODE THE QUERIES, as the test data could
					 * be different. List<String> all_queries = Arrays.asList(
					 * // "data visualize display dataset datasets",
					 * "complex data set display",
					 * "large data set visualization", "visualizing dataset");
					 */
					String[] RankMethod = { "VSM","BM25" }; // "BM25" or "VSM"
					for (String method : RankMethod) {
						System.out.println("Using " + method);
						Iterator<String> querys = all_queries.iterator();
						while (querys.hasNext()) {
							String query = querys.next();
							engine.printQuery(query);
							List<Document> d_results = engine.search(query,
									max_result_size, method, stop, port);
							engine.printResults(d_results);

							engine.calPrecisionRecallCurve(d_results, true);
						}
						System.out
								.println("==========================================");
						number_of_queries--;
						if (number_of_queries <= 0)
							break;// comment out if all available queries are to be made
					}
				} // end of stop word variation
			} // end of morphologicall variation
		} // end of arv.length > 0
		else
			System.out
					.println("ERROR: the path of a Corpus  file has to be passed as a command line argument.");
	}
}