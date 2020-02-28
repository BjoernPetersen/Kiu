import 'package:kiu/view/common.dart';
import 'package:kiu/view/page/queue_page.dart';
import 'package:kiu/view/page/search_page.dart';
import 'package:kiu/view/page/suggestions_page.dart';
import 'package:kiu/view/resources/messages.i18n.dart';
import 'package:kiu/view/routing/unanimated_route.dart';

class NavigationBar extends StatelessWidget {
  final BottomCategory category;
  final Function() onClick;

  const NavigationBar(this.category, {this.onClick}) : super();

  @override
  Widget build(BuildContext context) => BottomNavigationBar(
        currentIndex: category.index,
        type: BottomNavigationBarType.fixed,
        // TODO remove sublist when favorites are implemented
        items: BottomCategory.values
            .sublist(0, 3)
            .map((category) => BottomNavigationBarItem(
                  icon: category.icon,
                  activeIcon: category.activeIcon,
                  title: Text(category.text(context.messages.page)),
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
  String text(PageMessages messages) {
    switch (this) {
      case BottomCategory.queue:
        return messages.queue;
      case BottomCategory.suggestions:
        return messages.suggestion;
      case BottomCategory.search:
        return messages.search;
      case BottomCategory.favorites:
        return messages.favorites;
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
    final nav = Navigator.of(context);
    while (nav.canPop()) {
      nav.pop();
    }
    switch (this) {
      case BottomCategory.queue:
        nav.pushReplacement(UnanimatedRoute((_) => QueuePage()));
        break;
      case BottomCategory.suggestions:
        nav.push(UnanimatedRoute((_) => SuggestionsPage()));
        break;
      case BottomCategory.search:
        nav.push(UnanimatedRoute((_) => SearchPage()));
        break;
      default:
        print('Page not implemented: $this');
    }
  }
}
