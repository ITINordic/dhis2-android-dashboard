/*
 * Copyright (c) 2015, University of Oslo
 *
 * All rights reserved.
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

package org.hisp.dhis.android.dashboard.ui.fragments;

import static android.text.TextUtils.isEmpty;

import static org.hisp.dhis.android.dashboard.api.models.DashboardItemContent.TYPE_REPORT_TABLE;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.hisp.dhis.android.dashboard.DhisApplication;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.api.controllers.DhisController;
import org.hisp.dhis.android.dashboard.api.job.Job;
import org.hisp.dhis.android.dashboard.api.job.JobExecutor;
import org.hisp.dhis.android.dashboard.api.models.AttributeDimension;
import org.hisp.dhis.android.dashboard.api.models.DataElementDimension;
import org.hisp.dhis.android.dashboard.api.models.EventReport;
import org.hisp.dhis.android.dashboard.api.models.UIDObject;
import org.hisp.dhis.android.dashboard.api.models.meta.ResponseHolder;
import org.hisp.dhis.android.dashboard.api.network.APIException;
import org.hisp.dhis.android.dashboard.api.network.DhisApi;
import org.hisp.dhis.android.dashboard.api.network.RepoManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.mime.TypedInput;

public class WebViewFragment extends BaseFragment {
    private static final String DASHBOARD_ELEMENT_ID = "arg:dashboardElementId";
    private Context mContext;
    private static final String DASHBOARD_TYPE = "dashboardType";

    @Bind(R.id.web_view_content)
    WebView mWebView;

    @Bind(R.id.container_layout_progress_bar)
    View mProgressBarContainer;

    public static WebViewFragment newInstance(String id, String dashboardType) {
        Bundle args = new Bundle();
        args.putString(DASHBOARD_ELEMENT_ID, id);
        args.putString(DASHBOARD_TYPE, dashboardType);

        WebViewFragment fragment = new WebViewFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContext = getContext();
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        mWebView.getSettings().setBuiltInZoomControls(true);
        if (getArguments() != null && !isEmpty(getArguments()
                .getString(DASHBOARD_ELEMENT_ID)) && !isEmpty(getArguments()
                .getString(DASHBOARD_TYPE))) {
            if (getArguments()
                    .getString(DASHBOARD_TYPE).equals(TYPE_REPORT_TABLE)) {
                JobExecutor.enqueueJob(new GetReportTableJob(this, getArguments()
                        .getString(DASHBOARD_ELEMENT_ID), mContext));
            } else {
                JobExecutor.enqueueJob(new GetEventReportTableJob(this, getArguments()
                        .getString(DASHBOARD_ELEMENT_ID), mContext));
            }
        }
    }

    public void onDataDownloaded(ResponseHolder<String> data) {
        mProgressBarContainer.setVisibility(View.GONE);

        if (data.getApiException() == null) {
            mWebView.loadData(data.getItem(), "text/html", "UTF-8");
        } else {
            if (isAdded()) {
                ((DhisApplication) (getActivity().getApplication()))
                        .showApiExceptionMessage(data.getApiException());
            }
        }
    }

    static class GetReportTableJob extends Job<ResponseHolder<String>> {
        static final int JOB_ID = 4573452;

        final WeakReference<WebViewFragment> mFragmentRef;
        final String mDashboardElementId;
        Context mContext;

        public GetReportTableJob(WebViewFragment fragment, String dashboardElementId,
                Context context) {
            super(JOB_ID);

            mFragmentRef = new WeakReference<>(fragment);
            mDashboardElementId = dashboardElementId;
            mContext = context;
        }

        static String readInputStream(TypedInput in) {
            StringBuilder builder = new StringBuilder();
            try {
                BufferedReader bufferedStream
                        = new BufferedReader(new InputStreamReader(in.in()));
                try {
                    String line;
                    while ((line = bufferedStream.readLine()) != null) {
                        builder.append(line);
                        builder.append('\n');
                    }
                    return builder.toString();
                } finally {
                    bufferedStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return builder.toString();
        }

        @Override
        public ResponseHolder<String> inBackground() {
            ResponseHolder<String> responseHolder = new ResponseHolder<>();

            try {
                DhisApi dhisApi = RepoManager.createService(DhisController.getInstance()
                                .getServerUrl(),
                        DhisController.getInstance().getUserCredentials(),
                        mContext);
                responseHolder.setItem(
                        readInputStream(dhisApi.getReportTableData(mDashboardElementId).getBody()));
            } catch (APIException exception) {
                responseHolder.setApiException(exception);
            }

            return responseHolder;
        }

        @Override
        public void onFinish(ResponseHolder<String> result) {
            if (mFragmentRef.get() != null) {
                mFragmentRef.get().onDataDownloaded(result);
            }
        }
    }

    static class GetEventReportTableJob extends GetReportTableJob {

        Context mContext;

        public GetEventReportTableJob(WebViewFragment fragment, String dashboardElementId,
                Context context) {
            super(fragment, dashboardElementId, context);

            mContext = context;
        }

        @Override
        public ResponseHolder<String> inBackground() {
            ResponseHolder<String> responseHolder = new ResponseHolder<>();
            EventReport eventReport;

            try {
                DhisApi dhisApi = RepoManager.createService(
                        DhisController.getInstance().getServerUrl(),
                        DhisController.getInstance().getUserCredentials(), mContext);
                eventReport = dhisApi.getEventReport(mDashboardElementId);
                responseHolder.setItem(readInputStream(
                        dhisApi.getEventReportTableData(eventReport.getProgram().getuId(),
                                eventReport.getProgramStage().getuId(),
                                getDimensions(eventReport),
                                eventReport.getOutputType(),
                                eventReport.getAggregationType(),
                                eventReport.getDataElementValueDimension() != null
                                        ? eventReport.getDataElementValueDimension().getuId()
                                        : null,
                                eventReport.getDataTypeString(),
                                getFilters(eventReport))
                                .getBody()));
            } catch (APIException exception) {
                responseHolder.setApiException(exception);
            }

            return responseHolder;
        }

        private List<String> getDimensions(EventReport eventReport) {
            List<String> dimensions = new ArrayList<>();
            if (!eventReport.isPEInFilters()) {
                dimensions.add(eventReport.getRelativePeriods().getRelativePeriodString());
            }
            if (!eventReport.isOUInFilters()) {
                dimensions.add(eventReport.getOUDimensionFilter());
            }
            for (DataElementDimension dimension : eventReport.getDataElementDimensions()) {
                if (!eventReport.isInFilters(dimension.getDataElement().getuId())) {
                    dimensions.add(eventReport.getDimensionFilter(dimension));
                }
            }
            for (UIDObject column : eventReport.getColumns()) {
                if (eventReport.isValidColumn(column)) {
                    dimensions.add(column.getuId());
                }
            }

            for (AttributeDimension attributeDimension : eventReport.getAttributeDimensions()) {
                dimensions.add(attributeDimension.getAttribute().getuId());
            }
            return dimensions;
        }

        private List<String> getFilters(EventReport eventReport) {
            List<String> filters = new ArrayList<>();
            if (eventReport.isOUInFilters()) {
                filters.add(eventReport.getOUDimensionFilter());
            }
            if (eventReport.isPEInFilters()) {
                filters.add(eventReport.getRelativePeriods().getRelativePeriodString());
            }

            for (DataElementDimension dimension : eventReport.getDataElementDimensions()) {
                if (eventReport.isInFilters(dimension.getDataElement().getuId())) {
                    filters.add(eventReport.getDimensionFilter(dimension));
                }
            }

            return filters;
        }
    }
}