package com.example.weathernow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {



    @PrimaryKey(autoGenerate = true)
    private int id;

    private String content;
    private long timestamp;

    // Constructor
    public NotificationEntity(String content, long timestamp) {
        this.content = content;
        this.timestamp = timestamp;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }


    // Getter for content
    public String getContent() {
        return content;
    }

    // Getter for timestamp
    public long getTimestamp() {
        return timestamp;
    }

    // Setter for content (if needed)
    public void setContent(String content) {
        this.content = content;
    }

    // Setter for timestamp (if needed)
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
