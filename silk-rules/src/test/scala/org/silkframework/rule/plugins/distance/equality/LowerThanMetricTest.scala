/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.silkframework.rule.plugins.distance.equality

import org.scalatest.{FlatSpec, Matchers}
import org.silkframework.test.PluginTest
import org.silkframework.testutil.approximatelyEqualTo


class LowerThanMetricTest extends PluginTest {
  lazy val metric = new LowerThanMetric()
  val t = 1.0

  "LowerThanMetric" should "return 0.0 if the source number is smaller than the target number and 1.0 otherwise" in {
    metric.evaluate("1", "2", t) should be(approximatelyEqualTo(0.0))
    metric.evaluate("2", "1", t) should be(approximatelyEqualTo(1.0))
    metric.evaluate("0.1", "0.2", t) should be(approximatelyEqualTo(0.0))
    metric.evaluate("0.2", "0.1", t) should be(approximatelyEqualTo(1.0))
    metric.evaluate("5", "20", t) should be(approximatelyEqualTo(0.0))
    metric.evaluate("20", "5", t) should be(approximatelyEqualTo(1.0))
  }

  "LowerThanMetric" should "return 0.0 if the source string is lower than the target string and 1.0 otherwise" in {
    metric.evaluate("aaa", "aab", t) should be(approximatelyEqualTo(0.0))
    metric.evaluate("aab", "aaa", t) should be(approximatelyEqualTo(1.0))
  }

  override def pluginObject = metric
}