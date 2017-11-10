package de.thm.thmflashcards.persistance;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Yannick Bals on 10.11.2017.
 */

@Entity(tableName = "categories")
public class Category {

    @PrimaryKey( autoGenerate = true)
    private int id;

    //Per default the field name is used as the column name
    @ColumnInfo(name = "category_name")
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


