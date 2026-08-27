package com.google.android.gms.common.internal;

import android.os.Bundle;

import com.google.android.gms.common.internal.IGmsCallbacks;
import com.google.android.gms.common.internal.GetServiceRequest;
import com.google.android.gms.common.internal.ValidateAccountRequest;

interface IGmsServiceBroker {
    void getService(IGmsCallbacks callback, in GetServiceRequest request) = 45;
    void validateAccount(IGmsCallbacks callback, in ValidateAccountRequest request) = 46;
}
