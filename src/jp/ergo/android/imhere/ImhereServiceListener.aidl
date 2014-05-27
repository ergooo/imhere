package jp.ergo.android.imhere;


interface ImhereServiceListener{
    void onLocationProviderNotFound();
    void onAccountNotValid();
    void onGeocoderNotWorking();
    void onMailSendingFailed(String message);
    void onComplete();
}