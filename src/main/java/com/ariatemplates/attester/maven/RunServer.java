/*
 * Copyright 2012 Amadeus s.a.s.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ariatemplates.attester.maven;

import java.util.List;

/**
 * Runs <a href="https://github.com/ariatemplates/attester#usage">attester</a>
 * with the <code>--server-only</code> option. This is useful to run tests
 * manually.
 *
 * @goal run
 */
public class RunServer extends RunAttester {

    @Override
    protected List<String> getNodeArguments() {
        List<String> res = super.getNodeArguments();
        res.add("--server-only");
        return res;
    }
}
