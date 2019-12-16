final _protocol = RegExp("([a-z]+://)", caseSensitive: false);

String sanitizeHost(String ip) {
  ip = ip.trim();
  final protocolMatch = _protocol.matchAsPrefix(ip);
  if (protocolMatch != null) {
    ip = ip.substring(protocolMatch.group(0).length);
  }
  return ip;
}
