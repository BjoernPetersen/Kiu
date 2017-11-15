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
    Thread(object : Runnable {
      override fun run() {
        Config.user = userName
        try {
          Auth.apiKey
          loginSuccess()
        } catch (e: RegisterException) {
          Log.d(TAG, "Could not register", e)
          loginFailure(e.reason)
        } catch (e: LoginException) {
          Log.d(TAG, "Could not login", e)
          loginFailure(e.reason)
        } catch (e: ChangePasswordException) {
          Log.d(TAG, "Could not change password", e)
          loginFailure(e.reason)
        } catch (e: ConnectionException) {
          val host = HostDiscoverer().call()
          if (host != null) {
            Config.host = host
            this.run()
          } else {
            runOnUiThread {
              setInputEnabled(true)
              Toast.makeText(this@LoginActivity, R.string.connection_error, Toast.LENGTH_SHORT)
                  .show()
            }
          }
        } catch (e: AuthException) {
          Log.wtf(TAG, e)
        }

      }
    }, "loginThread").start()
  }

  private fun loginSuccess() {
    runOnUiThread { finish() }
  }

  private fun loginFailure(reason: RegisterException.Reason) {
    runOnUiThread({
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
    })
  }

  private fun loginFailure(reason: LoginException.Reason) {
    runOnUiThread({
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
    })
  }

  private fun loginFailure(reason: ChangePasswordException.Reason) {
    runOnUiThread {
      setInputEnabled(true)

      Log.wtf(TAG, "Got ChangePasswordException at login " + reason)
      Toast.makeText(this@LoginActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show()
    }
  }

  internal fun setInputEnabled(enable: Boolean) {
    username?.isEnabled = enable
    password?.isEnabled = enable
    login_button?.isEnabled = enable
  }
}
