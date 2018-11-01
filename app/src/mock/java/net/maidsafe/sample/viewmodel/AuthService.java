package net.maidsafe.sample.viewmodel;

import android.net.Uri;
import android.system.Os;

import net.maidsafe.api.Authenticator;
import net.maidsafe.api.model.AuthIpcRequest;
import net.maidsafe.api.model.IpcRequest;

public class AuthService {

    private static final String LOCATOR = "locator";
    private static final String PASSWORD = "password";
    private static final String INVITE = "invite";
    private static final String ACCOUNT_EXISTS_CODE = "-102";

    public static Uri mockAuthenticate(String uri) throws Exception {
        Authenticator authenticator = null;
        try {
            authenticator = Authenticator.createAccount(LOCATOR, PASSWORD, INVITE).get();
        } catch (Exception e) {
            if (e.getMessage().contains(ACCOUNT_EXISTS_CODE)) {
                authenticator = Authenticator.login(LOCATOR, PASSWORD).get();
            }
        }

        if (authenticator == null) {
            throw new Exception("Not logged in!" + "\nMOCK VAULT PATH: " + Os.getenv("SAFE_MOCK_VAULT_PATH"));
        }
        String data = uri.replaceAll(".*\\/+", "");
        IpcRequest request = authenticator.decodeIpcMessage(data).get();
        AuthIpcRequest authIpcRequest = (AuthIpcRequest) request;
        String response = authenticator.encodeAuthResponse(authIpcRequest, true).get();
        String appId = authIpcRequest.getAuthReq().getApp().getId();

        return Uri.parse(appId + "://" + response);
    }

}
