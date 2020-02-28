import 'package:flutter/material.dart';
import 'package:kiu/view/widget/discovery/bot_card.dart';
import 'package:kiu/view/widget/discovery/discovery_controller.dart';
import 'package:kiu/view/widget/discovery/discovery_refresh_button.dart';
import 'package:kiu/view/widget/loader.dart';
import 'package:kiu/view/widget/loader_sliver.dart';
import 'package:provider/provider.dart';

class DiscoveryContent extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final controller = Provider.of<DiscoveryController>(context);
    if (controller.found.isEmpty) {
      if (controller.isLoading) {
        return Center(child: Loader());
      } else {
        return _buildEmpty(context);
      }
    }

    return CustomScrollView(
      slivers: <Widget>[
        _buildFound(context),
        ..._buildLoader(context),
      ],
    );
  }

  Widget _buildFound(BuildContext context) {
    final controller = Provider.of<DiscoveryController>(context);
    return SliverList(
      delegate: SliverChildListDelegate(
        controller.found
            .map((e) => BotCard(
                  ip: e,
                  onTap: () => controller.setIp(Navigator.of(context), e),
                ))
            .toList(growable: false),
      ),
    );
  }

  List<Widget> _buildLoader(BuildContext context) {
    if (!Provider.of<DiscoveryController>(context).isLoading) return [];
    return [LoaderSliver()];
  }

  Widget _buildEmpty(BuildContext context) {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Text(
            "No bots found.",
            textScaleFactor: 1.5,
          ),
          Row(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[DiscoveryRefreshButton(size: 48)],
          ),
        ],
      ),
    );
  }
}
