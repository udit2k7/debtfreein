package com.debtfreein.app.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.debtfreein.app.data.model.Expense;
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
public final class ExpenseDao_Impl implements ExpenseDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Expense> __insertionAdapterOfExpense;

  private final EntityDeletionOrUpdateAdapter<Expense> __deletionAdapterOfExpense;

  private final EntityDeletionOrUpdateAdapter<Expense> __updateAdapterOfExpense;

  public ExpenseDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfExpense = new EntityInsertionAdapter<Expense>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `expenses` (`id`,`amount`,`merchant`,`timestamp`,`category`,`cardId`,`rawSmsText`,`isReimbursableClaim`,`expenseCategory`,`status`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Expense entity) {
        statement.bindLong(1, entity.getId());
        statement.bindDouble(2, entity.getAmount());
        statement.bindString(3, entity.getMerchant());
        statement.bindLong(4, entity.getTimestamp());
        statement.bindString(5, entity.getCategory());
        if (entity.getCardId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getCardId());
        }
        if (entity.getRawSmsText() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getRawSmsText());
        }
        final int _tmp = entity.isReimbursableClaim() ? 1 : 0;
        statement.bindLong(8, _tmp);
        statement.bindString(9, entity.getExpenseCategory());
        if (entity.getStatus() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getStatus());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getNotes());
        }
      }
    };
    this.__deletionAdapterOfExpense = new EntityDeletionOrUpdateAdapter<Expense>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `expenses` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Expense entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfExpense = new EntityDeletionOrUpdateAdapter<Expense>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `expenses` SET `id` = ?,`amount` = ?,`merchant` = ?,`timestamp` = ?,`category` = ?,`cardId` = ?,`rawSmsText` = ?,`isReimbursableClaim` = ?,`expenseCategory` = ?,`status` = ?,`notes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Expense entity) {
        statement.bindLong(1, entity.getId());
        statement.bindDouble(2, entity.getAmount());
        statement.bindString(3, entity.getMerchant());
        statement.bindLong(4, entity.getTimestamp());
        statement.bindString(5, entity.getCategory());
        if (entity.getCardId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getCardId());
        }
        if (entity.getRawSmsText() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getRawSmsText());
        }
        final int _tmp = entity.isReimbursableClaim() ? 1 : 0;
        statement.bindLong(8, _tmp);
        statement.bindString(9, entity.getExpenseCategory());
        if (entity.getStatus() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getStatus());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getNotes());
        }
        statement.bindLong(12, entity.getId());
      }
    };
  }

  @Override
  public Object insertExpense(final Expense expense, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfExpense.insertAndReturnId(expense);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExpense(final Expense expense, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfExpense.handle(expense);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateExpense(final Expense expense, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfExpense.handle(expense);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Expense>> getAllExpensesFlow() {
    final String _sql = "SELECT * FROM expenses ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"expenses"}, new Callable<List<Expense>>() {
      @Override
      @NonNull
      public List<Expense> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfMerchant = CursorUtil.getColumnIndexOrThrow(_cursor, "merchant");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfCardId = CursorUtil.getColumnIndexOrThrow(_cursor, "cardId");
          final int _cursorIndexOfRawSmsText = CursorUtil.getColumnIndexOrThrow(_cursor, "rawSmsText");
          final int _cursorIndexOfIsReimbursableClaim = CursorUtil.getColumnIndexOrThrow(_cursor, "isReimbursableClaim");
          final int _cursorIndexOfExpenseCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "expenseCategory");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<Expense> _result = new ArrayList<Expense>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Expense _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpMerchant;
            _tmpMerchant = _cursor.getString(_cursorIndexOfMerchant);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final Long _tmpCardId;
            if (_cursor.isNull(_cursorIndexOfCardId)) {
              _tmpCardId = null;
            } else {
              _tmpCardId = _cursor.getLong(_cursorIndexOfCardId);
            }
            final String _tmpRawSmsText;
            if (_cursor.isNull(_cursorIndexOfRawSmsText)) {
              _tmpRawSmsText = null;
            } else {
              _tmpRawSmsText = _cursor.getString(_cursorIndexOfRawSmsText);
            }
            final boolean _tmpIsReimbursableClaim;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsReimbursableClaim);
            _tmpIsReimbursableClaim = _tmp != 0;
            final String _tmpExpenseCategory;
            _tmpExpenseCategory = _cursor.getString(_cursorIndexOfExpenseCategory);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new Expense(_tmpId,_tmpAmount,_tmpMerchant,_tmpTimestamp,_tmpCategory,_tmpCardId,_tmpRawSmsText,_tmpIsReimbursableClaim,_tmpExpenseCategory,_tmpStatus,_tmpNotes);
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
  public Object getAllExpenses(final Continuation<? super List<Expense>> $completion) {
    final String _sql = "SELECT * FROM expenses ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Expense>>() {
      @Override
      @NonNull
      public List<Expense> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfMerchant = CursorUtil.getColumnIndexOrThrow(_cursor, "merchant");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfCardId = CursorUtil.getColumnIndexOrThrow(_cursor, "cardId");
          final int _cursorIndexOfRawSmsText = CursorUtil.getColumnIndexOrThrow(_cursor, "rawSmsText");
          final int _cursorIndexOfIsReimbursableClaim = CursorUtil.getColumnIndexOrThrow(_cursor, "isReimbursableClaim");
          final int _cursorIndexOfExpenseCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "expenseCategory");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<Expense> _result = new ArrayList<Expense>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Expense _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpMerchant;
            _tmpMerchant = _cursor.getString(_cursorIndexOfMerchant);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final Long _tmpCardId;
            if (_cursor.isNull(_cursorIndexOfCardId)) {
              _tmpCardId = null;
            } else {
              _tmpCardId = _cursor.getLong(_cursorIndexOfCardId);
            }
            final String _tmpRawSmsText;
            if (_cursor.isNull(_cursorIndexOfRawSmsText)) {
              _tmpRawSmsText = null;
            } else {
              _tmpRawSmsText = _cursor.getString(_cursorIndexOfRawSmsText);
            }
            final boolean _tmpIsReimbursableClaim;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsReimbursableClaim);
            _tmpIsReimbursableClaim = _tmp != 0;
            final String _tmpExpenseCategory;
            _tmpExpenseCategory = _cursor.getString(_cursorIndexOfExpenseCategory);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new Expense(_tmpId,_tmpAmount,_tmpMerchant,_tmpTimestamp,_tmpCategory,_tmpCardId,_tmpRawSmsText,_tmpIsReimbursableClaim,_tmpExpenseCategory,_tmpStatus,_tmpNotes);
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
  public Flow<List<Expense>> getExpensesByCardFlow(final long cardId) {
    final String _sql = "SELECT * FROM expenses WHERE cardId = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, cardId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"expenses"}, new Callable<List<Expense>>() {
      @Override
      @NonNull
      public List<Expense> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfMerchant = CursorUtil.getColumnIndexOrThrow(_cursor, "merchant");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfCardId = CursorUtil.getColumnIndexOrThrow(_cursor, "cardId");
          final int _cursorIndexOfRawSmsText = CursorUtil.getColumnIndexOrThrow(_cursor, "rawSmsText");
          final int _cursorIndexOfIsReimbursableClaim = CursorUtil.getColumnIndexOrThrow(_cursor, "isReimbursableClaim");
          final int _cursorIndexOfExpenseCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "expenseCategory");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<Expense> _result = new ArrayList<Expense>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Expense _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpMerchant;
            _tmpMerchant = _cursor.getString(_cursorIndexOfMerchant);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final Long _tmpCardId;
            if (_cursor.isNull(_cursorIndexOfCardId)) {
              _tmpCardId = null;
            } else {
              _tmpCardId = _cursor.getLong(_cursorIndexOfCardId);
            }
            final String _tmpRawSmsText;
            if (_cursor.isNull(_cursorIndexOfRawSmsText)) {
              _tmpRawSmsText = null;
            } else {
              _tmpRawSmsText = _cursor.getString(_cursorIndexOfRawSmsText);
            }
            final boolean _tmpIsReimbursableClaim;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsReimbursableClaim);
            _tmpIsReimbursableClaim = _tmp != 0;
            final String _tmpExpenseCategory;
            _tmpExpenseCategory = _cursor.getString(_cursorIndexOfExpenseCategory);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new Expense(_tmpId,_tmpAmount,_tmpMerchant,_tmpTimestamp,_tmpCategory,_tmpCardId,_tmpRawSmsText,_tmpIsReimbursableClaim,_tmpExpenseCategory,_tmpStatus,_tmpNotes);
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
  public Object findMatchingExpenses(final double amount, final long timestamp,
      final Continuation<? super List<Expense>> $completion) {
    final String _sql = "SELECT * FROM expenses WHERE amount = ? AND abs(timestamp - ?) <= 900000 ORDER BY abs(timestamp - ?) ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindDouble(_argIndex, amount);
    _argIndex = 2;
    _statement.bindLong(_argIndex, timestamp);
    _argIndex = 3;
    _statement.bindLong(_argIndex, timestamp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Expense>>() {
      @Override
      @NonNull
      public List<Expense> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfMerchant = CursorUtil.getColumnIndexOrThrow(_cursor, "merchant");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfCardId = CursorUtil.getColumnIndexOrThrow(_cursor, "cardId");
          final int _cursorIndexOfRawSmsText = CursorUtil.getColumnIndexOrThrow(_cursor, "rawSmsText");
          final int _cursorIndexOfIsReimbursableClaim = CursorUtil.getColumnIndexOrThrow(_cursor, "isReimbursableClaim");
          final int _cursorIndexOfExpenseCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "expenseCategory");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<Expense> _result = new ArrayList<Expense>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Expense _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpMerchant;
            _tmpMerchant = _cursor.getString(_cursorIndexOfMerchant);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final Long _tmpCardId;
            if (_cursor.isNull(_cursorIndexOfCardId)) {
              _tmpCardId = null;
            } else {
              _tmpCardId = _cursor.getLong(_cursorIndexOfCardId);
            }
            final String _tmpRawSmsText;
            if (_cursor.isNull(_cursorIndexOfRawSmsText)) {
              _tmpRawSmsText = null;
            } else {
              _tmpRawSmsText = _cursor.getString(_cursorIndexOfRawSmsText);
            }
            final boolean _tmpIsReimbursableClaim;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsReimbursableClaim);
            _tmpIsReimbursableClaim = _tmp != 0;
            final String _tmpExpenseCategory;
            _tmpExpenseCategory = _cursor.getString(_cursorIndexOfExpenseCategory);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new Expense(_tmpId,_tmpAmount,_tmpMerchant,_tmpTimestamp,_tmpCategory,_tmpCardId,_tmpRawSmsText,_tmpIsReimbursableClaim,_tmpExpenseCategory,_tmpStatus,_tmpNotes);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
