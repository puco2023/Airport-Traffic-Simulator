package gui;

import java.util.ArrayList;
import model.Airport;

final class AirportLookup {

    private AirportLookup() {
    }

    static Airport findByCode(ArrayList<Airport> airports, String code) {
        for (Airport airport : airports) {
            if (airport.getCode().equals(code)) {
                return airport;
            }
        }

        return null;
    }
}
