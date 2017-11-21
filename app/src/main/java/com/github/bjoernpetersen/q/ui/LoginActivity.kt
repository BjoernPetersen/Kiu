package com.github.bjoernpetersen.q.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.*
import com.github.bjoernpetersen.q.api.action.DiscoverHost
import com.github.bjoernpetersen.q.tag
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*

private val TAG = LoginActivity::class.java.simpleName

class LoginActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    setTitle(R.string.title_login)

    password.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

      override fun afterTextChanged(s: Editable) {
        changePassword(s.toString())
        password.error = null
      }
    })

    username.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

      override fun afterTextChanged(s: Editable) {
        username.error = null
      }
    })

    login_button.setOnClickListener { login() }
  }

  private fun changePassword(password: String) {
    Config.password = password.trim()
  }

  override fun onResume() {
    super.onResume()
    checkWifiState(this)
  }

  private fun login() {
    val userName = username?.text?.toString()?.trim() ?: return
    if (userName.isEmpty()) {
      username.error = getString(R.string.error_empty)
      return
    }

    setInputEnabled(false)
    Config.user = userName
    Observable.fromCallable { Auth.apiKey }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          loginSuccess()
        }, {
          when (it) {
            is RegisterException -> {
              Log.d(tag(), "Could not register", it)
              loginFailure(it.reason)
            }
            is LoginException -> {
              Log.d(tag(), "Could not log in", it)
              loginFailure(it.reason)
            }
            is ChangePasswordException -> {
              Log.d(tag(), "Could not change password", it)
              loginFailure(it.reason)
            }
            is ConnectionException -> {
              Log.d(tag(), "Connection exception", it)
              Toast.makeText(this, R.string.trying_discover, Toast.LENGTH_SHORT).show()
              DiscoverHost().defaultAction({
                Toast.makeText(this, R.string.discover_success, Toast.LENGTH_SHORT).show()
                setInputEnabled(true)
              }, {
                Toast.makeText(this, R.string.connection_error, Toast.LENGTH_SHORT).show()
                setInputEnabled(true)
              })
            }
            is ServerErrorException -> {
              Log.d(tag(), "Server error", it)
              Toast.makeText(this, R.string.server_error, Toast.LENGTH_SHORT).show()
              setInputEnabled(true)
            }
            is AuthException -> Log.wtf(tag(), it)
          }
        })
  }

  private fun loginSuccess() {
    finish()
  }

  private fun loginFailure(reason: RegisterException.Reason) {
    setInputEnabled(true)

    when (reason) {
      RegisterException.Reason.TAKEN -> {
        username?.error = getString(R.string.error_username_taken)
      }
      else -> {
        Log.wtf(TAG, "Registering failed for reason " + reason)
        username?.error = "Not sure what went wrong"
      }
    }
  }

  private fun loginFailure(reason: LoginException.Reason) {
    setInputEnabled(true)

    when (reason) {
      LoginException.Reason.NEEDS_AUTH -> {
        password?.visibility = View.VISIBLE
        Toast.makeText(this@LoginActivity, R.string.needs_password, Toast.LENGTH_SHORT).show()
      }
      LoginException.Reason.WRONG_PASSWORD -> {
        password?.error = getString(R.string.wrong_password)
      }
      LoginException.Reason.WRONG_UUID -> {
        username?.error = getString(R.string.error_username_taken)
      }
      else -> {
        Log.wtf(TAG, "Login failed for reason " + reason)
        username!!.error = "Not sure what went wrong"
      }
    }
  }

  private fun loginFailure(reason: ChangePasswordException.Reason) {
    setInputEnabled(true)

    Log.wtf(TAG, "Got ChangePasswordException at login " + reason)
    Toast.makeText(this@LoginActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show()
  }

  private fun setInputEnabled(enable: Boolean) {
    username?.isEnabled = enable
    password?.isEnabled = enable
    login_button?.isEnabled = enable
  }
}
