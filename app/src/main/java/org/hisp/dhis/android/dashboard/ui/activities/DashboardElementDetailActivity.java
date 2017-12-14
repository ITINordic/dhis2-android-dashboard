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

package org.hisp.dhis.android.dashboard.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.api.controllers.DhisController;
import org.hisp.dhis.android.dashboard.api.models.DashboardElement;
import org.hisp.dhis.android.dashboard.api.models.DashboardElement$Table;
import org.hisp.dhis.android.dashboard.api.models.DashboardItemContent;
import org.hisp.dhis.android.dashboard.api.models.Interpretation;
import org.hisp.dhis.android.dashboard.api.models.InterpretationElement;
import org.hisp.dhis.android.dashboard.api.models.InterpretationElement$Table;
import org.hisp.dhis.android.dashboard.ui.fragments.ImageViewFragment;
import org.hisp.dhis.android.dashboard.ui.fragments.MapImageViewFragment;
import org.hisp.dhis.android.dashboard.ui.fragments.WebViewFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DashboardElementDetailActivity extends BaseActivity {
    private static final String DASHBOARD_ELEMENT_ID = "arg:dashboardElementId";
    private static final String INTERPRETATION_ELEMENT_ID = "arg:interpretationElementId";


    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    public static Intent newIntentForDashboardElement(Activity activity, long dashboardElementId) {
        Intent intent = new Intent(activity, DashboardElementDetailActivity.class);
        intent.putExtra(DASHBOARD_ELEMENT_ID, dashboardElementId);
        return intent;
    }

    public static Intent newIntentForInterpretationElement(Activity activity, long interpretationElementId) {
        Intent intent = new Intent(activity, DashboardElementDetailActivity.class);
        intent.putExtra(INTERPRETATION_ELEMENT_ID, interpretationElementId);
        return intent;
    }

    private long getDashboardElementId() {
        return getIntent().getLongExtra(DASHBOARD_ELEMENT_ID, -1);
    }

    private long getInterpretationElementId() {
        return getIntent().getLongExtra(INTERPRETATION_ELEMENT_ID, -1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_element_detail);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        long dashboardElementId = getDashboardElementId();
        long interpretationElementId = getInterpretationElementId();

        if (dashboardElementId > 0) {
            DashboardElement element = new Select()
                    .from(DashboardElement.class)
                    .where(Condition.column(DashboardElement$Table.ID)
                            .is(getDashboardElementId()))
                    .querySingle();
            handleDashboardElement(element, getApplicationContext());
        }

        if (interpretationElementId > 0) {
            InterpretationElement element = new Select()
                    .from(InterpretationElement.class)
                    .where(Condition.column(InterpretationElement$Table
                            .ID).is(interpretationElementId))
                    .querySingle();
            handleInterpretationElement(element, getApplicationContext());
        }
    }

    private void handleDashboardElement(DashboardElement element, Context context) {

        if (element == null || element.getDashboardItem() == null) {
            return;
        }

        mToolbar.setTitle(element.getDisplayName());
        switch (element.getDashboardItem().getType()) {
            case DashboardItemContent.TYPE_CHART: {
                String request = DhisController.getInstance().buildImageUrl("charts", element.getUId(), context);
                attachFragment(ImageViewFragment.newInstance(request));
                break;
            }
            case DashboardItemContent.TYPE_EVENT_CHART: {
                String request = DhisController.getInstance().buildImageUrl("eventCharts", element.getUId(), context);
                attachFragment(ImageViewFragment.newInstance(request));
                break;
            }
            case DashboardItemContent.TYPE_MAP: {
                String request = DhisController.getInstance().buildImageUrl("maps", element.getUId(), context);
                attachFragment(MapImageViewFragment.newInstance(request));
                break;
            }
            case DashboardItemContent.TYPE_REPORT_TABLE:
            case DashboardItemContent.TYPE_EVENT_REPORT: {
                String elementId = element.getUId();
                if(!isAttached(element.getDashboardItem().getType())){
                    attachFragment(WebViewFragment.newInstance(elementId,
                            element.getDashboardItem().getType()), element.getDashboardItem().getType());
                }
                break;
            }
        }
    }

    private boolean isAttached(String type) {
        if(getSupportFragmentManager()!=null && getSupportFragmentManager().getFragments()!=null) {
            if(getSupportFragmentManager().findFragmentByTag(type)!=null) {
                return true;
            }
        }
        return false;
    }

    private void handleInterpretationElement(InterpretationElement element, Context context) {
        if (element == null || element.getInterpretation() == null) {
            return;
        }

        mToolbar.setTitle(element.getDisplayName());
        switch (element.getInterpretation().getType()) {
            case Interpretation.TYPE_CHART: {
                String request = DhisController.getInstance().buildImageUrl("charts", element.getUId(), context);
                attachFragment(ImageViewFragment.newInstance(request));
                break;
            }
            case Interpretation.TYPE_MAP: {
                String request = DhisController.getInstance().buildImageUrl("maps", element.getUId(), context);
                attachFragment(MapImageViewFragment.newInstance(request));
                break;
            }
            case Interpretation.TYPE_REPORT_TABLE: {
                String elementId = element.getUId();
                if(!isAttached(element.getType())) {
                    attachFragment(WebViewFragment.newInstance(elementId, element.getType()),
                            element.getType());
                }
                break;
            }
            case Interpretation.TYPE_DATA_SET_REPORT: {
                break;
            }
        }
    }

    private void attachFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment, tag)
                .commit();
    }

    private void attachFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
