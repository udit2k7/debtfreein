package com.debtfreein.app.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.debtfreein.app.data.model.CreditCard;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
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
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CardDao_Impl implements CardDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CreditCard> __insertionAdapterOfCreditCard;

  private final EntityDeletionOrUpdateAdapter<CreditCard> __deletionAdapterOfCreditCard;

  private final EntityDeletionOrUpdateAdapter<CreditCard> __updateAdapterOfCreditCard;

  public CardDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCreditCard = new EntityInsertionAdapter<CreditCard>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `credit_cards` (`id`,`name`,`issuer`,`currentBalance`,`creditLimit`,`apr`,`dueDay`,`nextDueDate`,`minimumPayment`,`cardLastFour`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CreditCard entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getIssuer());
        statement.bindDouble(4, entity.getCurrentBalance());
        statement.bindDouble(5, entity.getCreditLimit());
        statement.bindDouble(6, entity.getApr());
        statement.bindLong(7, entity.getDueDay());
        if (entity.getNextDueDate() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getNextDueDate());
        }
        statement.bindDouble(9, entity.getMinimumPayment());
        statement.bindString(10, entity.getCardLastFour());
      }
    };
    this.__deletionAdapterOfCreditCard = new EntityDeletionOrUpdateAdapter<CreditCard>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `credit_cards` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CreditCard entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfCreditCard = new EntityDeletionOrUpdateAdapter<CreditCard>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `credit_cards` SET `id` = ?,`name` = ?,`issuer` = ?,`currentBalance` = ?,`creditLimit` = ?,`apr` = ?,`dueDay` = ?,`nextDueDate` = ?,`minimumPayment` = ?,`cardLastFour` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CreditCard entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getIssuer());
        statement.bindDouble(4, entity.getCurrentBalance());
        statement.bindDouble(5, entity.getCreditLimit());
        statement.bindDouble(6, entity.getApr());
        statement.bindLong(7, entity.getDueDay());
        if (entity.getNextDueDate() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getNextDueDate());
        }
        statement.bindDouble(9, entity.getMinimumPayment());
        statement.bindString(10, entity.getCardLastFour());
        statement.bindLong(11, entity.getId());
      }
    };
  }

  @Override
  public Object insertCard(final CreditCard card, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCreditCard.insertAndReturnId(card);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCard(final CreditCard card, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfCreditCard.handle(card);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCard(final CreditCard card, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCreditCard.handle(card);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CreditCard>> getAllCardsFlow() {
    final String _sql = "SELECT * FROM credit_cards ORDER BY apr DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"credit_cards"}, new Callable<List<CreditCard>>() {
      @Override
      @NonNull
      public List<CreditCard> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIssuer = CursorUtil.getColumnIndexOrThrow(_cursor, "issuer");
          final int _cursorIndexOfCurrentBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "currentBalance");
          final int _cursorIndexOfCreditLimit = CursorUtil.getColumnIndexOrThrow(_cursor, "creditLimit");
          final int _cursorIndexOfApr = CursorUtil.getColumnIndexOrThrow(_cursor, "apr");
          final int _cursorIndexOfDueDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDay");
          final int _cursorIndexOfNextDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextDueDate");
          final int _cursorIndexOfMinimumPayment = CursorUtil.getColumnIndexOrThrow(_cursor, "minimumPayment");
          final int _cursorIndexOfCardLastFour = CursorUtil.getColumnIndexOrThrow(_cursor, "cardLastFour");
          final List<CreditCard> _result = new ArrayList<CreditCard>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CreditCard _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIssuer;
            _tmpIssuer = _cursor.getString(_cursorIndexOfIssuer);
            final double _tmpCurrentBalance;
            _tmpCurrentBalance = _cursor.getDouble(_cursorIndexOfCurrentBalance);
            final double _tmpCreditLimit;
            _tmpCreditLimit = _cursor.getDouble(_cursorIndexOfCreditLimit);
            final double _tmpApr;
            _tmpApr = _cursor.getDouble(_cursorIndexOfApr);
            final int _tmpDueDay;
            _tmpDueDay = _cursor.getInt(_cursorIndexOfDueDay);
            final String _tmpNextDueDate;
            if (_cursor.isNull(_cursorIndexOfNextDueDate)) {
              _tmpNextDueDate = null;
            } else {
              _tmpNextDueDate = _cursor.getString(_cursorIndexOfNextDueDate);
            }
            final double _tmpMinimumPayment;
            _tmpMinimumPayment = _cursor.getDouble(_cursorIndexOfMinimumPayment);
            final String _tmpCardLastFour;
            _tmpCardLastFour = _cursor.getString(_cursorIndexOfCardLastFour);
            _item = new CreditCard(_tmpId,_tmpName,_tmpIssuer,_tmpCurrentBalance,_tmpCreditLimit,_tmpApr,_tmpDueDay,_tmpNextDueDate,_tmpMinimumPayment,_tmpCardLastFour);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllCards(final Continuation<? super List<CreditCard>> $completion) {
    final String _sql = "SELECT * FROM credit_cards ORDER BY apr DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CreditCard>>() {
      @Override
      @NonNull
      public List<CreditCard> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIssuer = CursorUtil.getColumnIndexOrThrow(_cursor, "issuer");
          final int _cursorIndexOfCurrentBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "currentBalance");
          final int _cursorIndexOfCreditLimit = CursorUtil.getColumnIndexOrThrow(_cursor, "creditLimit");
          final int _cursorIndexOfApr = CursorUtil.getColumnIndexOrThrow(_cursor, "apr");
          final int _cursorIndexOfDueDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDay");
          final int _cursorIndexOfNextDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextDueDate");
          final int _cursorIndexOfMinimumPayment = CursorUtil.getColumnIndexOrThrow(_cursor, "minimumPayment");
          final int _cursorIndexOfCardLastFour = CursorUtil.getColumnIndexOrThrow(_cursor, "cardLastFour");
          final List<CreditCard> _result = new ArrayList<CreditCard>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CreditCard _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIssuer;
            _tmpIssuer = _cursor.getString(_cursorIndexOfIssuer);
            final double _tmpCurrentBalance;
            _tmpCurrentBalance = _cursor.getDouble(_cursorIndexOfCurrentBalance);
            final double _tmpCreditLimit;
            _tmpCreditLimit = _cursor.getDouble(_cursorIndexOfCreditLimit);
            final double _tmpApr;
            _tmpApr = _cursor.getDouble(_cursorIndexOfApr);
            final int _tmpDueDay;
            _tmpDueDay = _cursor.getInt(_cursorIndexOfDueDay);
            final String _tmpNextDueDate;
            if (_cursor.isNull(_cursorIndexOfNextDueDate)) {
              _tmpNextDueDate = null;
            } else {
              _tmpNextDueDate = _cursor.getString(_cursorIndexOfNextDueDate);
            }
            final double _tmpMinimumPayment;
            _tmpMinimumPayment = _cursor.getDouble(_cursorIndexOfMinimumPayment);
            final String _tmpCardLastFour;
            _tmpCardLastFour = _cursor.getString(_cursorIndexOfCardLastFour);
            _item = new CreditCard(_tmpId,_tmpName,_tmpIssuer,_tmpCurrentBalance,_tmpCreditLimit,_tmpApr,_tmpDueDay,_tmpNextDueDate,_tmpMinimumPayment,_tmpCardLastFour);
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
  public Object getCardByLastFour(final String lastFour,
      final Continuation<? super CreditCard> $completion) {
    final String _sql = "SELECT * FROM credit_cards WHERE cardLastFour = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, lastFour);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CreditCard>() {
      @Override
      @Nullable
      public CreditCard call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIssuer = CursorUtil.getColumnIndexOrThrow(_cursor, "issuer");
          final int _cursorIndexOfCurrentBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "currentBalance");
          final int _cursorIndexOfCreditLimit = CursorUtil.getColumnIndexOrThrow(_cursor, "creditLimit");
          final int _cursorIndexOfApr = CursorUtil.getColumnIndexOrThrow(_cursor, "apr");
          final int _cursorIndexOfDueDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDay");
          final int _cursorIndexOfNextDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextDueDate");
          final int _cursorIndexOfMinimumPayment = CursorUtil.getColumnIndexOrThrow(_cursor, "minimumPayment");
          final int _cursorIndexOfCardLastFour = CursorUtil.getColumnIndexOrThrow(_cursor, "cardLastFour");
          final CreditCard _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIssuer;
            _tmpIssuer = _cursor.getString(_cursorIndexOfIssuer);
            final double _tmpCurrentBalance;
            _tmpCurrentBalance = _cursor.getDouble(_cursorIndexOfCurrentBalance);
            final double _tmpCreditLimit;
            _tmpCreditLimit = _cursor.getDouble(_cursorIndexOfCreditLimit);
            final double _tmpApr;
            _tmpApr = _cursor.getDouble(_cursorIndexOfApr);
            final int _tmpDueDay;
            _tmpDueDay = _cursor.getInt(_cursorIndexOfDueDay);
            final String _tmpNextDueDate;
            if (_cursor.isNull(_cursorIndexOfNextDueDate)) {
              _tmpNextDueDate = null;
            } else {
              _tmpNextDueDate = _cursor.getString(_cursorIndexOfNextDueDate);
            }
            final double _tmpMinimumPayment;
            _tmpMinimumPayment = _cursor.getDouble(_cursorIndexOfMinimumPayment);
            final String _tmpCardLastFour;
            _tmpCardLastFour = _cursor.getString(_cursorIndexOfCardLastFour);
            _result = new CreditCard(_tmpId,_tmpName,_tmpIssuer,_tmpCurrentBalance,_tmpCreditLimit,_tmpApr,_tmpDueDay,_tmpNextDueDate,_tmpMinimumPayment,_tmpCardLastFour);
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

  @Override
  public Object getCardById(final long id, final Continuation<? super CreditCard> $completion) {
    final String _sql = "SELECT * FROM credit_cards WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CreditCard>() {
      @Override
      @Nullable
      public CreditCard call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIssuer = CursorUtil.getColumnIndexOrThrow(_cursor, "issuer");
          final int _cursorIndexOfCurrentBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "currentBalance");
          final int _cursorIndexOfCreditLimit = CursorUtil.getColumnIndexOrThrow(_cursor, "creditLimit");
          final int _cursorIndexOfApr = CursorUtil.getColumnIndexOrThrow(_cursor, "apr");
          final int _cursorIndexOfDueDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDay");
          final int _cursorIndexOfNextDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextDueDate");
          final int _cursorIndexOfMinimumPayment = CursorUtil.getColumnIndexOrThrow(_cursor, "minimumPayment");
          final int _cursorIndexOfCardLastFour = CursorUtil.getColumnIndexOrThrow(_cursor, "cardLastFour");
          final CreditCard _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIssuer;
            _tmpIssuer = _cursor.getString(_cursorIndexOfIssuer);
            final double _tmpCurrentBalance;
            _tmpCurrentBalance = _cursor.getDouble(_cursorIndexOfCurrentBalance);
            final double _tmpCreditLimit;
            _tmpCreditLimit = _cursor.getDouble(_cursorIndexOfCreditLimit);
            final double _tmpApr;
            _tmpApr = _cursor.getDouble(_cursorIndexOfApr);
            final int _tmpDueDay;
            _tmpDueDay = _cursor.getInt(_cursorIndexOfDueDay);
            final String _tmpNextDueDate;
            if (_cursor.isNull(_cursorIndexOfNextDueDate)) {
              _tmpNextDueDate = null;
            } else {
              _tmpNextDueDate = _cursor.getString(_cursorIndexOfNextDueDate);
            }
            final double _tmpMinimumPayment;
            _tmpMinimumPayment = _cursor.getDouble(_cursorIndexOfMinimumPayment);
            final String _tmpCardLastFour;
            _tmpCardLastFour = _cursor.getString(_cursorIndexOfCardLastFour);
            _result = new CreditCard(_tmpId,_tmpName,_tmpIssuer,_tmpCurrentBalance,_tmpCreditLimit,_tmpApr,_tmpDueDay,_tmpNextDueDate,_tmpMinimumPayment,_tmpCardLastFour);
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
