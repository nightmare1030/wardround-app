package com.wardround.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Patient::class,
        WardRoundRecord::class,
        FocusItem::class,
        OperationLog::class,
        Treatment::class,
        Medication::class,
        Exam::class,
        DrainageRecord::class,
        TreatmentTemplate::class,
        TemplateMedication::class,
        InfusionGroup::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun recordDao(): WardRoundRecordDao
    abstract fun focusItemDao(): FocusItemDao
    abstract fun operationLogDao(): OperationLogDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun medicationDao(): MedicationDao
    abstract fun examDao(): ExamDao
    abstract fun drainageDao(): DrainageDao
    abstract fun treatmentTemplateDao(): TreatmentTemplateDao
    abstract fun templateMedicationDao(): TemplateMedicationDao
    abstract fun infusionGroupDao(): InfusionGroupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ward_round_records ADD COLUMN imagePath TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE patients ADD COLUMN surgeryDate INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS treatments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "patientId INTEGER NOT NULL, " +
                        "treatmentDate INTEGER NOT NULL, " +
                        "doctorOrder TEXT NOT NULL, " +
                        "drainageVolume INTEGER, " +
                        "createdAt INTEGER NOT NULL, " +
                        "FOREIGN KEY(patientId) REFERENCES patients(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_treatments_patientId ON treatments(patientId)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS medications (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "treatmentId INTEGER NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "dose TEXT NOT NULL, " +
                        "diluent TEXT NOT NULL, " +
                        "diluentVolume INTEGER NOT NULL, " +
                        "FOREIGN KEY(treatmentId) REFERENCES treatments(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_medications_treatmentId ON medications(treatmentId)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS exams (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "patientId INTEGER NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "note TEXT NOT NULL, " +
                        "imagePath TEXT NOT NULL, " +
                        "examDate INTEGER NOT NULL, " +
                        "createdAt INTEGER NOT NULL, " +
                        "FOREIGN KEY(patientId) REFERENCES patients(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exams_patientId ON exams(patientId)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN doseUnit TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS drainage_records (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "patientId INTEGER NOT NULL, " +
                        "recordDate INTEGER NOT NULL, " +
                        "volume INTEGER NOT NULL, " +
                        "createdAt INTEGER NOT NULL, " +
                        "FOREIGN KEY(patientId) REFERENCES patients(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_drainage_records_patientId ON drainage_records(patientId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_drainage_records_patientId_recordDate ON drainage_records(patientId, recordDate)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS treatment_templates (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "content TEXT NOT NULL, " +
                        "createdAt INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS template_medications (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "templateId INTEGER NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "dose TEXT NOT NULL, " +
                        "doseUnit TEXT NOT NULL, " +
                        "diluent TEXT NOT NULL, " +
                        "diluentVolume INTEGER NOT NULL, " +
                        "FOREIGN KEY(templateId) REFERENCES treatment_templates(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_template_medications_templateId ON template_medications(templateId)")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN orderType TEXT NOT NULL DEFAULT '长期'")
                db.execSQL("ALTER TABLE medications ADD COLUMN usage TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE medications ADD COLUMN stopped INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE medications ADD COLUMN stoppedDate INTEGER")
                db.execSQL("ALTER TABLE patients ADD COLUMN dischargeDate INTEGER")
                db.execSQL("ALTER TABLE template_medications ADD COLUMN orderType TEXT NOT NULL DEFAULT '长期'")
                db.execSQL("ALTER TABLE template_medications ADD COLUMN usage TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS infusion_groups (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "treatmentId INTEGER NOT NULL, " +
                        "diluent TEXT NOT NULL, " +
                        "diluentVolume INTEGER NOT NULL, " +
                        "orderType TEXT NOT NULL, " +
                        "usage TEXT NOT NULL, " +
                        "stopped INTEGER NOT NULL, " +
                        "stoppedDate INTEGER, " +
                        "createdAt INTEGER NOT NULL, " +
                        "FOREIGN KEY(treatmentId) REFERENCES treatments(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_infusion_groups_treatmentId ON infusion_groups(treatmentId)")
                db.execSQL("ALTER TABLE medications ADD COLUMN groupId INTEGER")
                db.execSQL("ALTER TABLE patients ADD COLUMN surgeryStartTime INTEGER")
                db.execSQL("ALTER TABLE patients ADD COLUMN surgeryEndTime INTEGER")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE patients ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE patients ADD COLUMN deletedAt INTEGER")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ward_round.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9).build().also { INSTANCE = it }
            }
        }
    }
}