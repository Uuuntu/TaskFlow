package com.example.to_dolist;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.to_dolist.R;
import com.skydoves.colorpickerview.ColorPickerView ;

import java.util.Arrays;
import java.util.Calendar;

public class CreateTaskActivity extends AppCompatActivity {
    private boolean isEditMode = false;
    private int editPosition = -1;
    boolean isEdit = false;
    private static final String KEY_COLORS = "gradient_colors";
    private int[] selectedColors = new int[]{Color.RED, Color.GREEN};
    private Task newTask = new Task();
    private Button btnPriority, btnDeadline, btnProgress, btnGradient, btnSave;

    private EditText etTitle, etDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        // Инициализация всех view ОДИН РАЗ в начале
        initViews();
        isEdit = getIntent().getBooleanExtra("isEdit", false);

        // Проверяем, открыты ли мы для редактирования
        if (getIntent().hasExtra("edit_task")) {
            isEditMode = true;
            editPosition = getIntent().getIntExtra("edit_position", -1);
            Task task = (Task) getIntent().getSerializableExtra("edit_task");

            if (task != null) {
                newTask = task; // Используем переданную задачу для редактирования
                selectedColors = task.getGradientColors();

                // Заполняем поля данными задачи
                etTitle.setText(task.getTitle() != null ? task.getTitle() : "");
                etDescription.setText(task.getDescription() != null ? task.getDescription() : "");
                btnProgress.setText(task.getProgress() + "%");
                btnPriority.setText(String.valueOf(task.getPriority()));

                // Устанавливаем дедлайн если он есть
                if (task.getDeadline() != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(task.getDeadline());
                    btnDeadline.setText(String.format("%d/%d/%d",
                            cal.get(Calendar.DAY_OF_MONTH),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.YEAR)));
                }

                // Меняем текст кнопки сохранения
                btnSave.setText("Сохранить изменения");

                // Устанавливаем градиент
                //updateGradientPreview();
            }
        }

        if (savedInstanceState != null) {
            selectedColors = savedInstanceState.getIntArray(KEY_COLORS);
        }

        setupClickListeners();
        //updateButtonGradient(null);
    }

    private void initViews() {
        etTitle = findViewById(R.id.taskTitle);
        etDescription = findViewById(R.id.taskDescription);
        btnPriority = findViewById(R.id.btnPriority);
        btnDeadline = findViewById(R.id.btnDeadline);
        btnProgress = findViewById(R.id.btnProgress);
        //btnGradient = findViewById(R.id.btnGradient);
        btnSave = findViewById(R.id.saveButton);
        Button btnCancel = findViewById(R.id.cancelButton);

        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        btnPriority.setOnClickListener(v -> showPriorityDialog());
        btnDeadline.setOnClickListener(v -> showDatePickerDialog());
        btnProgress.setOnClickListener(v -> showProgressDialog());
        //btnGradient.setOnClickListener(v -> showColorDialog());
        btnSave.setOnClickListener(v -> saveTask());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(KEY_COLORS, selectedColors);
    }

    /*private void updateButtonGradient(GradientDrawable oldDrawable) {
        GradientDrawable newDrawable = createGradientDrawable();
        if (oldDrawable != null) {
            TransitionDrawable transition = new TransitionDrawable(new Drawable[]{oldDrawable, newDrawable});
            btnSave.setBackground(transition);
            transition.startTransition(300);
        } else {
            btnSave.setBackground(newDrawable);
        }
        // Установить текст белым
        btnSave.setTextColor(Color.WHITE);
    }*/

    private GradientDrawable createGradientDrawable() {
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                selectedColors
        );
        gradient.setCornerRadius(360f);
        return gradient;
    }

    private int getDominantColor() {
        int r = (Color.red(selectedColors[0]) + Color.red(selectedColors[1])) / 2;
        int g = (Color.green(selectedColors[0]) + Color.green(selectedColors[1])) / 2;
        int b = (Color.blue(selectedColors[0]) + Color.blue(selectedColors[1])) / 2;
        return Color.rgb(r, g, b);
    }

    private void setTextColorBasedOnLuminance(int color) {
        double luminance = 0.2126 * Color.red(color) + 0.7152 * Color.green(color) + 0.0722 * Color.blue(color);
        // Порог 180 вместо 128 — чтобы чаще выбирался белый текст
        btnSave.setTextColor(luminance > 180 ? Color.BLACK : Color.WHITE);
    }

    /*private void updateGradientPreview() {
        GradientDrawable gradient = createGradientDrawable();
        btnGradient.setBackground(gradient);
    }*/

    private void showPriorityDialog() {
        String[] priorities = new String[10];
        for (int i = 0; i < 10; i++) priorities[i] = String.valueOf(i + 1);

        new AlertDialog.Builder(this)
                .setTitle("Выберите важность")
                .setItems(priorities, (dialog, which) -> {
                    btnPriority.setText(priorities[which]);
                    newTask.setPriority(which + 1);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    newTask.setDeadline(calendar.getTime());
                    btnDeadline.setText(String.format("%d/%d/%d", day, month + 1, year));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showProgressDialog() {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Введите процент")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    int progress = Integer.parseInt(input.getText().toString());
                    if (progress >= 0 && progress <= 100) {
                        btnProgress.setText(progress + "%");
                        newTask.setProgress(progress);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    /*private void showColorDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.color_picker_dialog, null);
        ColorPickerView colorPicker1 = dialogView.findViewById(R.id.colorPicker1);
        ColorPickerView colorPicker2 = dialogView.findViewById(R.id.colorPicker2);
        new AlertDialog.Builder(this)
                .setTitle("Выберите цвета градиента")
                .setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> {
                    selectedColors[0] = colorPicker1.getColor();
                    selectedColors[1] = colorPicker2.getColor();
                    newTask.setGradientColors(selectedColors);
                    updateGradientPreview();
                    updateButtonGradient((GradientDrawable) btnSave.getBackground()); // Обновить градиент
                })
                .setNegativeButton("Отмена", null)
                .show();
    }*/


    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        if (!title.isEmpty()) {
            newTask.setTitle(title);
            newTask.setDescription(description);
            newTask.setGradientColors(selectedColors);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("task", newTask);
            if (isEditMode) {
                resultIntent.putExtra("position", editPosition);
            }
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            etTitle.setError("Введите название задачи");
        }
    }
}