package ir_course;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;

public class Reporter {
	/****************************************************************************
	 * prints current query
	 * Takes query string and query identifier 
	 ****************************************************************************/
	public void printQuery(String query, int qNum) {
		if (query != null) {
			System.out.print("Search query " + qNum + " :(" + query + "):\n");
		}
	}

	/****************************************************************************
	 * prints search results
	 * takes List of documents, sorts them and print each 
	 ****************************************************************************/
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

	/****************************************************************************
	 * prints pairs of precision-recall values of an interpolated curve. Takes
	 * an Evaluator object Use other tools to draw the graph.
	 ****************************************************************************/
	public void showAll_point_Interpolated_Precision_Recall(Evaluator eval) {

		System.out.println("=======11-point precision-recall curve========\n");
		for (double[] pr : eval.getInterpPrecionRecallCurve()) {
			System.out.printf("%f\t%f\n", pr[0], pr[1]);
		}
	}// all-point Interpolated Precision Recall

	/****************************************************************************
	 * prints pairs of precision-recall values of a curve. Takes an Evaluator
	 * object Use other tools to draw the graph.
	 ****************************************************************************/
	public void ShowCalculate_11_point_Interpolated_Precision_Recall_Curve(Evaluator eval) {
		System.out.println("=======11-point precision-recall curve========\n");
		for (double[] pr : eval.getPt11PrecionRecallCurve()) {
			System.out.printf("%f\t%f\n", pr[0], pr[1]);
		}
	}// end of printing precision-recall curve

	/****************************************************************************
	 * prints doc hits Takes an Evaluator object , and query identifier
	 ****************************************************************************/
	public void print_hist(Evaluator eval, int query) {
		System.out.println("\t\thit counts of query:  " + query + " : " + eval.getHits().length);

	}// end of printing hits

	/****************************************************************************
	 * prints data for precision recall, only one pair Takes an Evaluator object
	 * , query counter and upper limit on doc hit
	 ****************************************************************************/
	public void print_precision_recall(Evaluator eval, int query, int max_hit) {
		max_hit = max_hit > eval.getResultSet().size() ? eval.getResultSet().size() : max_hit;
		double[] onePrecisionRecall = eval.calPrecisionRecall(max_hit);
		System.out.println("Pr: " + onePrecisionRecall[0] + "\tRec:" + onePrecisionRecall[1]);

	}// end of printing precision and recall.
}
