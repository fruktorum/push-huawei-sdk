package com.devinotele.huaweidevinosdk.sdk;

class HelpersPackage {

    private SharedPrefsHelper sharedPrefsHelper;
    private DevinoNetworkRepository networkRepository;
    private NotificationsHelper notificationsHelper;
    private DevinoLocationHelper devinoLocationHelper;
    private SaveTokenUseCase saveTokenUseCase;

    SharedPrefsHelper getSharedPrefsHelper() {
        return sharedPrefsHelper;
    }

    void setSharedPrefsHelper(SharedPrefsHelper sharedPrefsHelper) {
        this.sharedPrefsHelper = sharedPrefsHelper;
    }

    void setNetworkRepository(DevinoNetworkRepository networkRepository) {
        this.networkRepository = networkRepository;
    }

    void setNotificationsHelper(NotificationsHelper notificationsHelper) {
        this.notificationsHelper = notificationsHelper;
    }

    void setDevinoLocationHelper(DevinoLocationHelper devinoLocationHelper) {
        this.devinoLocationHelper = devinoLocationHelper;
    }

    void setSaveTokenUseCaseHelper(SaveTokenUseCase saveTokenUseCase) {
        this.saveTokenUseCase = saveTokenUseCase;
    }

    DevinoNetworkRepository getNetworkRepository() {
        return networkRepository;
    }

    NotificationsHelper getNotificationsHelper() {
        return notificationsHelper;
    }

    DevinoLocationHelper getDevinoLocationHelper() {
        return devinoLocationHelper;
    }

    SaveTokenUseCase getSaveTokenUseCaseHelper() {
        return saveTokenUseCase;
    }
}