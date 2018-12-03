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

public class TodoList implements Serializable, Parcelable {

    // Serial version ID – Just a random number
    private static final long serialVersionUID = 2458779654123571L;

    private String listTitle;
    private Date date;
    private byte[] content; // serialized MDataInfo
    private long version;


    public TodoList(final String listTitle, final Date date, final byte[] content) {
        this.listTitle = listTitle;
        this.date = date;
        this.content = content;
        this.version = 0;
    }

    public String getListTitle() {
        return listTitle;
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

    public static TodoList getListInfo(final byte[] stream) {
        TodoList todoList = null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(stream);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            todoList = (TodoList) ois.readObject();
        } catch (IOException e) {
            Log.e("ERROR:", e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERROR:", e.getMessage());
        }
        return todoList;
    }

    public void setListTitle(final String listTitle) {
        this.listTitle = listTitle;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(final byte[] content) {
        this.content = content;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(final long version) {
        this.version = version;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int i) {
        parcel.writeString(listTitle);
        parcel.writeValue(date);
        parcel.writeByteArray(content);
        parcel.writeLong(version);
    }

    public static final Parcelable.Creator<TodoList> CREATOR = new Parcelable.Creator<TodoList>() {
        public TodoList createFromParcel(final Parcel parcel) {
            return new TodoList(parcel);
        }
        public TodoList[] newArray(final int size) {
            return new TodoList[size];
        }
    };

    public TodoList(final Parcel parcel) {
        listTitle = parcel.readString();
        date = (Date) parcel.readValue(Date.class.getClassLoader());
        parcel.readByteArray(content);
        version = parcel.readLong();
    }
}
