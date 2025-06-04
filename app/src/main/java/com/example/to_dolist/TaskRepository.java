package com.example.to_dolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private TaskDbHelper dbHelper;
    private TaskRepository repository;

    public TaskRepository(Context context) {
        dbHelper = new TaskDbHelper(context);
    }

    // Добавить задачу
    public long addTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskDbHelper.COLUMN_TITLE, task.getTitle());
        values.put(TaskDbHelper.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskDbHelper.COLUMN_PRIORITY, task.getPriority());
        long id = db.insert(TaskDbHelper.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    // Получить все задачи
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskDbHelper.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_DESCRIPTION));
                int priority = cursor.getInt(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_PRIORITY));
                tasks.add(new Task(id, title, description, priority)); // теперь ошибки не будет!
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }

    // Удалить задачу по id
    public void deleteTask(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TaskDbHelper.TABLE_NAME, TaskDbHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Обновить задачу
    public void updateTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskDbHelper.COLUMN_TITLE, task.getTitle());
        values.put(TaskDbHelper.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskDbHelper.COLUMN_PRIORITY, task.getPriority());
        db.update(TaskDbHelper.TABLE_NAME, values, TaskDbHelper.COLUMN_ID + "=?", new String[]{String.valueOf(task.getId())});
        db.close();
    }
}