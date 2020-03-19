import 'package:fluttertoast/fluttertoast.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/login_service.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/page/overflow.dart';
import 'package:kiu/view/resources/messages.i18n.dart';
import 'package:kiu/view/widget/basic_awareness_body.dart';
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
  String _lastPass = "";
  InputError _passError = InputError.none;

  @override
  void initState() {
    super.initState();

    _name.text = Preference.username.getString();

    _name.addListener(() => setState(() {
          _nameError = InputError.none;
          _passError = InputError.none;
          Preference.username.setString(_name.text);
          _requiresPassword = false;
        }));
    _password.addListener(() {
      if (_lastPass != _password.text) {
        setState(() {
          _passError = InputError.none;
        });
      }
      _lastPass = _password.text;
    });
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
          title: Text(context.messages.page.login),
          actions: <Widget>[
            createOverflowItems(context, hidden: [
              Choice.logout,
              Choice.set_password,
              Choice.refresh_token,
            ]),
          ],
        ),
        floatingActionButton: _isLoading
            ? null
            : FloatingActionButton(
                child: Icon(Icons.navigate_next),
                onPressed: () => _performSignIn(context),
                tooltip: context.messages.common.submit,
              ),
        body: _isLoading
            ? Loader(text: context.messages.common.pleaseWait)
            : BasicAwarenessBody(child: _createBody(context)),
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
          hintText: context.messages.login.name,
          errorText: _nameError.text(context.messages.login.input),
        ),
      ),
    ];

    if (_requiresPassword) {
      result.add(TextField(
        obscureText: true,
        autofocus: true,
        controller: _password,
        decoration: InputDecoration(
          hintText: context.messages.login.password,
          errorText: _passError.text(context.messages.login.input),
        ),
      ));
    }

    return result;
  }

  Future<void> _performSignIn(BuildContext context) async {
    final name = _name.value.text.trim();
    if (name.isEmpty) {
      setState(() {
        _nameError = InputError.blank;
      });
    } else {
      setState(() {
        _isLoading = true;
      });

      final navigator = Navigator.of(context);
      final login = service<LoginService>();
      try {
        final password = _requiresPassword ? _password.value.text : null;
        final token = await login.login(name, password);
        Preference.username.setString(name);
        Preference.token.setString(token.accessToken);
        Preference.refresh_token.setString(token.refreshToken);
        service<ConnectionManager>().reset();
        navigator.pushReplacementNamed('/queue');
      } on MissingBotException {
        Fluttertoast.showToast(msg: context.messages.login.errorNoBot);
        navigator.pushNamed("/selectBot");
      } on IOException {
        Fluttertoast.showToast(msg: context.messages.login.errorIo);
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
  // ignore: missing_return
  String text(InputLoginMessages messages) {
    switch (this) {
      case InputError.none:
        return null;
      case InputError.blank:
        return messages.blank;
      case InputError.wrong:
        return messages.wrong;
      case InputError.conflict:
        return messages.conflict;
    }
  }
}
