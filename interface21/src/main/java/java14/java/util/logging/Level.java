// Java Logging API for JDKs prior to 1.4
// Copyright (C) 2001 Brian R. Gilstrap
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details (a copy of the license
// .1 is included in the file GLPL in the doc directory of the source
// distribution).
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

// UNMODIFIED

package java14.java.util.logging;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Level implements Serializable {

    public static final Level ALL = new Level( "ALL", Integer.MIN_VALUE );
    public static final Level FINEST = new Level( "FINEST", 300 );
    public static final Level FINER = new Level( "FINER", 400 );
    public static final Level FINE = new Level( "FINE", 500 );
    public static final Level CONFIG = new Level( "CONFIG", 700 );
    public static final Level INFO = new Level( "INFO", 800 );
    public static final Level WARNING = new Level( "WARNING", 900 );
    public static final Level SEVERE = new Level( "SEVERE", 1000 );
    public static final Level OFF = new Level( "OFF", Integer.MAX_VALUE );


    private static final Map nameToLevelMap = new HashMap( 17 );

    static {
        nameToLevelMap.put( ALL.theName, ALL );
        nameToLevelMap.put( FINEST.theName, FINEST );
        nameToLevelMap.put( FINER.theName, FINER );
        nameToLevelMap.put( FINE.theName, FINE );
        nameToLevelMap.put( CONFIG.theName, CONFIG );
        nameToLevelMap.put( INFO.theName, INFO );
        nameToLevelMap.put( WARNING.theName, WARNING );
        nameToLevelMap.put( SEVERE.theName, SEVERE );
        nameToLevelMap.put( OFF.theName, OFF );
    }

    public static Level parse( String name ) {
        Level retval = (Level) nameToLevelMap.get( name );
        if ( retval != null ) {
            return retval;
        }
        try {
            int parsed = Integer.parseInt( name );
            retval = getByValue( parsed );
            if ( retval == null ) {
                throw new IllegalArgumentException( "Integer value '"+name+"' does not correspond to any predefined Level!" );
            }
            return retval;
        }
        catch ( NumberFormatException nfe ) {
            throw new IllegalArgumentException( "Provided name '"+name+"' is not a level name and not parseable as an integer!" );
        }
    }

    private String theName;
    private String bundleName;
    private int theValue;

    protected Level( String name, int value ) {
        theName = name;
        theValue = value;
    }

    protected Level( String name, int value, String aBundleName ) {
        theName = name;
        theValue = value;
        bundleName = aBundleName;
    }

    public String getLocalizedName() {
        if ( bundleName == null ) {
            return theName;
        }
        String retval = null;
        ResourceBundle bundle = ResourceBundle.getBundle( bundleName );
        if ( bundle != null ) {
           retval = bundle.getString( theName );
        }
        if ( retval == null ) {
            retval = theName;
        }
        return retval;
    }

    public String getName() {
        return theName;
    }

    public String getResourceBundleName() {
        return bundleName;
    }

    public boolean equals( Object o ) {
        if ( ! ( o instanceof Level ) ) {
            return false;
        }
        Level l = (Level)o;
        return theValue == l.theValue;
    }

    public int hashCode() {
        return theValue;
    }

    public int intValue() {
        return theValue;
    }

    public String toString() {
        return theName;
    }

    private Object readResolve() throws ObjectStreamException {
        Level retval = getByValue( theValue );
        if ( retval == null ) {
           throw new InvalidObjectException( "Unable to resolve object!" );
        }
        return retval;
    }

    private static Level getByValue( int aValue ) {
        if ( aValue ==  ALL.theValue ) {
            return ALL;
        }
        else if ( aValue ==  FINEST.theValue ) {
            return FINEST;
        }
        else if ( aValue ==  FINER.theValue ) {
            return FINER;
        }
        else if ( aValue ==  FINE.theValue ) {
            return FINE;
        }
        else if ( aValue ==  CONFIG.theValue ) {
            return CONFIG;
        }
        else if ( aValue ==  INFO.theValue ) {
            return INFO;
        }
        else if ( aValue ==  WARNING.theValue ) {
            return WARNING;
        }
        else if ( aValue ==  SEVERE.theValue ) {
            return SEVERE;
        }
        else if ( aValue ==  OFF.theValue ) {
            return OFF;
        }
        return null;
    }
}
