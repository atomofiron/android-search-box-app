package ru.atomofiron.regextool.screens.finder.history.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = { ItemHistory.class }, version = 1)
public abstract class HistoryDatabase extends RoomDatabase {
   public abstract HistoryDao historyDao();
}