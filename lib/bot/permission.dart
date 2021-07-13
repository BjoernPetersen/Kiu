enum Permission {
  /// The permission to skip the current song or remove songs from the queue.
  /// Note that a user is always allowed to remove a song from the queue which he added himself.
  SKIP,

  /// The permission to remove songs from the upcoming suggestions of a suggester.
  ///
  /// Note that doing this will also trigger [Suggester.dislike], thus affecting future suggestions.
  DISLIKE,

  /// The permission to move songs around in the queue.
  MOVE,

  /// Pause/resume current song.
  PAUSE,

  /// Put new songs into the queue.
  ENQUEUE,

  /// Change the bot volume.
  CHANGE_VOLUME,

  /// Shut down the bot.
  EXIT,
}

Permission? parsePermission(dynamic value) {
  switch (value) {
    case 'skip':
      return Permission.SKIP;
    case 'dislike':
      return Permission.DISLIKE;
    case 'move':
      return Permission.MOVE;
    case 'pause':
      return Permission.PAUSE;
    case 'enqueue':
      return Permission.ENQUEUE;
    case 'change_volume':
      return Permission.CHANGE_VOLUME;
    case 'exit':
      return Permission.EXIT;
    default:
      print('Unknown permission: $value');
      return null;
  }
}
