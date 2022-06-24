package com.moop.gamerguides.helper


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHandler(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME: String = "course_db"
        private const val DB_VERSION: Int = 1

        // table names
        private const val USER_LIST: String = "users"
        private const val COURSE_LIST: String = "courses"
        private const val COURSE_USER: String = "user_courses"

        // column names for all tables
        private const val COURSE_ID: String = "course_id"
        private const val USER_ID: String = "user_id"

        // USER_LIST column names
        private const val USER_USERNAME: String = "username"
        private const val USER_EMAIL: String = "email"
        private const val USER_PASS: String = "password"

        // COURSE_LIST column names
        private const val COURSE_NAME: String = "name"
        private const val COURSE_DURATION: String = "duration"
        private const val COURSE_DESCRIPTION: String = "description"
        private const val COURSE_COST: String = "cost"
        private const val COURSE_VID_URL: String = "vid_url"
        private const val COURSE_IMG_URL: String = "img_url"

        // COURSE_USER column names
        private const val COURSE_DONE: String = "isFinished"
        private const val COURSE_PAYMENT_DONE: String = "isPaid"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // create USER_LIST table
        var createTableQuery: String = (
                "CREATE TABLE IF NOT EXISTS "
                        + USER_LIST + "("
                        + USER_ID + " TEXT PRIMARY KEY, "
                        + USER_USERNAME + " TEXT, "
                        + USER_EMAIL + " TEXT, "
                        + USER_PASS + " TEXT)"
                )
        db?.execSQL(createTableQuery)

        // create COURSE_LIST table
        createTableQuery = (
                "CREATE TABLE IF NOT EXISTS "
                        + COURSE_LIST + "("
                        + COURSE_ID + " TEXT PRIMARY KEY, "
                        + COURSE_NAME + " TEXT, "
                        + COURSE_DURATION + " TEXT, "
                        + COURSE_DESCRIPTION + " TEXT, "
                        + COURSE_COST + " TEXT, "
                        + COURSE_VID_URL + " TEXT, "
                        + COURSE_IMG_URL + " TEXT)"
                )
        db?.execSQL(createTableQuery)

        // create COURSE_USER table
        createTableQuery = (
                "CREATE TABLE IF NOT EXISTS "
                        + COURSE_USER + "("
                        + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, " // only used for primary key purposes
                        + USER_ID + " TEXT, "
                        + COURSE_ID + " TEXT, "
                        + COURSE_DONE + " TEXT, "
                        + COURSE_PAYMENT_DONE + " TEXT)"
                )
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $USER_LIST")
        db?.execSQL("DROP TABLE IF EXISTS $COURSE_LIST")
        db?.execSQL("DROP TABLE IF EXISTS $COURSE_USER")
        onCreate(db)
    }

    // insert data into table
    fun addNewUser(uid: String, username: String, email: String, password: String) {
        val db = this.writableDatabase
        val data = ContentValues()
        data.put(USER_ID, uid)
        data.put(USER_USERNAME, username)
        data.put(USER_EMAIL, email)
        data.put(USER_PASS, password)

        db.insert(USER_LIST, null, data)
        db.close()
    }
    fun addNewCourse(cid: String, name: String, duration: String, description: String, cost: String, vidUrl: String, imgUrl: String) {
        val db = this.writableDatabase
        val data = ContentValues()
        data.put(COURSE_ID, cid)
        data.put(COURSE_NAME, name)
        data.put(COURSE_DURATION, duration)
        data.put(COURSE_DESCRIPTION, description)
        data.put(COURSE_COST, cost)
        data.put(COURSE_VID_URL, vidUrl)
        data.put(COURSE_IMG_URL, imgUrl)

        db.insert(COURSE_LIST, null, data)
        db.close()
    }
    fun addNewUserCourse(cid: String, uid: String) {
        val db = this.writableDatabase
        val data = ContentValues()
        data.put(USER_ID, uid)
        data.put(COURSE_ID, cid)
        data.put(COURSE_DONE, "FALSE")
        data.put(COURSE_PAYMENT_DONE, "FALSE")

        db.insert(COURSE_USER, null, data)
        db.close()
    }

    // remove data from table
    fun removeUser(uid: String) {
        val db = this.writableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $USER_LIST WHERE $USER_ID='$uid'"
            , null
        )
        if (cursor.moveToFirst()) {
            db.execSQL(
                "DELETE FROM $USER_LIST WHERE $USER_ID='$uid'"
            )
        }
        cursor.close()
        db.close()
    }
    fun removeCourse(cid: String) {
        val db = this.writableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $COURSE_LIST WHERE $COURSE_ID='$cid'"
            , null
        )
        if (cursor.moveToFirst()) {
            db.execSQL(
                "DELETE FROM $COURSE_LIST WHERE $COURSE_ID='$cid'"
            )
        }
        cursor.close()
        db.close()
    }
    fun removeUserCourse(uid: String, cid: String) {
        val db = this.writableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT _id FROM $COURSE_USER WHERE $USER_ID='$uid' AND $COURSE_ID='$cid'"
            , null
        )
        if (cursor.moveToFirst()) {
            val id = cursor.getString(1)
            db.execSQL(
                "DELETE FROM $COURSE_USER WHERE _id='$id'"
            )
        }
        cursor.close()
        db.close()
    }

    // update data from COURSE_USER table
    fun isCourseDone(uid: String, cid: String, isDone: String) {
        val db = this.writableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT _id FROM $COURSE_USER WHERE $USER_ID='$uid' AND $COURSE_ID='$cid'"
            , null
        )
        if (cursor.moveToFirst()) {
            val id = cursor.getString(1)
            val boolIsDone = isDone.uppercase()
            db.execSQL(
                "UPDATE $COURSE_USER SET $COURSE_DONE=$boolIsDone WHERE _id='$id'"
            )
        }
    }
    fun isCoursePaymentDone(uid: String, cid: String, isPaymentDone: String) {
        val db = this.writableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT _id FROM $COURSE_USER WHERE $USER_ID='$uid' AND $COURSE_ID='$cid'"
            , null
        )
        if (cursor.moveToFirst()) {
            val id = cursor.getString(1)
            val boolIsDone = isPaymentDone.uppercase()
            db.execSQL(
                "UPDATE $COURSE_USER SET $COURSE_PAYMENT_DONE=$boolIsDone WHERE _id='$id'"
            )
        }
    }

}
