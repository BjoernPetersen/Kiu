package com.github.bjoernpetersen.q.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.github.bjoernpetersen.jmusicbot.client.ApiException;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.api.Connection;

public class LoginActivity extends AppCompatActivity {

  private static final String TAG = LoginActivity.class.getSimpleName();

  private Button login;
  private EditText userName;
  private EditText password;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    setTitle(R.string.title_login);

    password = (EditText) findViewById(R.id.password);
    password.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        changePassword(s.toString());
        password.setError(null);
      }
    });
    userName = (EditText) findViewById(R.id.username);
    userName.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        userName.setError(null);
      }
    });
    login = (Button) findViewById(R.id.login_button);
    login.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        login();
      }
    });
  }

  private void changePassword(String password) {
    password = password.trim();
    Connection.get(this).getConfiguration().setPassword(password);
  }

  @Override
  protected void onDestroy() {
    this.userName = null;
    this.login = null;
    super.onDestroy();
  }

  private void login() {
    final String userName = this.userName.getText().toString().trim();
    if (userName.isEmpty()) {
      this.userName.setError(getString(R.string.error_empty));
      return;
    }

    setInputEnabled(false);
    final Connection connection = Connection.get(this);
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          connection.setUsername(userName);
          loginSuccess();
        } catch (ApiException e) {
          Log.e(TAG, "Could not set username", e);
          loginFailure(e.getCode());
        }
      }
    }, "loginThread").start();
  }

  private void loginSuccess() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        finish();
      }
    });
  }

  private void loginFailure(final int code) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setInputEnabled(true);

        if (code == 400) {
          userName.setError(getString(R.string.error_username_taken));
          return;
        }

        if (code == 401) {
          password.setVisibility(View.VISIBLE);
          Toast.makeText(LoginActivity.this, R.string.needs_password, Toast.LENGTH_SHORT).show();
          return;
        }

        if (code == 403) {
          password.setError(getString(R.string.wrong_password));
          return;
        }

        Toast.makeText(LoginActivity.this, "Login error: " + code, Toast.LENGTH_SHORT).show();
      }
    });
  }

  void setInputEnabled(boolean enable) {
    userName.setEnabled(enable);
    password.setEnabled(enable);
    login.setEnabled(enable);
  }


}
