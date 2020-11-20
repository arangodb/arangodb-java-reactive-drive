/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
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
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package deployments;

import org.immutables.value.Value;

/**
 * @author Michele Rastelli
 */
@Value.Immutable(builder = false)
public abstract class ArangoVersion implements Comparable<ArangoVersion> {

    @Value.Parameter(order = 1)
    abstract int getMajor();

    @Value.Parameter(order = 2)
    abstract int getMinor();

    @Value.Parameter(order = 3)
    abstract int getPatch();

    @Override
    public int compareTo(ArangoVersion o) {
        if (getMajor() < o.getMajor())
            return -1;
        else if (getMajor() > o.getMajor())
            return 1;
        else if (getMinor() < o.getMinor())
            return -1;
        else if (getMinor() > o.getMinor())
            return 1;
        else
            return Integer.compare(getPatch(), o.getPatch());
    }

}
