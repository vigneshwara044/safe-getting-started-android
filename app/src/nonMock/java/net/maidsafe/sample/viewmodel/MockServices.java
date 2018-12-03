package net.maidsafe.sample.viewmodel;

import android.net.Uri;

public final class MockServices {

    private MockServices() {

    }

    public static Uri mockAuthenticate(final String uri) throws Exception {
        throw new java.lang.Exception("Only applicable for mock build");
    }

    public static void simulateDisconnect() {
        // Only applicable for the mock flavour
    }

}
