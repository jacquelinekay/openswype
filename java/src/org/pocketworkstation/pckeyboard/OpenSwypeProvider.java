package org.pocketworkstation.pckeyboard;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.pocketworkstation.pckeyboard.SwypeDictionary.Bigrams;
import org.pocketworkstation.pckeyboard.SwypeDictionary.Unigrams;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class OpenSwypeProvider extends ContentProvider {
	private Context mContext;
	public static DatabaseHelper mdbHelper;
	private static final UriMatcher sUriMatcher;

	public static class Constants {
	public static final String DB_NAME = "frequencies.sqlite";
	public static final String UNIGRAMS_TABLE_NAME = "unigrams";
	public static final String BIGRAMS_TABLE_NAME = "bigrams";
	public static final String AUTHORITY = "org.pocketworkstation.pckeyboard.provider.OpenSwype";
	}
	
	private static final String DATABASE_PATH = "/data/data/org.pocketworkstation.pckeyboard/databases/";
	private static final int DATABASE_VERSION = 1;
	public static final int UNIGRAMS = 1;
	public static final int UNIGRAMS_ID = 2;
	public static final int BIGRAMS = 3;
	public static final int BIGRAMS_ID = 4;
	
	public static class DatabaseHelper extends SQLiteOpenHelper{
		Context myContext;
		public DatabaseHelper(Context context) {
			super(context, Constants.DB_NAME, null, DATABASE_VERSION);
			myContext = context;
			try{
				createDataBase(false);
			} catch(IOException ioe){
				throw new Error("Unable to create database!");
			}
		}
		public void createDataBase(boolean upgrade) throws IOException{
			boolean dbExist = checkDataBase();
			if(dbExist && !upgrade){

			}else{
				SQLiteDatabase db = this.getReadableDatabase();
				if(dbExist && upgrade){
					db.delete(Unigrams.TABLE_NAME, null, null);
					db.delete(Bigrams.TABLE_NAME, null, null);
				}
				try {
					copyDataBase();
				} catch (IOException e) {
					throw new Error("Error copying database");
				}
			}
		}

		/**
		 * Check if the database already exist to avoid re-copying the file each time you open the application.
		 * @return true if it exists, false if it doesn't
		 */
		private boolean checkDataBase(){
			SQLiteDatabase checkDB = null;
			try{
				String myPath = DATABASE_PATH + Constants.DB_NAME;
				checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
			}catch(SQLiteException e){
				//database does't exist yet.
			}
			if(checkDB != null){
				checkDB.close();
			}
			return checkDB != null ? true : false;
		}

		/**
		 * Copies your database from your local assets-folder to the just created empty database in the
		 * system folder, from where it can be accessed and handled.
		 * This is done by transferring bytestream.
		 * */
		private void copyDataBase() throws IOException{
			//Open your local db as the input stream
			InputStream myInput = myContext.getAssets().open(Constants.DB_NAME);
			// Path to the just created empty db
			String outFileName = DATABASE_PATH + Constants.DB_NAME;
			//Open the empty db as the output stream
			OutputStream myOutput = new FileOutputStream(outFileName);
			//transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer))>0){
				myOutput.write(buffer, 0, length);
			}
			//Close the streams
			myOutput.flush();
			myOutput.close();
			myInput.close();

		}

		@Override
		public void onCreate(SQLiteDatabase db) {

		}

		//TODO: implement?
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/*Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");*/
			db.execSQL("DROP TABLE IF EXISTS " + Unigrams.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Bigrams.TABLE_NAME);
		}

	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mdbHelper.getWritableDatabase();
		int count;
		String id;
		switch (sUriMatcher.match(uri)) {
		case UNIGRAMS:
			count = db.delete(Unigrams.TABLE_NAME, where, whereArgs);
			break;
		case UNIGRAMS_ID:
			id = uri.getPathSegments().get(1);
			count = db.delete(Unigrams.TABLE_NAME, Unigrams.ID + "=" + id
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		case BIGRAMS:
			count = db.delete(Bigrams.TABLE_NAME, where, whereArgs);
			break;
		case BIGRAMS_ID:
			id = uri.getPathSegments().get(1);
			count = db.delete(Bigrams.TABLE_NAME, Bigrams.ID + "=" + id
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {

		String tableName;
		String column;
		Uri contentUri;
		switch (sUriMatcher.match(uri)) {
		case UNIGRAMS:
			tableName = Unigrams.TABLE_NAME;
			column = Unigrams.ID;
			contentUri = Unigrams.CONTENT_URI;
			break;

		case BIGRAMS:
			tableName = Bigrams.TABLE_NAME;
			column = Bigrams.ID;
			contentUri = Bigrams.CONTENT_URI;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mdbHelper.getWritableDatabase();
		long rowId = db.insert(tableName, column, initialValues);
		if (rowId > 0) {
			Uri articleUri = ContentUris.withAppendedId(contentUri, rowId);
			getContext().getContentResolver().notifyChange(articleUri, null);
			return articleUri;
		}

		throw new SQLException("Failed to insert row into " + tableName);
	}

	@Override
	public boolean onCreate() {

		mContext = getContext();
		mdbHelper = new DatabaseHelper(mContext);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (sUriMatcher.match(uri)) {
		case UNIGRAMS:
			qb.setTables(Unigrams.TABLE_NAME);
			//qb.setProjectionMap(sEventsProjectionMap);
			break;
		case UNIGRAMS_ID:
			qb.setTables(Unigrams.TABLE_NAME);
			//qb.setProjectionMap(sEventsProjectionMap);
			qb.appendWhere(Unigrams.ID + "=" + uri.getPathSegments().get(1));
			break;
		case BIGRAMS:
			qb.setTables(Bigrams.TABLE_NAME);
			//qb.setProjectionMap(sEventsProjectionMap);
			break;
		case BIGRAMS_ID:
			qb.setTables(Bigrams.TABLE_NAME);
			//qb.setProjectionMap(sEventsProjectionMap);
			qb.appendWhere(Bigrams.ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Get the database and run the query
		SQLiteDatabase db = mdbHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		// Tell the cursor what uri to watch, so it knows when its source data changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mdbHelper.getWritableDatabase();
		int count;
		String id;
		switch (sUriMatcher.match(uri)) {
		case UNIGRAMS:
			count = db.update(Unigrams.TABLE_NAME, values, where, whereArgs);
			break;

		case UNIGRAMS_ID:
			id = uri.getPathSegments().get(1);
			count = db.update(Unigrams.TABLE_NAME, values, Unigrams.ID + "=" + id
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;

		case BIGRAMS:
			count = db.update(Bigrams.TABLE_NAME, values, where, whereArgs);
			break;

		case BIGRAMS_ID:
			id = uri.getPathSegments().get(1);
			count = db.update(Bigrams.TABLE_NAME, values, Bigrams.ID + "=" + id
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Constants.AUTHORITY, "unigrams", UNIGRAMS);
		sUriMatcher.addURI(Constants.AUTHORITY, "unigrams/#", UNIGRAMS_ID);
		sUriMatcher.addURI(Constants.AUTHORITY, "bigrams", BIGRAMS);
		sUriMatcher.addURI(Constants.AUTHORITY, "bigrams/#", BIGRAMS_ID);
	}

}
