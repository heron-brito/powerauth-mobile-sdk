/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getlime.security.powerauth.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.getlime.security.powerauth.core.TokenCalculator;
import io.getlime.security.powerauth.exception.PowerAuthErrorCodes;
import io.getlime.security.powerauth.sdk.impl.PowerAuthAuthorizationHttpHeader;
import io.getlime.security.powerauth.sdk.impl.PowerAuthPrivateTokenData;

/**
 * The <code>PowerAuthToken</code> class generates a token based authorization headers.
 * You have to use {@codePowerAuthTokenStore} to get an instance of this class.
 * <p>
 * The whole interface is thread safe.
 */
public class PowerAuthToken {

    /**
     * Reference to store which created this instance of token.
     */
    public final PowerAuthTokenStore tokenStore;
    /**
     * Token's private data
     */
    private final PowerAuthPrivateTokenData tokenData;

    public PowerAuthToken(@NonNull PowerAuthTokenStore store, @NonNull PowerAuthPrivateTokenData tokenData) {
        this.tokenStore = store;
        this.tokenData = tokenData;
    }

    /**
     * Contains true if token has valid data and can be used for generating headers.
     */
    public boolean isValid() {
        return tokenData != null && tokenStore != null && tokenData.hasValidData();
    }


    /**
     * Compares this token to the specified object.
     * @param anObject object to compare
     * @return true if objects are equal.
     */
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof PowerAuthToken) {
            PowerAuthToken anotherToken = (PowerAuthToken) anObject;
            if (anotherToken.isValid() && this.isValid()) {
                return tokenStore == anotherToken.tokenStore &&
                        tokenData.equals(anotherToken.tokenData);
            }
        }
        return false;
    }

    /**
     * @return symbolic name of token or null if token contains an invalid data.
     */
    protected @Nullable String getTokenName() {
        return tokenData != null ? tokenData.name : null;
    }


    /**
     * Generates a new HTTP header for token based authorization.
     *
     * @return calculated HTTP authorization header. The header object contains an information
     *         about error, so check its <code>isValid()</code> method afterwards.
     */
    public @NonNull PowerAuthAuthorizationHttpHeader generateHeader() {
        int errorCode;
        if (this.isValid()) {
            if (tokenStore.canRequestForAccessToken()) {
                String headerValue = TokenCalculator.calculateTokenValue(tokenData);
                if (headerValue != null) {
                    return PowerAuthAuthorizationHttpHeader.createTokenHeader(headerValue);
                } else {
                    errorCode = PowerAuthErrorCodes.PA2ErrorCodeSignatureError;
                }
            } else {
                errorCode = PowerAuthErrorCodes.PA2ErrorCodeMissingActivation;
            }
        } else {
            errorCode = PowerAuthErrorCodes.PA2ErrorCodeInvalidToken;
        }
        // In case of error, create an object with error.
        return PowerAuthAuthorizationHttpHeader.createError(errorCode);
    }
}