package net.maidsafe.sample.model;

import android.os.Parcel;
import android.os.Parcelable;

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

    private String description;
    private Boolean isComplete;
    private Date date;
    private long version;

    public Task(String description, Date date) {
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
            e.printStackTrace();
        }
        return stream;
    }

    public static Task toTask(byte[] stream) {
        Task task = null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(stream);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            task = (Task) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return task;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getComplete() {
        return isComplete;
    }

    public void setComplete(Boolean complete) {
        isComplete = complete;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(description);
        parcel.writeValue(isComplete);
        parcel.writeValue(date);
        parcel.writeLong(version);
    }

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        public Task createFromParcel(Parcel parcel) {
            return new Task(parcel);
        }
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    public Task(Parcel parcel){
        description = parcel.readString();
        isComplete = (Boolean) parcel.readValue(Boolean.class.getClassLoader());
        date = (Date)parcel.readValue(Date.class.getClassLoader());
        version = parcel.readLong();
    }

}
