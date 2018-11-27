package net.maidsafe.sample.viewmodel;

import android.net.Uri;

public class MockServices {

    public static Uri mockAuthenticate(String uri) throws Exception {
        throw new Exception("Only applicable for mock build");
    }

    public static void simulateDisconnect() {

    }

}
