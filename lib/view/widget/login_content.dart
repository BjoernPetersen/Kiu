import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class LoginContent extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _LoginContentState();
  }
}

class _LoginContentState extends State<LoginContent> {
  final _name = TextEditingController();

  @override
  void dispose() {
    super.dispose();
    _name.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.of(context).size;
    return SingleChildScrollView(
      child: Container(
        padding: EdgeInsets.symmetric(vertical: 20, horizontal: size.width / 8),
        child: Center(
          child: Column(
            children: <Widget>[
              Image.asset(
                'assets/kiu.png',
                height: size.height / 3,
              ),
              SizedBox(height: size.height / 10),
              TextField(
                decoration: InputDecoration(hintText: "Enter your name"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
