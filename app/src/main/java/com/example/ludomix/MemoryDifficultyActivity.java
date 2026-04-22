package com.example.ludomix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ludomix.R;

public class MemoryDifficultyActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "EXTRA_DIFFICULTY";
    public static final int DIFF_EASY = 0;
    public static final int DIFF_MEDIUM = 1;
    public static final int DIFF_HARD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_difficulty);

        Button btnEasy = (Button) findViewById(R.id.btnEasy);
        Button btnMedium = (Button) findViewById(R.id.btnMedium);
        Button btnHard = (Button) findViewById(R.id.btnHard);

        btnEasy.setOnClickListener(v -> startMemoryWith(DIFF_EASY));
        btnMedium.setOnClickListener(v -> startMemoryWith(DIFF_MEDIUM));
        btnHard.setOnClickListener(v -> startMemoryWith(DIFF_HARD));
    }

    private void startMemoryWith(int difficulty) {
        Intent intent = new Intent(this, MemoryGameActivity.class);
        intent.putExtra(EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
        finish();
    }
}
