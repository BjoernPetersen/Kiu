package com.github.bjoernpetersen.q.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.*

private val TAG = LoginActivity::class.java.simpleName

class LoginActivity : AppCompatActivity() {

    private var login: Button? = null
    private var userName: EditText? = null
    private var password: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setTitle(R.string.title_login)

        password = (findViewById(R.id.password) as EditText).apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                    changePassword(s.toString())
                    error = null
                }
            })
        }

        userName = (findViewById(R.id.username) as EditText).apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                    error = null
                }
            })
        }

        login = (findViewById(R.id.login_button) as Button).apply {
            setOnClickListener { login() }
        }
    }

    private fun changePassword(password: String) {
        Config.password = password.trim()
    }

    override fun onDestroy() {
        this.password = null
        this.userName = null
        this.login = null
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        checkWifiState(this)
    }

    private fun login() {
        val userName = this.userName?.text?.toString()?.trim() ?: return
        if (userName.isEmpty()) {
            this.userName?.error = getString(R.string.error_empty)
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
                    userName?.error = getString(R.string.error_username_taken)
                }
                else -> {
                    Log.wtf(TAG, "Registering failed for reason " + reason)
                    userName?.error = "Not sure what went wrong"
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
                    userName?.error = getString(R.string.error_username_taken)
                }
                else -> {
                    Log.wtf(TAG, "Login failed for reason " + reason)
                    userName!!.error = "Not sure what went wrong"
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
        userName?.isEnabled = enable
        password?.isEnabled = enable
        login?.isEnabled = enable
    }
}
