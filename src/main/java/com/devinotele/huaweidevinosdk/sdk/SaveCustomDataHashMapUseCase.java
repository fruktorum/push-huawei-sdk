package com.devinotele.huaweidevinosdk.sdk;

public class SaveCustomDataHashMapUseCase extends BaseUC {

    private DevinoLogsCallback logsCallback;

    SaveCustomDataHashMapUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    void run(String customData) {
        sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_CUSTOM_DATA, customData);
        logsCallback.onMessageLogged("Save CustomData = " + customData);
    }
}