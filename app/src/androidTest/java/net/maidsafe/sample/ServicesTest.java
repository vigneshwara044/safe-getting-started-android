package net.maidsafe.sample;

import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.system.Os;
import android.util.Log;

import net.maidsafe.api.Client;
import net.maidsafe.api.model.App;
import net.maidsafe.safe_app.MDataInfo;
import net.maidsafe.sample.model.Task;
import net.maidsafe.sample.viewmodel.AuthService;
import net.maidsafe.sample.viewmodel.Services;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Date;
import java.util.List;

public class ServicesTest {

    private static String APP_ID = "net.maidsafe.sample.test";
    private static String APP_NAME = "SAFE Todo Test";
    private static String APP_VENDOR = "Maidsafe.net";
    private static String APP_VERSION = "0.1.0";
    private static String AUTH_URL_PREFIX = "safe-auth://";

    @BeforeClass
    public static void mockVaultSetup() throws Exception{
        String mockPath = InstrumentationRegistry.getTargetContext().getExternalCacheDir().getPath();
        File file = new File(mockPath);
        if(file.delete()){
            Log.d("STAGE","MockVault deleted");
        }
        file.mkdir();
        Os.setenv("SAFE_MOCK_VAULT_PATH", mockPath, true);
    }

    @Test
    public void authURITest() throws Exception {
        App app = new App(APP_ID, APP_NAME, APP_VENDOR, APP_VERSION);
        String authUrl = Services.authenticateApplication(app);
        Assert.assertNotNull(authUrl);
        Assert.assertTrue(authUrl.contains(AUTH_URL_PREFIX));
    }

    @Test
    public void authResponseTest() throws Exception {
        App app = new App(APP_ID, APP_NAME, APP_VENDOR, APP_VERSION);
        String authUrl = Services.authenticateApplication(app);
        Uri authResponse = AuthService.mockAuthenticate(authUrl);
        Assert.assertTrue(authResponse.getScheme().equals(APP_ID));

        Client c = Services.handleUriActivation(authResponse, app);
        Assert.assertNotNull(c);
    }

    @Test
    public void mutableDataTest() throws Exception {
        App app = new App(APP_ID, APP_NAME, APP_VENDOR, APP_VERSION);
        String authUrl = Services.authenticateApplication(app);
        Uri authResponse = AuthService.mockAuthenticate(authUrl);
        {
            Client client = Services.handleUriActivation(authResponse, app);
            // Prepare mutable data for the first time
            MDataInfo mDataInfo = Services.prepareMutableData(client, app);
            int size = 0;
            // add data
            Services.addTask(mDataInfo, client, size, new Task(randomAlphaNumeric(10), new Date()));
            size = Services.listEntries(client, mDataInfo).size();
            Services.addTask(mDataInfo, client, size, new Task(randomAlphaNumeric(10), new Date()));
            List<Task> taskList = Services.listEntries(client, mDataInfo);
            Assert.assertEquals(2, taskList.size());
            // update data
            Task t = taskList.get(0);
            t.setComplete(!t.getComplete());
            t.setVersion(t.getVersion() + 1);
            Services.updateTask(mDataInfo, client, t);
            taskList = Services.listEntries(client, mDataInfo);
            Assert.assertEquals(t.getVersion(), taskList.get(0).getVersion());
            // delete data
            Services.deleteTask(mDataInfo, client, t);
            taskList = Services.listEntries(client, mDataInfo);
            Assert.assertEquals(1, taskList.size());
        }
        // fetching existing data
        Client client = Services.handleUriActivation(authResponse, app);
        MDataInfo mDataInfo = Services.prepareMutableData(client, app);
        List<Task> taskList = Services.listEntries(client, mDataInfo);
        Assert.assertEquals(1, taskList.size());
    }

    public static String randomAlphaNumeric(int count) {
        final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

}
