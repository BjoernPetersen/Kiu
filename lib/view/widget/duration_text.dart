import 'package:flutter/material.dart';
import 'package:sprintf/sprintf.dart';

class DurationText extends StatelessWidget {
  final String duration;

  DurationText(int durationSeconds)
      : this.duration = _formatDuration(durationSeconds);

  @override
  Widget build(BuildContext context) => Text(
        duration,
        style: TextStyle(color: Colors.black45),
      );
}

String _formatDuration(int durationSeconds) {
  final duration = Duration(seconds: durationSeconds);
  final hours = duration.inHours;
  final minutes = duration.inMinutes % 60;
  final seconds = duration.inSeconds % 60;
  if (hours == 0) {
    return sprintf("%02i:%02i", [minutes, seconds]);
  } else {
    return sprintf("%i:%02i:%02i", [hours, minutes, seconds]);
  }
}
