package app;

import android.app.Application;

public class App extends Application {

    public static volatile App app = null;

    @Override
    public void onCreate() {
        super.onCreate();
        App.app = this;
    }

}
