import 'package:flutter/material.dart';
import 'package:kiu/view/widget/loader.dart';

class LoadingDelegate<T> extends StatefulWidget {
  final Future<T> Function() action;
  final Widget Function(BuildContext context, T item) itemBuilder;
  final Widget Function(BuildContext context) loaderBuilder;

  LoadingDelegate({
    @required this.action,
    @required this.itemBuilder,
    this.loaderBuilder,
  }) : super();

  @override
  State<StatefulWidget> createState() => new _LoadingDelegateState(
      action: action, itemBuilder: itemBuilder, loaderBuilder: loaderBuilder);
}

class _LoadingDelegateState<T> extends State<LoadingDelegate<T>> {
  final Future<T> Function() action;
  final Widget Function(BuildContext context, T item) itemBuilder;
  final Widget Function(BuildContext context) loaderBuilder;
  T _item;
  bool _isLoading = false;
  bool _isError = false;

  _LoadingDelegateState({
    @required this.action,
    @required this.itemBuilder,
    this.loaderBuilder,
  }) : super();

  Future<void> _load() async {
    setState(() {
      _isError = false;
      _isLoading = true;
    });
    try {
      _item = await action();
    } catch (e) {
      print("Unexpected error $e");
      _isError = true;
    } finally {
      if (mounted)
        setState(() {
          _isLoading = false;
        });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_item == null) {
      if (_isError) {
        try {
          Material.of(context);
        } catch (e) {
          return Text("Error");
        }
        return IconButton(
          icon: Icon(Icons.refresh),
          onPressed: _load,
        );
      }
      if (!_isLoading) {
        _load();
      }
      return loaderBuilder == null ? Loader() : loaderBuilder(context);
    } else {
      return itemBuilder(context, _item);
    }
  }
}
