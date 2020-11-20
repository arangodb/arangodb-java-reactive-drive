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

package com.arangodb.reactive.api.collection.impl;


import com.arangodb.reactive.api.collection.CollectionApi;
import com.arangodb.reactive.api.collection.entity.CollectionChecksumEntity;
import com.arangodb.reactive.api.collection.entity.DetailedCollectionEntity;
import com.arangodb.reactive.api.collection.entity.SimpleCollectionEntity;
import com.arangodb.reactive.api.collection.options.*;
import com.arangodb.reactive.api.reactive.ArangoDatabase;
import com.arangodb.reactive.api.reactive.impl.ArangoClientImpl;
import com.arangodb.reactive.connection.ArangoRequest;
import com.arangodb.reactive.connection.ArangoResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.arangodb.reactive.api.util.ArangoResponseField.RESULT_JSON_POINTER;

/**
 * @author Michele Rastelli
 */
public final class CollectionApiImpl extends ArangoClientImpl implements CollectionApi {

    private static final String PATH_API = "/_api/collection";

    private final String dbName;

    public CollectionApiImpl(final ArangoDatabase arangoDatabase) {
        super((ArangoClientImpl) arangoDatabase);
        dbName = arangoDatabase.name();
    }

    @Override
    public Flux<SimpleCollectionEntity> getCollections(final CollectionsReadParams params) {
        return getCommunication()
                .execute(
                        ArangoRequest.builder()
                                .database(dbName)
                                .requestType(ArangoRequest.RequestType.GET)
                                .path(PATH_API)
                                .putQueryParams(
                                        CollectionsReadParams.EXCLUDE_SYSTEM_PARAM,
                                        params.getExcludeSystem().map(String::valueOf)
                                )
                                .build()
                )
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserializeListAtJsonPointer(RESULT_JSON_POINTER, bytes, SimpleCollectionEntity.class))
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<DetailedCollectionEntity> createCollection(
            final CollectionCreateOptions options,
            final CollectionCreateParams params
    ) {
        return getCommunication()
                .execute(
                        ArangoRequest.builder()
                                .database(dbName)
                                .requestType(ArangoRequest.RequestType.POST)
                                .body(getSerde().serialize(options))
                                .path(PATH_API)
                                .putQueryParams(
                                        "enforceReplicationFactor",
                                        params.getEnforceReplicationFactor().map(it -> it ? "1" : "0")
                                )
                                .putQueryParams(
                                        "waitForSyncReplication",
                                        params.getWaitForSyncReplication().map(it -> it ? "1" : "0")
                                )
                                .build()
                )
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserialize(bytes, DetailedCollectionEntity.class));
    }

    @Override
    public Mono<Void> dropCollection(final String name, final CollectionDropParams params) {
        return getCommunication()
                .execute(
                        ArangoRequest.builder()
                                .database(dbName)
                                .requestType(ArangoRequest.RequestType.DELETE)
                                .path(PATH_API + "/" + name)
                                .putQueryParams(
                                        CollectionDropParams.IS_SYSTEM_PARAM,
                                        params.isSystem().map(String::valueOf)
                                )
                                .build()
                )
                .then();
    }

    @Override
    public Mono<SimpleCollectionEntity> getCollection(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.GET)
                        .path(PATH_API + "/" + name)
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserialize(bytes, SimpleCollectionEntity.class));
    }

    @Override
    public Mono<DetailedCollectionEntity> getCollectionProperties(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.GET)
                        .path(PATH_API + "/" + name + "/properties")
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserialize(bytes, DetailedCollectionEntity.class));
    }

    @Override
    public Mono<DetailedCollectionEntity> changeCollectionProperties(final String name, final CollectionChangePropertiesOptions options) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.PUT)
                        .path(PATH_API + "/" + name + "/properties")
                        .body(getSerde().serialize(options))
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserialize(bytes, DetailedCollectionEntity.class));
    }

    @Override
    public Mono<SimpleCollectionEntity> renameCollection(final String name, final CollectionRenameOptions options) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.PUT)
                        .path(PATH_API + "/" + name + "/rename")
                        .body(getSerde().serialize(options))
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserialize(bytes, SimpleCollectionEntity.class));
    }


    @Override
    public Mono<Long> getCollectionCount(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.GET)
                        .path(PATH_API + "/" + name + "/count")
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserializeAtJsonPointer("/count", bytes, Long.class));
    }

    @Override
    public Mono<CollectionChecksumEntity> getCollectionChecksum(final String name, final CollectionChecksumParams params) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.GET)
                        .path(PATH_API + "/" + name + "/checksum")
                        .putQueryParams(
                                CollectionChecksumParams.WITH_REVISIONS,
                                params.getWithRevisions().map(String::valueOf)
                        )
                        .putQueryParams(
                                CollectionChecksumParams.WITH_DATA,
                                params.getWithData().map(String::valueOf)
                        )
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserialize(bytes, CollectionChecksumEntity.class));
    }

    @Override
    public Mono<Map<String, Object>> getCollectionStatistics(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.GET)
                        .path(PATH_API + "/" + name + "/figures")
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserializeAtJsonPointer("/figures", bytes, Map.class));
    }

    @Override
    public Mono<Void> loadCollection(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.PUT)
                        .path(PATH_API + "/" + name + "/load")
                        .build())
                .then();
    }

    @Override
    public Mono<Void> loadCollectionIndexes(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.PUT)
                        .path(PATH_API + "/" + name + "/loadIndexesIntoMemory")
                        .build())
                .then();
    }

    @Override
    public Mono<Void> recalculateCollectionCount(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.PUT)
                        .path(PATH_API + "/" + name + "/recalculateCount")
                        .build())
                .then();
    }

    @Override
    public Mono<Void> truncateCollection(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.PUT)
                        .path(PATH_API + "/" + name + "/truncate")
                        .build())
                .then();
    }

    @Override
    public Mono<String> getResponsibleShard(final String name, final Object document) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.PUT)
                        .path(PATH_API + "/" + name + "/responsibleShard")
                        .body(getSerde().serialize(document))
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserializeAtJsonPointer("/shardId", bytes, String.class));
    }

    @Override
    public Mono<String> getCollectionRevision(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.GET)
                        .path(PATH_API + "/" + name + "/revision")
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserializeAtJsonPointer("/revision", bytes, String.class));
    }

    @Override
    public Flux<String> getCollectionShards(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.GET)
                        .path(PATH_API + "/" + name + "/shards")
                        .build())
                .map(ArangoResponse::getBody)
                .map(bytes -> getSerde().deserializeListAtJsonPointer("/shards", bytes, String.class))
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Void> unloadCollection(final String name) {
        return getCommunication()
                .execute(ArangoRequest.builder()
                        .database(dbName)
                        .requestType(ArangoRequest.RequestType.PUT)
                        .path(PATH_API + "/" + name + "/unload")
                        .build())
                .then();
    }

}
