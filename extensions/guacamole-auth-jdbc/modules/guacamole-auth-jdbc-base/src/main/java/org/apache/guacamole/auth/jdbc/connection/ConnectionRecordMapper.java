/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.guacamole.auth.jdbc.connection;

import java.util.List;
import org.apache.guacamole.auth.jdbc.base.ActivityRecordMapper;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper for connection record objects.
 */
public interface ConnectionRecordMapper extends ActivityRecordMapper<ConnectionRecordModel> {

    /**
     * @param identifier
     *  the identifier of the connection
     * @param user_id
     * @return 
     *
     */
    List<ConnectionRecordModel> getUserConnection(@Param("identifier") Integer identifier,
            @Param("user_id") Integer user_id);
    
        /**
     * @param identifier
     *  the identifier of the connection group
     * @param user_id
     * @return 
     *
     */
    List<ConnectionRecordModel> getUserGroupConnection(@Param("identifier") String identifier,
            @Param("user_id") Integer user_id);
}
