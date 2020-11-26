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

package api;


import com.arangodb.codegen.GenerateSyncApi;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Michele Rastelli
 */
@GenerateSyncApi
public interface TestApi {

    String name();

    default Mono<String> defaultMethod() {
        return Mono.just("defaultMethod");
    }

    Mono<Void> voidMethod();

    Flux<String> stringsCollectionMethod();

    Mono<String> stringMethodWithStringArgument(String value);

    Flux<String> stringsCollectionMethodWithStringArgument(String a, String b, String c);
}
