package com.debtfreein.app.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.debtfreein.app.data.model.TokenSpend;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TokenSpendDao_Impl implements TokenSpendDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TokenSpend> __insertionAdapterOfTokenSpend;

  public TokenSpendDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTokenSpend = new EntityInsertionAdapter<TokenSpend>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `token_spends` (`id`,`timestamp`,`model`,`inputTokens`,`outputTokens`,`costInr`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TokenSpend entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindString(3, entity.getModel());
        statement.bindLong(4, entity.getInputTokens());
        statement.bindLong(5, entity.getOutputTokens());
        statement.bindDouble(6, entity.getCostInr());
      }
    };
  }

  @Override
  public Object insertTokenSpend(final TokenSpend spend,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTokenSpend.insert(spend);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllTokenSpends(final Continuation<? super List<TokenSpend>> $completion) {
    final String _sql = "SELECT * FROM token_spends ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TokenSpend>>() {
      @Override
      @NonNull
      public List<TokenSpend> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfModel = CursorUtil.getColumnIndexOrThrow(_cursor, "model");
          final int _cursorIndexOfInputTokens = CursorUtil.getColumnIndexOrThrow(_cursor, "inputTokens");
          final int _cursorIndexOfOutputTokens = CursorUtil.getColumnIndexOrThrow(_cursor, "outputTokens");
          final int _cursorIndexOfCostInr = CursorUtil.getColumnIndexOrThrow(_cursor, "costInr");
          final List<TokenSpend> _result = new ArrayList<TokenSpend>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TokenSpend _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpModel;
            _tmpModel = _cursor.getString(_cursorIndexOfModel);
            final long _tmpInputTokens;
            _tmpInputTokens = _cursor.getLong(_cursorIndexOfInputTokens);
            final long _tmpOutputTokens;
            _tmpOutputTokens = _cursor.getLong(_cursorIndexOfOutputTokens);
            final double _tmpCostInr;
            _tmpCostInr = _cursor.getDouble(_cursorIndexOfCostInr);
            _item = new TokenSpend(_tmpId,_tmpTimestamp,_tmpModel,_tmpInputTokens,_tmpOutputTokens,_tmpCostInr);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTokenSpendsSince(final long sinceTimestamp,
      final Continuation<? super List<TokenSpend>> $completion) {
    final String _sql = "SELECT * FROM token_spends WHERE timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceTimestamp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TokenSpend>>() {
      @Override
      @NonNull
      public List<TokenSpend> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfModel = CursorUtil.getColumnIndexOrThrow(_cursor, "model");
          final int _cursorIndexOfInputTokens = CursorUtil.getColumnIndexOrThrow(_cursor, "inputTokens");
          final int _cursorIndexOfOutputTokens = CursorUtil.getColumnIndexOrThrow(_cursor, "outputTokens");
          final int _cursorIndexOfCostInr = CursorUtil.getColumnIndexOrThrow(_cursor, "costInr");
          final List<TokenSpend> _result = new ArrayList<TokenSpend>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TokenSpend _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpModel;
            _tmpModel = _cursor.getString(_cursorIndexOfModel);
            final long _tmpInputTokens;
            _tmpInputTokens = _cursor.getLong(_cursorIndexOfInputTokens);
            final long _tmpOutputTokens;
            _tmpOutputTokens = _cursor.getLong(_cursorIndexOfOutputTokens);
            final double _tmpCostInr;
            _tmpCostInr = _cursor.getDouble(_cursorIndexOfCostInr);
            _item = new TokenSpend(_tmpId,_tmpTimestamp,_tmpModel,_tmpInputTokens,_tmpOutputTokens,_tmpCostInr);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTotalCostInrSince(final long sinceTimestamp,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT SUM(costInr) FROM token_spends WHERE timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceTimestamp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
