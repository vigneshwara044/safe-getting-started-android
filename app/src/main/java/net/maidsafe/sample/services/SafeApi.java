package net.maidsafe.sample.services;

import android.content.Context;
import android.util.Log;

import net.maidsafe.api.Client;
import net.maidsafe.api.Session;
import net.maidsafe.api.listener.OnDisconnected;
import net.maidsafe.api.model.AuthResponse;
import net.maidsafe.api.model.DecodeResult;
import net.maidsafe.api.model.NativeHandle;
import net.maidsafe.safe_app.MDataEntry;
import net.maidsafe.safe_app.MDataInfo;
import net.maidsafe.safe_app.MDataValue;
import net.maidsafe.safe_app.PermissionSet;
import net.maidsafe.utils.Constants;

import java.util.List;

import static android.util.Log.i;

public class SafeApi {

    private static final SafeApi instance = new SafeApi();
    private static boolean loaded = false;

    private SafeApi() {
    }

    public static SafeApi getInstance(Context context) throws Exception{
        if (loaded) {
            return  instance;
        }
        Client.load(context);
        loaded = true;
        return instance;
    }

    private Session session;
    private String appId;
    private final String listKey = "myTodoLists";

    public void connect(String response, String appId, OnDisconnected onDisconnected) throws Exception {
        this.appId = appId;
        DecodeResult decodeResult = Session.decodeIpcMessage(response).get();
        if (decodeResult.getClass().equals(AuthResponse.class)) {
            AuthResponse authResponse = (AuthResponse) decodeResult;
            session = Client.connect(appId, authResponse.getAuthGranted()).get();
            session.setOnDisconnectListener(onDisconnected);
            i("STAGE:", "Connected to the SAFE Network");
        } else {
            throw new Exception("Could not connect to the SAFE Network");
        }
    }

    public MDataInfo getSectionsFromAppContainer() throws Exception {
        final String APP_CONTAINER_NAME = "apps/";
        final String NO_SUCH_DATA_ERROR_CODE = "-106";
        MDataInfo info;

        MDataInfo appContainerInfo = session.getContainerMDataInfo(APP_CONTAINER_NAME + appId).get();
        byte[] key = listKey.getBytes();
        try {
            MDataValue mDataValue = session.mData.getValue(appContainerInfo, key).get();
            if(mDataValue.getContentLen() <= 0) {
                info = initAppData(appContainerInfo);
            } else {
                info = session.mData.deserialise(mDataValue.getContent()).get();
            }
        } catch (Exception e) {
            if (e.getMessage().contains(NO_SUCH_DATA_ERROR_CODE)) {
                info = initAppData(appContainerInfo);
            } else {
                throw e;
            }
        }
        return info;
    }

    public MDataInfo initAppData(MDataInfo appContainerInfo) throws Exception {
        MDataInfo mdInfo = newMutableData(16290);
        saveMdInfo(appContainerInfo, mdInfo);
        return  mdInfo;
    }

    public List<MDataEntry> getEntries(MDataInfo mDataInfo) throws Exception{
        NativeHandle entryHandle = session.mData.getEntriesHandle(mDataInfo).get();
        List<MDataEntry> entries = session.mDataEntries.listEntries(entryHandle).get();
        return entries;
    }

    public void insertPermissions(MDataInfo mDataInfo) throws Exception {
        PermissionSet permissionSet = new PermissionSet();
        permissionSet.setInsert(true);
        permissionSet.setUpdate(true);
        permissionSet.setRead(true);
        permissionSet.setDelete(true);
        NativeHandle permissionHandle = session.mDataPermission.newPermissionHandle().get();
        session.mDataPermission.insert(permissionHandle, session.crypto.getAppPublicSignKey().get(),
                permissionSet).get();

        session.mData.put(mDataInfo, permissionHandle, Constants.ANYONE_HANDLE).get();
        Log.i("STAGE:", "MData PUT is complete");
    }

    public void addEntry(byte[] key, byte[] value, MDataInfo mDataInfo) throws Exception{
        NativeHandle actionHandle = session.mDataEntryAction.newEntryAction().get();
        session.mDataEntryAction.insert(actionHandle, key, value).get();
        session.mData.mutateEntries(mDataInfo, actionHandle).get();
    }

    public void deleteEntry(byte[] key, long version, MDataInfo mDataInfo) throws Exception {
        NativeHandle actionHandle = session.mDataEntryAction.newEntryAction().get();
        session.mDataEntryAction.delete(actionHandle, key, version).get();
        session.mData.mutateEntries(mDataInfo, actionHandle).get();
    }

    public void updateEntry(byte[] key, byte[] newValue, long version, MDataInfo mDataInfo) throws Exception {
        NativeHandle actionHandle = session.mDataEntryAction.newEntryAction().get();
        session.mDataEntryAction.update(actionHandle, key, newValue, version).get();
        session.mData.mutateEntries(mDataInfo, actionHandle).get();
    }

    public long getEntriesLength(MDataInfo mDataInfo) throws Exception {
        final String DATA_NOT_FOUND_EXCEPTION = "-103";
        try {
            NativeHandle entriesHandle = session.mData.getEntriesHandle(mDataInfo).get();
            return session.mDataEntries.length(entriesHandle).get();
        } catch (Exception e) {
            if(e.getMessage().contains(DATA_NOT_FOUND_EXCEPTION)) {
                return 0;
            } else {
                throw e;
            }
        }
    }

    public MDataInfo newMutableData(long tagType) throws Exception{
        MDataInfo listInfo = session.mData.getRandomPrivateMData(tagType).get();
        return listInfo;
    }

    private void saveMdInfo(MDataInfo appContainerInfo, MDataInfo mDataInfo) throws Exception {
        byte[] serializedMdInfo = session.mData.serialise(mDataInfo).get();
        NativeHandle containerEntry = session.mDataEntryAction.newEntryAction().get();
        session.mDataEntryAction.insert(containerEntry, listKey.getBytes(), serializedMdInfo).get();
        session.mData.mutateEntries(appContainerInfo, containerEntry);
    }

    public byte[] serializeMdInfo(MDataInfo mDataInfo) throws Exception {
        return session.mData.serialise(mDataInfo).get();
    }

    public MDataInfo deserializeMdInfo(byte[] serializedInfo) throws Exception {
        return session.mData.deserialise(serializedInfo).get();
    }

    public void reconnect() throws Exception {
        session.reconnect().get();
    }

    public void disconnect() throws Exception {
        session.testSimulateDisconnect().get();
    }
}
