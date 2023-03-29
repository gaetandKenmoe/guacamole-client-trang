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

package org.apache.guacamole.timer;

/**
 *
 * @author kenmoe
 */
public class GuacamoleTime {
    
    public final static long SECS_MINUTE = 60;
    public final static long SECS_HEURE = 60 * SECS_MINUTE;
    public final static long SECS_DAY = 24 * SECS_HEURE;
    public final static long SECS_MONTH = 30 * SECS_DAY;
    
    public static String formatToHour(long remaining_time) {
        remaining_time = remaining_time / 1000;
        String heure, min, sec;
        
        heure = Long.toString(remaining_time / SECS_HEURE);
        heure = heure.length() == 1 ? "0" + heure : heure;
        remaining_time %= SECS_HEURE;
        min = Long.toString(remaining_time / SECS_MINUTE);
        min = min.length() == 1 ? "0" + min : min;
        remaining_time %= SECS_MINUTE;
        sec = Long.toString(remaining_time);
        sec = sec.length() == 1 ? "0" + sec : sec;
        return heure + ":" + min + ":" + sec;
    }
}
