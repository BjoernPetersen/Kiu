import 'package:fluttertoast/fluttertoast.dart';
import 'package:kiu/data/urls.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/discovery/discovery_controller.dart';
import 'package:kiu/view/widget/input_dialog.dart';
import 'package:provider/provider.dart';

class ManualDiscoveryButton extends StatelessWidget {
  Future<void> _enterManually(BuildContext context) async {
    final result = await showDialog(
      context: context,
      builder: (context) => InputDialog(
        hint: context.messages.discovery.manual.hint,
      ),
    );
    if (result != null) {
      final sanitizedIp = sanitizeHost(result);
      Provider.of<DiscoveryController>(context, listen: false).setIp(
        Navigator.of(context),
        sanitizedIp,
      );
      Fluttertoast.showToast(
        msg: context.messages.discovery.manual.usingIp(sanitizedIp),
        toastLength: Toast.LENGTH_SHORT,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: Icon(Icons.keyboard),
      tooltip: context.messages.discovery.manual.button,
      onPressed: () => _enterManually(context),
    );
  }
}
