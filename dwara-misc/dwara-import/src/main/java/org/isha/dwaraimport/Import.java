package org.isha.dwaraimport;

import org.json.simple.parser.ParseException;

import java.io.IOException;

public class Import {
    public static void main (String[] args) throws IOException, ParseException {
        String bruFileLocation = args[0];
        String jsonFolderPathLocation = args[1];
        String destinationXMLLocation = args[2];

        DwaraImport dwaraImport = new DwaraImport();
        dwaraImport.apacheGetXMLData(bruFileLocation, jsonFolderPathLocation, destinationXMLLocation);
    }

}
