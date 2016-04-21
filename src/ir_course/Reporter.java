package ir_course;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;

public class Reporter {
	/****************************************************************************
	 *			print query.
	 ****************************************************************************/
	public void printQuery(String query,int qNum) {
		if (query != null) {
			System.out.print("Search query " + qNum +" +:("+query+"):\n");
		}
	}
	/****************************************************************************
	 *			printResults.
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
}
