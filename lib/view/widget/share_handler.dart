import 'dart:async';

import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/state/live_state.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/sharing_data.dart';
import 'package:kiu/view/common.dart';
import 'package:receive_sharing_intent/receive_sharing_intent.dart';

class ShareHandler extends StatefulWidget {
  final Widget child;

  const ShareHandler({required this.child}) : super();

  @override
  _ShareHandlerState createState() => _ShareHandlerState();
}

class _ShareHandlerState extends State<ShareHandler> {
  bool _isHandlingShares = false;
  late StreamSubscription _shareSub;

  Future<bool> _handleShare(String? share) async {
    if (share == null) return false;
    final parsed = await extractSharingData(share);
    if (parsed == null) {
      Fluttertoast.showToast(msg: context.messages.share.parseError);
      return true;
    }
    try {
      final bot = await service<AccessManager>().createService();
      final queue = await bot.enqueue(parsed.songId, parsed.providerId);
      service<LiveState>().queueState.update(queue);
      Fluttertoast.showToast(msg: context.messages.share.success);
      return true;
    } catch (err) {
      Fluttertoast.showToast(msg: context.messages.share.failure);
      return true;
    }
  }

  _handleShares() {
    _isHandlingShares = true;
    ReceiveSharingIntent.getInitialText().then((it) {
      final handle = _handleShare(it);
      ReceiveSharingIntent.reset();
      handle.then((containedShare) {
        if (containedShare) SystemNavigator.pop();
      });
    });
    _shareSub = ReceiveSharingIntent.getTextStream().listen(_handleShare);
  }

  @override
  void dispose() {
    _shareSub.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (!_isHandlingShares) {
      _handleShares();
    }
    return widget.child;
  }
}
