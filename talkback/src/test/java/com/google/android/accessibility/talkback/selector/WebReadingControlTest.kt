/*
 * Copyright 2026 Backtalk contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.accessibility.talkback.selector

import android.content.Context
import android.content.res.XmlResourceParser
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.google.android.accessibility.talkback.R
import com.google.android.accessibility.talkback.contextmenu.ContextMenuItem
import com.google.android.accessibility.talkback.menurules.NodeMenuRuleCreator
import com.google.android.accessibility.talkback.selector.SelectorController.Setting
import com.google.android.accessibility.utils.FormFactorUtils
import com.google.android.accessibility.utils.SharedPreferencesUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.xmlpull.v1.XmlPullParser

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WebReadingControlTest {
  @Test
  fun selectedWebReadingControlsResistAutomaticActions() {
    listOf(
        Setting.GRANULARITY_HEADINGS,
        Setting.GRANULARITY_LINKS,
        Setting.GRANULARITY_CONTROLS,
        Setting.GRANULARITY_CHARACTERS,
        Setting.GRANULARITY_WORDS,
    ).forEach { setting ->
      assertTrue(SelectorController.shouldPreserveWebReadingControl(setting, Setting.ACTIONS, true, true))
    }
    Setting.values().filter { it.name.startsWith("GRANULARITY_WEB_") }.forEach { setting ->
      assertTrue(SelectorController.shouldPreserveWebReadingControl(setting, Setting.ACTIONS, true, true))
    }
  }

  @Test
  fun disabledPreferenceAllowsAutomaticActions() {
    assertFalse(
        SelectorController.shouldPreserveWebReadingControl(
            Setting.GRANULARITY_HEADINGS, Setting.ACTIONS, false, true))
  }

  @Test
  fun currentActionsIsNotTreatedAsAWebReadingControl() {
    assertFalse(
        SelectorController.shouldPreserveWebReadingControl(
            Setting.ACTIONS, Setting.ACTIONS, true, true))
  }

  @Test
  fun actionsRemainEligibleWithPreservationEnabled() {
    val context = RuntimeEnvironment.getApplication()
    FormFactorUtils.initialize(context)
    val prefs = SharedPreferencesUtils.getSharedPreferences(context)
    prefs.edit().putBoolean(context.getString(R.string.pref_selector_actions_key), true).apply()
    val nodeInfo = AccessibilityNodeInfo.obtain()
    nodeInfo.addAction(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT)
    val node = AccessibilityNodeInfoCompat.wrap(nodeInfo)
    val menuCreator =
        object : NodeMenuRuleCreator(null, null, null, null) {
          override fun getNodeMenuByRule(
              rule: NodeMenuRuleCreator.MenuRules,
              context: Context,
              node: AccessibilityNodeInfoCompat,
              includeAncestors: Boolean,
          ): List<ContextMenuItem> {
            assertEquals(NodeMenuRuleCreator.MenuRules.RULE_CUSTOM_ACTION, rule)
            assertTrue(includeAncestors)
            return listOf(ContextMenuItem(context, 0, 1, 0, "Custom action"))
          }
        }
    val actions = ActionsSetting(menuCreator, null)

    listOf(false, true).forEach { preserve ->
      prefs
          .edit()
          .putBoolean(context.getString(R.string.pref_preserve_web_reading_control_key), preserve)
          .apply()
      assertTrue(actions.isNodeSupportSetting(context, node))
      assertFalse(actions.isNodeSupportSetting(context, null))
    }
    nodeInfo.recycle()
  }

  @Test
  fun nonWebAndOtherContextualTransitionsAreUnchanged() {
    assertFalse(
        SelectorController.shouldPreserveWebReadingControl(
            Setting.GRANULARITY_HEADINGS, Setting.ACTIONS, true, false))
    assertFalse(
        SelectorController.shouldPreserveWebReadingControl(
            Setting.GRANULARITY_HEADINGS, Setting.ADJUSTABLE_WIDGET, true, true))
    assertFalse(
        SelectorController.shouldPreserveWebReadingControl(
            Setting.GRANULARITY_DEFAULT, Setting.ACTIONS, true, true))
    listOf(Setting.GRANULARITY_WINDOWS, Setting.GRANULARITY_CONTAINERS, Setting.GRANULARITY_SEARCH)
        .forEach { setting ->
          assertFalse(
              SelectorController.shouldPreserveWebReadingControl(setting, Setting.ACTIONS, true, true))
        }
  }

  @Test
  fun navigationPreferenceIsOffByDefaultAndHasAccessibleText() {
    val resources = RuntimeEnvironment.getApplication().resources
    assertFalse(resources.getBoolean(R.bool.pref_preserve_web_reading_control_default))
    assertTrue(resources.getString(R.string.title_pref_preserve_web_reading_control).isNotBlank())
    assertTrue(resources.getString(R.string.summary_pref_preserve_web_reading_control).isNotBlank())

    val parser: XmlResourceParser = resources.getXml(R.xml.navigation_preferences)
    try {
      while (parser.eventType != XmlPullParser.END_DOCUMENT) {
        if (parser.eventType == XmlPullParser.START_TAG &&
            parser.getAttributeResourceValue(ANDROID_NS, "key", 0) ==
                R.string.pref_preserve_web_reading_control_key) {
          assertEquals(
              R.bool.pref_preserve_web_reading_control_default,
              parser.getAttributeResourceValue(ANDROID_NS, "defaultValue", 0))
          assertEquals(
              R.string.title_pref_preserve_web_reading_control,
              parser.getAttributeResourceValue(ANDROID_NS, "title", 0))
          assertEquals(
              R.string.summary_pref_preserve_web_reading_control,
              parser.getAttributeResourceValue(ANDROID_NS, "summary", 0))
          return
        }
        parser.next()
      }
    } finally {
      parser.close()
    }
    throw AssertionError("Web reading control preference is missing from navigation settings")
  }

  private companion object {
    const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
  }
}
