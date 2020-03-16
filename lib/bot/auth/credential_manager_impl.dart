import 'package:kiu/bot/auth/credential_manager.dart';
import 'package:kiu/bot/bot.dart';
import 'package:sqflite/sqflite.dart';
import 'package:sqflite/sqlite_api.dart';

class CredentialManagerImpl implements CredentialManager {
  Future<Database> _db;

  CredentialManagerImpl() {
    _db = init();
  }

  Future<Database> init() async {
    final db = await openDatabase('ip_refresh.db');
    await _createIfNotExists(db);
    return db;
  }

  @override
  Future<void> close() async {
    final db = await _db;
    await db.close();
  }

  Future<void> _createIfNotExists(Database db) async {
    await db.execute(
      'CREATE TABLE IF NOT EXISTS Tokens(ip TEXT PRIMARY KEY, token TEXT NOT NULL)',
    );
  }

  @override
  Future<String> getRefreshToken(Bot bot) async {
    final db = await _db;
    final result = await db.query(
      "Tokens",
      columns: ["token"],
      where: "ip = ?",
      whereArgs: [bot.ip],
    );
    if (result.isEmpty) {
      return null;
    } else {
      return result.first['token'];
    }
  }

  @override
  Future<void> removeRefreshToken(Bot bot) async {
    final db = await _db;
    await db.delete(
      'Tokens',
      where: 'ip = ?',
      whereArgs: [bot.ip],
    );
  }

  @override
  Future<void> setRefreshToken(Bot bot, String token) async {
    final db = await _db;
    await db.insert(
      "Tokens",
      {
        "ip": bot.ip,
        "token": token,
      },
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }
}
