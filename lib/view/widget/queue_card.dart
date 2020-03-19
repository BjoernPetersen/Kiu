import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/permission.dart';
import 'package:kiu/bot/state/live_state.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/confirmation_dialog.dart';
import 'package:kiu/view/widget/song_tile.dart';

class QueueCard extends StatelessWidget {
  final SongEntry songEntry;
  final AccessManager accessManager;

  QueueCard(this.songEntry) : accessManager = service<AccessManager>();

  Future<void> _delete() async {
    final song = songEntry.song;
    try {
      final bot = await accessManager.createService();
      final queue = await bot.dequeue(song.id, song.provider.id);
      service<LiveState>().queueState.update(queue);
    } catch (e) {
      print(e);
    }
  }

  Future<void> _confirmDeletion(BuildContext context) async {
    final result = await showDialog(
      context: context,
      builder: (context) => ConfirmationDialog(
        title: Text(context.messages.queue.remove.confirm),
        content: SongTile(songEntry.song),
      ),
    );
    if (result == true) _delete();
  }

  Widget _createTrailing(BuildContext context) {
    if (accessManager.hasPermission(Permission.SKIP)) {
      return IconButton(
        icon: Icon(Icons.delete),
        tooltip: context.messages.queue.remove.tooltip,
        onPressed: () => _confirmDeletion(context),
      );
    } else {
      return null;
    }
  }

  @override
  Widget build(BuildContext context) => Card(
        child: SongTile(
          songEntry.song,
          username: songEntry.userName,
          trailing: _createTrailing(context),
        ),
      );
}
