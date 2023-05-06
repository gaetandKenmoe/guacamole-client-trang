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

package org.apache.guacamole.rest.history;

import java.io.File;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleUnsupportedException;
import org.apache.guacamole.environment.Environment;
import org.apache.guacamole.environment.LocalEnvironment;
import org.apache.guacamole.net.auth.ActivityRecordSet;
import org.apache.guacamole.net.auth.ConnectionRecord;
import org.apache.guacamole.net.auth.UserContext;
import org.apache.guacamole.net.auth.simple.SimpleActivityRecordSet;
import org.apache.guacamole.properties.FileGuacamoleProperty;

/**
 * A REST resource for retrieving and managing the history records of Guacamole
 * objects.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HistoryResource {

        /**
     * The default directory to search for associated session recordings, if
     * not overridden with the "recording-search-path" property.
     */
    private static final File DEFAULT_RECORDING_SEARCH_PATH = new File("/var/lib/guacamole/recordings");

    /**
     * The directory to search for associated session recordings. By default,
     * "/var/lib/guacamole/recordings" will be used.
     */
    private static final FileGuacamoleProperty RECORDING_SEARCH_PATH = new FileGuacamoleProperty() {

        @Override
        public String getName() {
            return "recording-search-path";
        }

    };
    
    /**
     * The UserContext whose associated connection history is being exposed.
     */
    private final UserContext userContext;

    /**
     * Creates a new HistoryResource which exposes the connection history
     * associated with the given UserContext.
     *
     * @param userContext
     *     The UserContext whose connection history should be exposed.
     */
    public HistoryResource(UserContext userContext) {
        this.userContext = userContext;
    }

    /**
     * Retrieves the usage history for all connections. Filtering may be
     * applied via the returned ConnectionHistoryResource.
     *
     * @return
     *     A resource which exposes connection records that may optionally be
     *     filtered, each record describing the start and end times that a
     *     particular connection was used.
     *
     * @throws GuacamoleException
     *     If an error occurs while retrieving the connection history.
     */
    @Path("connections")
    public ConnectionHistoryResource getConnectionHistory() throws GuacamoleException {
        try {
            return new ConnectionHistoryResource(userContext.getConnectionHistory());
        }
        catch (GuacamoleUnsupportedException e) {
            return new ConnectionHistoryResource(new SimpleActivityRecordSet<>());
        }
    }

    /**
     * Retrieves the login history for all users. Filtering may be applied via
     * the returned UserHistoryResource.
     *
     * @return
     *     A resource which exposes user records that may optionally be
     *     filtered, each record describing the start and end times of a user
     *     session.
     *
     * @throws GuacamoleException
     *     If an error occurs while retrieving the user history.
     */
    @Path("users")
    public UserHistoryResource getUserHistory() throws GuacamoleException {
        try {
            return new UserHistoryResource(userContext.getUserHistory());
        }
        catch (GuacamoleUnsupportedException e) {
            return new UserHistoryResource(new SimpleActivityRecordSet<>());
        }
    }
    
    /**
     * Delete file of a connection history.
     *
     * @param identifier
     * 
     * @return
     *     A resource which exposes connection records that may optionally be
     *     filtered, each record describing the start and end times that a
     *     particular connection was used.
     *
     * @throws GuacamoleException
     *     If an error occurs while retrieving the connection history.
     */
    @GET
    @Path("deleteFile")
    public String deleteFileConnectionHistory(@QueryParam("identifier") String identifier) throws GuacamoleException {
        try {
            String message = "";
            message += "Identifier: " + identifier;
            ActivityRecordSet<ConnectionRecord> connectionHistories;
            connectionHistories = userContext.getConnectionHistory();
            if (identifier != null) {
                ConnectionRecord connectionRecord;
                connectionRecord = connectionHistories.get(identifier);
                if (connectionRecord != null) {
                    UUID uuid = connectionRecord.getUUID();
                    message += " - Record: " + connectionRecord.getUsername();
                    if (uuid != null) {
                        message += " - UUID: " + uuid.toString();
                        Environment environment = LocalEnvironment.getInstance();
                        File recordingPath;
                        recordingPath = environment.getProperty(RECORDING_SEARCH_PATH, DEFAULT_RECORDING_SEARCH_PATH);
                        message += " - recordingPath: " + recordingPath.getAbsolutePath();
                        File recordingFile = new File(recordingPath, uuid.toString());
                        message += " - recordingFile: " + recordingFile.getAbsolutePath();
                        if (recordingFile.canRead()) {
                            message += " - can read: " + recordingFile.getAbsolutePath();
                            if (recordingFile.delete())
                               message += " - Delete sucessfully";
                            else
                               message += " - Impossible to delete";
                        } else {
                            if (recordingFile.delete())
                               message += " - Delete sucessfully";
                            else
                               message += " - Impossible to delete";
                            message += " - Impossible to read: " + recordingFile.getAbsolutePath();
                        }
                    }
                }
            }
            return "\"" + message + "\"";
        }
        catch (GuacamoleUnsupportedException e) {
            return e.getMessage();
        }
    }

}
