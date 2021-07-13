import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/discovery/discovery_controller.dart';
import 'package:provider/provider.dart';

class DiscoveryRefreshButton extends StatelessWidget {
  final double? size;

  const DiscoveryRefreshButton({
    this.size,
  }) : super();

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
