import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/discovery/discovery_controller.dart';
import 'package:provider/provider.dart';

class DiscoveryRefreshButton extends StatelessWidget {
  final double size;

  const DiscoveryRefreshButton({
    Key key,
    this.size,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) => IconButton(
        icon: Icon(
          Icons.refresh,
          size: size,
        ),
        onPressed: Provider.of<DiscoveryController>(context).refresh,
        tooltip: context.messages.discovery.refresh,
      );
}
