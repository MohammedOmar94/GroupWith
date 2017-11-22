package haitsu.groupup.other;

/**
 * Code adapted from
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DBHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    public static final String DATABASE_NAME = "FlashcardBuddy";
    // Table names
    private static final String TABLE_LEITNER_SYSTEM = "LeitnerSystem";
    private static final String TABLE_RESULTS = "Results";
    private static final String TABLE_SUPERMEMO = "SuperMemo";
    private static final String TABLE_SUPERMEMO_AI = "SuperMemoAI";
    // Flashcards Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_WORD = "word";
    private static final String KEY_TRANSLATION = "wordTranslated";
    private static final String KEY_SPELLING = "last_spelling_error";
    private static final String KEY_INTERVAL = "repetitionInterval";
    // private static final String KEY_DAYS_LEFT = "daysUntilReview";//Use for leitner.
    private static final String KEY_DATE_ADDED = "dateAdded";
    private static final String KEY_REVIEW_DATE = "reviewDate";

    private static final String KEY_EFACTOR = "efactor";//Column exclusive to SuperMemo.
    private static final String KEY_BOX_NUMBER = "boxNumber";//Column exclusive to the LeitnerSystem.
    private static final String KEY_QUALITY_OF_RESPONSE = "qualityOfResponse";

    /* Column names for the Results table. */
    private static final String KEY_ALGORITHM = "algorithmName";
    private static final String KEY_SUCCESS_COUNT = "successCount";
    private static final String KEY_CURRENT_INTERVAL = "currentInterval";
    private static final String KEY_SUCCESS_RATE = "successRate";
    private static final String KEY_START_DATE = "startDate";
    private static final String KEY_END_DATE = "endDate";


    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String[] statements = new String[]{createMtGroupsTable()};

        for (String sql : statements) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEITNER_SYSTEM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUPERMEMO);
// Creating tables again
        onCreate(db);
    }

    public String createMtGroupsTable() {
        String CREATE_LEITNER_SYSTEM_TABLE = "CREATE TABLE MyGroups ("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + "Userid TEXT, "
                + "lastMessage TEXT,"
                + "messageDate TEXT, "
                + "sentBy TEXT" + ")";
        return CREATE_LEITNER_SYSTEM_TABLE;
    }

    public List<ChatMessage> getData(){
        String selectQuery = "SELECT * FROM MyGroups";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        List<ChatMessage> row = new ArrayList<ChatMessage>();
// looping through all rows and add
// ng to list
        if (cursor.moveToFirst()) {
            do {
                ChatMessage message = new ChatMessage();
                message.setMessageText(cursor.getString(2));
                message.setMessageTime(cursor.getLong(3));
                message.setMessageUser(cursor.getString(4));
                row.add(message);
            } while (cursor.moveToNext());
        }
        return row;
    }

    public void displayMessage() {
        Log.d("My Groups: ", "Display chats data..");
        List<ChatMessage> rows = null;
        rows = getData();
        for (ChatMessage message : rows) {
            Date messageDate = new Date(message.getMessageTime());
            String log = "message: " + message.getMessageText()
                    + " ,time: " + android.text.format.DateFormat.format("HH:mm", messageDate)
                    + " ,sent by: " +  message.getMessageUser();
            Log.d("My Groups: ", log);
        }
    }

    public void addData(){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Userid","12");
        values.put("[Name]","testName");
        values.put("lastMessage","abc");
        values.put("messageDate","01/01/1979");
        values.put("sentBy","Simon");
        db.insert("MyGroups", null, values);
        db.close(); // Closing database connection
    }

    public void addMessage(String userid, String message, long messageTime, String sentBy){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
//        db.delete("MyGroups",null, null);
        values.put("Userid", userid);
        values.put("lastMessage", message);
        values.put("messageDate", Long.toString(messageTime));
        values.put("sentBy", sentBy);
        db.insert("MyGroups", null, values);
        db.close(); // Closing database connection
    }

    public void dropTable(String TABLE_NAME) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

//    /* Gets results for all algorithms. */
//    public List<Flashcard> getFlashcardResults() throws ParseException {
//        List<Flashcard> FlashcardList = new ArrayList<Flashcard>();
//        DateFormat format = new SimpleDateFormat("EEEE dd-MM-yyy");
//        format.setTimeZone(TimeZone.getTimeZone("GMT"));
//// Select All Query
//        String selectQuery = "SELECT * FROM Results";
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//// looping through all rows and add
//// ng to list
//        if (cursor.moveToFirst()) {
//            do {
//                Flashcard fc = new Flashcard();
//                fc.setId(Integer.parseInt(cursor.getString(0)));
//                fc.setAlgorithmName(cursor.getString(1));
//                fc.setSuccessCount(Integer.parseInt(cursor.getString(2)));
//                fc.setCurrentInterval(Integer.parseInt(cursor.getString(3)));
//                fc.setSuccessRate(Double.parseDouble(cursor.getString(4)));
//                fc.setStartDate(cursor.getString(5));
//                fc.setEndDate(cursor.getString(6));
//                FlashcardList.add(fc);
//            } while (cursor.moveToNext());
//        }
//
//// return contact list
//        return FlashcardList;
//    }
//
//    /* Gets results for a single algorithm. */
//    public List<Flashcard> getFlashcardResult(String algorithmName) throws ParseException {
//        List<Flashcard> FlashcardList = new ArrayList<Flashcard>();
//        DateFormat format = new SimpleDateFormat("EEEE dd-MM-yyy");
//        format.setTimeZone(TimeZone.getTimeZone("GMT"));
//// Select All Query
//        String selectQuery = "SELECT * FROM Results WHERE algorithmName='" + algorithmName + "'";
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//// looping through all rows and add
//// ng to list
//        if (cursor.moveToFirst()) {
//            do {
//                Flashcard fc = new Flashcard();
//                fc.setId(Integer.parseInt(cursor.getString(0)));
//                fc.setAlgorithmName(cursor.getString(1));
//                fc.setSuccessCount(Integer.parseInt(cursor.getString(2)));
//                fc.setCurrentInterval(Integer.parseInt(cursor.getString(3)));
//                fc.setSuccessRate(Double.parseDouble(cursor.getString(4)));
//                fc.setStartDate(cursor.getString(5));
//                fc.setEndDate(cursor.getString(6));
//                FlashcardList.add(fc);
//            } while (cursor.moveToNext());
//        }
//
//// return contact list
//        return FlashcardList;
//    }

//    /* Adds word to database. */
//    public void addFlashcard(Flashcard Flashcard, String TABLE_NAME) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(KEY_WORD, Flashcard.getWord()); // Flashcard Name
//        values.put(KEY_TRANSLATION, Flashcard.getWordTranslated()); // Only works SuperMemo, but Leitner sets property.
//        values.put(KEY_INTERVAL, Flashcard.getInterval()); // Flashcard Phone Number
//        if (TABLE_NAME == "SuperMemo") {
//            values.put(KEY_EFACTOR, 2.5f);
//            values.put(KEY_DATE_ADDED, SuperMemo.getCurrentDate()); // Flashcard Phone Number
//            values.put(KEY_REVIEW_DATE, Flashcard.getReviewDate()); // Flashcard Phone Number
//            values.put(KEY_QUALITY_OF_RESPONSE, -1); // Has to be -1, since you can rate 0 stars, and on the first review, it's technically an improvement.
//        } else if (TABLE_NAME == "LeitnerSystem") {
//            System.out.println("The word translated is :" + Flashcard.getWordTranslated());
//            values.put(KEY_BOX_NUMBER, 1);
//            values.put(KEY_DATE_ADDED, LeitnerSystem.getCurrentDate()); // Flashcard Phone Number
//            values.put(KEY_REVIEW_DATE, Flashcard.getReviewDate()); // Flashcard Phone Number
//        } else if (TABLE_NAME == "SuperMemoAI") {
//            values.put(KEY_EFACTOR, 2.5f);
//            values.put(KEY_DATE_ADDED, SuperMemo.getCurrentDate()); // Flashcard Phone Number
//            values.put(KEY_REVIEW_DATE, Flashcard.getReviewDate()); // Flashcard Phone Number
//            values.put(KEY_QUALITY_OF_RESPONSE, -1); // Has to be -1, since you can rate 0 stars, and on the first review, it's technically an improvement.
//        }
//        values.put(KEY_SPELLING, ""); // Flashcard Phone Number
//// Inserting Row
//        db.insert(TABLE_NAME, null, values);
//        db.close(); // Closing database connection
//    }
//
//    /* Adds algorithm results to table. */
//    public void addResults(String TABLE_NAME) throws ParseException {
//        Calendar c = Calendar.getInstance();
//        DateFormat date = new SimpleDateFormat("EEEE dd-MM-yyy");
//        date.setTimeZone(TimeZone.getTimeZone("GMT"));
//        c.setTime(date.parse(SuperMemo.getCurrentDate()));
//        c.add(Calendar.WEEK_OF_MONTH, 2);
//        String endDate = date.format(c.getTime());
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(KEY_ALGORITHM, TABLE_NAME); // Flashcard Name
//        values.put(KEY_SUCCESS_COUNT, 0); // Only works SuperMemo, but Leitner sets property.
//        values.put(KEY_CURRENT_INTERVAL, 0); // Flashcard Phone Number
//        values.put(KEY_SUCCESS_RATE, 0);
//        values.put(KEY_START_DATE, Flashcard.getCurrentDate()); // Flashcard Phone Number
//        values.put(KEY_END_DATE, endDate); // Flashcard Phone Number
//
//// Inserting Row
//        db.insert(TABLE_RESULTS, null, values);
//        db.close(); // Closing database connection
//    }
//
//    /* Updates results for a particular algorithm. */
//    public void updateResults(String algorithmName, String answerType, int currentInterval, int successCount, int qualityOfResponse) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        int successfulAnswer = 0;
//        int id = 0;
//
//        if (algorithmName == "LeitnerSystem") {
//            id = 1;
//            if (answerType == "Okay") {
//                successfulAnswer = 1;
//            } else if (answerType == "Difficult") {
//                //successfulAnswer = -1;
//            }
//        }
//
//        if (algorithmName == "SuperMemo") {
//            id = 2;
//            System.out.println("The answerType is " + answerType + " Quality " + qualityOfResponse);
//            if (Integer.parseInt(answerType) > qualityOfResponse || Integer.parseInt(answerType) == 5) {//There's an improvement, rating vs previousRating
//                successfulAnswer = 1;
//            } else if (Integer.parseInt(answerType) < qualityOfResponse) {
//                //    successfulAnswer = -1;
//            } else if (Integer.parseInt(answerType) == qualityOfResponse) {
//                successfulAnswer = 0;
//            }
//        }
//
//        if (algorithmName == "SuperMemoAI") {
//            System.out.println("The answerType is " + answerType + " Quality " + qualityOfResponse);
//            id = 3;
//            if (Integer.parseInt(answerType) > qualityOfResponse || Integer.parseInt(answerType) == 5) {//There's an improvement, rating vs previousRating
//                successfulAnswer = 1;
//            } else if (Integer.parseInt(answerType) < qualityOfResponse) {
//                //  successfulAnswer = -1;
//            } else if (Integer.parseInt(answerType) == qualityOfResponse) {
//                successfulAnswer = 0;
//            }
//        }
//
//        successCount = successCount + successfulAnswer;
//        if (successCount < 0) {
//            successCount = 0;
//        }
//        double successRate = (successCount * 100 / currentInterval);//successCount is basically subtracts the fail count.
//        //     if (currentInterval ==) {
//        values.put(KEY_SUCCESS_COUNT, successCount);
//        System.out.println("Updating interval to " + currentInterval + " id is " + id + " success count is " + successCount);
//        values.put(KEY_CURRENT_INTERVAL, currentInterval);
//        values.put(KEY_SUCCESS_RATE, successRate);
//        db.update("Results", values, "algorithmName='" + algorithmName + "'", null);
//        // }
//    }
//
//    // Getting All Flashcards
//    public List<Flashcard> getAllFlashcards(String TABLE_NAME, Flashcard flashcard) {
//        List<Flashcard> FlashcardList = new ArrayList<Flashcard>();
//// Select All Query
//        String selectQuery = "SELECT * FROM " + TABLE_NAME;
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//// looping through all rows and add
//// ng to list
//        if (cursor.moveToFirst()) {
//            do {
//                flashcard.setId(Integer.parseInt(cursor.getString(0)));
//                flashcard.setWord(cursor.getString(1));
//                flashcard.setInterval(Integer.parseInt(cursor.getString(2)));
//                // Adding contact to list
//                FlashcardList.add(flashcard);
//            } while (cursor.moveToNext());
//        }
//// return contact list
//        return FlashcardList;
//    }
//
//

//
//    public String createSupermemoTable() {
//        String CREATE_SUPERMEMO_TABLE = "CREATE TABLE " + TABLE_SUPERMEMO + "("
//                + KEY_ID + " INTEGER PRIMARY KEY, "
//                + KEY_WORD + " TEXT,"
//                + KEY_TRANSLATION + " TEXT,"
//                + KEY_INTERVAL + " INTEGER, "
//                + KEY_EFACTOR + " DOUBLE, "
//                + KEY_SPELLING + " INTEGER, "
//                + KEY_QUALITY_OF_RESPONSE + " INTEGER, "
//                + KEY_DATE_ADDED + " TEXT, "
//                + KEY_REVIEW_DATE + " TEXT" + ")";
//        return CREATE_SUPERMEMO_TABLE;
//    }
//
//    public String createSupermemoAITable() {
//        String CREATE_SUPERMEMOAI_TABLE = "CREATE TABLE " + TABLE_SUPERMEMO_AI + "("
//                + KEY_ID + " INTEGER PRIMARY KEY, "
//                + KEY_WORD + " TEXT,"
//                + KEY_TRANSLATION + " TEXT,"
//                + KEY_INTERVAL + " INTEGER, "
//                + KEY_EFACTOR + " DOUBLE, "
//                + KEY_SPELLING + " INTEGER, "
//                + KEY_QUALITY_OF_RESPONSE + " INTEGER, "
//                + KEY_DATE_ADDED + " TEXT, "
//                + KEY_REVIEW_DATE + " TEXT" + ")";
//        return CREATE_SUPERMEMOAI_TABLE;
//    }
//
//    public String createResultsTable() {
//        String CREATE_RESULTS_TABLE = "CREATE TABLE " + TABLE_RESULTS + "("
//                + KEY_ID + " INTEGER PRIMARY KEY, "
//                + KEY_ALGORITHM + " TEXT,"
//                + KEY_SUCCESS_COUNT + " TEXT,"
//                + KEY_CURRENT_INTERVAL + " INTEGER, "
//                + KEY_SUCCESS_RATE + " DOUBLE, "
//                + KEY_START_DATE + " TEXT, "
//                + KEY_END_DATE + " TEXT" + ")";
//        return CREATE_RESULTS_TABLE;
//    }
//
//
//    public void databaseStatus() throws ParseException {
//        Flashcard flashcard = new Flashcard();
//        if (databaseEmpty("LeitnerSystem")) {
//            Log.d("Empty database: ", "Adding data ..");
//            addFlashcard(new LeitnerSystem(0, "Kore", "This", 0, null, LeitnerSystem.getCurrentDate(), LeitnerSystem.getCurrentDate(), 1), "LeitnerSystem");
//            addResults("LeitnerSystem");
//            //addFlashcard(new LeitnerSystem(0, "Sore", "That", 0, null, LeitnerSystem.getCurrentDate(), LeitnerSystem.getCurrentDate(), 1), "LeitnerSystem");
//        } else {
//            Log.d("Full LeitnerSystem:", "Enough data is already stored ..");
//        }
//        if (databaseEmpty("SuperMemo")) {
//            //addFlashcard(new SuperMemo(0, "Kore", "This", 0, null, flashcard.getCurrentDate(), flashcard.getCurrentDate(), 2.5f, 0), "SuperMemo");
//            addFlashcard(new SuperMemo(0, "Sore", "That", 0, null, SuperMemo.getCurrentDate(), SuperMemo.getCurrentDate(), 2.5f, 0), "SuperMemo");
//            addResults("SuperMemo");
//        } else {
//            Log.d("Full SuperMemo: ", "Enough data is already stored ..");
//        }
//        if (databaseEmpty("SuperMemoAI")) {
//            //addFlashcard(new SuperMemo(0, "Kore", "This", 0, null, flashcard.getCurrentDate(), flashcard.getCurrentDate(), 2.5f, 0), "SuperMemo");
//            addFlashcard(new SuperMemo(0, "Nani", "What", 0, null, SuperMemo.getCurrentDate(), SuperMemo.getCurrentDate(), 2.5f, 0), "SuperMemoAI");
//            addResults("SuperMemoAI");
//        } else {
//            Log.d("Full SuperMemo: ", "Enough data is already stored ..");
//        }
//    }
//
//    public Boolean databaseEmpty(String TABLE_NAME) {
//        String selectQuery = "SELECT * FROM " + TABLE_NAME;
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        boolean empty = true;
//        try {
//            if (cursor.moveToFirst()) { //If it isn't empty...
//                empty = false;
//            } else {
//                empty = true;
//            }
//        } finally {
//            cursor.close();
//        }
//        return empty;
//    }
//
//    public void showAllTables() {
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor c = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table'", null);
//        while (c.moveToNext()) {
//            for (int i = 0; i < c.getCount(); i++) {
//                System.out.println(c.getString(i));
//            }
//        }
//    }
//
//    public void deleteTable(String TABLE_NAME, String WHERE_CLAUSE) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        if (WHERE_CLAUSE != null) {
//            WHERE_CLAUSE = "id = 3";
//        }
//        db.delete(TABLE_NAME, WHERE_CLAUSE, null);
//        db.close();
//    }
//

//
//    //Gets the end date for an algorithm.
//    public Date checkEndDate(String algorithmName) throws ParseException {
//        Date endDate = null;
//        DateFormat format = new SimpleDateFormat("EEEE dd-MM-yyy");
//        format.setTimeZone(TimeZone.getTimeZone("GMT"));
//        String selectQuery = "SELECT * FROM Results WHERE algorithmName ='" + algorithmName + "'";
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        if (cursor.moveToFirst()) {
//            do {
//                endDate = format.parse(cursor.getString(6));
//            } while (cursor.moveToNext());
//        }
//        //System.out.println("End date for " + algorithmName + " is " + endDate);
//        return endDate;
//    }
//
//
//    /*****
//     * METHODS ONLY USED IN TESTS
//     ******/
//
//    public int getAvaliableCards(String TABLE_NAME) {
//        String selectQuery = "SELECT * FROM " + TABLE_NAME;
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        int id = 0;
//        try {
//            id = cursor.getCount();
//        } finally {
//            cursor.close();
//        }
//        return id;
//    }
//
//    //Sets the review date for an algorithm, allows for endless reviews. Used for testing the results screen.
//    public void testReviewDate(int id, String TABLE_NAME) throws ParseException {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(KEY_REVIEW_DATE, SuperMemo.getCurrentDate());
//        //    System.out.println("Updated row:" + newReviewDate);
//        //    System.out.println("Row ID is :" + ls.getId());
//        db.update(TABLE_NAME, values, "id=" + id, null);
//    }
//
//    //Sets review date as the end date for an algorithm.
//    public void testEndDate(int id, String TABLE_NAME) throws ParseException {
//        Calendar c = Calendar.getInstance();
//        DateFormat date = new SimpleDateFormat("EEEE dd-MM-yyy");
//        date.setTimeZone(TimeZone.getTimeZone("GMT"));
//        c.setTime(date.parse(SuperMemo.getCurrentDate()));
//        c.add(Calendar.WEEK_OF_MONTH, 2);
//        String endDate = date.format(c.getTime());
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(KEY_REVIEW_DATE, endDate);
//        System.out.println("Updated row:" + endDate);
//        //    System.out.println("Row ID is :" + ls.getId());
//        db.update(TABLE_NAME, values, "id=" + id, null);
//    }
}


