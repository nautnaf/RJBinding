package com.nautnaf.adk.binding.showcase;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.nautnaf.adk.binding.hook.LayoutInflaterHook;
import com.nautnaf.adk.binding.parse.ViewAttributeParser;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ViewAttributeParser parser = new ViewAttributeParser();
    LayoutInflater inflater = LayoutInflaterHook.hook(this, parser);
    setContentView(inflater.inflate(R.layout.activity_main, null));
  }
}
