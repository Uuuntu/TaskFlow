package com.example.to_dolist;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    public interface OnTaskClickListener {
        void onTaskClick(Task task, int position);
        void onTaskLongClick(Task task, int position);
    }

    private List<Task> tasks;
    private OnTaskClickListener listener;
    private List<Task> allTasks = new ArrayList<>();
    private List<Task> filteredTasks = new ArrayList<>();
    private boolean isFilterActive = false;
    private int lastSortType = 0;
    private int lastMinProgress = 0;
    private boolean isFilterDialogShown = false;
    private int lastMaxProgress = 100;

    private int currentSortType = 0;
    private int currentMin = 0;
    private int currentMax = 100;

    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
        this.listener = listener;
    }

    public void addTask(Task task) {
        // long id = repository.addTask(task);
        // task.setId(id);
        tasks.add(task);
        allTasks.add(task);
        filteredTasks.add(task);
        notifyDataSetChanged();
    }


    public void setTasks(List<Task> newTasks) {
        tasks.clear();
        allTasks.clear();
        filteredTasks.clear();
        if (newTasks != null) {
            tasks.addAll(newTasks);
            allTasks.addAll(newTasks);
            filteredTasks.addAll(newTasks);
        }
        if (isFilterActive) applyCurrentFilter();
        else notifyDataSetChanged();
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }

    public void updateTask(int position, Task updatedTask) {
        // Обновляем в основном списке
        allTasks.set(position, updatedTask);
        // Если фильтр активен — применяем фильтр заново
        if (isFilterActive) {
            applyCurrentFilter();
        } else {
            notifyItemChanged(position);
        }
    }

    public void removeTask(int position) {
        if (isFilterActive) {
            if (position < 0 || position >= filteredTasks.size()) return;
            Task toRemove = filteredTasks.get(position);
            int origPos = tasks.indexOf(toRemove);
            if (origPos != -1) tasks.remove(origPos);
            allTasks.remove(toRemove);
            filteredTasks.remove(position);
            notifyDataSetChanged();
        } else {
            if (position < 0 || position >= tasks.size()) return;
            Task toRemove = tasks.get(position);
            tasks.remove(position);
            allTasks.remove(toRemove);
            notifyDataSetChanged();
        }
    }

    public void removeExampleTask() {
        if (!tasks.isEmpty() && tasks.get(0).isExample()) {
            Task example = tasks.get(0);
            tasks.remove(0);
            allTasks.remove(example);
            filteredTasks.remove(example);
            notifyDataSetChanged();
        }
    }

    public void applyFilterAndSorting(int sortType, int min, int max) {
        isFilterActive = true;
        lastSortType = sortType;
        lastMinProgress = min;
        lastMaxProgress = max;

        currentSortType = sortType;
        currentMin = min;
        currentMax = max;

        filteredTasks.clear();
        for (Task task : allTasks) {
            if (task.getProgress() >= min && task.getProgress() <= max) {
                filteredTasks.add(task);
            }
        }

        switch (sortType) {
            case 0: // По важности
                Collections.sort(filteredTasks, (a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
                break;
            case 1: // По дате
                Collections.sort(filteredTasks, (a, b) -> {
                    Date d1 = a.getDeadline(), d2 = b.getDeadline();
                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return 1;
                    if (d2 == null) return -1;
                    return d1.compareTo(d2);
                });
                break;
            case 2: // По порядку добавления
            default:
                break;
        }
        notifyDataSetChanged();
    }

    public void resetFilter() {
        isFilterActive = false;
        notifyDataSetChanged();
    }


    public void applyCurrentFilter() {
        // Например, фильтр по важности (priority >= 8)
        filteredTasks.clear();
        for (Task t : allTasks) {
            if (t.getPriority() >= 8) filteredTasks.add(t);
        }
        notifyDataSetChanged();
    }

    public boolean isFilterActive() {
        return isFilterActive;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = isFilterActive ? filteredTasks.get(position) : tasks.get(position);
        holder.taskNumber.setText(String.valueOf(position + 1));
        holder.taskTitle.setText(task.getTitle());


        String desc = task.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            holder.taskDescription.setVisibility(View.GONE);
        } else {
            holder.taskDescription.setText(desc);
            holder.taskDescription.setVisibility(View.VISIBLE);
        }

        // 1. Создаём новый GradientDrawable для прогресса
        int[] colors = task.getGradientColors();
        if (colors == null || colors.length != 2) {
            colors = new int[]{Color.RED, Color.GREEN};
        }
        GradientDrawable progressGradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, colors);
        progressGradient.setCornerRadius(20f);

        // 2. Оборачиваем в ClipDrawable
        ClipDrawable clip = new ClipDrawable(progressGradient, Gravity.START, ClipDrawable.HORIZONTAL);

        // 3. Создаём LayerDrawable (фон + прогресс)
        Drawable background = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.progress_background);
        LayerDrawable layer = new LayerDrawable(new Drawable[]{background, clip});
        layer.setId(0, android.R.id.background);
        layer.setId(1, android.R.id.progress);

        // 4. Устанавливаем новый Drawable
        holder.progressBar.setProgressDrawable(layer);

        // 5. Сразу после этого устанавливаем прогресс
        holder.progressBar.setProgress(task.getProgress());

        holder.tvProgressPercent.setText(task.getProgress() + "%");
        holder.tvProgressPercent.setTextColor(getContrastColor(colors[0]));

        // Если хотите анимацию — делайте её только после установки Drawable и прогресса
        // (иначе анимация может не работать корректно)
        ObjectAnimator animator = ObjectAnimator.ofInt(holder.progressBar, "progress", task.getProgress());
        animator.setDuration(500);
        animator.start();

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTaskClick(task, position);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onTaskLongClick(task, position);
            return true;
        });

        holder.deleteButton.setOnClickListener(v -> removeTask(position));

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                colors
        );
        gradient.setCornerRadius(16f);
    }

    @Override
    public int getItemCount() {
        return isFilterActive ? filteredTasks.size() : tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskNumber, taskTitle, taskDescription, tvProgressPercent;
        ProgressBar progressBar;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskNumber = itemView.findViewById(R.id.taskNumber);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    private int getContrastColor(int color) {
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }



}