/*
 * Copyright (c) 2015, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dhis2.android.dashboard.api.network.managers;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.dhis2.android.dashboard.api.models.DashboardElement;
import org.dhis2.android.dashboard.api.network.converters.DashboardConverter;
import org.dhis2.android.dashboard.api.network.converters.DashboardElementConverter;
import org.dhis2.android.dashboard.api.network.converters.DashboardItemConverter;
import org.dhis2.android.dashboard.api.network.converters.IJsonConverter;
import org.dhis2.android.dashboard.api.network.converters.UserAccountConverter;
import org.dhis2.android.dashboard.api.models.Dashboard;
import org.dhis2.android.dashboard.api.models.DashboardItem;
import org.dhis2.android.dashboard.api.models.UserAccount;

import java.util.List;

import static org.dhis2.android.dashboard.api.utils.Preconditions.isNull;

public class JsonManager implements IJsonManager {
    private final ObjectMapper mMapper;

    public JsonManager(ObjectMapper mapper) {
        mMapper = isNull(mapper, "ObjectMapper must not be null");
    }

    @Override
    public IJsonConverter<UserAccount, UserAccount> getUserAccountConverter() {
        return new UserAccountConverter(mMapper);
    }

    @Override
    public IJsonConverter<List<Dashboard>, List<Dashboard>> getDashboardConverter() {
        return new DashboardConverter(mMapper);
    }

    @Override
    public IJsonConverter<List<DashboardItem>, List<DashboardItem>> getDashboardItemConverter() {
        return new DashboardItemConverter(mMapper);
    }

    @Override
    public IJsonConverter<List<DashboardElement>, List<DashboardElement>> getDashboardElementConverter(String type) {
        return new DashboardElementConverter(mMapper, type);
    }
}
