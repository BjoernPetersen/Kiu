import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:kiu/bot/login_service.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/widget/loader.dart';

class LoginPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  bool _isLoading = false;
  bool _requiresPassword = false;
  final _name = TextEditingController();
  InputError _nameError = InputError.none;
  final _password = TextEditingController();
  InputError _passError = InputError.none;

  @override
  void initState() {
    super.initState();
    _name.addListener(() => setState(() {
          _nameError = InputError.none;
          _passError = InputError.none;
          // _requiresPassword = false;
        }));
    _password.addListener(() => setState(() {
          _passError = InputError.none;
        }));
  }

  @override
  void dispose() {
    super.dispose();
    _name.dispose();
    _password.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(
          title: Text("Sign in"),
        ),
        floatingActionButton: _isLoading
            ? null
            : FloatingActionButton(
                child: Icon(Icons.navigate_next),
                onPressed: _performSignIn,
                tooltip: 'Continue',
              ),
        body: _isLoading ? Loader(text: "Please wait") : _createBody(context),
      );

  Widget _createBody(BuildContext context) {
    final size = MediaQuery.of(context).size;
    return SingleChildScrollView(
      child: Container(
        padding: EdgeInsets.symmetric(vertical: 20, horizontal: size.width / 8),
        child: Center(
          child: Column(children: _createInputs(size)),
        ),
      ),
    );
  }

  List<Widget> _createInputs(Size size) {
    final result = <Widget>[
      Image.asset(
        'assets/kiu.png',
        height: size.height / 3,
      ),
      SizedBox(height: size.height / 10),
      TextField(
        controller: _name,
        decoration: InputDecoration(
          hintText: "Enter your name",
          errorText: _nameError.text,
        ),
      ),
    ];

    if (_requiresPassword) {
      result.add(TextField(
        obscureText: true,
        controller: _password,
        decoration: InputDecoration(
          hintText: "Enter a password",
          errorText: _passError.text,
        ),
      ));
    }

    return result;
  }

  Future<void> _performSignIn() async {
    final text = _name.value.text.trim();
    if (text.isEmpty) {
      setState(() {
        _nameError = InputError.blank;
      });
    } else {
      setState(() {
        _isLoading = true;
      });

      final login = service<LoginService>();
      try {
        final password = _requiresPassword ? _password.value.text : null;
        final token = await login.login(text, password);
        Preference.token.setString(token);
      } on MissingBotException {
        Fluttertoast.showToast(msg: 'No bot selected');
        Navigator.pushNamed(context, "/selectBot");
      } on IOException {
        Fluttertoast.showToast(
            msg: "IO error. Please try again or choose different bot");
      } on ConflictException {
        setState(() {
          if (_requiresPassword) {
            _passError = InputError.wrong;
          } else {
            _nameError = InputError.conflict;
          }
        });
      } on WrongCredentialsException {
        setState(() {
          _passError = InputError.wrong;
        });
      } on MissingPasswordException {
        if (!_requiresPassword) {
          setState(() {
            _requiresPassword = true;
            _passError = InputError.blank;
          });
        }
      } finally {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }
}

enum InputError { none, blank, wrong, conflict }

extension on InputError {
  String get text {
    switch (this) {
      case InputError.none:
        return null;
      case InputError.blank:
        return "Can't be blank";
      case InputError.wrong:
        return "Wrong credentials";
      case InputError.conflict:
        return "Username taken";
    }
  }
}
