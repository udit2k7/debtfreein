package com.debtfreein.app.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.debtfreein.app.data.model.Investment;
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
public final class InvestmentDao_Impl implements InvestmentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Investment> __insertionAdapterOfInvestment;

  private final EntityDeletionOrUpdateAdapter<Investment> __updateAdapterOfInvestment;

  private final SharedSQLiteStatement __preparedStmtOfDeleteInvestmentById;

  public InvestmentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfInvestment = new EntityInsertionAdapter<Investment>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `investments` (`id`,`symbol`,`name`,`quantity`,`purchasePrice`,`currentPrice`,`assetType`,`expectedReturnApr`,`brokerName`,`monthlySipAmount`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Investment entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSymbol());
        statement.bindString(3, entity.getName());
        statement.bindDouble(4, entity.getQuantity());
        statement.bindDouble(5, entity.getPurchasePrice());
        statement.bindDouble(6, entity.getCurrentPrice());
        statement.bindString(7, entity.getAssetType());
        statement.bindDouble(8, entity.getExpectedReturnApr());
        statement.bindString(9, entity.getBrokerName());
        statement.bindDouble(10, entity.getMonthlySipAmount());
      }
    };
    this.__updateAdapterOfInvestment = new EntityDeletionOrUpdateAdapter<Investment>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `investments` SET `id` = ?,`symbol` = ?,`name` = ?,`quantity` = ?,`purchasePrice` = ?,`currentPrice` = ?,`assetType` = ?,`expectedReturnApr` = ?,`brokerName` = ?,`monthlySipAmount` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Investment entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSymbol());
        statement.bindString(3, entity.getName());
        statement.bindDouble(4, entity.getQuantity());
        statement.bindDouble(5, entity.getPurchasePrice());
        statement.bindDouble(6, entity.getCurrentPrice());
        statement.bindString(7, entity.getAssetType());
        statement.bindDouble(8, entity.getExpectedReturnApr());
        statement.bindString(9, entity.getBrokerName());
        statement.bindDouble(10, entity.getMonthlySipAmount());
        statement.bindLong(11, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteInvestmentById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM investments WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertInvestment(final Investment investment,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfInvestment.insertAndReturnId(investment);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAllInvestments(final List<Investment> investments,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfInvestment.insert(investments);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateInvestment(final Investment investment,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfInvestment.handle(investment);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteInvestmentById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteInvestmentById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteInvestmentById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Investment>> getAllInvestmentsFlow() {
    final String _sql = "SELECT * FROM investments ORDER BY symbol ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"investments"}, new Callable<List<Investment>>() {
      @Override
      @NonNull
      public List<Investment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "symbol");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfPurchasePrice = CursorUtil.getColumnIndexOrThrow(_cursor, "purchasePrice");
          final int _cursorIndexOfCurrentPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPrice");
          final int _cursorIndexOfAssetType = CursorUtil.getColumnIndexOrThrow(_cursor, "assetType");
          final int _cursorIndexOfExpectedReturnApr = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedReturnApr");
          final int _cursorIndexOfBrokerName = CursorUtil.getColumnIndexOrThrow(_cursor, "brokerName");
          final int _cursorIndexOfMonthlySipAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "monthlySipAmount");
          final List<Investment> _result = new ArrayList<Investment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Investment _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSymbol;
            _tmpSymbol = _cursor.getString(_cursorIndexOfSymbol);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpQuantity;
            _tmpQuantity = _cursor.getDouble(_cursorIndexOfQuantity);
            final double _tmpPurchasePrice;
            _tmpPurchasePrice = _cursor.getDouble(_cursorIndexOfPurchasePrice);
            final double _tmpCurrentPrice;
            _tmpCurrentPrice = _cursor.getDouble(_cursorIndexOfCurrentPrice);
            final String _tmpAssetType;
            _tmpAssetType = _cursor.getString(_cursorIndexOfAssetType);
            final double _tmpExpectedReturnApr;
            _tmpExpectedReturnApr = _cursor.getDouble(_cursorIndexOfExpectedReturnApr);
            final String _tmpBrokerName;
            _tmpBrokerName = _cursor.getString(_cursorIndexOfBrokerName);
            final double _tmpMonthlySipAmount;
            _tmpMonthlySipAmount = _cursor.getDouble(_cursorIndexOfMonthlySipAmount);
            _item = new Investment(_tmpId,_tmpSymbol,_tmpName,_tmpQuantity,_tmpPurchasePrice,_tmpCurrentPrice,_tmpAssetType,_tmpExpectedReturnApr,_tmpBrokerName,_tmpMonthlySipAmount);
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
  public Object getAllInvestments(final Continuation<? super List<Investment>> $completion) {
    final String _sql = "SELECT * FROM investments ORDER BY symbol ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Investment>>() {
      @Override
      @NonNull
      public List<Investment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "symbol");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfPurchasePrice = CursorUtil.getColumnIndexOrThrow(_cursor, "purchasePrice");
          final int _cursorIndexOfCurrentPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPrice");
          final int _cursorIndexOfAssetType = CursorUtil.getColumnIndexOrThrow(_cursor, "assetType");
          final int _cursorIndexOfExpectedReturnApr = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedReturnApr");
          final int _cursorIndexOfBrokerName = CursorUtil.getColumnIndexOrThrow(_cursor, "brokerName");
          final int _cursorIndexOfMonthlySipAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "monthlySipAmount");
          final List<Investment> _result = new ArrayList<Investment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Investment _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSymbol;
            _tmpSymbol = _cursor.getString(_cursorIndexOfSymbol);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpQuantity;
            _tmpQuantity = _cursor.getDouble(_cursorIndexOfQuantity);
            final double _tmpPurchasePrice;
            _tmpPurchasePrice = _cursor.getDouble(_cursorIndexOfPurchasePrice);
            final double _tmpCurrentPrice;
            _tmpCurrentPrice = _cursor.getDouble(_cursorIndexOfCurrentPrice);
            final String _tmpAssetType;
            _tmpAssetType = _cursor.getString(_cursorIndexOfAssetType);
            final double _tmpExpectedReturnApr;
            _tmpExpectedReturnApr = _cursor.getDouble(_cursorIndexOfExpectedReturnApr);
            final String _tmpBrokerName;
            _tmpBrokerName = _cursor.getString(_cursorIndexOfBrokerName);
            final double _tmpMonthlySipAmount;
            _tmpMonthlySipAmount = _cursor.getDouble(_cursorIndexOfMonthlySipAmount);
            _item = new Investment(_tmpId,_tmpSymbol,_tmpName,_tmpQuantity,_tmpPurchasePrice,_tmpCurrentPrice,_tmpAssetType,_tmpExpectedReturnApr,_tmpBrokerName,_tmpMonthlySipAmount);
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
