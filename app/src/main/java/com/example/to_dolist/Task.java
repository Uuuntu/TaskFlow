package com.example.to_dolist;

import android.graphics.Color;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Task implements Serializable {
    private String title = "";
    private String description = "";
    private int priority = 1;
    private Date deadline;
    private int progress = 0;
    private int[] gradientColors;
    private long id;
    private boolean isExample = false;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title != null ? title : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }
    public void setDescription(String description) { this.description = description; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }


    public int[] getGradientColors() {
        if (gradientColors == null || gradientColors.length != 2) {
            return new int[]{Color.RED, Color.GREEN}; // дефолт
        }
        return gradientColors;
    }

    public void setGradientColors(int[] colors) {
        this.gradientColors = colors;
    }


    public boolean isExample() {
        return isExample;
    }

    public void setExample(boolean example) {
        isExample = example;
    }
    public Task() {

    }

    public Task(long id, String title, String description, int priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
    }
    public Task copy() {
        Task copy = new Task();
        copy.setTitle(this.title);
        copy.setDescription(this.description);
        copy.setPriority(this.priority);
        copy.setDeadline(this.deadline != null ? new Date(this.deadline.getTime()) : null);
        copy.setProgress(this.progress);
        copy.setGradientColors(this.gradientColors.clone());
        copy.setExample(this.isExample);
        return copy;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
