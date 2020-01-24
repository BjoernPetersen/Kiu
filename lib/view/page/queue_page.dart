import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/sharing_data.dart';
import 'package:kiu/view/page/overflow.dart';
import 'package:kiu/view/widget/embedded_player.dart';
import 'package:kiu/view/widget/navigation_bar.dart';
import 'package:kiu/view/widget/queue_list.dart';
import 'package:receive_sharing_intent/receive_sharing_intent.dart';

class QueuePage extends StatefulWidget {
  @override
  _QueuePageState createState() => _QueuePageState();
}

class _QueuePageState extends State<QueuePage> {
  bool _isHandlingShares = false;

  Future<void> _handleShare(String share) async {
    if (share == null) return;
    final parsed = extractSharingData(share);
    if (parsed == null) return;
    try {
      final bot = await service<ConnectionManager>().getService();
      await bot.enqueue(parsed.songId, parsed.providerId);
    } catch (err) {
      await Fluttertoast.showToast(msg: "Could not enqueue shared song");
    }
  }

  _handleShares() {
    _isHandlingShares = true;
    ReceiveSharingIntent.getInitialText().then(_handleShare);
    ReceiveSharingIntent.getTextStream().listen(_handleShare);
  }

  @override
  Widget build(BuildContext context) {
    if (!_isHandlingShares) {
      _handleShares();
    }
    return Scaffold(
        appBar: AppBar(
          title: Text('Kiu'),
          actions: <Widget>[createOverflowItems(context)],
        ),
        body: EmbeddedPlayer(child: QueueList()),
        bottomNavigationBar: NavigationBar(BottomCategory.queue));
  }
}
