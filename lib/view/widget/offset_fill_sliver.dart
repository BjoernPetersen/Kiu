import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';

class OffsetFillSliver extends SingleChildRenderObjectWidget {
  final double offset;

  OffsetFillSliver({@required this.offset});

  @override
  RenderObject createRenderObject(BuildContext context) {
    return _RenderOffsetFillSliver(offset);
  }
}

class _RenderOffsetFillSliver extends RenderSliverSingleBoxAdapter {
  final double offset;

  _RenderOffsetFillSliver(this.offset);

  @override
  void performLayout() {
    geometry = SliverGeometry(
      scrollExtent: max(0.0, constraints.viewportMainAxisExtent - offset),
    );
  }
}
