package com.kesco.adk.binding.showcase;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.kesco.adk.binding.hook.LayoutInflaterHook;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LayoutInflaterHook.hook(this);
    LayoutInflater inflater = LayoutInflater.from(this);
    setContentView(inflater.inflate(R.layout.activity_main, null));
  }
}
