package net.maidsafe.sample.viewmodel;

import android.net.Uri;
import android.util.Log;

import net.maidsafe.api.Client;
import net.maidsafe.api.Session;
import net.maidsafe.api.model.App;
import net.maidsafe.api.model.AuthResponse;
import net.maidsafe.api.model.DecodeResult;
import net.maidsafe.api.model.NativeHandle;
import net.maidsafe.api.model.Request;
import net.maidsafe.safe_app.AppExchangeInfo;
import net.maidsafe.safe_app.AuthReq;
import net.maidsafe.safe_app.ContainerPermissions;
import net.maidsafe.safe_app.MDataInfo;
import net.maidsafe.safe_app.MDataValue;
import net.maidsafe.safe_app.PermissionSet;
import net.maidsafe.sample.model.Task;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.util.Log.i;

public class Services {

    private static final String AUTH_URL_PREFIX = "safe-auth://";
    private static final String APP_CONTAINER_NAME = "apps/";
    private static final String NO_SUCH_DATA_ERROR_CODE = "-106";


    public static void addTask(MDataInfo mDataInfo, Client client, int size, Task task) throws Exception {
        byte[] value = Task.toStream(task);
        byte[] key = getByteArray(task.getDate().hashCode());
        if (size == 0) {
            // For first entry, PUT MData into the network along with permissions
            PermissionSet permissionSet = new PermissionSet();
            permissionSet.setInsert(true);
            permissionSet.setUpdate(true);
            permissionSet.setRead(true);
            permissionSet.setDelete(true);
            NativeHandle permissionHandle = client.mDataPermission.newPermissionHandle().get();
            client.mDataPermission.insert(permissionHandle, client.crypto.getAppPublicSignKey().get(),
                    permissionSet).get();

            NativeHandle entriesHandle = client.mDataEntries.newEntriesHandle().get();
            client.mDataEntries.insert(entriesHandle, key, value).get();
            client.mData.put(mDataInfo, permissionHandle, entriesHandle).get();
            Log.i("STAGE:", "MData PUT is complete");
        } else {
            // Mutate exisiting MData with new entry
            NativeHandle actionHandle = client.mDataEntryAction.newEntryAction().get();
            client.mDataEntryAction.insert(actionHandle, key, value).get();
            client.mData.mutateEntries(mDataInfo, actionHandle).get();
            Log.i("STAGE:", "Data Mutated");
        }
    }

    public static void deleteTask(MDataInfo mDataInfo, Client client, Task t) throws Exception {
        NativeHandle actionHandle = client.mDataEntryAction.newEntryAction().get();
        client.mDataEntryAction.delete(actionHandle, getByteArray(t.getDate().hashCode()), t.getVersion() + 1).get();
        client.mData.mutateEntries(mDataInfo, actionHandle).get();
    }

    public static void updateTask(MDataInfo mDataInfo, Client client, Task task) throws Exception {
        Thread.sleep(2000);
        NativeHandle actionHandle = client.mDataEntryAction.newEntryAction().get();
        client.mDataEntryAction.update(actionHandle, getByteArray(task.getDate().hashCode()), Task.toStream(task), task.getVersion());
        client.mData.mutateEntries(mDataInfo, actionHandle).get();
        Log.i("STAGE", task.getDescription() + "\n marked as " + (task.getComplete() ? "complete" : "incomplete"));
    }

    public static String authenticateApplication(App app) throws Exception {
        i("STAGE:", "Loading native libraries..");
        Client.load();

        AppExchangeInfo appExchangeInfo = new AppExchangeInfo(app.getId(), "",
                app.getName(), app.getVendor());
        ContainerPermissions[] permissions = new ContainerPermissions[0];
        AuthReq authReq = new AuthReq(appExchangeInfo, true,
                permissions, permissions.length, 0);
        Request request = Client.encodeAuthReq(authReq).get();

        String openUri = AUTH_URL_PREFIX + app.getId() + '/' + request.getUri();

        return openUri;
    }

    public static Client handleUriActivation(Uri data, App app) throws Exception {
        String uri = data.toString().replaceAll(".*\\/+", "");
        DecodeResult decodeResult = Session.decodeIpcMessage(uri).get();
        if (decodeResult.getClass().equals(AuthResponse.class)) {
            AuthResponse response = (AuthResponse) decodeResult;
            Client c = (Client) Session.connect(app.getId(), response.getAuthGranted()).get();
            i("STAGE:", "Connected to the SAFE Network");
            return c;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static MDataInfo prepareMutableData(Client client, App app) throws Exception {
        // Get MData of the App Containter
        MDataInfo containerMdata = client.getContainerMDataInfo( APP_CONTAINER_NAME + app.getId()).get();
        final String dataKey = "myTodoList";
        byte[] containerKey = dataKey.getBytes();
        try {
            // Check for existing list MData
            MDataValue mDataValue = client.mData.getValue(containerMdata, containerKey).get();
            if (mDataValue.getContentLen() <= 0) {
                // Create new MData for list items
                MDataInfo newMdInfo = createMdata(client, containerMdata, containerKey);
                return newMdInfo;
            } else {
                // Deserialize existing MDataInfo and fetch the list entries
                MDataInfo oldMdInfo = client.mData.deserialise(mDataValue.getContent()).get();
                return oldMdInfo;
            }
        } catch (Exception e) {
            if (e.getMessage().contains(NO_SUCH_DATA_ERROR_CODE)) {
                // Handle requested data not found (from line 180) for non-existent MData
                MDataInfo newMdInfo = createMdata(client, containerMdata, containerKey);
                return newMdInfo;
            } else {
                throw e;
            }
        }
    }

    public static List<Task> listEntries(Client client, MDataInfo mdInfo) throws Exception {
        List<Task> list = new ArrayList<>();
        NativeHandle entryHandle = client.mData.getEntriesHandle(mdInfo).get();
        client.mDataEntries.listEntries(entryHandle).get().forEach(entry -> {
            if (entry.getValue().getContentLen() != 0) {
                list.add(Task.toTask(entry.getValue().getContent()));
            }
        });
        Log.i("DATA:", "List size: " + list.size());
        return list;
    }



    private static MDataInfo createMdata(Client client, MDataInfo containerMdata, byte[] containerKey) throws Exception {
        // create new mutable data
        long tagType = 16290;
        MDataInfo mdInfo = client.mData.getRandomPrivateMData(tagType).get();

        // store mdInfo in App container
        byte[] serializedMDInfo = client.mData.serialise(mdInfo).get();
        NativeHandle containerEntry = client.mDataEntryAction.newEntryAction().get();
        client.mDataEntryAction.insert(containerEntry, containerKey, serializedMDInfo).get();
        client.mData.mutateEntries(containerMdata, containerEntry).get();

        return mdInfo;
    }


    private static byte[] getByteArray(int x) {
        return ByteBuffer.allocate(4).putInt(x).array();
    }


}
