package ru.atomofiron.regextool.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class Result implements Iterator, Parcelable {
	public final String path;
	private int itPosition = 0;
	private final ArrayList<Integer> startPositions = new ArrayList<>();
	private final ArrayList<Integer> endPositions = new ArrayList<>();

	public Result(String path) {
		this.path = path;
	}

	private Result(Parcel in) {
		path = in.readString();
		int size = in.readInt();

		int[] startArr = new int[size];
		int[] endArr = new int[size];
		in.readIntArray(startArr);
		in.readIntArray(endArr);

		for (int it : startArr) startPositions.add(it);
		for (int it: endArr) endPositions.add(it);
	}

	public void add(int start, int end) {
		startPositions.add(start);
		endPositions.add(end);
	}

	public int size() {
		return startPositions.size();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean moveToFirst() {
		itPosition = 0;
		return hasNext();
	}

	public boolean moveToLast() {
		itPosition = startPositions.size() > 0 ? startPositions.size() - 1 : 0;
		return hasNext();
	}

	@Override
	public boolean hasNext() {
		return startPositions.size() > itPosition;
	}

	@Override
	public int[] next() {
		return hasNext() ? new int[] { startPositions.get(itPosition), endPositions.get(itPosition++) } : null;
	}

	public static final Creator<Result> CREATOR = new Creator<Result>() {
		@Override
		public Result createFromParcel(Parcel in) {
			return new Result(in);
		}

		@Override
		public Result[] newArray(int size) {
			return new Result[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(path);
		parcel.writeInt(startPositions.size());

		int[] startArr = new int[startPositions.size()];
		int[] endArr = new int[endPositions.size()];
		for (int j = 0; j < startPositions.size(); j++) {
			startArr[j] = startPositions.get(j);
			endArr[j] = endPositions.get(j);
		}
		parcel.writeIntArray(startArr);
		parcel.writeIntArray(endArr);
	}

	@Override
	public String toString() {
		return path;
	}

	public String toMarkdown() {
		String name = path.substring(path.lastIndexOf(File.separatorChar) + 1);
		int index = name.lastIndexOf('.');
		name = index > 0 ? name.substring(0, index) : name;
		return String.format("[%1$s](%2$s)  \n", name, path);
	}
}
