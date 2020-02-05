import 'package:flutter/material.dart';
import 'package:kiu/view/page/overflow.dart';
import 'package:kiu/view/widget/basic_awareness_body.dart';
import 'package:kiu/view/widget/embedded_player.dart';
import 'package:kiu/view/widget/navigation_bar.dart';
import 'package:kiu/view/widget/queue_list.dart';

class QueuePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text('Kiu'),
          actions: <Widget>[createOverflowItems(context)],
        ),
        body: EmbeddedPlayer(child: BasicAwarenessBody(child: QueueList())),
        bottomNavigationBar: NavigationBar(BottomCategory.queue));
  }
}
