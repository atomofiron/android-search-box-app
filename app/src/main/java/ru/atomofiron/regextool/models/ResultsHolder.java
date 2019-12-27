package ru.atomofiron.regextool.models;

import java.util.ArrayList;

public final class ResultsHolder {
	private static ArrayList<Result> results = null;
	private static Result result = null;

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

	public static void setResult(Result result) {
		ResultsHolder.result = result;
	}

	public static Result getResult() {
		Result result = ResultsHolder.result;

		resetResult();

		return result;
	}

	public static void resetResult() {
		result = null;
	}
}
