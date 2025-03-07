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

package org.apache.shardingsphere.encrypt.rewrite.condition.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Encrypt condition for equal.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class EncryptBinaryCondition implements EncryptCondition {
    
    private final String columnName;
    
    private final String tableName;
    
    private final String operator;
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final ExpressionSegment expressionSegment;
    
    private final Map<Integer, Integer> positionIndexMap = new LinkedHashMap<>();
    
    private final Map<Integer, Object> positionValueMap = new LinkedHashMap<>();
    
    public EncryptBinaryCondition(final String columnName, final String tableName, final String operator, final int startIndex, final int stopIndex, final ExpressionSegment expressionSegment) {
        this.columnName = columnName;
        this.tableName = tableName;
        this.operator = operator;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.expressionSegment = expressionSegment;
        putPositionMap(expressionSegment);
    }
    
    private void putPositionMap(final ExpressionSegment expressionSegment) {
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            positionIndexMap.put(0, ((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
        } else if (expressionSegment instanceof LiteralExpressionSegment) {
            positionValueMap.put(0, ((LiteralExpressionSegment) expressionSegment).getLiterals());
        } else if (expressionSegment instanceof FunctionSegment) {
            Collection<ExpressionSegment> parameters = ((FunctionSegment) expressionSegment).getParameters();
            Iterator<ExpressionSegment> iterator = parameters.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                ExpressionSegment next = iterator.next();
                if (next instanceof LiteralExpressionSegment) {
                    positionValueMap.put(i, ((LiteralExpressionSegment) next).getLiterals());
                }
                i++;
            }
        }
    }
    
    /**
     * Get values.
     *
     * @param params SQL parameters
     * @return values
     */
    public List<Object> getValues(final List<Object> params) {
        List<Object> result = new ArrayList<>(positionValueMap.values());
        for (Entry<Integer, Integer> entry : positionIndexMap.entrySet()) {
            Object param = params.get(entry.getValue());
            if (entry.getKey() < result.size()) {
                result.add(entry.getKey(), param);
            } else {
                result.add(param);
            }
        }
        return result;
    }
}
