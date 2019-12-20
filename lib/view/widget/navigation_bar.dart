import 'package:flutter/material.dart';
import 'package:kiu/view/page/queue_page.dart';
import 'package:kiu/view/page/search_page.dart';
import 'package:kiu/view/page/suggestions_page.dart';

class NavigationBar extends StatelessWidget {
  final BottomCategory category;
  final Function() onClick;

  const NavigationBar(this.category, {this.onClick}) : super();

  @override
  Widget build(BuildContext context) => BottomNavigationBar(
        currentIndex: category.index,
        type: BottomNavigationBarType.fixed,
        items: BottomCategory.values
            .map((category) => BottomNavigationBarItem(
                  icon: category.icon,
                  activeIcon: category.activeIcon,
                  title: Text(category.text),
                ))
            .toList(growable: false),
        onTap: (index) {
          final category = BottomCategory.values[index];
          if (category == this.category && onClick != null)
            onClick();
          else
            category.goTo(context);
        },
      );
}

enum BottomCategory { queue, suggestions, search, favorites }

extension on BottomCategory {
  String get text {
    switch (this) {
      case BottomCategory.queue:
        return 'Queue';
      case BottomCategory.suggestions:
        return 'Suggestion';
      case BottomCategory.search:
        return 'Search';
      case BottomCategory.favorites:
        return 'Favorites';
    }
  }

  Icon get icon {
    switch (this) {
      case BottomCategory.queue:
        return Icon(Icons.queue_music);
      case BottomCategory.suggestions:
        return Icon(Icons.all_inclusive);
      case BottomCategory.search:
        return Icon(Icons.search);
      case BottomCategory.favorites:
        return Icon(Icons.star_border);
    }
  }

  Icon get activeIcon {
    switch (this) {
      case BottomCategory.favorites:
        return Icon(Icons.star);
      default:
        return icon;
    }
  }

  goTo(BuildContext context) {
    switch (this) {
      case BottomCategory.queue:
        _routeTo(context, (_) => QueuePage());
        break;
      case BottomCategory.suggestions:
        _routeTo(context, (_) => SuggestionsPage());
        break;
      case BottomCategory.search:
        _routeTo(context, (_) => SearchPage());
        break;
      default:
        print('Page not implemented: $this');
    }
  }

  _routeTo(BuildContext context, Widget Function(BuildContext) builder) {
    final nav = Navigator.of(context);
    nav.pushReplacement(_UnanimatedRoute(builder));
  }
}

class _UnanimatedRoute<T> extends MaterialPageRoute<T> {
  _UnanimatedRoute(WidgetBuilder builder) : super(builder: builder);

  @override
  Widget buildTransitions(BuildContext context, Animation<double> animation,
      Animation<double> secondaryAnimation, Widget child) {
    return child;
  }
}
