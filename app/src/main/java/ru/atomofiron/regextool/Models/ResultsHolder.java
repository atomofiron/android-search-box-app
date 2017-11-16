package ru.atomofiron.regextool.Models;

import java.util.ArrayList;

public final class ResultsHolder {
	private static ArrayList<Result> results = null;

	private ResultsHolder() {
	}

	public static void setResults(ArrayList<Result> results) {
		ResultsHolder.results = results;
	}

	public static ArrayList<Result> getResults() {
		ArrayList<Result> results = ResultsHolder.results;

		resetResults();

		return results;
	}

	public static void resetResults() {
		results = null;
	}
}
