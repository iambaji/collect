/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.activities

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.R
import org.odk.collect.android.formmanagement.FormNavigator
import org.odk.collect.android.formmanagement.formmap.FormMapViewModel
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.geo.SelectionMapFragment
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

/**
 * Show a map with points representing saved instances of the selected form.
 */
class FormMapActivity : LocalizedActivity() {

    @Inject
    lateinit var formsRepositoryProvider: FormsRepositoryProvider

    @Inject
    lateinit var instancesRepositoryProvider: InstancesRepositoryProvider

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    private lateinit var viewModel: FormMapViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.form_map_activity)

        val formId = intent.getLongExtra(EXTRA_FORM_ID, -1)
        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FormMapViewModel(
                        resources,
                        formId,
                        formsRepositoryProvider.get(),
                        instancesRepositoryProvider.get(),
                        settingsProvider
                    ) as T
                }
            }
        )[ARG_VIEW_MODEL_KEY, FormMapViewModel::class.java]

        val formNavigator = FormNavigator(
            currentProjectProvider,
            settingsProvider,
            instancesRepositoryProvider::get
        )

        supportFragmentManager.setFragmentResultListener(
            SelectionMapFragment.REQUEST_SELECT_ITEM,
            this
        ) { _: String?, result: Bundle ->
            if (result.containsKey(SelectionMapFragment.RESULT_SELECTED_ITEM)) {
                val instanceId = result.getLong(SelectionMapFragment.RESULT_SELECTED_ITEM)
                formNavigator.editInstance(this, instanceId)
            } else if (result.containsKey(SelectionMapFragment.RESULT_CREATE_NEW_ITEM)) {
                formNavigator.newInstance(this, formId)
            }
        }

        supportFragmentManager.beginTransaction()
            .add(
                R.id.selection_map,
                SelectionMapFragment::class.java,
                Bundle().also {
                    it.putString(SelectionMapFragment.ARG_VIEW_MODEL_KEY, ARG_VIEW_MODEL_KEY)
                }
            )
            .commit()
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    companion object {
        const val EXTRA_FORM_ID = "form_id"
        const val ARG_VIEW_MODEL_KEY = "form_map"
    }
}
