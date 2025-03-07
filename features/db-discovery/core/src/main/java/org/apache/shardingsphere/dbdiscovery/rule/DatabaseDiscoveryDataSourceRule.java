/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.dbdiscovery.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.exception.MissingRequiredDataSourceNamesConfigurationException;
import org.apache.shardingsphere.dbdiscovery.exception.MissingRequiredGroupNameConfigurationException;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProvider;
import org.apache.shardingsphere.infra.datasource.mapper.DataSourceRole;
import org.apache.shardingsphere.infra.datasource.mapper.DataSourceRoleInfo;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Database discovery data source rule.
 */
@Getter
public final class DatabaseDiscoveryDataSourceRule {
    
    private final String groupName;
    
    private final List<String> dataSourceNames;
    
    private final Properties heartbeatProps;
    
    private final DatabaseDiscoveryProvider provider;
    
    private final Collection<String> disabledDataSourceNames = new HashSet<>();
    
    private volatile String primaryDataSourceName;
    
    public DatabaseDiscoveryDataSourceRule(final DatabaseDiscoveryDataSourceRuleConfiguration config,
                                           final Properties props, final DatabaseDiscoveryProvider provider) {
        checkConfiguration(config);
        groupName = config.getGroupName();
        dataSourceNames = config.getDataSourceNames();
        this.heartbeatProps = props;
        this.provider = provider;
    }
    
    private void checkConfiguration(final DatabaseDiscoveryDataSourceRuleConfiguration config) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(config.getGroupName()), "Group name is required.");
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(config.getGroupName()), MissingRequiredGroupNameConfigurationException::new);
        ShardingSpherePreconditions.checkState(null != config.getDataSourceNames()
                && !config.getDataSourceNames().isEmpty(), MissingRequiredDataSourceNamesConfigurationException::new);
    }
    
    /**
     * Get replica data source names.
     *
     * @return available replica data source names
     */
    public List<String> getReplicaDataSourceNames() {
        return dataSourceNames.stream().filter(each -> !disabledDataSourceNames.contains(each) && !primaryDataSourceName.equals(each)).collect(Collectors.toList());
    }
    
    /**
     * Disable data source.
     *
     * @param dataSourceName data source name to be disabled
     */
    public void disableDataSource(final String dataSourceName) {
        disabledDataSourceNames.add(dataSourceName);
    }
    
    /**
     * Enable data source.
     *
     * @param dataSourceName data source name to be enabled
     */
    public void enableDataSource(final String dataSourceName) {
        disabledDataSourceNames.remove(dataSourceName);
    }
    
    /**
     * Change primary data source name.
     *
     * @param primaryDataSourceName to be changed primary data source name
     */
    public void changePrimaryDataSourceName(final String primaryDataSourceName) {
        this.primaryDataSourceName = primaryDataSourceName;
    }
    
    /**
     *  Get data source.
     *
     * @param dataSourceMap data source map
     * @return data source
     */
    public Map<String, DataSource> getDataSourceGroup(final Map<String, DataSource> dataSourceMap) {
        Map<String, DataSource> result = new HashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            if (dataSourceNames.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    /**
     * Get data source mapper.
     *
     * @return data source mapper
     */
    public Map<String, Collection<DataSourceRoleInfo>> getDataSourceMapper() {
        return Collections.singletonMap(groupName, getActualDataSourceNames());
    }
    
    private Collection<DataSourceRoleInfo> getActualDataSourceNames() {
        Collection<DataSourceRoleInfo> result = new LinkedHashSet<>();
        result.add(new DataSourceRoleInfo(primaryDataSourceName, DataSourceRole.PRIMARY));
        dataSourceNames.forEach(each -> {
            if (!primaryDataSourceName.equals(each)) {
                result.add(new DataSourceRoleInfo(each, DataSourceRole.MEMBER));
            }
        });
        return result;
    }
}
