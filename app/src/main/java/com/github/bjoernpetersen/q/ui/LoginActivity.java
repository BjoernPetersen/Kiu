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
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.api.Auth;
import com.github.bjoernpetersen.q.api.AuthException;
import com.github.bjoernpetersen.q.api.ChangePasswordException;
import com.github.bjoernpetersen.q.api.Config;
import com.github.bjoernpetersen.q.api.ConnectionException;
import com.github.bjoernpetersen.q.api.HostDiscoverer;
import com.github.bjoernpetersen.q.api.LoginException;
import com.github.bjoernpetersen.q.api.RegisterException;

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
    Config.INSTANCE.setPassword(password);
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
    new Thread(new Runnable() {
      @Override
      public void run() {
        Config.INSTANCE.setUser(userName);
        try {
          Auth.INSTANCE.getApiKey();
          loginSuccess();
        } catch (RegisterException e) {
          Log.d(TAG, "Could not register", e);
          loginFailure(e.getReason());
        } catch (LoginException e) {
          Log.d(TAG, "Could not login", e);
          loginFailure(e.getReason());
        } catch (ChangePasswordException e) {
          Log.d(TAG, "Could not change password", e);
          loginFailure(e.getReason());
        } catch (ConnectionException e) {
          String host;
          if ((host = new HostDiscoverer().call()) != null) {
            Config.INSTANCE.setHost(host);
            this.run();
          } else {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                setInputEnabled(true);
                Toast.makeText(LoginActivity.this, R.string.connection_error, Toast.LENGTH_SHORT)
                    .show();
              }
            });
          }
        } catch (AuthException e) {
          Log.wtf(TAG, e);
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

  private void loginFailure(final RegisterException.Reason reason) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setInputEnabled(true);

        switch (reason) {
          case TAKEN:
            userName.setError(getString(R.string.error_username_taken));
            return;
          default:
            Log.wtf(TAG, "Registering failed for reason " + reason);
            userName.setError("Not sure what went wrong");
        }
      }
    });
  }

  private void loginFailure(final LoginException.Reason reason) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setInputEnabled(true);

        switch (reason) {
          case NEEDS_AUTH:
            password.setVisibility(View.VISIBLE);
            Toast.makeText(LoginActivity.this, R.string.needs_password, Toast.LENGTH_SHORT).show();
            return;
          case WRONG_PASSWORD:
            password.setError(getString(R.string.wrong_password));
            return;
          case WRONG_UUID:
            userName.setError(getString(R.string.error_username_taken));
            return;
          default:
            Log.wtf(TAG, "Login failed for reason " + reason);
            userName.setError("Not sure what went wrong");
        }
      }
    });
  }

  private void loginFailure(final ChangePasswordException.Reason reason) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setInputEnabled(true);

        switch (reason) {
          default:
            Log.wtf(TAG, "Got ChangePasswordException at login " + reason);
            Toast.makeText(LoginActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  void setInputEnabled(boolean enable) {
    userName.setEnabled(enable);
    password.setEnabled(enable);
    login.setEnabled(enable);
  }


}
