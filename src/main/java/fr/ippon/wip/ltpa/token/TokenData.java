/*
 *	Copyright 2010,2011 Ippon Technologies 
 *  
 *	This file is part of Web Integration Portlet (WIP).
 *	Web Integration Portlet (WIP) is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Web Integration Portlet (WIP) is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Web Integration Portlet (WIP).  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ippon.wip.ltpa.token;

import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

public class TokenData {
    public static final SimpleTimeZone utcTimeZone = new SimpleTimeZone(0, "UTC");

    public String username;
    public GregorianCalendar tokenCreated;
    public GregorianCalendar tokenExpiration;

    public TokenData() {
        username = "";
        tokenCreated = new GregorianCalendar(utcTimeZone);
        tokenCreated.setTimeInMillis(0);
        tokenExpiration = new GregorianCalendar(utcTimeZone);
        tokenExpiration.setTimeInMillis(0);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append("[ username:").append(username).append(", tokenCreated: ").append(tokenCreated.getTime().toString());
        buf.append(", tokenExpiration: ").append(tokenExpiration.getTime().toString()).append(" ]");

        return buf.toString();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public GregorianCalendar getTokenCreated() {
        return tokenCreated;
    }

    public void setTokenCreated(GregorianCalendar tokenCreated) {
        this.tokenCreated = tokenCreated;
    }

    public GregorianCalendar getTokenExpiration() {
        return tokenExpiration;
    }

    public void setTokenExpiration(GregorianCalendar tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }


}
