package com.example.to_dolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ADD_TASK = 1;
    private static final int REQUEST_CODE_EDIT_TASK = 2;
    private static final int EDIT_TASK_REQUEST_CODE = 2;
    private int currentSortType = 0; // 0 - по умолчанию
    private int currentMin = 0;
    private int currentMax = 100;

    private TaskRepository repository;
    private TaskAdapter adapter;
    private boolean isFirstLaunch = true;
    private boolean isFilterDialogShown = false;
    public interface OnTaskLongClickListener {

        void onTaskLongClick(Task task, int position);
    }
    private List<Task> tasks = new ArrayList<>();
    private RecyclerView recyclerView;

    private OnTaskLongClickListener longClickListener;

    public void setOnTaskLongClickListener(OnTaskLongClickListener listener) {
        this.longClickListener = listener;
    }
    private static final String PREFS_NAME = "tasks_prefs";
    private static final String KEY_TASKS = "tasks_json";
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new TaskRepository(this);
        tasks = repository.getAllTasks();

        recyclerView = findViewById(R.id.tasksRecyclerView);
        adapter = new TaskAdapter(tasks);
        recyclerView.setAdapter(adapter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        RecyclerView recyclerView = findViewById(R.id.tasksRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView = findViewById(R.id.tasksRecyclerView);
        adapter = new TaskAdapter(tasks);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (savedInstanceState != null) {
            isFirstLaunch = savedInstanceState.getBoolean("isFirstLaunch", true);
            List<Task> savedTasks = (List<Task>) savedInstanceState.getSerializable("tasks");
            if (savedTasks != null) {
                adapter.setTasks(savedTasks);
            }
        }

        if (adapter.getItemCount() == 0 && isFirstLaunch) {
            addExampleTask();
            isFirstLaunch = false;
        }

        findViewById(R.id.addButton).setOnClickListener(v ->
                startActivityForResult(
                        new Intent(this, CreateTaskActivity.class),
                        REQUEST_CODE_ADD_TASK
                )
        );

        findViewById(R.id.filterIcon).setOnClickListener(v -> showFilterDialog());
        loadTasks();
    }
    private void saveTasks() {
        List<Task> tasks = adapter.getTasks();
        String json = gson.toJson(tasks);
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_TASKS, json)
                .apply();
    }

    private void showEditTaskDialog(Task task, int position) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        final EditText editTitle = new EditText(this);
        editTitle.setHint("Название задачи");
        editTitle.setText(task.getTitle());
        layout.addView(editTitle);

        final EditText editDescription = new EditText(this);
        editDescription.setHint("Описание задачи");
        editDescription.setText(task.getDescription());
        layout.addView(editDescription);

        new AlertDialog.Builder(this)
                .setTitle("Редактировать задачу")
                .setView(layout)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    task.setTitle(editTitle.getText().toString());
                    task.setDescription(editDescription.getText().toString());
                    adapter.notifyItemChanged(position);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void loadTasks() {
        String json = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_TASKS, null);
        if (json != null) {
            List<Task> tasks = gson.fromJson(json, new TypeToken<List<Task>>(){}.getType());
            if (tasks != null) adapter.setTasks(tasks);
        }
    }

    private void addExampleTask() {
        Task exampleTask = new Task();
        exampleTask.setTitle("Пример задачи");
        exampleTask.setDescription("Нажмите + чтобы добавить новую");
        exampleTask.setProgress(30);
        exampleTask.setPriority(3);
        exampleTask.setGradientColors(new int[]{Color.RED, Color.BLUE});
        exampleTask.setExample(true);
        adapter.addTask(exampleTask);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_ADD_TASK && data != null && data.hasExtra("task")) {
                Task newTask = (Task) data.getSerializableExtra("task");
                if (newTask != null) {
                    adapter.addTask(newTask);
                    adapter.removeExampleTask();
                }
            }
            else if (requestCode == EDIT_TASK_REQUEST_CODE) {
                Task updatedTask = (Task) data.getSerializableExtra("task");
                int position = data.getIntExtra("position", -1);
                if (position != -1) {
                    adapter.updateTask(position, updatedTask);
                }
            }
            else if (requestCode == REQUEST_CODE_EDIT_TASK && data.hasExtra("edited_task")) {
                Task editedTask = (Task) data.getSerializableExtra("edited_task");
                int position = data.getIntExtra("edit_position", -1);
                if (editedTask != null && position != -1) {
                    adapter.updateTask(position, editedTask);
                    if (adapter.isFilterActive()) {
                        adapter.applyCurrentFilter();
                    }
                }
            }
        }
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);

        Spinner sortSpinner = dialogView.findViewById(R.id.sortSpinner);
        RangeSlider rangeSlider = dialogView.findViewById(R.id.rangeSlider);
        TextView tvMin = dialogView.findViewById(R.id.tvMin);
        TextView tvMax = dialogView.findViewById(R.id.tvMax);
        Button btnApply = dialogView.findViewById(R.id.btnApplyFilter);



        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.sort_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        sortSpinner.setSelection(currentSortType);
        rangeSlider.setValues((float) currentMin, (float) currentMax);
        tvMin.setText("Мин: " + currentMin + "%");
        tvMax.setText("Макс: " + currentMax + "%");

        builder.setView(dialogView);

        builder.setNegativeButton("Сбросить", (dialog, which) -> {
            currentSortType = 0;
            currentMin = 0;
            currentMax = 100;
            adapter.resetFilter();
        });

        AlertDialog dialog = builder.create();

        btnApply.setOnClickListener(v -> {
            currentSortType = sortSpinner.getSelectedItemPosition();
            List<Float> values = rangeSlider.getValues();
            currentMin = Math.round(values.get(0));
            currentMax = Math.round(values.get(1));
            adapter.applyFilterAndSorting(currentSortType, currentMin, currentMax);
            dialog.dismiss();
        });

        rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            tvMin.setText("Мин: " + Math.round(values.get(0)) + "%");
            tvMax.setText("Макс: " + Math.round(values.get(1)) + "%");
        });

        dialog.show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isFirstLaunch", isFirstLaunch);
        outState.putSerializable("tasks", new ArrayList<>(adapter.getTasks()));
    }
    @Override
    protected void onPause() {
        super.onPause();
        saveTasksAsync();
    }


    private void saveTasksAsync() {
        new Thread(() -> {
            List<Task> tasks = adapter.getTasks();
            String json = gson.toJson(tasks);
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_TASKS, json)
                    .apply();
        }).start();
    }
}