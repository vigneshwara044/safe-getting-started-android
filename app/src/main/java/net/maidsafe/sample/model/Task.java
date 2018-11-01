package net.maidsafe.sample.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Task implements Serializable {

    // Serial version ID – Just a random number
    private static final long serialVersionUID = 2645416516514354354L;

    String description;
    Boolean isComplete;
    int id;
    long version;

    public Task(String description, int id) {
        this.id = id;
        this.description = description;
        this.isComplete = false;
        this.version = 0;
    }

    public static byte[] toStream(Task task) {
        byte[] stream = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos);) {
            oos.writeObject(task);
            stream = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream;
    }

    public static Task toTask(byte[] stream) {
        Task task = null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(stream);
             ObjectInputStream ois = new ObjectInputStream(bais);) {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

}
