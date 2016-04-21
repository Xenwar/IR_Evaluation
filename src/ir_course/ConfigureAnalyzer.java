package ir_course;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

public class ConfigureAnalyzer {
	// Either empty Or default
	private CharArraySet stopSet;

	private int morphologySelector;
	private boolean stopWordsSelector;
	private Analyzer analyzer = null;

	// stop = true => use anyalyzer's default stop word set
	// morpho = 1 => standard analyzer, 2 = English
	ConfigureAnalyzer(int morpho, boolean stop) {
		Analyzer tmp = null;
		this.setMorphologySelector(morpho);
		this.setStopWordsSelector(stop);
		this.setAnalyzer(tmp);
	}

	// Setters and getters
	public CharArraySet getStopSet() {
		return stopSet;
	}

	public void setStopSet(CharArraySet stopSet) {
		this.stopSet = stopSet;
	}

	public int getMorphologySelector() {
		return morphologySelector;
	}

	public void setMorphologySelector(int morphologySelector) {
		this.morphologySelector = morphologySelector;
	}

	public boolean getStopWordsSelector() {
		return stopWordsSelector;
	}

	public void setStopWordsSelector(boolean stopWordsSelector) {
		this.stopWordsSelector = stopWordsSelector;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(Analyzer analyzer) {
		// Set up StandardAnalyzer
		if (this.getMorphologySelector() == 0) {
			// remove stop words
			if (this.stopWordsSelector) {
				CharArraySet defaultlist = StandardAnalyzer.STOP_WORDS_SET;
				this.setStopSet(defaultlist);
				analyzer = new StandardAnalyzer(this.getStopSet());
			}
			// do not remove stop words
			else {
				CharArraySet disableRemoval = CharArraySet.EMPTY_SET;
				this.setStopSet(disableRemoval);
				analyzer = new StandardAnalyzer(this.getStopSet());
			}
		} //
			// Set up EnglishAnalyzer
		if (this.getMorphologySelector() == 1) {
			// remove stop words
			if (this.stopWordsSelector) {
				CharArraySet engDefaultlist = EnglishAnalyzer.getDefaultStopSet();
				this.setStopSet(engDefaultlist);
				analyzer = new EnglishAnalyzer(this.getStopSet());
			}
			// do not remove stop words
			else {
				CharArraySet disableRemoval = CharArraySet.EMPTY_SET;
				this.setStopSet(disableRemoval);
				analyzer = new EnglishAnalyzer(this.getStopSet());
			}
		} //
		this.analyzer = analyzer;
	}

	@Override
	public String toString() {
		String retunthis = "\nAnalyzer :\t" + this.getAnalyzer().getClass().getSimpleName() + "  "
				+ this.getAnalyzer().getVersion() + "\nStop Word List:\t"
				+ (this.getStopSet().size() > 0 ? this.getStopSet().toString() : "disabled");
		return retunthis;
	}// end of toString

	public static void main(String[] args) {
		int chooseAnalyzer[] = { 0, 1 };
		boolean stopOrNot[] = { true, false };
		// for standard analyzers.
		for (int analyzer : chooseAnalyzer)
			for (boolean stop : stopOrNot) {
				String info = new ConfigureAnalyzer(analyzer, stop).toString();
				System.out.println(info);
			}
	}// end of main method for Testing purposes.
}// end of class configAnalyzer
