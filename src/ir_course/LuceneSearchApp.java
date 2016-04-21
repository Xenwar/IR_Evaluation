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
		private int TASK_NUMBER = 13;
		String[] RankMethod = { "VSM", "BM25" };
		private String filePath;
		int max_result_size = 500;
		boolean removingStops[] = { true,false };
		int chooseAnalyzer[] = {0,1};//if needed KStemFilter
		
		public LuceneSearchApp(String filepath) {
			this.filePath = filepath;
			
		}//ending constructor. 

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
	 *			Driver program
	 ****************************************************************************/
	public static void main(String[] args) throws IOException {
		if (args.length > -1) {
			LuceneSearchApp app = new LuceneSearchApp("input/corpus_part2.xml");
			Reporter report = new Reporter();
			
			for (String method : app.getRankMethod())
			for (boolean stop : app.getRemovingStops()) {
				for (int stdVsEng : app.getChooseAnalyzer()) {					
					 int qCounter = 0;

					Evaluator engine = new Evaluator(stdVsEng,stop,app.getTASK_NUMBER());
					DocumentCollectionParser parser = new DocumentCollectionParser();
					parser.parse(app.getFilePath());
					
					List<DocumentInCollection> docs = parser.getDocuments();
					engine.index(docs);
					engine.setAll_queries(docs);
					{
						System.out.println("Method: \n" + method + "Analyzer: " + engine.confianlyz.toString());
						Iterator<String> querys = engine.getAll_queries().iterator();
						while (querys.hasNext()) {
							qCounter++;
							String query = querys.next();
							report.printQuery(query,qCounter);
							engine.search(query,app.getMax_result_size(), method);
							engine.print_hist(qCounter);
							//report.printResults(engine.getResultSet());
							engine.calPrecisionRecallCurve(engine.getResultSet());
							engine.showMaxprecisionRecallCurve();
							engine.showPrecisionRecallCurve();
	
						}
						System.out
								.println("=========================================");

					}
				} // end of stop word variation
			} // end of morphologicall variation
		} // end of arv.length > 0
		else
			System.out
					.println("ERROR: the path of a Corpus  file has to be passed as a command line argument.");
	}
}