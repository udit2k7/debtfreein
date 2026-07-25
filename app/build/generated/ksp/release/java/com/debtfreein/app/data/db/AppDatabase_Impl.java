package com.debtfreein.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.debtfreein.app.data.dao.CardDao;
import com.debtfreein.app.data.dao.CardDao_Impl;
import com.debtfreein.app.data.dao.ExpenseDao;
import com.debtfreein.app.data.dao.ExpenseDao_Impl;
import com.debtfreein.app.data.dao.InvestmentDao;
import com.debtfreein.app.data.dao.InvestmentDao_Impl;
import com.debtfreein.app.data.dao.SystemLogDao;
import com.debtfreein.app.data.dao.SystemLogDao_Impl;
import com.debtfreein.app.data.dao.TokenSpendDao;
import com.debtfreein.app.data.dao.TokenSpendDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile CardDao _cardDao;

  private volatile ExpenseDao _expenseDao;

  private volatile InvestmentDao _investmentDao;

  private volatile SystemLogDao _systemLogDao;

  private volatile TokenSpendDao _tokenSpendDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `credit_cards` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `issuer` TEXT NOT NULL, `currentBalance` REAL NOT NULL, `creditLimit` REAL NOT NULL, `apr` REAL NOT NULL, `dueDay` INTEGER NOT NULL, `nextDueDate` TEXT, `minimumPayment` REAL NOT NULL, `cardLastFour` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `merchant` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `category` TEXT NOT NULL, `cardId` INTEGER, `rawSmsText` TEXT, `isReimbursableClaim` INTEGER NOT NULL, `expenseCategory` TEXT NOT NULL, `status` TEXT, `notes` TEXT, FOREIGN KEY(`cardId`) REFERENCES `credit_cards`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_cardId` ON `expenses` (`cardId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `investments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `symbol` TEXT NOT NULL, `name` TEXT NOT NULL, `quantity` REAL NOT NULL, `purchasePrice` REAL NOT NULL, `currentPrice` REAL NOT NULL, `assetType` TEXT NOT NULL, `expectedReturnApr` REAL NOT NULL, `brokerName` TEXT NOT NULL, `monthlySipAmount` REAL NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `system_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `message` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `level` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `token_spends` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `model` TEXT NOT NULL, `inputTokens` INTEGER NOT NULL, `outputTokens` INTEGER NOT NULL, `costInr` REAL NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '20c3a6f2d9b4ddc39232b5f39d74e7c3')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `credit_cards`");
        db.execSQL("DROP TABLE IF EXISTS `expenses`");
        db.execSQL("DROP TABLE IF EXISTS `investments`");
        db.execSQL("DROP TABLE IF EXISTS `system_logs`");
        db.execSQL("DROP TABLE IF EXISTS `token_spends`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCreditCards = new HashMap<String, TableInfo.Column>(10);
        _columnsCreditCards.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditCards.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditCards.put("issuer", new TableInfo.Column("issuer", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditCards.put("currentBalance", new TableInfo.Column("currentBalance", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditCards.put("creditLimit", new TableInfo.Column("creditLimit", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditCards.put("apr", new TableInfo.Column("apr", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditCards.put("dueDay", new TableInfo.Column("dueDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditCards.put("nextDueDate", new TableInfo.Column("nextDueDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditCards.put("minimumPayment", new TableInfo.Column("minimumPayment", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditCards.put("cardLastFour", new TableInfo.Column("cardLastFour", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCreditCards = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCreditCards = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCreditCards = new TableInfo("credit_cards", _columnsCreditCards, _foreignKeysCreditCards, _indicesCreditCards);
        final TableInfo _existingCreditCards = TableInfo.read(db, "credit_cards");
        if (!_infoCreditCards.equals(_existingCreditCards)) {
          return new RoomOpenHelper.ValidationResult(false, "credit_cards(com.debtfreein.app.data.model.CreditCard).\n"
                  + " Expected:\n" + _infoCreditCards + "\n"
                  + " Found:\n" + _existingCreditCards);
        }
        final HashMap<String, TableInfo.Column> _columnsExpenses = new HashMap<String, TableInfo.Column>(11);
        _columnsExpenses.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("merchant", new TableInfo.Column("merchant", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("cardId", new TableInfo.Column("cardId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("rawSmsText", new TableInfo.Column("rawSmsText", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("isReimbursableClaim", new TableInfo.Column("isReimbursableClaim", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("expenseCategory", new TableInfo.Column("expenseCategory", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("status", new TableInfo.Column("status", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExpenses = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysExpenses.add(new TableInfo.ForeignKey("credit_cards", "SET NULL", "NO ACTION", Arrays.asList("cardId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesExpenses = new HashSet<TableInfo.Index>(1);
        _indicesExpenses.add(new TableInfo.Index("index_expenses_cardId", false, Arrays.asList("cardId"), Arrays.asList("ASC")));
        final TableInfo _infoExpenses = new TableInfo("expenses", _columnsExpenses, _foreignKeysExpenses, _indicesExpenses);
        final TableInfo _existingExpenses = TableInfo.read(db, "expenses");
        if (!_infoExpenses.equals(_existingExpenses)) {
          return new RoomOpenHelper.ValidationResult(false, "expenses(com.debtfreein.app.data.model.Expense).\n"
                  + " Expected:\n" + _infoExpenses + "\n"
                  + " Found:\n" + _existingExpenses);
        }
        final HashMap<String, TableInfo.Column> _columnsInvestments = new HashMap<String, TableInfo.Column>(10);
        _columnsInvestments.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("symbol", new TableInfo.Column("symbol", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("quantity", new TableInfo.Column("quantity", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("purchasePrice", new TableInfo.Column("purchasePrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("currentPrice", new TableInfo.Column("currentPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("assetType", new TableInfo.Column("assetType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("expectedReturnApr", new TableInfo.Column("expectedReturnApr", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("brokerName", new TableInfo.Column("brokerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("monthlySipAmount", new TableInfo.Column("monthlySipAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInvestments = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesInvestments = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoInvestments = new TableInfo("investments", _columnsInvestments, _foreignKeysInvestments, _indicesInvestments);
        final TableInfo _existingInvestments = TableInfo.read(db, "investments");
        if (!_infoInvestments.equals(_existingInvestments)) {
          return new RoomOpenHelper.ValidationResult(false, "investments(com.debtfreein.app.data.model.Investment).\n"
                  + " Expected:\n" + _infoInvestments + "\n"
                  + " Found:\n" + _existingInvestments);
        }
        final HashMap<String, TableInfo.Column> _columnsSystemLogs = new HashMap<String, TableInfo.Column>(4);
        _columnsSystemLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSystemLogs.put("message", new TableInfo.Column("message", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSystemLogs.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSystemLogs.put("level", new TableInfo.Column("level", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSystemLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSystemLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSystemLogs = new TableInfo("system_logs", _columnsSystemLogs, _foreignKeysSystemLogs, _indicesSystemLogs);
        final TableInfo _existingSystemLogs = TableInfo.read(db, "system_logs");
        if (!_infoSystemLogs.equals(_existingSystemLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "system_logs(com.debtfreein.app.data.model.SystemLog).\n"
                  + " Expected:\n" + _infoSystemLogs + "\n"
                  + " Found:\n" + _existingSystemLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsTokenSpends = new HashMap<String, TableInfo.Column>(6);
        _columnsTokenSpends.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTokenSpends.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTokenSpends.put("model", new TableInfo.Column("model", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTokenSpends.put("inputTokens", new TableInfo.Column("inputTokens", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTokenSpends.put("outputTokens", new TableInfo.Column("outputTokens", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTokenSpends.put("costInr", new TableInfo.Column("costInr", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTokenSpends = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTokenSpends = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTokenSpends = new TableInfo("token_spends", _columnsTokenSpends, _foreignKeysTokenSpends, _indicesTokenSpends);
        final TableInfo _existingTokenSpends = TableInfo.read(db, "token_spends");
        if (!_infoTokenSpends.equals(_existingTokenSpends)) {
          return new RoomOpenHelper.ValidationResult(false, "token_spends(com.debtfreein.app.data.model.TokenSpend).\n"
                  + " Expected:\n" + _infoTokenSpends + "\n"
                  + " Found:\n" + _existingTokenSpends);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "20c3a6f2d9b4ddc39232b5f39d74e7c3", "0b0f2f55b2f73eb1ced767aff1f6e29a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "credit_cards","expenses","investments","system_logs","token_spends");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `credit_cards`");
      _db.execSQL("DELETE FROM `expenses`");
      _db.execSQL("DELETE FROM `investments`");
      _db.execSQL("DELETE FROM `system_logs`");
      _db.execSQL("DELETE FROM `token_spends`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CardDao.class, CardDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ExpenseDao.class, ExpenseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(InvestmentDao.class, InvestmentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SystemLogDao.class, SystemLogDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TokenSpendDao.class, TokenSpendDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CardDao cardDao() {
    if (_cardDao != null) {
      return _cardDao;
    } else {
      synchronized(this) {
        if(_cardDao == null) {
          _cardDao = new CardDao_Impl(this);
        }
        return _cardDao;
      }
    }
  }

  @Override
  public ExpenseDao expenseDao() {
    if (_expenseDao != null) {
      return _expenseDao;
    } else {
      synchronized(this) {
        if(_expenseDao == null) {
          _expenseDao = new ExpenseDao_Impl(this);
        }
        return _expenseDao;
      }
    }
  }

  @Override
  public InvestmentDao investmentDao() {
    if (_investmentDao != null) {
      return _investmentDao;
    } else {
      synchronized(this) {
        if(_investmentDao == null) {
          _investmentDao = new InvestmentDao_Impl(this);
        }
        return _investmentDao;
      }
    }
  }

  @Override
  public SystemLogDao systemLogDao() {
    if (_systemLogDao != null) {
      return _systemLogDao;
    } else {
      synchronized(this) {
        if(_systemLogDao == null) {
          _systemLogDao = new SystemLogDao_Impl(this);
        }
        return _systemLogDao;
      }
    }
  }

  @Override
  public TokenSpendDao tokenSpendDao() {
    if (_tokenSpendDao != null) {
      return _tokenSpendDao;
    } else {
      synchronized(this) {
        if(_tokenSpendDao == null) {
          _tokenSpendDao = new TokenSpendDao_Impl(this);
        }
        return _tokenSpendDao;
      }
    }
  }
}
