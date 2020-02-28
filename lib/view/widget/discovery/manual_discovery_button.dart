import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:kiu/data/urls.dart';
import 'package:kiu/view/widget/discovery/discovery_controller.dart';
import 'package:kiu/view/widget/input_dialog.dart';
import 'package:provider/provider.dart';

class ManualDiscoveryButton extends StatelessWidget {
  Future<void> _enterManually(BuildContext context) async {
    final result = await showDialog(
      context: context,
      child: InputDialog(
        hint: 'Enter bot IP (e.g. 192.168.178.42)',
      ),
    );
    if (result != null) {
      final sanitizedIp = sanitizeHost(result);
      Provider.of<DiscoveryController>(context, listen: false).setIp(
        Navigator.of(context),
        sanitizedIp,
      );
      Fluttertoast.showToast(
        msg: "Using IP: $sanitizedIp",
        toastLength: Toast.LENGTH_SHORT,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: Icon(Icons.keyboard),
      tooltip: 'Enter manually',
      onPressed: () => _enterManually(context),
    );
  }
}
