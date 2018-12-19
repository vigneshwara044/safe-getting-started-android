package net.maidsafe.sample.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable, Parcelable {

    // Serial version ID
    private static final long serialVersionUID = 2645416516514354354L;

    private String title;
    private String description;
    private Boolean isComplete;
    private final Date date;
    private long version;

    public Task(final String title, final String description, final Date date) {
        this.title = title;
        this.date = date;
        this.description = description;
        this.isComplete = false;
        this.version = 0;
    }

    public byte[] toStream() {
        byte[] stream = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(this);
            stream = baos.toByteArray();
        } catch (IOException e) {
            Log.e("ERROR:", e.getMessage());
        }
        return stream;
    }

    public static Task toTask(final byte[] stream) {
        Task task = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(stream);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            task = (Task) ois.readObject();
        } catch (IOException e) {
            Log.e("ERROR:", e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERROR:", e.getMessage());
        }
        return task;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(final long version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Boolean getComplete() {
        return isComplete;
    }

    public void setComplete(final Boolean complete) {
        isComplete = complete;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int i) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeValue(isComplete);
        parcel.writeValue(date);
        parcel.writeLong(version);
    }

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        public Task createFromParcel(final Parcel parcel) {
            return new Task(parcel);
        }
        public Task[] newArray(final int size) {
            return new Task[size];
        }
    };

    public Task(final Parcel parcel) {
        title = parcel.readString();
        description = parcel.readString();
        isComplete = (Boolean) parcel.readValue(Boolean.class.getClassLoader());
        date = (Date) parcel.readValue(Date.class.getClassLoader());
        version = parcel.readLong();
    }

}
