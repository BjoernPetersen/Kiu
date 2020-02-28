import 'package:flutter/material.dart';
import 'package:kiu/view/widget/loader.dart';

class LoaderSliver extends StatelessWidget {
  @override
  Widget build(BuildContext context) => SliverList(
        delegate: SliverChildBuilderDelegate(
          (_, index) => Padding(
            padding: EdgeInsets.symmetric(vertical: 8.0),
            child: Loader(),
          ),
          childCount: 1,
        ),
      );
}
