/*
 * Copyright (c) 2016 Evolveum
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
 */

package com.evolveum.polygon.connector.ekoop;

/**
 * E-Koop Filter, we have only one service method to get filtered users by TCKN (Turkish Citizenship Number) - represented as UID over connector.
 * In all other cases connector return all users and Polygon/ConnId connector framework filter then.
 *
 * @author gpalos
 */
public class EkoopIKFilter {

    /**
     * Filter by UID - TCKN (Turkish Citizenship Number)
     */
    public String byUid;

    @Override
    public String toString() {
        return "EkoopIKFilter{" +
                "byUid=" + byUid +
                '}';
    }
}
