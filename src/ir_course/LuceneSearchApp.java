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
import java.util.Iterator;
import java.util.List;

public class LuceneSearchApp {
	/*****************************************************************************
	 * fields - names are descriptive of their purpose.
	 * chooseAnalyzer: if 0 standard analyzer, if 1 English analyzer
	 * removingStops: if true, removes default stop words of the analyzer
	 ****************************************************************************/
	private int TASK_NUMBER = 13;
	private String folder_identifier;
	String[] RankMethod = { "VSM", "BM25" };
	private String filePath;
	int max_result_size = 500;
	boolean removingStops[] = { true, false };
	int chooseAnalyzer[] = { 0, 1 };// if needed KStemFilter

	/*****************************************************************************
	 * Getters and Setters.
	 ****************************************************************************/
	public LuceneSearchApp(String filepath) {
		this.filePath = filepath;

	}// ending constructor.

	public String getFolder_identifier() {
		return folder_identifier;
	}

	public void setFolder_identifier(String folder_identifier) {
		this.folder_identifier = folder_identifier;
	}

	public int getTASK_NUMBER() {
		return TASK_NUMBER;
	}

	public void setTASK_NUMBER(int tASK_NUMBER) {
		TASK_NUMBER = tASK_NUMBER;
	}

	public String[] getRankMethod() {
		return RankMethod;
	}

	public void setRankMethod(String[] rankMethod) {
		this.RankMethod = rankMethod;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getMax_result_size() {
		return max_result_size;
	}

	public void setMax_result_size(int max_result_size) {
		this.max_result_size = max_result_size;
	}

	public boolean[] getRemovingStops() {
		return removingStops;
	}

	public void setRemovingStops(boolean[] removingStops) {
		this.removingStops = removingStops;
	}

	public int[] getChooseAnalyzer() {
		return chooseAnalyzer;
	}

	public void setChooseAnalyzer(int[] chooseAnalyzer) {
		this.chooseAnalyzer = chooseAnalyzer;
	}

	/****************************************************************************
	 * Driver program
	 ****************************************************************************/
	public static void main(String[] args) throws IOException {
		if (args.length > 0) {
			/*----------------------------------------------------------
			 * create object app=driver and report for printing purpose.
			 * create prefix of folder path for saving indexes. 
			 ------------------------------------------------------------*/
			//LuceneSearchApp app = new LuceneSearchApp("input/corpus_part2.xml");
			LuceneSearchApp app = new LuceneSearchApp(args[0]);
			Reporter report = new Reporter();
			String indexStorageFolderprefix;
			/*----------------------------------------------------------
			 * process different combinations of comparisons. 
			 * change the order of the for loops based on the parameters.
			 ------------------------------------------------------------*/
			for (int stdVsEng : app.getChooseAnalyzer())
				for (boolean stop : app.getRemovingStops())
					for (String method : app.getRankMethod()) {
						String aboutStop = (stop == true) ? "_WRemoved_" : "_WsNot_removed_";
						String aboutStemmer = (stdVsEng == 0) ? "_standard" : "_English";

						Evaluator engine = new Evaluator(stdVsEng, stop, app.getTASK_NUMBER());
						indexStorageFolderprefix = "./index/" + method + aboutStop + aboutStemmer;
						app.setFolder_identifier(indexStorageFolderprefix);

						DocumentCollectionParser parser = new DocumentCollectionParser();
						parser.parse(app.getFilePath());

						List<DocumentInCollection> docs = parser.getDocuments();
						engine.index(docs, app.getFolder_identifier());
						engine.setAll_queries(docs);
						int qCounter = 0;
						System.out.println("=========================================");
						System.out.println("\nMethod: \t" + method + engine.getConfianlyz().toString());
						Iterator<String> querys = engine.getAll_queries().iterator();
						while (querys.hasNext()) {
							qCounter++;
							String query = querys.next();
							/*----------------------------------------------------------
							 * make calculations.
							 ------------------------------------------------------------*/
							engine.search(query, app.getMax_result_size(), method, app.getFolder_identifier());
							engine.cal11ptPrecisionRecallCurve(engine.getResultSet());
							engine.calInterpolatedPrecisionRecallCurve();
							/*----------------------------------------------------------
							 * make reports.
							 ------------------------------------------------------------*/
							report.printQuery(query,qCounter);
							report.print_hist(engine,qCounter);
							report.print_precision_recall(engine,qCounter,100);
							engine.clearResultSet();
							report.printResults(engine.getResultSet());
							report.showAll_point_Interpolated_Precision_Recall(engine);
							report.ShowCalculate_11_point_Interpolated_Precision_Recall_Curve(engine);
						} // end of while
						System.out.println("=========================================");

					} // end of stop word variation
			indexStorageFolderprefix = "";
		} // end of arv.length > 0
		else
			System.out.println("ERROR: the path of a Corpus  file has to be passed as a command line argument.");
	}
}
