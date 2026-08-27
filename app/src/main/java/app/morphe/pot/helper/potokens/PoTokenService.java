/*
 * Original Work Copyright 2013-2015 µg Project Team.
 * Modifications and additions Copyright (c) 2026 Morphe.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.morphe.pot.helper.potokens;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.common.Feature;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.internal.IStatusCallback;
import com.google.android.gms.common.internal.ConnectionInfo;
import com.google.android.gms.common.internal.GetServiceRequest;
import com.google.android.gms.common.internal.IGmsCallbacks;
import com.google.android.gms.common.internal.IGmsServiceBroker;
import com.google.android.gms.common.internal.ValidateAccountRequest;
import com.google.android.gms.potokens.PoToken;
import com.google.android.gms.potokens.internal.IPoTokensService;
import com.google.android.gms.potokens.internal.ITokenCallbacks;

public class PoTokenService extends Service {
    private static final String TAG = "morphe: PoTokensService";
    private final Feature[] feature = { new Feature("PO_TOKENS", 1) };
    private final ConnectionInfo connectionInfo = new ConnectionInfo(feature);
    private final IGmsServiceBroker broker;

    public PoTokenService() {
        broker = new IGmsServiceBroker.Stub() {
            @Override
            public void getService(IGmsCallbacks callback, GetServiceRequest request) throws RemoteException {
                int serviceId = request.serviceId;
                if (serviceId == 285 || serviceId == -1) {
                    handleServiceRequest(callback, request);
                } else {
                    Log.d(TAG, "Service not supported: " + request);
                    throw new IllegalArgumentException("Service not supported: " + request.serviceId);
                }
            }

            public void handleServiceRequest(IGmsCallbacks callback, GetServiceRequest request) throws RemoteException {
                Log.d(TAG, "bound by: " + request);

                IPoTokensService.Stub serviceImpl = new IPoTokensService.Stub() {

                    @Override
                    public void responseStatus(IStatusCallback callback, int code) throws RemoteException {
                        callback.onResult(Status.SUCCESS);
                    }

                    @Override
                    public void responseStatusToken(ITokenCallbacks callback, int code, byte[] bytes) throws RemoteException {
                        callback.responseToken(Status.SUCCESS, new PoToken(PoTokenServiceImpl.buildPoTokenResult(bytes)));
                    }
                };

                callback.onPostInitCompleteWithConnectionInfo(0, serviceImpl.asBinder(), connectionInfo);
            }

            @Override
            public void validateAccount(IGmsCallbacks callback, ValidateAccountRequest request) {
                throw new IllegalArgumentException("ValidateAccountRequest not supported");
            }

            @Override
            public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                if (super.onTransact(code, data, reply, flags)) return true;
                Log.d(TAG, "onTransact [unknown]: " + code + ", " + data + ", " + flags);
                return false;
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: " + intent);
        return broker.asBinder();
    }
}
